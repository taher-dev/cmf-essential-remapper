package com.monmoy.cmfessentialremapper.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.R
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceElevated
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@Composable
fun ModeIndicatorCard(
    currentMode: RingerMode,
    isChangingMode: Boolean,
    onModeSelected: (RingerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NothingSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(NothingSurfaceBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeItem(
                    label = stringResource(id = R.string.mode_silent),
                    icon = Icons.Filled.NotificationsOff,
                    isActive = currentMode == RingerMode.SILENT,
                    isChanging = isChangingMode,
                    onClick = { onModeSelected(RingerMode.SILENT) },
                    modifier = Modifier.weight(1f)
                )
                ModeItem(
                    label = stringResource(id = R.string.mode_vibrate),
                    icon = Icons.Filled.Vibration,
                    isActive = currentMode == RingerMode.VIBRATE,
                    isChanging = isChangingMode,
                    onClick = { onModeSelected(RingerMode.VIBRATE) },
                    modifier = Modifier.weight(1f)
                )
                ModeItem(
                    label = stringResource(id = R.string.mode_ring),
                    icon = Icons.Filled.Notifications,
                    isActive = currentMode == RingerMode.RING,
                    isChanging = isChangingMode,
                    onClick = { onModeSelected(RingerMode.RING) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isChanging: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) NothingSurfaceElevated else NothingBlack,
        animationSpec = tween(durationMillis = 180),
        label = "modeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) CmfRedAccent else NothingSurfaceBorder,
        animationSpec = tween(durationMillis = 180),
        label = "modeBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (isActive) NothingWhite else NothingGrayMedium,
        animationSpec = tween(durationMillis = 180),
        label = "modeText"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isActive) CmfRedAccent else NothingGrayDark,
        animationSpec = tween(durationMillis = 180),
        label = "modeIcon"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                enabled = !isActive && !isChanging,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = CmfRedAccent),
                onClick = onClick
            )
            .padding(vertical = 20.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isActive) CmfRedAccent else NothingGrayDark)
        )
    }
}
