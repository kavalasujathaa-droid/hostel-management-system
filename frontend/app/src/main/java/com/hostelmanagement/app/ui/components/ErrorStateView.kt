package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.core.theme.StatusOverdueText

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Unable to load data"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = StatusOverdueText
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing16))

        Text(
            text = title,
            style = AppTypography.titleMedium,
            color = Slate900,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing8))

        Text(
            text = message,
            style = AppTypography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing20))

        AppButton(
            text = "Retry",
            onClick = onRetry,
            variant = ButtonVariant.Secondary
        )
    }
}
