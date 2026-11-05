package com.monmoy.cmfessentialremapper.domain.usecase

import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe CMF Essential Key connection and mapping status.
 */
class GetEssentialKeyStatusUseCase(
    private val repository: RingerModeRepository
) {
    operator fun invoke(): Flow<EssentialKeyStatus> = repository.observeEssentialKeyStatus()
}

