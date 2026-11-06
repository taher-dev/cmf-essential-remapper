package com.monmoy.cmfessentialremapper.presentation.setup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuSetupDialog(
    status: EssentialSpaceStatus,
    onDismissRequest: () -> Unit,
    onLaunchShizuku: () -> Unit,
    onInstallShizuku: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onCopyAdbCommand: (String) -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NothingSurface)
                .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PACKAGE SUPPRESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CmfRedAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Setup Shizuku",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NothingWhite
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NothingBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NothingWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NothingBlack)
                    .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CmfRedAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nothing OS prevents disabling Essential Space via Android App Info. Shizuku uses standard Android ADB / Wireless Debugging permissions to disable it cleanly without root.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NothingGrayLight,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Status Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NothingBlack)
                    .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shizuku Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NothingWhite
                )

                val (badgeText, badgeColor) = when {
                    !status.isShizukuInstalled -> "NOT INSTALLED" to NothingGrayMedium
                    !status.isShizukuRunning -> "INSTALLED (STOPPED)" to CmfRedAccent
                    status.isShizukuPermissionGranted -> "READY & AUTHORIZED" to NothingWhite
                    else -> "NEEDS PERMISSION" to NothingGrayLight
                }

                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Install or Open
            SetupStepItem(
                stepNumber = "1",
                title = if (!status.isShizukuInstalled) "Install Shizuku" else "Open Shizuku App",
                description = if (!status.isShizukuInstalled) {
                    "Install the free Shizuku app from Google Play Store."
                } else {
                    "Launch the installed Shizuku manager on your phone."
                },
                actionButton = {
                    if (!status.isShizukuInstalled) {
                        Button(
                            onClick = onInstallShizuku,
                            colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INSTALL VIA PLAY STORE", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onLaunchShizuku,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                        ) {
                            Text("OPEN SHIZUKU", style = MaterialTheme.typography.labelSmall, color = NothingWhite)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 2: Wireless Debugging
            SetupStepItem(
                stepNumber = "2",
                title = "Start via Wireless Debugging",
                description = "In Developer Options, turn on Wireless Debugging. In Shizuku, tap 'Pairing' and enter the pairing code, then tap 'Start'.",
                actionButton = {
                    OutlinedButton(
                        onClick = onOpenDeveloperOptions,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                    ) {
                        Text("DEVELOPER OPTIONS", style = MaterialTheme.typography.labelSmall, color = NothingWhite)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 3: Authorize
            SetupStepItem(
                stepNumber = "3",
                title = "Authorize CMF Remapper",
                description = "Grant Shizuku permission to allow CMF Essential Remapper to manage packages.",
                actionButton = {
                    Button(
                        onClick = onRequestShizukuPermission,
                        enabled = status.isShizukuRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status.isShizukuPermissionGranted) NothingSurfaceBorder else CmfRedAccent,
                            disabledContainerColor = NothingGrayDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (status.isShizukuPermissionGranted) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = NothingWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUTHORIZED", style = MaterialTheme.typography.labelSmall, color = NothingWhite)
                        } else {
                            Text("AUTHORIZE NOW", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Alternative: PC ADB Command
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NothingBlack)
                    .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alternative: PC ADB Command",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingWhite
                        )

                        IconButton(
                            onClick = {
                                onCopyAdbCommand("adb shell pm disable-user --user 0 com.nothing.ntessentialspace")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Command",
                                tint = NothingGrayLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "If connected to a computer, you can run this command directly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = NothingGrayLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingSurface)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .clickable {
                                onCopyAdbCommand("adb shell pm disable-user --user 0 com.nothing.ntessentialspace")
                            }
                    ) {
                        Text(
                            text = "adb shell pm disable-user --user 0 com.nothing.ntessentialspace",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = CmfRedAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NothingBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
            ) {
                Text(text = "DONE", style = MaterialTheme.typography.titleSmall, color = NothingWhite)
            }
        }
    }
}

@Composable
private fun SetupStepItem(
    stepNumber: String,
    title: String,
    description: String,
    actionButton: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NothingBlack)
            .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(NothingSurface)
                        .border(1.dp, NothingSurfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingWhite
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = NothingWhite
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = NothingGrayLight,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            actionButton()
        }
    }
}
