package com.monmoy.cmfessentialremapper.presentation.setup.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@Composable
fun SetupProgressIndicator(
    progressPercent: Float,
    isDetectionReady: Boolean,
    isRingerToggleReady: Boolean,
    isSinglePressTakeoverReady: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 400),
        label = "setupProgress"
    )

    Card(
        modifier = modifier
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
                Text(
                    text = "SETUP READINESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingGrayMedium
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (progressPercent >= 1.0f) CmfRedAccent else NothingWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CmfRedAccent,
                trackColor = NothingBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Readiness status indicators
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadinessRow(
                    label = "Detection Ready",
                    isReady = isDetectionReady,
                    detail = if (isDetectionReady) "Service active & key mapped" else "Accessibility service or key identification pending"
                )
                ReadinessRow(
                    label = "Ringer Toggle Ready",
                    isReady = isRingerToggleReady,
                    detail = if (isRingerToggleReady) "Notification policy access granted" else "Notification policy access required for Silent mode"
                )
                ReadinessRow(
                    label = "Single-Press Takeover",
                    isReady = isSinglePressTakeoverReady,
                    detail = if (isSinglePressTakeoverReady) "Essential Space suppressed" else "Disable Essential Space to prevent conflict"
                )
            }
        }
    }
}

@Composable
private fun ReadinessRow(
    label: String,
    isReady: Boolean,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isReady) CmfRedAccent else NothingGrayDark)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isReady) NothingWhite else NothingGrayMedium
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = NothingGrayDark
            )
        }
    }
}

