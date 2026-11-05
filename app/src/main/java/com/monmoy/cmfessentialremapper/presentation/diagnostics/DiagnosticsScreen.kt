package com.monmoy.cmfessentialremapper.presentation.diagnostics

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBackground
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager

@Composable
fun DiagnosticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning by EssentialKeyAccessibilityService.isServiceRunning.collectAsState()
    val lastGesture by EssentialKeyAccessibilityService.lastRecognizedGesture.collectAsState()
    val lastAction by EssentialKeyAccessibilityService.lastActionResult.collectAsState()
    val savedKeyCode by EssentialKeyAccessibilityService.savedKeyCodeFlow.collectAsState()
    val savedScanCode by EssentialKeyAccessibilityService.savedScanCodeFlow.collectAsState()
    val lastRawKey by EssentialKeyAccessibilityService.lastRawKeyEvent.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NothingBackground
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
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "LIVE DIAGNOSTICS",
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingWhite
                    )
                    Text(
                        text = "Hardware Event Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayMedium
                    )
                }
            }

            // Diagnostics Cards
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DiagnosticRow(
                        label = "Accessibility Service",
                        value = if (isRunning) "CONNECTED / ACTIVE" else "DISCONNECTED",
                        highlight = isRunning
                    )
                    DiagnosticRow(
                        label = "Target Key Identifier",
                        value = "KeyCode: $savedKeyCode / ScanCode: $savedScanCode",
                        highlight = true
                    )
                    DiagnosticRow(
                        label = "Last Recognized Gesture",
                        value = lastGesture?.title ?: "None (Awaiting event)",
                        highlight = lastGesture != null
                    )
                    DiagnosticRow(
                        label = "Last Action Execution",
                        value = lastAction ?: "Idle",
                        highlight = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Raw Hardware Event Card
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
                    Text(
                        text = "LAST RAW HARDWARE EVENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (lastRawKey != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingBlack)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Key Code: ${lastRawKey?.keyCode}", style = MaterialTheme.typography.bodyMedium, color = NothingWhite)
                            Text(text = "Scan Code: ${lastRawKey?.scanCode}", style = MaterialTheme.typography.bodyMedium, color = CmfRedAccent)
                            Text(text = "Action: ${if (lastRawKey?.action == 0) "ACTION_DOWN" else "ACTION_UP"}", style = MaterialTheme.typography.bodyMedium, color = NothingGrayLight)
                            Text(text = "Repeat Count: ${lastRawKey?.repeatCount}", style = MaterialTheme.typography.bodyMedium, color = NothingGrayLight)
                            Text(text = "Device ID: ${lastRawKey?.deviceId}", style = MaterialTheme.typography.bodyMedium, color = NothingGrayDark)
                            Text(text = "Event Time: ${lastRawKey?.eventTime} ms", style = MaterialTheme.typography.bodyMedium, color = NothingGrayDark)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingBlack)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "No hardware key events captured yet. Press the physical Essential Key on your CMF Phone 2 Pro.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NothingGrayLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    highlight: Boolean
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = NothingGrayMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = if (highlight) NothingWhite else NothingGrayLight
        )
    }
}

