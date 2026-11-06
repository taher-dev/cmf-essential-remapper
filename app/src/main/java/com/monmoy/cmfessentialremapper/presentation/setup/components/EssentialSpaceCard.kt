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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingBlack
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayDark
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceManager
import com.monmoy.cmfessentialremapper.utils.EssentialSpaceStatus
import com.monmoy.cmfessentialremapper.utils.PackageInstallStatus

@Composable
fun EssentialSpaceCard(
    status: EssentialSpaceStatus,
    onDisablePackage: (String) -> Unit,
    onRestorePackage: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onRestoreAll: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizukuSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (status.isTakeoverReady) CmfRedAccent else NothingGrayDark)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Essential Space Control",
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (status.isTakeoverReady) "SUPPRESSED" else "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status.isTakeoverReady) NothingWhite else CmfRedAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The physical Essential Key on Nothing OS launches Essential Space by default. To allow single-press remapping to switch ringer modes without opening Essential Space, the native space package must be disabled.",
                style = MaterialTheme.typography.bodyMedium,
                color = NothingGrayLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Warning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NothingBlack)
                    .border(1.dp, CmfRedAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "WARNING: Disabling Essential Space and Recorder will remove their associated functionality. This change is reversible, but uninstalling CMF Essential Remapper will not automatically restore them.",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingGrayLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Package Live Status
            PackageStatusRow(
                name = "Essential Space",
                pkg = EssentialSpaceManager.PKG_ESSENTIAL_SPACE,
                status = status.spacePackageStatus,
                onDisable = { onDisablePackage(EssentialSpaceManager.PKG_ESSENTIAL_SPACE) },
                onRestore = { onRestorePackage(EssentialSpaceManager.PKG_ESSENTIAL_SPACE) },
                onAppInfo = { onOpenAppInfo(EssentialSpaceManager.PKG_ESSENTIAL_SPACE) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PackageStatusRow(
                name = "Essential Recorder",
                pkg = EssentialSpaceManager.PKG_ESSENTIAL_RECORDER,
                status = status.recorderPackageStatus,
                onDisable = { onDisablePackage(EssentialSpaceManager.PKG_ESSENTIAL_RECORDER) },
                onRestore = { onRestorePackage(EssentialSpaceManager.PKG_ESSENTIAL_RECORDER) },
                onAppInfo = { onOpenAppInfo(EssentialSpaceManager.PKG_ESSENTIAL_RECORDER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shizuku Integration Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NothingBlack)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shizuku Integration",
                            style = MaterialTheme.typography.titleSmall,
                            color = NothingWhite
                        )
                        val shizukuBadge = when {
                            !status.isShizukuInstalled -> "NOT INSTALLED"
                            !status.isShizukuRunning -> "NOT RUNNING"
                            status.isShizukuPermissionGranted -> "AUTHORIZED"
                            else -> "NEEDS PERMISSION"
                        }
                        Text(
                            text = shizukuBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status.isShizukuPermissionGranted) NothingWhite else NothingGrayMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (status.isShizukuPermissionGranted) {
                            "Shizuku service active & authorized. Essential Space packages can be disabled and restored automatically."
                        } else if (status.isShizukuRunning) {
                            "Shizuku service detected but needs authorization to manage packages."
                        } else {
                            "Shizuku bridge is not running. Setup Shizuku via Wireless Debugging to disable Essential Space without root."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenShizukuSetup,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!status.isShizukuRunning || !status.isShizukuPermissionGranted) CmfRedAccent else NothingBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = if (status.isShizukuRunning && status.isShizukuPermissionGranted) {
                                androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                            } else null
                        ) {
                            Text(
                                text = if (!status.isShizukuInstalled || !status.isShizukuRunning) {
                                    "SETUP SHIZUKU"
                                } else {
                                    "SHIZUKU GUIDE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = NothingWhite
                            )
                        }

                        if (status.isShizukuRunning && !status.isShizukuPermissionGranted) {
                            OutlinedButton(
                                onClick = onRequestShizukuPermission,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
                            ) {
                                Text(
                                    text = "AUTHORIZE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NothingWhite
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore All Button
            OutlinedButton(
                onClick = onRestoreAll,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder)
            ) {
                Text(
                    text = "RESTORE ALL PACKAGES",
                    style = MaterialTheme.typography.titleSmall,
                    color = NothingWhite
                )
            }
        }
    }
}

@Composable
private fun PackageStatusRow(
    name: String,
    pkg: String,
    status: PackageInstallStatus,
    onDisable: () -> Unit,
    onRestore: () -> Unit,
    onAppInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NothingBlack)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, style = MaterialTheme.typography.titleSmall, color = NothingWhite)
                Text(text = pkg, style = MaterialTheme.typography.labelSmall, color = NothingGrayDark)
            }
            Text(
                text = status.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = when (status) {
                    PackageInstallStatus.DISABLED -> NothingWhite
                    PackageInstallStatus.ENABLED -> CmfRedAccent
                    PackageInstallStatus.NOT_INSTALLED -> NothingGrayDark
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status == PackageInstallStatus.ENABLED) {
                Button(
                    onClick = onDisable,
                    colors = ButtonDefaults.buttonColors(containerColor = CmfRedAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "DISABLE", style = MaterialTheme.typography.labelSmall)
                }
            } else if (status == PackageInstallStatus.DISABLED) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(containerColor = NothingSurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "RESTORE", style = MaterialTheme.typography.labelSmall, color = NothingWhite)
                }
            }

            OutlinedButton(
                onClick = onAppInfo,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NothingSurfaceBorder),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "APP INFO", style = MaterialTheme.typography.labelSmall, color = NothingGrayLight)
            }
        }
    }
}

