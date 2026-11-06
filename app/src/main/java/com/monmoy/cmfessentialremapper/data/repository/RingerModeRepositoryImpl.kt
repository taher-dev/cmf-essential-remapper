package com.monmoy.cmfessentialremapper.data.repository

import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.receiver.RingerModeReceiver
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import com.monmoy.cmfessentialremapper.utils.AudioUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

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

    // Tracks when Silent mode was triggered to automatically suppress secondary DND activations
    @Volatile
    private var silentModeRequestedTimestamp: Long = 0L

    private fun isShizukuReady(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    private fun executeShizukuCommands(vararg commands: String) {
        try {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            for (cmdStr in commands) {
                try {
                    val cmd = arrayOf("sh", "-c", cmdStr)
                    val process = newProcessMethod.invoke(null, cmd, null, null) as Process
                    process.waitFor()
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    override fun isDoNotDisturbActive(): Boolean {
        val nm = notificationManager ?: return false
        return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    override suspend fun disableDoNotDisturb() = withContext(Dispatchers.IO) {
        try {
            if (hasNotificationPolicyAccess()) {
                notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        } catch (_: Exception) {}

        if (isShizukuReady()) {
            executeShizukuCommands(
                "cmd notification set_dnd off",
                "settings put global zen_mode 0"
            )
        }
    }

    override fun observeRingerMode(): Flow<RingerMode> = callbackFlow {
        trySend(getCurrentRingerMode())

        val receiver = RingerModeReceiver {
            // If DND was activated shortly after user requested Silent mode, auto-dismiss DND
            if (System.currentTimeMillis() - silentModeRequestedTimestamp < 3500) {
                if (isDoNotDisturbActive() && hasNotificationPolicyAccess()) {
                    try {
                        notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    } catch (_: Exception) {}
                }
            }
            trySend(getCurrentRingerMode())
        }

        val filter = IntentFilter().apply {
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("android.media.STREAM_MUTE_CHANGED_ACTION")
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

                // 1. Clear Vibrate state trap:
                // Direct jump from VIBRATE to SILENT is rejected by Android's state machine.
                // We bounce through NORMAL to clear the vibration flag first.
                if (am.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    delay(150)
                }

                // Mark timestamp so reactive receiver also intercepts and kills incoming DND activations
                silentModeRequestedTimestamp = System.currentTimeMillis()

                // 2. Set hardware/system ringer to SILENT
                am.ringerMode = AudioManager.RINGER_MODE_SILENT

                // 3. Multi-stage DND disengagement workaround:
                // Android's AudioService asynchronously activates DND across IPC threads.
                // We run a staged suppression sequence over 1.2s to turn off DND the moment it triggers.
                repeat(4) { stage ->
                    delay(if (stage == 0) 150 else 250)
                    if (isDoNotDisturbActive()) {
                        disableDoNotDisturb()
                    }
                }
            }

            RingerMode.VIBRATE -> {
                val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                if (currentVol > 0) {
                    savedRingVolume = currentVol
                }
                disableDoNotDisturb()
                am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            RingerMode.RING -> {
                disableDoNotDisturb()
                am.ringerMode = AudioManager.RINGER_MODE_NORMAL

                // Ensure ring volume is audible if it was muted
                val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                if (currentVol == 0) {
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                    val target = if (savedRingVolume > 0) savedRingVolume else (max * 0.7).toInt().coerceAtLeast(1)
                    try {
                        am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
                    } catch (_: Exception) {}
                }
            }
        }
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
