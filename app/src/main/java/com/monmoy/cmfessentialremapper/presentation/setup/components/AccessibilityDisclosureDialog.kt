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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityDisclosureDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "ACCESSIBILITY // DISCLOSURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingGrayMedium
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CmfRedAccent)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hardware Key Detection",
                style = MaterialTheme.typography.headlineMedium,
                color = NothingWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "CMF Essential Remapper uses Android Accessibility Services exclusively to detect your physical Essential Key presses. It processes hardware key events locally and does not read screen content, passwords, text fields, or personal information.",
                style = MaterialTheme.typography.bodyLarge,
                color = NothingGrayLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingGrayLight
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CmfRedAccent,
                        contentColor = NothingWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CONTINUE",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

