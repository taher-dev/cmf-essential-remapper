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
import kotlinx.coroutines.flow.distinctUntilChanged
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
    }.distinctUntilChanged()

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

        // 1. Privileged setRingerModeInternal via Shizuku (if running & authorized)
        val shizukuSuccess = setRingerModeViaShizuku(targetSystemMode)

        if (!shizukuSuccess) {
            // 2. Standard Android framework path
            when (mode) {
                RingerMode.SILENT -> {
                    if (!hasNotificationPolicyAccess()) {
                        throw SecurityException("Notification Policy Access is required to switch to Silent mode.")
                    }

                    val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                    if (currentVol > 0) {
                        savedRingVolume = currentVol
                    }

                    // Directly mute ring stream with FLAG_ALLOW_RINGER_MODES so transition to silent is accepted
                    try {
                        am.adjustStreamVolume(
                            AudioManager.STREAM_RING,
                            AudioManager.ADJUST_MUTE,
                            AudioManager.FLAG_ALLOW_RINGER_MODES
                        )
                    } catch (_: Throwable) {}

                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                }

                RingerMode.VIBRATE -> {
                    val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                    if (currentVol > 0) {
                        savedRingVolume = currentVol
                    }

                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }

                RingerMode.RING -> {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL

                    // Unmute streams and restore ring volume
                    try {
                        am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                        am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                        val currentVol = am.getStreamVolume(AudioManager.STREAM_RING)
                        if (currentVol == 0) {
                            val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                            val target = if (savedRingVolume > 0) savedRingVolume else (max * 0.7).toInt().coerceAtLeast(1)
                            am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
                        }
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    /**
     * Invokes internal system AudioService.setRingerModeInternal via Shizuku.
     * Uses caller "com.android.shell" since Shizuku runs with Shell UID 2000.
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
                setRingerModeInternal.invoke(audioService, targetMode, "com.android.shell")
                invoked = true
            } catch (_: Throwable) {
                // Method 2: Direct binder transaction using transaction code
                @Suppress("DEPRECATION")
                val code = SystemServiceHelper.getTransactionCode("android.media.IAudioService", "setRingerModeInternal")
                if (code != null) {
                    val data = Parcel.obtain()
                    val reply = Parcel.obtain()
                    try {
                        data.writeInterfaceToken("android.media.IAudioService")
                        data.writeInt(targetMode)
                        data.writeString("com.android.shell")
                        shizukuBinder.transact(code, data, reply, 0)
                        reply.readException()
                        invoked = true
                    } finally {
                        data.recycle()
                        reply.recycle()
                    }
                }
            }

            if (invoked && targetMode == AudioManager.RINGER_MODE_NORMAL) {
                try {
                    audioManager?.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                    audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                } catch (_: Throwable) {}
            }
            invoked
        } catch (_: Throwable) {
            false
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
