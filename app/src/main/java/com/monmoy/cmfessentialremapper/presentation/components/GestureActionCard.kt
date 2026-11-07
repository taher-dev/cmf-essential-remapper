package com.monmoy.cmfessentialremapper.presentation.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.monmoy.cmfessentialremapper.domain.model.KeyAction
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.utils.KeyGesture

/**
 * Card group matching the exact font hierarchy, visual indicator rhythm, and spacing
 * of the Setup & Permissions card for a consistent Nothing OS UI.
 */
@Composable
fun GestureActionCard(
    singlePressAction: KeyAction,
    doublePressAction: KeyAction,
    longPressAction: KeyAction,
    onGestureClick: (KeyGesture) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Button Mappings",
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
            Column(modifier = Modifier.fillMaxWidth()) {
                GestureRowItem(
                    title = "Single Press",
                    action = singlePressAction,
                    onClick = { onGestureClick(KeyGesture.SINGLE_PRESS) }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingSurfaceBorder
                )

                GestureRowItem(
                    title = "Double Press",
                    action = doublePressAction,
                    onClick = { onGestureClick(KeyGesture.DOUBLE_PRESS) }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingSurfaceBorder
                )

                GestureRowItem(
                    title = "Long Press",
                    action = longPressAction,
                    onClick = { onGestureClick(KeyGesture.LONG_PRESS) }
                )
            }
        }
    }
}

@Composable
private fun GestureRowItem(
    title: String,
    action: KeyAction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = CmfRedAccent),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (action != KeyAction.NONE) CmfRedAccent else NothingGrayMedium)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NothingWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (action != KeyAction.NONE) action.title else "No action assigned",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (action != KeyAction.NONE) CmfRedAccent else NothingGrayLight
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Edit Action",
            tint = NothingGrayLight,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dialog for selecting a functional action for a chosen gesture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSelectionDialog(
    gesture: KeyGesture,
    currentAction: KeyAction,
    onSelectAction: (KeyAction) -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(NothingSurface)
                .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = gesture.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NothingWhite
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CmfRedAccent)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Assign function",
                style = MaterialTheme.typography.labelSmall,
                color = NothingGrayLight
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                items(KeyAction.entries.toTypedArray()) { action ->
                    val isSelected = action == currentAction
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) NothingGrayDark else NothingSurface)
                            .border(
                                1.dp,
                                if (isSelected) CmfRedAccent else NothingSurfaceBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = CmfRedAccent),
                                onClick = { onSelectAction(action) }
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CmfRedAccent,
                                unselectedColor = NothingGrayMedium
                            ),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = action.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = NothingGrayLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NothingSurfaceBorder)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NothingWhite
                    )
                }
            }
        }
    }
}
