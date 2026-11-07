package com.monmoy.cmfessentialremapper.presentation.home

import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.KeyAction
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.utils.KeyGesture

/**
 * Immutable UI State for the Home screen.
 */
data class HomeUiState(
    val currentRingerMode: RingerMode = RingerMode.RING,
    val keyStatus: EssentialKeyStatus = EssentialKeyStatus.NotConfigured,
    val isSetupComplete: Boolean = false,
    val isChangingMode: Boolean = false,
    val isPrivacyPolicyDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val singlePressAction: KeyAction = KeyAction.CYCLE_RINGER,
    val doublePressAction: KeyAction = KeyAction.NONE,
    val longPressAction: KeyAction = KeyAction.NONE,
    val isVisualOverlayEnabled: Boolean = true,
    val selectedGestureForDialog: KeyGesture? = null
)
