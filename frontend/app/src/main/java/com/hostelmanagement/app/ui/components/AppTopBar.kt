package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navAction = onNavigateBack ?: onNavigationClick
    val icon = navigationIcon ?: Icons.AutoMirrored.Filled.ArrowBack

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = AppTypography.titleLarge,
                        color = TextPrimary
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = AppTypography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            },
            navigationIcon = {
                if (navAction != null) {
                    IconButton(onClick = navAction) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Navigate back",
                            tint = TextPrimary
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureWhite,
                titleContentColor = TextPrimary,
                navigationIconContentColor = TextPrimary,
                actionIconContentColor = TextPrimary
            )
        )
        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle)
    }
}
