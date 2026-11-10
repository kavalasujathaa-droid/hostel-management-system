package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate200

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    padding: Dp = AppDimens.Spacing16,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(AppDimens.HairlineBorder, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.Spacing2 * 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            content = content
        )
    }
}
