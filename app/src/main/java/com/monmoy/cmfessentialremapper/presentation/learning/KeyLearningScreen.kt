package com.monmoy.cmfessentialremapper.presentation.learning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

@Composable
fun KeyLearningScreen(
    uiState: KeyLearningUiState,
    onBackClick: () -> Unit,
    onStartDetection: () -> Unit,
    onStopDetection: () -> Unit,
    onSaveKey: () -> Unit,
    onRetry: () -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
                        text = "DYNAMIC KEY LEARNING",
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingWhite
                    )
                    Text(
                        text = "Hardware Event Identifier",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayMedium
                    )
                }
            }

            // Service status banner
            if (!uiState.isServiceRunning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NothingBlack)
                        .border(1.dp, CmfRedAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "NOTICE: Accessibility Service is not running. Please enable CMF Essential Remapper in Accessibility Settings before detecting keys.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NothingGrayLight
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Detection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NothingSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (uiState.isListening) CmfRedAccent else NothingSurfaceBorder
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS // DETECTOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingGrayMedium
                        )

                        if (uiState.isListening) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CmfRedAccent)
                                        .alpha(pulseAlpha)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LISTENING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CmfRedAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isListening) {
                        Icon(
                            imageVector = Icons.Filled.RadioButtonChecked,
                            contentDescription = null,
                            tint = CmfRedAccent,
                            modifier = Modifier
                                .size(56.dp)
                                .alpha(pulseAlpha)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Press your physical Essential Key...",
                            style = MaterialTheme.typography.titleMedium,
                            color = NothingWhite
                        )
                    } else if (uiState.detectedKeyCode != null || uiState.detectedScanCode != null) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = CmfRedAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "KEY IDENTIFIED",
                            style = MaterialTheme.typography.titleMedium,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingBlack)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "KEYCODE", style = MaterialTheme.typography.labelSmall, color = NothingGrayMedium)
                                Text(
                                    text = "${uiState.detectedKeyCode ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = NothingWhite
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(NothingSurfaceBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "SCANCODE", style = MaterialTheme.typography.labelSmall, color = NothingGrayMedium)
                                Text(
                                    text = "${uiState.detectedScanCode ?: 250}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = CmfRedAccent
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Active Saved Identifier",
                            style = MaterialTheme.typography.labelSmall,
                            color = NothingGrayMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingBlack)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "SAVED KEYCODE", style = MaterialTheme.typography.labelSmall, color = NothingGrayMedium)
                                Text(
                                    text = "${uiState.currentSavedKeyCode}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NothingWhite
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(NothingSurfaceBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "SAVED SCANCODE", style = MaterialTheme.typography.labelSmall, color = NothingGrayMedium)
                                Text(
                                    text = "${uiState.currentSavedScanCode}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NothingWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    uiState.message?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NothingGrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action buttons
                    if (uiState.isListening) {
                        Button(
                            onClick = onStopDetection,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NothingBlack),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                        ) {
                            Text(text = "CANCEL LISTENING", color = NothingWhite)
                        }
                    } else if (uiState.detectedKeyCode != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                            ) {
                                Text(text = "RETRY", color = NothingWhite)
                            }
                            Button(
                                onClick = onSaveKey,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "SAVE KEY", color = NothingWhite)
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartDetection,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.isServiceRunning
                        ) {
                            Text(text = "START DETECTION", color = NothingWhite)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset to defaults
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                OutlinedButton(
                    onClick = onResetToDefaults,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                ) {
                    Text(
                        text = "RESET TO REFERENCE DEFAULTS (0 / 250)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

