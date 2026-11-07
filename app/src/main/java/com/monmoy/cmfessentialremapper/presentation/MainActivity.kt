package com.monmoy.cmfessentialremapper.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monmoy.cmfessentialremapper.presentation.diagnostics.DiagnosticsScreen
import com.monmoy.cmfessentialremapper.presentation.home.HomeScreen
import com.monmoy.cmfessentialremapper.presentation.home.HomeViewModel
import com.monmoy.cmfessentialremapper.presentation.learning.KeyLearningScreen
import com.monmoy.cmfessentialremapper.presentation.learning.KeyLearningViewModel
import com.monmoy.cmfessentialremapper.presentation.setup.SetupScreen
import com.monmoy.cmfessentialremapper.presentation.setup.SetupViewModel
import com.monmoy.cmfessentialremapper.presentation.theme.CMFEssentialRemapperTheme

enum class AppScreen {
    HOME,
    SETUP,
    KEY_LEARNING,
    DIAGNOSTICS
}

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CMFEssentialRemapperTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        val uiState by homeViewModel.uiState.collectAsState()

                        HomeScreen(
                            uiState = uiState,
                            onNavigateToSetup = { currentScreen = AppScreen.SETUP },
                            onGestureClick = homeViewModel::onOpenActionSelector,
                            onSelectAction = homeViewModel::onSelectAction,
                            onDismissActionSelector = homeViewModel::onDismissActionSelector,
                            onToggleVisualOverlay = homeViewModel::onToggleVisualOverlay,
                            onModeSelected = homeViewModel::onSelectRingerMode,
                            onNavigateToKeyLearning = { currentScreen = AppScreen.KEY_LEARNING },
                            onNavigateToDiagnostics = { currentScreen = AppScreen.DIAGNOSTICS },
                            onErrorMessageDismissed = homeViewModel::onErrorMessageDismissed,
                            onOpenPrivacyPolicy = homeViewModel::onOpenPrivacyPolicy,
                            onDismissPrivacyPolicy = homeViewModel::onDismissPrivacyPolicy,
                            onDeveloperClick = homeViewModel::onOpenDeveloperInfo,
                            onDismissDeveloperInfo = homeViewModel::onDismissDeveloperInfo,
                            onAppResumed = homeViewModel::onAppResumed
                        )
                    }

                    AppScreen.SETUP -> {
                        BackHandler { currentScreen = AppScreen.HOME }
                        val setupViewModel: SetupViewModel = viewModel()
                        val setupUiState by setupViewModel.uiState.collectAsState()

                        SetupScreen(
                            uiState = setupUiState,
                            onBackClick = { currentScreen = AppScreen.HOME },
                            onRefreshClick = setupViewModel::refreshStatus,
                            onOpenNotificationPolicy = setupViewModel::onOpenNotificationPolicySettings,
                            onAccessibilityClick = setupViewModel::onAccessibilityCardClicked,
                            onConfirmAccessibilityDisclosure = setupViewModel::onConfirmAccessibilityDisclosure,
                            onDismissAccessibilityDisclosure = setupViewModel::onDismissAccessibilityDisclosure,
                            onOpenAppInfoForRestrictedSettings = setupViewModel::onOpenAppInfoForRestrictedSettings,
                            onOpenBatterySettings = setupViewModel::onOpenBatteryOptimizationSettings,
                            onDownloadShizuku = setupViewModel::onDownloadShizuku,
                            onOpenShizuku = setupViewModel::onOpenShizuku,
                            onRequestShizukuPermission = setupViewModel::onRequestShizukuPermission,
                            onDisableBothEssentialApps = setupViewModel::onDisableBothEssentialApps,
                            onRestoreBothEssentialApps = setupViewModel::onRestoreBothEssentialApps,
                            onOpenManualAppInfo = setupViewModel::onOpenPackageAppInfo,
                            onClearMessages = setupViewModel::clearMessages
                        )
                    }

                    AppScreen.KEY_LEARNING -> {
                        BackHandler { currentScreen = AppScreen.HOME }
                        val learningViewModel: KeyLearningViewModel = viewModel()
                        val learningUiState by learningViewModel.uiState.collectAsState()

                        KeyLearningScreen(
                            uiState = learningUiState,
                            onBackClick = { currentScreen = AppScreen.HOME },
                            onStartDetection = learningViewModel::startListening,
                            onStopDetection = learningViewModel::stopListening,
                            onSaveKey = learningViewModel::saveDetectedKey,
                            onRetry = learningViewModel::retry,
                            onResetToDefaults = learningViewModel::resetToDefaults
                        )
                    }

                    AppScreen.DIAGNOSTICS -> {
                        BackHandler { currentScreen = AppScreen.HOME }
                        DiagnosticsScreen(
                            onBackClick = { currentScreen = AppScreen.HOME }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.onAppResumed()
    }
}
