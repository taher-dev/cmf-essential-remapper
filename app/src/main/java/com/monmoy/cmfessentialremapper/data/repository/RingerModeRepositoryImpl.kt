package com.monmoy.cmfessentialremapper.data.repository

import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.receiver.RingerModeReceiver
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import com.monmoy.cmfessentialremapper.utils.AudioUtils
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class RingerModeRepositoryImpl(
    private val context: Context,
    private val dataStoreManager: DataStoreManager = DataStoreManager(context)
) : RingerModeRepository {

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    // Cached volume to restore when leaving silent/vibrate mode to ring
    private var savedRingVolume: Int = 0

    override fun observeRingerMode(): Flow<RingerMode> = callbackFlow {
        trySend(getCurrentRingerMode())

        val receiver = RingerModeReceiver {
            trySend(getCurrentRingerMode())
        }

        val filter = IntentFilter().apply {
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(receiver, filter)
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                // Ignore if already unregistered
            }
        }
    }

    override fun getCurrentRingerMode(): RingerMode {
        val am = audioManager ?: return RingerMode.RING
        return AudioUtils.intToRingerMode(am.ringerMode)
    }

    override fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }

    override suspend fun setRingerMode(mode: RingerMode) = withContext(Dispatchers.IO) {
        val am = audioManager
            ?: throw IllegalStateException("AudioManager service unavailable.")

        when (mode) {
            RingerMode.SILENT -> {
                if (!hasNotificationPolicyAccess()) {
                    throw SecurityException("Notification Policy Access is required to switch to Silent mode.")
                }

                val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                if (currentVol > 0) {
                    savedRingVolume = currentVol
                }

                // Directly apply SILENT mode
                am.ringerMode = AudioManager.RINGER_MODE_SILENT
                delay(120)

                // If device didn't switch from VIBRATE to SILENT (Nothing OS quirk),
                // explicitly mute ring/notification streams and retry
                if (am.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    try {
                        am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
                        am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                    } catch (_: Throwable) {}
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    delay(100)
                }

                // Workaround: Turn off DND immediately after Silent mode is activated
                turnOffDnd()
                delay(100)

                // If turning off DND reverted the ringer mode on some ROMs, re-apply silent and re-clear DND
                if (am.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    delay(80)
                    turnOffDnd()
                }
            }

            RingerMode.VIBRATE -> {
                val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                if (currentVol > 0) {
                    savedRingVolume = currentVol
                }
                am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                delay(100)
                // Ensure DND is turned off when leaving Silent
                turnOffDnd()
            }

            RingerMode.RING -> {
                am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                delay(100)
                // Ensure DND is turned off when leaving Silent
                turnOffDnd()

                // Restore audible ring volume if it was muted
                try {
                    am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                    am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                    val vol = am.getStreamVolume(AudioManager.STREAM_RING)
                    if (vol == 0) {
                        val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                        val target = if (savedRingVolume > 0) savedRingVolume else (max * 0.7).toInt().coerceAtLeast(1)
                        am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    private suspend fun turnOffDnd() {
        // Method 1: Official Android NotificationManager API
        try {
            if (hasNotificationPolicyAccess()) {
                val currentFilter = notificationManager?.currentInterruptionFilter
                if (currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL &&
                    currentFilter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
                ) {
                    notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        } catch (_: Throwable) {}

        // Method 2: Shizuku privileged shell command (if available)
        try {
            val spaceManager = EssentialSpaceManager(context)
            if (spaceManager.isShizukuReady) {
                spaceManager.runShizukuCommand(arrayOf("cmd", "notification", "set_dnd", "off"))
            }
        } catch (_: Throwable) {}
    }

    override fun observeEssentialKeyStatus(): Flow<EssentialKeyStatus> {
        return combine(
            dataStoreManager.isKeyConfigured,
            dataStoreManager.learnedKeyCode,
            dataStoreManager.learnedScanCode,
            EssentialKeyAccessibilityService.isServiceRunning
        ) { isConfigured, keyCode, scanCode, isRunning ->
            when {
                !isRunning -> EssentialKeyStatus.ServiceDisabled
                isConfigured -> EssentialKeyStatus.Ready(keyCode, scanCode)
                else -> EssentialKeyStatus.NotConfigured
            }
        }
    }

    override fun observeLearnedKey(): Flow<Pair<Int, Int>> {
        return combine(
            dataStoreManager.learnedKeyCode,
            dataStoreManager.learnedScanCode
        ) { keyCode, scanCode ->
            Pair(keyCode, scanCode)
        }
    }

    override fun observeIsKeyConfigured(): Flow<Boolean> {
        return dataStoreManager.isKeyConfigured
    }

    override suspend fun saveLearnedKey(keyCode: Int, scanCode: Int) {
        dataStoreManager.saveLearnedKey(keyCode, scanCode)
    }

    override suspend fun resetLearnedKey() {
        dataStoreManager.resetLearnedKey()
    }
}
