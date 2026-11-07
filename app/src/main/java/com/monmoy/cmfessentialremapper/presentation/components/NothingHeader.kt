package com.monmoy.cmfessentialremapper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monmoy.cmfessentialremapper.R
import com.monmoy.cmfessentialremapper.presentation.theme.CmfRedAccent
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurface
import com.monmoy.cmfessentialremapper.presentation.theme.NothingSurfaceBorder
import com.monmoy.cmfessentialremapper.presentation.theme.NothingWhite

@Composable
fun NothingHeader(
    modifier: Modifier = Modifier,
    onDeveloperClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(CmfRedAccent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                letterSpacing = (-0.3).sp
            ),
            color = NothingWhite,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (onDeveloperClick != null) {
            IconButton(
                onClick = onDeveloperClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NothingSurface)
                    .border(1.dp, NothingSurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Developer Info",
                    tint = NothingWhite,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

