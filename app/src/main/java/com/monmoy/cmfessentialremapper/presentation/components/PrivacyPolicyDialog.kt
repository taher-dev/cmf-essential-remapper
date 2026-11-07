package com.monmoy.cmfessentialremapper.presentation.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayLight
import com.monmoy.cmfessentialremapper.presentation.theme.NothingGrayMedium
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

/**
 * Open Source Privacy Policy Dialog detailing zero telemetry, offline execution,
 * and scoped Accessibility Service usage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(NothingSurface)
                .border(1.dp, NothingSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIVACY POLICY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = NothingWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Open Source Commitment & Transparency",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingGrayMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CmfRedAccent)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Content (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                PolicySection(
                    title = "1. Zero Internet Access (100% Offline)",
                    description = "CMF Essential Remapper does not request or possess the 'android.permission.INTERNET' permission in its manifest. The application cannot connect to the internet, make network requests, or transmit any data off your device under any circumstances."
                )

                PolicySection(
                    title = "2. No Data Collection or Telemetry",
                    description = "We do not collect, store, track, or share any personal information, device identifiers, usage analytics, crash reports, or behavioral metrics. No third-party SDKs, ad networks, or tracking libraries are bundled in this app."
                )

                PolicySection(
                    title = "3. Scoped Accessibility Service",
                    description = "The Accessibility Service operates strictly with 'canRetrieveWindowContent=\"false\"'. It cannot view, inspect, or log screen contents, text fields, keystrokes, passwords, or personal notifications. It is configured solely with 'flagRequestFilterKeyEvents' to intercept and remap the physical CMF Essential Key button."
                )

                PolicySection(
                    title = "4. Local Preferences Storage",
                    description = "All user configurations, gesture assignments, and learned key scan codes are saved strictly on your local device via Android Jetpack DataStore. Your preferences never leave your device."
                )

                PolicySection(
                    title = "5. Free & Open Source Software",
                    description = "This application is free and open-source software licensed under the Apache License, Version 2.0. The complete source code is publicly accessible and auditable by anyone in the open source community."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
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
                        text = "DISMISS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = NothingWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = NothingWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = NothingGrayLight,
            lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 1.25f
        )
    }
}
