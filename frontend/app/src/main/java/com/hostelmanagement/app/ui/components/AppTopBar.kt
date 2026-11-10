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
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900

import androidx.compose.ui.graphics.vector.ImageVector

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
                        color = Slate900
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = AppTypography.bodyMedium,
                            color = Slate500
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
                            tint = Slate900
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureWhite,
                titleContentColor = Slate900,
                navigationIconContentColor = Slate900,
                actionIconContentColor = Slate900
            )
        )
        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = Slate200)
    }
}
