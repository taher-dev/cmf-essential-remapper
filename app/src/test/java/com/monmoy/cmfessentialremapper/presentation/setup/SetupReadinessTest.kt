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
    }

    @Test
    fun testDetectionReadyRequiresBothServiceAndKey() {
        val onlyService = SetupUiState(
            isAccessibilityServiceEnabled = true,
            isKeyIdentified = false
        )
        assertFalse(onlyService.isDetectionReady)

        val onlyKey = SetupUiState(
            isAccessibilityServiceEnabled = false,
            isKeyIdentified = true
        )
        assertFalse(onlyKey.isDetectionReady)

        val both = SetupUiState(
            isAccessibilityServiceEnabled = true,
            isKeyIdentified = true
        )
        assertTrue(both.isDetectionReady)
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
            isKeyIdentified = true,
            isNotificationPolicyAccessGranted = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.ENABLED,
                recorderPackageStatus = PackageInstallStatus.ENABLED,
                isShizukuInstalled = false,
                isShizukuRunning = false,
                isShizukuPermissionGranted = false
            )
        )
        assertFalse(spaceActive.isSinglePressTakeoverReady)

        // Essential Space DISABLED -> ready
        val spaceDisabled = SetupUiState(
            isAccessibilityServiceEnabled = true,
            isKeyIdentified = true,
            isNotificationPolicyAccessGranted = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.DISABLED,
                recorderPackageStatus = PackageInstallStatus.DISABLED,
                isShizukuInstalled = false,
                isShizukuRunning = false,
                isShizukuPermissionGranted = false
            )
        )
        assertTrue(spaceDisabled.isSinglePressTakeoverReady)
    }

    @Test
    fun testProgressPercentCalculation() {
        val fullyConfigured = SetupUiState(
            isNotificationPolicyAccessGranted = true,
            isAccessibilityServiceEnabled = true,
            isBatteryOptimizationIgnored = true,
            isKeyIdentified = true,
            essentialSpaceStatus = EssentialSpaceStatus(
                spacePackageStatus = PackageInstallStatus.DISABLED,
                recorderPackageStatus = PackageInstallStatus.DISABLED,
                isShizukuInstalled = false,
                isShizukuRunning = false,
                isShizukuPermissionGranted = false
            )
        )

        assertEquals(1.0f, fullyConfigured.progressPercent, 0.01f)
    }
}

