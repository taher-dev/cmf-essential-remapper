package com.monmoy.cmfessentialremapper.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.presentation.components.KeyStatusCard
import com.monmoy.cmfessentialremapper.presentation.components.ModeIndicatorCard
import com.monmoy.cmfessentialremapper.presentation.components.NothingHeader
import com.monmoy.cmfessentialremapper.presentation.components.SettingsDialog
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBackground
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onModeSelected: (RingerMode) -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToKeyLearning: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onErrorMessageDismissed: () -> Unit,
    onSettingsClick: () -> Unit,
    onDismissSettingsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
            NothingHeader(
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Direct Ringer Mode Selector (Silent, Vibrate, Ring)
            ModeIndicatorCard(
                currentMode = uiState.currentRingerMode,
                isChangingMode = uiState.isChangingMode,
                onModeSelected = onModeSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Essential Key Hardware Status Card (Clickable to open Key Learning)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToKeyLearning
                    )
            ) {
                KeyStatusCard(
                    status = uiState.keyStatus
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Setup & Configuration Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = ripple(bounded = true, color = CmfRedAccent),
                        onClick = onNavigateToSetup
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NothingSurfaceBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isSetupComplete) CmfRedAccent else NothingGrayDark)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Setup & Permissions",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NothingWhite
                            )
                            Text(
                                text = if (uiState.isSetupComplete) "All services active" else "Configuration required",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isSetupComplete) NothingGrayLight else CmfRedAccent
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open Setup",
                        tint = NothingGrayLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Diagnostics Row Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = ripple(bounded = true, color = CmfRedAccent),
                        onClick = onNavigateToDiagnostics
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NothingSurfaceBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Key Telemetry & Diagnostics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NothingGrayLight
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Diagnostics",
                        tint = NothingGrayDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.isSettingsDialogOpen) {
            SettingsDialog(
                onDismissRequest = onDismissSettingsDialog
            )
        }
    }
}
