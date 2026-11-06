package com.monmoy.cmfessentialremapper.presentation.setup.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun StepGuideCard(
    stepNumber: String,
    title: String,
    description: String,
    statusText: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NothingSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCompleted) NothingSurfaceBorder else NothingSurfaceBorder
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Step Number + Title + Live Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Step Number pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCompleted) NothingBlack else CmfRedAccent.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (isCompleted) NothingSurfaceBorder else CmfRedAccent.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stepNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) NothingWhite else CmfRedAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Live Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(
                            1.dp,
                            if (isCompleted) SuccessGreen.copy(alpha = 0.5f) else NothingSurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CmfRedAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = statusText.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) SuccessGreen else NothingGrayLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minimal Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = NothingGrayLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            actions()
        }
    }
}
