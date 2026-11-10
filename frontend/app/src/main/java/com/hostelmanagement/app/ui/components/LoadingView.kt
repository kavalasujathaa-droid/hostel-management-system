package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900

@Composable
fun LoadingView(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { stateDescription = message },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Slate900,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing12))
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = Slate500
            )
        }
    }
}
