package com.monmoy.cmfessentialremapper.presentation.setup

import com.monmoy.cmfessentialremapper.utils.EssentialSpaceStatus
import com.monmoy.cmfessentialremapper.utils.PackageInstallStatus

data class SetupUiState(
    val isNotificationPolicyAccessGranted: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val essentialSpaceStatus: EssentialSpaceStatus = EssentialSpaceStatus(
        spacePackageStatus = PackageInstallStatus.ENABLED,
        recorderPackageStatus = PackageInstallStatus.ENABLED,
        isShizukuInstalled = false,
        isShizukuRunning = false,
        isShizukuPermissionGranted = false
    ),
    val isPerformingAction: Boolean = false,
    val showAccessibilityDisclosure: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Detection is ready when accessibility service is enabled (physical key is auto-detected).
     */
    val isDetectionReady: Boolean
        get() = isAccessibilityServiceEnabled

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

    /**
     * 5-step progress calculation:
     * 1. Notification Access
     * 2. Accessibility & Restricted Settings
     * 3. Battery Optimization
     * 4. Shizuku Setup & Authorization
     * 5. Disable Essential Space & Recorder
     */
    val progressPercent: Float
        get() = completedStepCount / 5f

    val completedStepCount: Int
        get() {
            var completed = 0
            if (isNotificationPolicyAccessGranted) completed++
            if (isAccessibilityServiceEnabled) completed++
            if (isBatteryOptimizationIgnored) completed++
            if (essentialSpaceStatus.isShizukuReady) completed++
            if (essentialSpaceStatus.isTakeoverReady) completed++
            return completed
        }
}

