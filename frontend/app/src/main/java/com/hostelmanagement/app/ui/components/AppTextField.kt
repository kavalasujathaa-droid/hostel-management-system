package com.hostelmanagement.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate300
import com.hostelmanagement.app.core.theme.Slate400
import com.hostelmanagement.app.core.theme.Slate700
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.core.theme.StatusOverdueText

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = AppTypography.titleMedium,
            color = Slate900
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing4))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.small,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = AppTypography.bodyMedium,
                        color = Slate400
                    )
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            textStyle = AppTypography.bodyLarge.copy(color = Slate900),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite,
                disabledContainerColor = Slate200,
                focusedBorderColor = Slate900,
                unfocusedBorderColor = Slate300,
                errorBorderColor = StatusOverdueText,
                errorContainerColor = PureWhite
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppDimens.Spacing2))
            Text(
                text = errorMessage,
                style = AppTypography.bodyMedium,
                color = StatusOverdueText
            )
        }
    }
}
