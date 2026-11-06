package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode

/**
 * Use case to change the device ringer mode directly without DND restrictions.
 */
class SetRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    suspend operator fun invoke(targetMode: RingerMode): Result<RingerMode> {
        val current = repository.getCurrentRingerMode()
        if (current == targetMode) {
            return Result.success(current)
        }

        return try {
            repository.setRingerMode(targetMode)
            var updated = repository.getCurrentRingerMode()
            if (updated != targetMode) {
                kotlinx.coroutines.delay(100)
                updated = repository.getCurrentRingerMode()
            }
            Result.success(if (updated == targetMode) updated else targetMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
