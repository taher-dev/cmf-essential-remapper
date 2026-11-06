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

    override suspend fun setRingerMode(mode: RingerMode) = withContext(Dispatchers.IO) {
        val am = audioManager
            ?: throw IllegalStateException("AudioManager service unavailable.")

        when (mode) {
            RingerMode.SILENT -> {
                if (isShizukuReady()) {
                    // 1. Transition through NORMAL if currently in VIBRATE so vibration engine clears
                    if (am.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                        try { am.ringerMode = AudioManager.RINGER_MODE_NORMAL } catch (_: Exception) {}
                        delay(100)
                    }

                    // 2. Set ringer mode via AudioManager if permission granted
                    if (hasNotificationPolicyAccess()) {
                        try { am.ringerMode = AudioManager.RINGER_MODE_SILENT } catch (_: Exception) {}
                    }

                    // 3. Use privileged Shizuku shell to set Silent mode and ensure DND is OFF:
                    //    - cmd audio set-ringer-mode 0 (SILENT)
                    //    - cmd media_session volume --stream 2 --set 0 (Mute Ring)
                    //    - cmd media_session volume --stream 5 --set 0 (Mute Notification)
                    //    - cmd notification set_dnd off (Disable DND filter)
                    //    - settings put global zen_mode 0 (Disable Zen Mode)
                    //    - settings put global mode_ringer 0 (Set global ringer to Silent)
                    executeShizukuCommands(
                        "cmd audio set-ringer-mode 0",
                        "cmd media_session volume --stream 2 --set 0",
                        "cmd media_session volume --stream 5 --set 0",
                        "cmd notification set_dnd off",
                        "settings put global zen_mode 0",
                        "settings put global mode_ringer 0"
                    )

                    // 4. Secondary pass after short delay to extinguish any asynchronous DND activation by AudioService
                    delay(150)
                    executeShizukuCommands(
                        "cmd notification set_dnd off",
                        "settings put global zen_mode 0"
                    )
                } else {
                    if (!hasNotificationPolicyAccess()) {
                        throw SecurityException("Notification Policy Access is required to switch to Silent mode.")
                    }
                    if (am.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        delay(150)
                    }
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
            }

            RingerMode.VIBRATE -> {
                am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                if (isShizukuReady()) {
                    executeShizukuCommands(
                        "cmd audio set-ringer-mode 1",
                        "cmd notification set_dnd off",
                        "settings put global zen_mode 0",
                        "settings put global mode_ringer 1"
                    )
                }
            }

            RingerMode.RING -> {
                am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                if (isShizukuReady()) {
                    executeShizukuCommands(
                        "cmd audio set-ringer-mode 2",
                        "cmd notification set_dnd off",
                        "settings put global zen_mode 0",
                        "settings put global mode_ringer 2"
                    )
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
