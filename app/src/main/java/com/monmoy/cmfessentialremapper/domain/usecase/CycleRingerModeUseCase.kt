package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Ordering sequence options for ringer mode cycling.
 */
enum class RingerCycleOrder {
    /** Ring -> Vibrate -> Silent -> Ring (Cycle Ring Mode 1) */
    RING_VIBRATE_SILENT,

    /** Ring -> Silent -> Vibrate -> Ring (Cycle Ring Mode 2) */
    RING_SILENT_VIBRATE
}

/**
 * Domain usecase to cycle the system ringer mode according to the chosen [RingerCycleOrder].
 *
 * Thread-safe via Mutex to prevent race conditions during rapid hardware key presses.
 */
class CycleRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    private val mutex = Mutex()

    suspend operator fun invoke(
        order: RingerCycleOrder = RingerCycleOrder.RING_VIBRATE_SILENT
    ): Result<RingerMode> = mutex.withLock {
        try {
            val current = repository.getCurrentRingerMode()
            val hasPolicy = repository.hasNotificationPolicyAccess()

            val nextMode = when (order) {
                RingerCycleOrder.RING_VIBRATE_SILENT -> when (current) {
                    RingerMode.RING -> RingerMode.VIBRATE
                    RingerMode.VIBRATE -> if (hasPolicy) RingerMode.SILENT else RingerMode.RING
                    RingerMode.SILENT -> RingerMode.RING
                }
                RingerCycleOrder.RING_SILENT_VIBRATE -> when (current) {
                    RingerMode.RING -> if (hasPolicy) RingerMode.SILENT else RingerMode.VIBRATE
                    RingerMode.SILENT -> RingerMode.VIBRATE
                    RingerMode.VIBRATE -> RingerMode.RING
                }
            }

            repository.setRingerMode(nextMode)
            var updated = repository.getCurrentRingerMode()
            var retries = 8
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
