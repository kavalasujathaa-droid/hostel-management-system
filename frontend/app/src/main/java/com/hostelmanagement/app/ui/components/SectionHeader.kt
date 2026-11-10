package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.TextPrimary

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionText,
                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = BrandPrimary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}
