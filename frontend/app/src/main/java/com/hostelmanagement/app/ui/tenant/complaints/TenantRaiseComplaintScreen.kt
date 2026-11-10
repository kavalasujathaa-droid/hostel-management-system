package com.hostelmanagement.app.ui.tenant.complaints

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.SectionHeader

@Composable
fun TenantRaiseComplaintScreen(
    onNavigateBack: () -> Unit,
    onComplaintSubmitted: () -> Unit,
    viewModel: TenantRaiseComplaintViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onImageSelected(uri, context.contentResolver)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onComplaintSubmitted()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Raise Complaint",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing16)
        ) {
            // Section 1: Issue Category Selection
            SectionHeader(title = "Select Issue Category")
            Text(
                text = "Choose the category that best describes your maintenance or facility request:",
                style = AppTypography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing10))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing8),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(COMPLAINT_CATEGORIES) { category ->
                    val isSelected = uiState.category == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategoryChanged(category) },
                        label = {
                            Text(
                                text = category,
                                style = AppTypography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = PureWhite,
                            containerColor = PureWhite,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) BrandPrimary else BorderSubtle
                        ),
                        shape = AppShapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing20))

            // Section 2: Complaint Details Card
            AppCard(padding = AppDimens.Spacing16) {
                AppTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChanged,
                    label = "Complaint Title",
                    placeholder = "e.g. Geyser not heating water in bathroom",
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${uiState.title.length}/150",
                        style = AppTypography.labelSmall,
                        color = if (uiState.title.length > 150) StatusOverdueText else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                AppTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = "Detailed Description",
                    placeholder = "Please explain the problem with specific details (location, timing, symptoms)...",
                    singleLine = false,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing20))

            // Section 3: Photo Attachment Card
            SectionHeader(title = "Attach Photo (Optional)")
            Text(
                text = "Photos help the maintenance team diagnose and fix the issue faster (PNG, JPG up to 5MB):",
                style = AppTypography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing10))

            if (uiState.selectedImageUri == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.medium)
                        .background(PureWhite)
                        .border(1.dp, BorderSubtle, AppShapes.medium)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(vertical = AppDimens.Spacing24, horizontal = AppDimens.Spacing16),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(BrandContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "Upload Photo",
                                tint = BrandPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppDimens.Spacing10))
                        Text(
                            text = "Tap to choose a photo",
                            style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandPrimary
                        )
                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                        Text(
                            text = "JPEG, PNG (Max 5 MB)",
                            style = AppTypography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                AppCard(padding = AppDimens.Spacing12) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Complaint photo preview",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(AppShapes.small)
                                .border(1.dp, BorderSubtle, AppShapes.small),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedImageName ?: "Selected image",
                                style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                            Text(
                                text = "Photo attached to ticket",
                                style = AppTypography.bodySmall,
                                color = BrandPrimary
                            )
                        }
                        IconButton(onClick = viewModel::onRemoveImage) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove photo",
                                tint = StatusOverdueText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing28))

            // Section 4: Submit Button
            val canSubmit = uiState.title.trim().length >= 3 &&
                    uiState.title.trim().length <= 150 &&
                    uiState.description.trim().length >= 5 &&
                    !uiState.isSubmitting

            AppButton(
                text = "Submit Complaint Ticket",
                onClick = viewModel::submitComplaint,
                enabled = canSubmit,
                isLoading = uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.Send
            )

            Spacer(modifier = Modifier.height(AppDimens.Spacing32))
        }
    }
}
