package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.delay

/**
 * Domain usecase to cycle the system ringer mode in sequence:
 * RING -> SILENT -> VIBRATE -> RING
 *
 * Used identically by UI and EssentialKeyAccessibilityService.
 */
class CycleRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    suspend operator fun invoke(): Result<RingerMode> {
        return try {
            val current = repository.getCurrentRingerMode()
            val nextMode = current.next()
            repository.setRingerMode(nextMode)
            var updated = repository.getCurrentRingerMode()
            var retries = 5
            while (updated != nextMode && retries > 0) {
                delay(40)
                updated = repository.getCurrentRingerMode()
                retries--
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

