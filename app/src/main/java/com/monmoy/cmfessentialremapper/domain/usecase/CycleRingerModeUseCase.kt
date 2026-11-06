package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode

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
            if (updated != nextMode) {
                kotlinx.coroutines.delay(100)
                updated = repository.getCurrentRingerMode()
            }
            Result.success(if (updated == nextMode) updated else nextMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

