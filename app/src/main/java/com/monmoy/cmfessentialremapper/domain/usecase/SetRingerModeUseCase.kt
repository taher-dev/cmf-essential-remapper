package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.delay

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
            var retries = 5
            while (updated != targetMode && retries > 0) {
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
