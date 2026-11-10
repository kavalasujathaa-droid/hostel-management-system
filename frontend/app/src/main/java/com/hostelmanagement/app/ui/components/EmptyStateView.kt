package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.Slate400
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900

@Composable
fun EmptyStateView(
    title: String,
    message: String = "",
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    actionButtonText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null
) {
    val finalMessage = if (message.isNotBlank()) message else (description ?: "")
    val finalActionText = actionButtonText ?: actionLabel
    val finalAction = onActionClick ?: onAction

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Slate400
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing16))
        }

        Text(
            text = title,
            style = AppTypography.titleMedium,
            color = Slate900,
            textAlign = TextAlign.Center
        )

        if (finalMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
            Text(
                text = finalMessage,
                style = AppTypography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }

        if (finalActionText != null && finalAction != null) {
            Spacer(modifier = Modifier.height(AppDimens.Spacing20))
            AppButton(
                text = finalActionText,
                onClick = finalAction,
                variant = ButtonVariant.Secondary
            )
        }
    }
}
