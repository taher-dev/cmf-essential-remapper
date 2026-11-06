package com.monmoy.cmfessentialremapper.presentation.setup

import com.monmoy.cmfessentialremapper.utils.EssentialSpaceStatus
import com.monmoy.cmfessentialremapper.utils.PackageInstallStatus

data class SetupUiState(
    val isNotificationPolicyAccessGranted: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isKeyIdentified: Boolean = false,
    val learnedKeyCode: Int = 0,
    val learnedScanCode: Int = 250,
    val essentialSpaceStatus: EssentialSpaceStatus = EssentialSpaceStatus(
        spacePackageStatus = PackageInstallStatus.ENABLED,
        recorderPackageStatus = PackageInstallStatus.ENABLED,
        isShizukuInstalled = false,
        isShizukuRunning = false,
        isShizukuPermissionGranted = false
    ),
    val isPerformingAction: Boolean = false,
    val showAccessibilityDisclosure: Boolean = false,
    val showShizukuSetupDialog: Boolean = false,
    val packageToDisableConfirm: String? = null,
    val showRestoreConfirmation: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Detection is ready when accessibility service is running and the hardware key is identified.
     */
    val isDetectionReady: Boolean
        get() = isAccessibilityServiceEnabled && isKeyIdentified

    /**
     * Ringer toggle is ready when notification policy access (DND/Silent access) is granted.
     */
    val isRingerToggleReady: Boolean
        get() = isNotificationPolicyAccessGranted

    /**
     * Full single press takeover is ready when detection, ringer toggle, and space suppression are all satisfied.
     */
    val isSinglePressTakeoverReady: Boolean
        get() = isDetectionReady && isRingerToggleReady && essentialSpaceStatus.isTakeoverReady

    val progressPercent: Float
        get() {
            var completed = 0
            if (isNotificationPolicyAccessGranted) completed++
            if (isAccessibilityServiceEnabled) completed++
            if (isBatteryOptimizationIgnored) completed++
            if (isKeyIdentified) completed++
            if (essentialSpaceStatus.isTakeoverReady) completed++
            return completed / 5f
        }
}

