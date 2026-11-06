package com.monmoy.cmfessentialremapper.presentation.setup

import com.monmoy.cmfessentialremapper.utils.EssentialSpaceStatus
import com.monmoy.cmfessentialremapper.utils.PackageInstallStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupReadinessTest {

    @Test
    fun testInitialStateIsNotReady() {
        val state = SetupUiState()

        assertFalse(state.isDetectionReady)
        assertFalse(state.isRingerToggleReady)
        assertFalse(state.isSinglePressTakeoverReady)
        assertEquals(0f, state.progressPercent, 0.01f)
        assertEquals(0, state.completedStepCount)
    }

    @Test
    fun testDetectionReadyRequiresAccessibilityService() {
        val serviceDisabled = SetupUiState(isAccessibilityServiceEnabled = false)
        assertFalse(serviceDisabled.isDetectionReady)

        val serviceEnabled = SetupUiState(isAccessibilityServiceEnabled = true)
        assertTrue(serviceEnabled.isDetectionReady)
    }

    @Test
    fun testRingerToggleReadyRequiresNotificationPolicyAccess() {
        val noPolicy = SetupUiState(isNotificationPolicyAccessGranted = false)
        assertFalse(noPolicy.isRingerToggleReady)

        val withPolicy = SetupUiState(isNotificationPolicyAccessGranted = true)
        assertTrue(withPolicy.isRingerToggleReady)
    }

    @Test
    fun testSinglePressTakeoverReadyRequiresAllConditions() {
        // Essential Space active (ENABLED) -> not ready
        val spaceActive = SetupUiState(
            isAccessibilityServiceEnabled = true,
            isNotificationPolicyAccessGranted = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.ENABLED,
                recorderPackageStatus = PackageInstallStatus.ENABLED,
                isShizukuInstalled = true,
                isShizukuRunning = true,
                isShizukuPermissionGranted = true
            )
        )
        assertFalse(spaceActive.isSinglePressTakeoverReady)

        // Essential Space DISABLED -> ready
        val spaceDisabled = SetupUiState(
            isAccessibilityServiceEnabled = true,
            isNotificationPolicyAccessGranted = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.DISABLED,
                recorderPackageStatus = PackageInstallStatus.DISABLED,
                isShizukuInstalled = true,
                isShizukuRunning = true,
                isShizukuPermissionGranted = true
            )
        )
        assertTrue(spaceDisabled.isSinglePressTakeoverReady)
    }

    @Test
    fun testProgressPercentCalculation() {
        val step2Done = SetupUiState(
            isNotificationPolicyAccessGranted = true,
            isAccessibilityServiceEnabled = true
        )
        assertEquals(2, step2Done.completedStepCount)
        assertEquals(0.4f, step2Done.progressPercent, 0.01f)

        val fullyConfigured = SetupUiState(
            isNotificationPolicyAccessGranted = true,
            isAccessibilityServiceEnabled = true,
            isBatteryOptimizationIgnored = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.DISABLED,
                recorderPackageStatus = PackageInstallStatus.DISABLED,
                isShizukuInstalled = true,
                isShizukuRunning = true,
                isShizukuPermissionGranted = true
            )
        )

        assertEquals(5, fullyConfigured.completedStepCount)
        assertEquals(1.0f, fullyConfigured.progressPercent, 0.01f)
    }
}
