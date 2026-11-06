package com.monmoy.cmfessentialremapper.presentation.setup

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val spaceManager = EssentialSpaceManager(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        refreshStatus()
    }
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshStatus()
    }
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        refreshStatus()
    }

    init {
        observeAccessibilityService()
        registerShizukuListeners()
        refreshStatus()
    }

    private fun registerShizukuListeners() {
        try {
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
        } catch (_: Throwable) {}
    }

    private fun observeAccessibilityService() {
        viewModelScope.launch {
            EssentialKeyAccessibilityService.isServiceRunning.collect { isRunning ->
                _uiState.update { current ->
                    current.copy(
                        isAccessibilityServiceEnabled = isRunning || checkAccessibilityEnabledFromSettings()
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

    // Step 1: Notification Policy Access
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

    // Step 2: Accessibility & Restricted Settings
    fun onAccessibilityCardClicked() {
        if (_uiState.value.isAccessibilityServiceEnabled) {
            openAccessibilitySettings()
        } else {
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

    // Step 3: Battery Optimization (Directly prompts system dialog for this package)
    fun onOpenBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {
                try {
                    val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(appDetails)
                } catch (_: Exception) {
                    _uiState.update { it.copy(errorMessage = "Could not open Battery Settings.") }
                }
            }
        }
    }

    // Step 4: Shizuku Setup & Authorization
    fun onDownloadShizuku() {
        try {
            context.startActivity(spaceManager.createDownloadShizukuIntent())
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Could not open Play Store.") }
        }
    }

    fun onOpenShizuku() {
        try {
            val intent = spaceManager.createOpenShizukuIntent()
            if (intent != null) {
                context.startActivity(intent)
            } else {
                onDownloadShizuku()
            }
        } catch (_: Exception) {
            onDownloadShizuku()
        }
    }

    fun onRequestShizukuPermission() {
        if (!spaceManager.isShizukuRunning()) {
            onOpenShizuku()
            return
        }
        spaceManager.requestShizukuPermission(1001)
    }

    // Step 5: Disable Essential Space & Recorder with One Button Tap
    fun onDisableBothEssentialApps() {
        _uiState.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            if (spaceManager.isShizukuRunning() && spaceManager.isShizukuPermissionGranted()) {
                val result = spaceManager.disableBothEssentialPackages()
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                infoMessage = "Essential Space and Recorder successfully disabled."
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
            } else {
                // If Shizuku not ready, guide user to manual App Info
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage = "Please authorize Shizuku in Step 4, or disable in App Info."
                    )
                }
                onOpenPackageAppInfo(EssentialSpaceManager.PKG_ESSENTIAL_SPACE)
            }
            refreshStatus()
        }
    }

    fun onRestoreBothEssentialApps() {
        _uiState.update { it.copy(isPerformingAction = true) }
        viewModelScope.launch {
            if (spaceManager.isShizukuRunning() && spaceManager.isShizukuPermissionGranted()) {
                val result = spaceManager.enableBothEssentialPackages()
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                infoMessage = "Essential Space and Recorder restored."
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isPerformingAction = false,
                                errorMessage = "Restore failed: ${err.localizedMessage}."
                            )
                        }
                    }
                )
            } else {
                _uiState.update { it.copy(isPerformingAction = false) }
                onOpenPackageAppInfo(EssentialSpaceManager.PKG_ESSENTIAL_SPACE)
            }
            refreshStatus()
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

    fun clearMessages() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
        } catch (_: Throwable) {}
    }
}
