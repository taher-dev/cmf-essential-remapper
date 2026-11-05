package com.monmoy.cmfessentialremapper.presentation.home

import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode

/**
 * Immutable UI State for the Home screen.
 */
data class HomeUiState(
    val currentRingerMode: RingerMode = RingerMode.RING,
    val keyStatus: EssentialKeyStatus = EssentialKeyStatus.NotConfigured,
    val isSetupComplete: Boolean = false,
    val isChangingMode: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val errorMessage: String? = null
)
