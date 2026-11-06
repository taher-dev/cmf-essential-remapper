package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Use case to change the device ringer mode directly.
 * Thread-safe via Mutex to prevent concurrent mode change conflicts.
 */
class SetRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    private val mutex = Mutex()

    suspend operator fun invoke(targetMode: RingerMode): Result<RingerMode> = mutex.withLock {
        val current = repository.getCurrentRingerMode()
        if (current == targetMode) {
            return@withLock Result.success(current)
        }

        try {
            repository.setRingerMode(targetMode)
            var updated = repository.getCurrentRingerMode()
            var retries = 6
            while (updated != targetMode && retries > 0) {
                delay(40)
                updated = repository.getCurrentRingerMode()
                retries--
            }
            Result.success(if (updated == targetMode) updated else targetMode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
