package com.monmoy.cmfessentialremapper.presentation.setup

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.presentation.setup.components.AccessibilityDisclosureDialog
import com.monmoy.cmfessentialremapper.presentation.setup.components.EssentialSpaceCard
import com.monmoy.cmfessentialremapper.presentation.setup.components.PermissionCard
import com.monmoy.cmfessentialremapper.presentation.setup.components.SetupProgressIndicator
import com.monmoy.cmfessentialremapper.presentation.setup.components.ShizukuSetupDialog
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBackground
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    uiState: SetupUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onOpenNotificationPolicy: () -> Unit,
    onOpenRestrictedSettingsGuide: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onConfirmAccessibilityDisclosure: () -> Unit,
    onDismissAccessibilityDisclosure: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenKeyLearning: () -> Unit,
    onDisablePackage: (String) -> Unit,
    onConfirmDisablePackage: (String) -> Unit,
    onDismissDisableConfirmation: () -> Unit,
    onRestorePackage: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onRestoreAllPackages: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizukuSetup: () -> Unit,
    onDismissShizukuSetup: () -> Unit,
    onLaunchShizukuApp: () -> Unit,
    onInstallShizuku: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onCopyAdbCommand: (String) -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
            // Setup Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(38.dp)
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
                            text = "SETUP & PERMISSIONS",
                            style = MaterialTheme.typography.titleMedium,
                            color = NothingWhite
                        )
                        Text(
                            text = "CMF Phone 2 Pro Integration",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingGrayMedium
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(38.dp)
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

            // Readiness Progress Indicator
            SetupProgressIndicator(
                progressPercent = uiState.progressPercent,
                isDetectionReady = uiState.isDetectionReady,
                isRingerToggleReady = uiState.isRingerToggleReady,
                isSinglePressTakeoverReady = uiState.isSinglePressTakeoverReady
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Notification Policy Access
            PermissionCard(
                title = "Notification Policy Access",
                explanation = "Required by Android to use the standard Silent ringer mode without throwing security exceptions.",
                statusText = if (uiState.isNotificationPolicyAccessGranted) "Granted" else "Required",
                isGranted = uiState.isNotificationPolicyAccessGranted,
                actionButtonText = if (uiState.isNotificationPolicyAccessGranted) "Settings (Granted)" else "Grant Access",
                onActionClick = onOpenNotificationPolicy
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Restricted Settings (Sideload Protection)
            PermissionCard(
                title = "Restricted Settings",
                explanation = "On Android 13+, sideloaded apps may have Accessibility blocked until 'Allow restricted settings' is enabled in App Info (three-dot menu).",
                statusText = if (uiState.isAccessibilityServiceEnabled) "Ready" else "Action Required",
                isGranted = uiState.isAccessibilityServiceEnabled,
                actionButtonText = "Open App Info",
                onActionClick = onOpenRestrictedSettingsGuide
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 3: Accessibility Service
            PermissionCard(
                title = "Accessibility Settings",
                explanation = "Enables physical Essential Key hardware event detection. Processes key events locally without reading screen content.",
                statusText = if (uiState.isAccessibilityServiceEnabled) "Enabled" else "Disabled",
                isGranted = uiState.isAccessibilityServiceEnabled,
                actionButtonText = if (uiState.isAccessibilityServiceEnabled) "Service Configured" else "Enable Accessibility",
                onActionClick = onAccessibilityClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 4: Battery Optimization
            PermissionCard(
                title = "Battery Optimization",
                explanation = "Allows CMF Essential Remapper to remain responsive in the background under Nothing OS aggressive app standby.",
                statusText = if (uiState.isBatteryOptimizationIgnored) "Unrestricted" else "Optimized",
                isGranted = uiState.isBatteryOptimizationIgnored,
                actionButtonText = "Battery Settings",
                onActionClick = onOpenBatterySettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key Identification & Learning Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NothingSurfaceBorder)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isKeyIdentified) CmfRedAccent else NothingGrayDark)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Essential Key Identifier",
                                style = MaterialTheme.typography.titleMedium,
                                color = NothingWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NothingBlack)
                                .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (uiState.isKeyIdentified) "MAPPED" else "DEFAULT (0/250)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isKeyIdentified) NothingWhite else NothingGrayMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current configuration: KeyCode ${uiState.learnedKeyCode}, ScanCode ${uiState.learnedScanCode}. Verify or dynamically learn the key on your CMF Phone 2 Pro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NothingGrayLight
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenKeyLearning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NothingBlack),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                    ) {
                        Text(
                            text = "OPEN KEY DETECTION & LEARNING",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Essential Space Control Section
            EssentialSpaceCard(
                status = uiState.essentialSpaceStatus,
                onDisablePackage = onDisablePackage,
                onRestorePackage = onRestorePackage,
                onOpenAppInfo = onOpenAppInfo,
                onRestoreAll = onRestoreAllPackages,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onOpenShizukuSetup = onOpenShizukuSetup
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Shizuku Setup Guide Dialog
        if (uiState.showShizukuSetupDialog) {
            ShizukuSetupDialog(
                status = uiState.essentialSpaceStatus,
                onDismissRequest = onDismissShizukuSetup,
                onLaunchShizuku = onLaunchShizukuApp,
                onInstallShizuku = onInstallShizuku,
                onOpenDeveloperOptions = onOpenDeveloperOptions,
                onRequestShizukuPermission = onRequestShizukuPermission,
                onCopyAdbCommand = onCopyAdbCommand
            )
        }

        // Accessibility Disclosure Dialog
        if (uiState.showAccessibilityDisclosure) {
            AccessibilityDisclosureDialog(
                onConfirm = onConfirmAccessibilityDisclosure,
                onDismissRequest = onDismissAccessibilityDisclosure
            )
        }

        // Package Disable Confirmation Dialog
        uiState.packageToDisableConfirm?.let { pkg ->
            BasicAlertDialog(
                onDismissRequest = onDismissDisableConfirmation
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(NothingSurface)
                        .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "DISABLE PACKAGE CONFIRMATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CmfRedAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Disable $pkg?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NothingWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Disabling this Nothing OS system package will stop Essential Space from intercepting the physical key. You can restore this package anytime from this screen.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NothingGrayLight
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismissDisableConfirmation) {
                            Text(text = "CANCEL", color = NothingGrayLight)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onConfirmDisablePackage(pkg) },
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "DISABLE PACKAGE", color = NothingWhite)
                        }
                    }
                }
            }
        }
    }
}

