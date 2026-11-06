package com.monmoy.cmfessentialremapper.presentation.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.monmoy.cmfessentialremapper.presentation.setup.components.AccessibilityDisclosureDialog
import com.monmoy.cmfessentialremapper.presentation.setup.components.StepGuideCard
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBackground
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager

private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun SetupScreen(
    uiState: SetupUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onOpenNotificationPolicy: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onConfirmAccessibilityDisclosure: () -> Unit,
    onDismissAccessibilityDisclosure: () -> Unit,
    onOpenAppInfoForRestrictedSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onDownloadShizuku: () -> Unit,
    onOpenShizuku: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onDisableBothEssentialApps: () -> Unit,
    onRestoreBothEssentialApps: () -> Unit,
    onOpenManualAppInfo: (String) -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Live auto-refresh whenever the user returns to this screen from any system setting or app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshClick()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressPercent,
        animationSpec = tween(durationMillis = 350),
        label = "stepProgress"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NothingBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NothingSurface)
                            .border(1.dp, NothingSurfaceBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NothingWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "SETUP GUIDE",
                            style = MaterialTheme.typography.titleMedium,
                            color = NothingWhite
                        )
                        Text(
                            text = "Step-by-step CMF 2 Pro configuration",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingGrayMedium
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NothingSurface)
                        .border(1.dp, NothingSurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = NothingGrayLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Minimal Progress Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NothingSurface)
                    .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPLETION PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingGrayMedium
                        )
                        Text(
                            text = "${uiState.completedStepCount} / 5 COMPLETED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.completedStepCount == 5) SuccessGreen else NothingWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = if (uiState.completedStepCount == 5) SuccessGreen else CmfRedAccent,
                        trackColor = NothingBlack
                    )
                }
            }

            // Completion Banner (if all 5 steps are done)
            AnimatedVisibility(visible = uiState.completedStepCount == 5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NothingSurface)
                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ALL SET — Physical key is ready to cycle ringer modes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NothingWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Notification Access
            StepGuideCard(
                stepNumber = "01",
                title = "Notification Access",
                description = "Required by Android to switch system ringer to Silent mode without DND restrictions.",
                statusText = if (uiState.isNotificationPolicyAccessGranted) "Granted" else "Required",
                isCompleted = uiState.isNotificationPolicyAccessGranted
            ) {
                Button(
                    onClick = onOpenNotificationPolicy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isNotificationPolicyAccessGranted) NothingBlack else CmfRedAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (uiState.isNotificationPolicyAccessGranted) androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder) else null
                ) {
                    Text(
                        text = if (uiState.isNotificationPolicyAccessGranted) "OPEN NOTIFICATION SETTINGS" else "GRANT ACCESS",
                        style = MaterialTheme.typography.titleSmall,
                        color = NothingWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 2: Accessibility Settings -> Restricted Settings
            StepGuideCard(
                stepNumber = "02",
                title = "Accessibility & Restricted Settings",
                description = "Enables hardware key detection. If greyed out on Android 13+, tap 'App Info' → three dots → 'Allow restricted settings'.",
                statusText = if (uiState.isAccessibilityServiceEnabled) "Enabled" else "Action Needed",
                isCompleted = uiState.isAccessibilityServiceEnabled
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccessibilityClick,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isAccessibilityServiceEnabled) NothingBlack else CmfRedAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (uiState.isAccessibilityServiceEnabled) androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder) else null
                    ) {
                        Text(
                            text = if (uiState.isAccessibilityServiceEnabled) "ACCESSIBILITY" else "ENABLE SERVICE",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingWhite
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAppInfoForRestrictedSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite)
                    ) {
                        Text(
                            text = "APP INFO",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingGrayLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 3: Battery Optimization (Direct package prompt)
            StepGuideCard(
                stepNumber = "03",
                title = "Battery Optimization",
                description = "Sets background usage to Unrestricted so Nothing OS does not kill key detection in standby.",
                statusText = if (uiState.isBatteryOptimizationIgnored) "Unrestricted" else "Optimized",
                isCompleted = uiState.isBatteryOptimizationIgnored
            ) {
                Button(
                    onClick = onOpenBatterySettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isBatteryOptimizationIgnored) NothingBlack else CmfRedAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (uiState.isBatteryOptimizationIgnored) androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder) else null
                ) {
                    Text(
                        text = if (uiState.isBatteryOptimizationIgnored) "BATTERY SETTINGS" else "SET UNRESTRICTED",
                        style = MaterialTheme.typography.titleSmall,
                        color = NothingWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 4: Setup Shizuku
            val shizukuReady = uiState.essentialSpaceStatus.isShizukuReady
            val shizukuRunning = uiState.essentialSpaceStatus.isShizukuRunning
            val shizukuInstalled = uiState.essentialSpaceStatus.isShizukuInstalled

            val shizukuStatus = when {
                shizukuReady -> "Authorized"
                shizukuRunning -> "Authorization Needed"
                shizukuInstalled -> "Not Running"
                else -> "Not Installed"
            }

            val shizukuDescription = when {
                shizukuReady -> "Shizuku is running and authorized. 1-tap disabling of Nothing OS overlays is active."
                shizukuRunning -> "Shizuku is active. Tap 'Authorize App' to grant package management permission."
                shizukuInstalled -> "Open Shizuku and start it via Wireless Debugging, then return to this screen."
                else -> "Download Shizuku to allow 1-tap disabling of Essential Space without rooting."
            }

            StepGuideCard(
                stepNumber = "04",
                title = "Setup Shizuku",
                description = shizukuDescription,
                statusText = shizukuStatus,
                isCompleted = shizukuReady
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!shizukuInstalled) {
                        Button(
                            onClick = onDownloadShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "DOWNLOAD SHIZUKU",
                                style = MaterialTheme.typography.titleSmall,
                                color = NothingWhite
                            )
                        }
                    } else if (!shizukuRunning) {
                        Button(
                            onClick = onOpenShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "OPEN SHIZUKU",
                                style = MaterialTheme.typography.titleSmall,
                                color = NothingWhite
                            )
                        }
                    } else if (!shizukuReady) {
                        Button(
                            onClick = onRequestShizukuPermission,
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "AUTHORIZE APP",
                                style = MaterialTheme.typography.titleSmall,
                                color = NothingWhite
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenShizuku,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite)
                        ) {
                            Text(
                                text = "OPEN",
                                style = MaterialTheme.typography.titleSmall,
                                color = NothingGrayLight
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onOpenShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite)
                        ) {
                            Text(
                                text = "OPEN SHIZUKU",
                                style = MaterialTheme.typography.titleSmall,
                                color = NothingGrayLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 5: Disable Essential Space & Recorder with One Button Tap
            val areBothDisabled = uiState.essentialSpaceStatus.areBothDisabled
            StepGuideCard(
                stepNumber = "05",
                title = "Disable Essential Apps",
                description = "Disables Essential Space & Recorder so single-pressing the physical key doesn't trigger Nothing OS overlays.",
                statusText = if (areBothDisabled) "Disabled" else "Active",
                isCompleted = areBothDisabled
            ) {
                if (!areBothDisabled) {
                    Button(
                        onClick = onDisableBothEssentialApps,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isPerformingAction,
                        colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (uiState.isPerformingAction) "DISABLING..." else "DISABLE BOTH APPS (1-TAP)",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingWhite
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Both packages are disabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                        OutlinedButton(
                            onClick = onRestoreBothEssentialApps,
                            enabled = !uiState.isPerformingAction,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite)
                        ) {
                            Text(
                                text = "RE-ENABLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = NothingGrayLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Accessibility Disclosure Dialog
        if (uiState.showAccessibilityDisclosure) {
            AccessibilityDisclosureDialog(
                onConfirm = onConfirmAccessibilityDisclosure,
                onDismissRequest = onDismissAccessibilityDisclosure
            )
        }
    }
}
