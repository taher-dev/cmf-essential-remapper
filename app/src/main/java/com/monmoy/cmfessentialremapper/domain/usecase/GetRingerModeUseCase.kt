package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe live device ringer mode changes and query current state.
 */
class GetRingerModeUseCase(
    private val repository: RingerModeRepository
) {
    operator fun invoke(): Flow<RingerMode> = repository.observeRingerMode()

    fun getCurrentMode(): RingerMode = repository.getCurrentRingerMode()
}
