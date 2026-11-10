package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate400
import com.hostelmanagement.app.core.theme.Slate600
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.core.theme.StatusOverdueText

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

enum class ButtonVariant {
    Primary,
    Secondary,
    Destructive;

    companion object {
        val Danger get() = Destructive
    }
}

typealias AppButtonVariant = ButtonVariant

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: ImageVector? = null
) {
    val isInteractive = enabled && !isLoading

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.small,
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate900,
                    contentColor = PureWhite,
                    disabledContainerColor = Slate200,
                    disabledContentColor = Slate400
                )
            ) {
                ButtonContent(text = text, isLoading = isLoading, contentColor = PureWhite, icon = icon)
            }
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.small,
                border = BorderStroke(AppDimens.HairlineBorder, if (isInteractive) Slate200 else Slate200),
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Slate900,
                    disabledContentColor = Slate400
                )
            ) {
                ButtonContent(text = text, isLoading = isLoading, contentColor = Slate900, icon = icon)
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.small,
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = StatusOverdueText,
                    disabledContainerColor = Slate200,
                    disabledContentColor = Slate400
                )
            ) {
                ButtonContent(text = text, isLoading = isLoading, contentColor = StatusOverdueText, icon = icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    contentColor: Color,
    icon: ImageVector? = null
) {
    Box(contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing8))
                }
                Text(
                    text = text,
                    style = AppTypography.labelLarge,
                    color = contentColor
                )
            }
        }
    }
}

