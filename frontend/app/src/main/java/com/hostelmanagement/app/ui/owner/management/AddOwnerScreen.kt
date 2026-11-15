package com.hostelmanagement.app.ui.owner.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.StatusOverdueBg
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar

@Composable
fun AddOwnerScreen(
    onNavigateBack: () -> Unit,
    onOwnerCreated: () -> Unit,
    viewModel: AddOwnerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onOwnerCreated()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add New Owner",
                subtitle = "Provision a co-owner account",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
        ) {
            // Explanatory Banner Card
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = AppDimens.Spacing14,
                    containerColor = BrandContainer,
                    borderColor = BorderSubtle
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SurfaceNeutral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Co-Owner Permissions",
                                style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "The new owner will have full equal access to manage rooms, tenants, billing, payments, and settings for this hostel.",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Input Form Card
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = AppDimens.Spacing20
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing14)
                    ) {
                        // Full Name
                        AppTextField(
                            value = uiState.fullName,
                            onValueChange = viewModel::onFullNameChange,
                            label = "Full Name *",
                            placeholder = "e.g. Priya Sharma",
                            leadingIcon = Icons.Default.Person,
                            isError = uiState.fullNameError != null,
                            errorMessage = uiState.fullNameError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Email Address
                        AppTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = "Email Address *",
                            placeholder = "owner@hostel.com",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = uiState.emailError != null,
                            errorMessage = uiState.emailError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone Number (Optional)
                        AppTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            label = "Phone Number (Optional)",
                            placeholder = "e.g. 9876543210",
                            leadingIcon = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password (Min 8 Chars)
                        AppTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = "Password * (Min 8 characters)",
                            placeholder = "Enter secure password",
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            onTrailingIconClick = viewModel::togglePasswordVisibility,
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = uiState.passwordError != null,
                            errorMessage = uiState.passwordError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm Password
                        AppTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = "Confirm Password *",
                            placeholder = "Re-enter password",
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = if (uiState.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            onTrailingIconClick = viewModel::toggleConfirmPasswordVisibility,
                            visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = uiState.confirmPasswordError != null,
                            errorMessage = uiState.confirmPasswordError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(com.hostelmanagement.app.core.theme.AppShapes.small)
                                    .background(StatusOverdueBg)
                                    .padding(AppDimens.Spacing10)
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = StatusOverdueText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimens.Spacing8))

                        // Submit Button
                        AppButton(
                            text = if (uiState.isLoading) "Creating Owner Account..." else "Create Owner Account",
                            onClick = viewModel::createOwner,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
