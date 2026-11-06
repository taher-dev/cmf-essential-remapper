package com.monmoy.cmfessentialremapper.data.repository

import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.Parcel
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
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class RingerModeRepositoryImpl(
    private val context: Context,
    private val dataStoreManager: DataStoreManager = DataStoreManager(context)
) : RingerModeRepository {

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

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

        val targetSystemMode = when (mode) {
            RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
            RingerMode.RING -> AudioManager.RINGER_MODE_NORMAL
        }

        // 1. Try privileged setRingerModeInternal via Shizuku.
        // On modern Android (Android 7+ through 15), setRingerModeInternal is the only
        // mechanism that switches to Silent without engaging Do Not Disturb (ZenMode).
        val shizukuSuccess = setRingerModeViaShizuku(targetSystemMode)

        if (!shizukuSuccess) {
            // Fallback for standard non-Shizuku path
            when (mode) {
                RingerMode.SILENT -> {
                    if (!hasNotificationPolicyAccess()) {
                        throw SecurityException("Notification Policy Access is required to switch to Silent mode.")
                    }
                    // Transition through NORMAL if in VIBRATE to reset vibration state
                    if (am.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        delay(150)
                    }
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                }

                RingerMode.VIBRATE -> {
                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    turnOffDndIfActive()
                }

                RingerMode.RING -> {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    turnOffDndIfActive()
                }
            }
        }

        // When switching to RING, ensure ring and notification volumes are restored to audible level
        if (mode == RingerMode.RING) {
            try {
                am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                if (currentVol == 0) {
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                    am.setStreamVolume(AudioManager.STREAM_RING, (max * 0.7).toInt().coerceAtLeast(1), 0)
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Invokes internal system AudioService.setRingerModeInternal via Shizuku.
     * This bypasses ZenModeHelper's automatic DND activation when entering SILENT mode.
     */
    private fun setRingerModeViaShizuku(targetMode: Int): Boolean {
        return try {
            if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val audioBinder = SystemServiceHelper.getSystemService("audio") ?: return false
            val shizukuBinder = ShizukuBinderWrapper(audioBinder)

            var invoked = false
            // Method 1: Reflection via IAudioService$Stub
            try {
                val stubClass = Class.forName("android.media.IAudioService\$Stub")
                val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
                val audioService = asInterfaceMethod.invoke(null, shizukuBinder)
                val setRingerModeInternal = audioService.javaClass.getMethod(
                    "setRingerModeInternal",
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                setRingerModeInternal.invoke(audioService, targetMode, context.packageName)
                invoked = true
            } catch (_: Throwable) {
                // Method 2: Direct binder transaction using transaction code
                val code = SystemServiceHelper.getTransactionCode("android.media.IAudioService", "setRingerModeInternal")
                if (code != null) {
                    val data = Parcel.obtain()
                    val reply = Parcel.obtain()
                    try {
                        data.writeInterfaceToken("android.media.IAudioService")
                        data.writeInt(targetMode)
                        data.writeString(context.packageName)
                        shizukuBinder.transact(code, data, reply, 0)
                        reply.readException()
                        invoked = true
                    } finally {
                        data.recycle()
                        reply.recycle()
                    }
                }
            }

            if (invoked) {
                turnOffDndIfActive()
            }
            invoked
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Ensures Do Not Disturb (ZenMode) is turned off.
     */
    private fun turnOffDndIfActive() {
        // Method 1: NotificationManager official API
        try {
            if (hasNotificationPolicyAccess()) {
                val filter = notificationManager?.currentInterruptionFilter
                if (filter != null && filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
                    filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
                ) {
                    notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        } catch (_: Throwable) {}

        // Method 2: Shizuku privileged shell command
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }
                val process = newProcessMethod.invoke(
                    null,
                    arrayOf("cmd", "notification", "set_dnd", "off"),
                    null,
                    null
                ) as Process
                process.waitFor()
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
