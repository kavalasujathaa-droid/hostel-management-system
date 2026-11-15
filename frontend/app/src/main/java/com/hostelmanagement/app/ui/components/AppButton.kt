package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.PrimaryPurple
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.PurpleGradient
import com.hostelmanagement.app.core.theme.StatusOverdueBg
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary

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
            val bgModifier = if (isInteractive) {
                Modifier.background(PurpleGradient, AppShapes.medium)
            } else {
                Modifier.background(Color(0xFFD6D0F5), AppShapes.medium)
            }

            Button(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.medium,
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .then(bgModifier)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = PureWhite,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = PureWhite
                )
            ) {
                ButtonContent(text = text, isLoading = isLoading, contentColor = PureWhite, icon = icon)
            }
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.medium,
                border = BorderStroke(AppDimens.HairlineBorder, BorderSubtle),
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PureWhite,
                    contentColor = TextPrimary,
                    disabledContentColor = TextMuted
                )
            ) {
                ButtonContent(text = text, isLoading = isLoading, contentColor = TextPrimary, icon = icon)
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                enabled = isInteractive,
                shape = AppShapes.medium,
                modifier = modifier
                    .height(AppDimens.ButtonHeight)
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusOverdueBg,
                    contentColor = StatusOverdueText,
                    disabledContainerColor = BorderSubtle,
                    disabledContentColor = TextMuted
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
    icon: ImageVector?
) {
    Box(contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
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
                    style = AppTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}
