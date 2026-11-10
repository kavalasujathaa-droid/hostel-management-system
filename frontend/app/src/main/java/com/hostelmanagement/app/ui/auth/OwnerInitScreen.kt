package com.hostelmanagement.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar

@Composable
fun OwnerInitScreen(
    onNavigateBack: () -> Unit,
    onInitSuccess: () -> Unit,
    viewModel: OwnerInitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onInitSuccess()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Hostel Setup",
                subtitle = "Initial owner account and settings",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing16)
        ) {
            AppCard(padding = AppDimens.Spacing20) {
                Text(
                    text = "Welcome to SmartTrack",
                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                Text(
                    text = "Set up your hostel profile to start managing rooms, tenants, and collections.",
                    style = AppTypography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing20))

                AppTextField(
                    value = state.hostelName,
                    onValueChange = viewModel::onHostelNameChange,
                    label = "Hostel Name",
                    placeholder = "e.g., Sunshine Boys Hostel"
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = "Owner Full Name",
                    placeholder = "e.g., Ramesh Kumar"
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Owner Email",
                    placeholder = "owner@example.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    placeholder = "Minimum 8 characters",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Contact Phone (Optional)",
                    placeholder = "+91 9876543210",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.address,
                    onValueChange = viewModel::onAddressChange,
                    label = "Hostel Address (Optional)",
                    placeholder = "Street, City, Pin Code"
                )

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = state.rentDueDay,
                    onValueChange = viewModel::onRentDueDayChange,
                    label = "Rent Due Day of Month",
                    placeholder = "1 to 28 (Default: 5)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(AppDimens.Spacing16))
                    Text(
                        text = state.errorMessage ?: "",
                        style = AppTypography.bodySmall,
                        color = StatusOverdueText
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.Spacing24))

                AppButton(
                    text = "Complete Hostel Setup",
                    onClick = viewModel::submit,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
