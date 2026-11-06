package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Domain usecase to cycle the system ringer mode in sequence:
 * RING -> VIBRATE -> SILENT -> RING
 *
 * Thread-safe via Mutex to prevent race conditions during rapid hardware key presses.
 */
class CycleRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    private val mutex = Mutex()

    suspend operator fun invoke(): Result<RingerMode> = mutex.withLock {
        try {
            val current = repository.getCurrentRingerMode()
            val hasPolicy = repository.hasNotificationPolicyAccess()

            // If notification policy access is not granted, cycle only between RING and VIBRATE
            // to avoid triggering Android's system security prompt for DND access.
            val nextMode = when (current) {
                RingerMode.RING -> RingerMode.VIBRATE
                RingerMode.VIBRATE -> if (hasPolicy) RingerMode.SILENT else RingerMode.RING
                RingerMode.SILENT -> RingerMode.RING
            }

            repository.setRingerMode(nextMode)
            var updated = repository.getCurrentRingerMode()
            var retries = 6
            while (updated != nextMode && retries > 0) {
                delay(40)
                updated = repository.getCurrentRingerMode()
                retries--
            }
            Result.success(if (updated == nextMode) updated else nextMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

