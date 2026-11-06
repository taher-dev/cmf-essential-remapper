package com.monmoy.cmfessentialremapper.presentation.setup

import android.app.Application
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dataStoreManager = DataStoreManager(context)
    private val spaceManager = EssentialSpaceManager(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        observeKeyAndServiceStatus()
        refreshStatus()
    }

    private fun observeKeyAndServiceStatus() {
        viewModelScope.launch {
            combine(
                dataStoreManager.isKeyConfigured,
                dataStoreManager.learnedKeyCode,
                dataStoreManager.learnedScanCode,
                EssentialKeyAccessibilityService.isServiceRunning
            ) { isConfigured, keyCode, scanCode, isServiceRunning ->
                Quadruple(isConfigured, keyCode, scanCode, isServiceRunning)
            }.collect { (isConfigured, keyCode, scanCode, isServiceRunning) ->
                _uiState.update { current ->
                    current.copy(
                        isKeyIdentified = isConfigured,
                        learnedKeyCode = keyCode,
                        learnedScanCode = scanCode,
                        isAccessibilityServiceEnabled = isServiceRunning || checkAccessibilityEnabledFromSettings()
                    )
                }
            }
        }
    }

    fun refreshStatus() {
        val hasNotificationPolicy = notificationManager?.isNotificationPolicyAccessGranted == true
        val isServiceRunning = EssentialKeyAccessibilityService.isServiceRunning.value || checkAccessibilityEnabledFromSettings()
        val isBatteryIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        val spaceStatus = spaceManager.getOverallStatus()

        _uiState.update {
            it.copy(
                isNotificationPolicyAccessGranted = hasNotificationPolicy,
                isAccessibilityServiceEnabled = isServiceRunning,
                isBatteryOptimizationIgnored = isBatteryIgnored,
                essentialSpaceStatus = spaceStatus
            )
        }
    }

    private fun checkAccessibilityEnabledFromSettings(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            enabledServices.contains(context.packageName)
        } catch (_: Exception) {
            false
        }
    }

    fun onAccessibilityCardClicked() {
        if (_uiState.value.isAccessibilityServiceEnabled) {
            // Already enabled, open settings directly for user inspection
            openAccessibilitySettings()
        } else {
            // Show required explicit disclosure before opening settings
            _uiState.update { it.copy(showAccessibilityDisclosure = true) }
        }
    }

    fun onDismissAccessibilityDisclosure() {
        _uiState.update { it.copy(showAccessibilityDisclosure = false) }
    }

    fun onConfirmAccessibilityDisclosure() {
        _uiState.update { it.copy(showAccessibilityDisclosure = false) }
        openAccessibilitySettings()
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Accessibility Settings.") }
        }
    }

    fun onOpenNotificationPolicySettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Notification Policy Settings.") }
        }
    }

    fun onOpenAppInfoForRestrictedSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open App Info.") }
        }
    }

    fun onOpenBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not open Battery Settings.") }
            }
        }
    }

    fun onOpenPackageAppInfo(packageName: String) {
        try {
            val intent = spaceManager.createOpenAppInfoIntent(packageName)
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open App Info for $packageName.") }
        }
    }

    fun onRequestDisablePackageConfirmation(packageName: String) {
        _uiState.update { it.copy(packageToDisableConfirm = packageName) }
    }

    fun onDismissDisablePackageConfirmation() {
        _uiState.update { it.copy(packageToDisableConfirm = null) }
    }

    fun onConfirmDisablePackage(packageName: String) {
        _uiState.update { it.copy(packageToDisableConfirm = null, isPerformingAction = true) }
        viewModelScope.launch {
            if (spaceManager.isShizukuRunning() && spaceManager.isShizukuPermissionGranted()) {
                val result = spaceManager.setPackageEnabledShizuku(packageName, enable = false)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                infoMessage = "Successfully disabled $packageName via Shizuku."
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                errorMessage = "Shizuku operation failed: ${err.localizedMessage}."
                            )
                        }
                    }
                )
            } else if (spaceManager.isShizukuRunning() && !spaceManager.isShizukuPermissionGranted()) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        infoMessage = "Shizuku permission required. Requesting permission..."
                    )
                }
                spaceManager.requestShizukuPermission(1001)
            } else {
                // Route user to setup Shizuku since Nothing OS protects system apps in Settings
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        showShizukuSetupDialog = true,
                        infoMessage = "Nothing OS prevents disabling system apps via App Info. Please set up Shizuku."
                    )
                }
            }
            refreshStatus()
        }
    }

    fun onRestorePackage(packageName: String) {
        _uiState.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            if (spaceManager.isShizukuRunning() && spaceManager.isShizukuPermissionGranted()) {
                val result = spaceManager.setPackageEnabledShizuku(packageName, enable = true)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                infoMessage = "Successfully restored $packageName."
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                errorMessage = "Shizuku restore failed: ${err.localizedMessage}."
                            )
                        }
                    }
                )
            } else if (spaceManager.isShizukuRunning() && !spaceManager.isShizukuPermissionGranted()) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        infoMessage = "Shizuku permission required. Requesting permission..."
                    )
                }
                spaceManager.requestShizukuPermission(1001)
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        showShizukuSetupDialog = true
                    )
                }
            }
            refreshStatus()
        }
    }

    fun onRestoreAllPackages() {
        _uiState.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            if (spaceManager.isShizukuRunning() && spaceManager.isShizukuPermissionGranted()) {
                spaceManager.setPackageEnabledShizuku(EssentialSpaceManager.PKG_ESSENTIAL_SPACE, true)
                spaceManager.setPackageEnabledShizuku(EssentialSpaceManager.PKG_ESSENTIAL_RECORDER, true)
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        infoMessage = "Restored Essential Space & Recorder via Shizuku."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        showShizukuSetupDialog = true,
                        infoMessage = "Please set up Shizuku to restore packages."
                    )
                }
            }
            refreshStatus()
        }
    }

    fun onOpenShizukuSetup() {
        _uiState.update { it.copy(showShizukuSetupDialog = true) }
    }

    fun onDismissShizukuSetup() {
        _uiState.update { it.copy(showShizukuSetupDialog = false) }
    }

    fun onLaunchShizukuApp() {
        try {
            val intent = spaceManager.createOpenShizukuIntent()
            if (intent != null) {
                context.startActivity(intent)
            } else {
                onInstallShizuku()
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Shizuku app.") }
        }
    }

    fun onInstallShizuku() {
        try {
            val intent = spaceManager.createInstallShizukuIntent()
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Google Play Store.") }
        }
    }

    fun onOpenDeveloperOptions() {
        try {
            val intent = spaceManager.createOpenDeveloperOptionsIntent()
            context.startActivity(intent)
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Developer Options.") }
        }
    }

    fun onCopyAdbCommand(command: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ADB Command", command)
            clipboard.setPrimaryClip(clip)
            _uiState.update { it.copy(infoMessage = "ADB command copied to clipboard!") }
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not copy to clipboard.") }
        }
    }

    fun onRequestShizukuPermission() {
        spaceManager.requestShizukuPermission(1001)
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

