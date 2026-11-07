package com.monmoy.cmfessentialremapper.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.monmoy.cmfessentialremapper.domain.model.KeyAction
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.presentation.components.ActionSelectionDialog
import com.monmoy.cmfessentialremapper.presentation.components.GestureActionCard
import com.monmoy.cmfessentialremapper.presentation.components.NothingHeader
import com.monmoy.cmfessentialremapper.presentation.components.PrivacyPolicyDialog
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBackground
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.utils.KeyGesture

private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToSetup: () -> Unit,
    onGestureClick: (KeyGesture) -> Unit,
    onSelectAction: (KeyGesture, KeyAction) -> Unit,
    onDismissActionSelector: () -> Unit,
    onToggleVisualOverlay: (Boolean) -> Unit = {},
    onModeSelected: (RingerMode) -> Unit = {},
    onNavigateToKeyLearning: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onErrorMessageDismissed: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onDismissPrivacyPolicy: () -> Unit,
    onAppResumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onAppResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            onErrorMessageDismissed()
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
            // Header
            NothingHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Setup & Configuration Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = CmfRedAccent),
                        onClick = onNavigateToSetup
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(NothingSurfaceBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isSetupComplete) SuccessGreen else CmfRedAccent)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Setup & Permissions",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NothingWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.isSetupComplete) "All permissions granted" else "Configuration required",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isSetupComplete) SuccessGreen else NothingGrayLight
                            )
                        }
                    }

                    if (uiState.isSetupComplete) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "All Permissions Granted",
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Open Setup",
                            tint = NothingGrayLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key Actions Configuration Section
            GestureActionCard(
                singlePressAction = uiState.singlePressAction,
                doublePressAction = uiState.doublePressAction,
                longPressAction = uiState.longPressAction,
                onGestureClick = onGestureClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Display & Overlay Preferences
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NothingWhite
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CmfRedAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NothingSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(NothingSurfaceBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Visual State Overlay",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NothingWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Show sleek HUD pill when Essential Key is pressed",
                                style = MaterialTheme.typography.labelSmall,
                                color = NothingGrayLight
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.isVisualOverlayEnabled,
                            onCheckedChange = onToggleVisualOverlay,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NothingWhite,
                                checkedTrackColor = CmfRedAccent,
                                uncheckedThumbColor = NothingGrayMedium,
                                uncheckedTrackColor = NothingSurfaceBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // About & Privacy Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NothingWhite
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CmfRedAccent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = CmfRedAccent),
                            onClick = onOpenPrivacyPolicy
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NothingSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(NothingSurfaceBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NothingWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "100% Offline • Zero Telemetry • Open Source",
                                style = MaterialTheme.typography.labelSmall,
                                color = NothingGrayLight
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Open Privacy Policy",
                            tint = NothingGrayLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Action Selection Dialog for Chosen Gesture
        uiState.selectedGestureForDialog?.let { gesture ->
            val currentAction = when (gesture) {
                KeyGesture.SINGLE_PRESS -> uiState.singlePressAction
                KeyGesture.DOUBLE_PRESS -> uiState.doublePressAction
                KeyGesture.LONG_PRESS -> uiState.longPressAction
                KeyGesture.TRIPLE_PRESS -> KeyAction.NONE
            }
            ActionSelectionDialog(
                gesture = gesture,
                currentAction = currentAction,
                onSelectAction = { action -> onSelectAction(gesture, action) },
                onDismissRequest = onDismissActionSelector
            )
        }

        if (uiState.isPrivacyPolicyDialogOpen) {
            PrivacyPolicyDialog(
                onDismissRequest = onDismissPrivacyPolicy
            )
        }
    }
}
