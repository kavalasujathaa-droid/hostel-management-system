package com.hostelmanagement.app.ui.owner.vacate

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun OwnerVacateScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerVacateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedRequestToReject by remember { mutableStateOf<VacateRequestResponseDto?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var selectedRequestToApprove by remember { mutableStateOf<VacateRequestResponseDto?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Vacate Notices",
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
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing8),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)
            ) {
                item {
                    val isSelected = uiState.selectedStatusFilter == "PENDING"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("PENDING") },
                        label = { Text("Pending", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                item {
                    val isSelected = uiState.selectedStatusFilter == "APPROVED"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("APPROVED") },
                        label = { Text("Approved", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                item {
                    val isSelected = uiState.selectedStatusFilter == "REJECTED"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("REJECTED") },
                        label = { Text("Rejected", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                item {
                    val isSelected = uiState.selectedStatusFilter == null
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            if (uiState.isLoading && uiState.requests.isEmpty()) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading vacate requests..."
                )
            } else if (uiState.requests.isEmpty()) {
                EmptyStateView(
                    title = "No Vacate Notices",
                    description = if (uiState.selectedStatusFilter != null) {
                        "No vacate requests matching status: ${uiState.selectedStatusFilter}"
                    } else {
                        "No move-out notices recorded."
                    },
                    actionLabel = "Refresh",
                    onAction = { viewModel.loadVacateRequests() },
                    icon = Icons.Outlined.CalendarToday,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimens.Spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                    }

                    items(uiState.requests, key = { it.id }) { request ->
                        VacateItemCard(
                            request = request,
                            isReviewing = uiState.isReviewing,
                            onApprove = { selectedRequestToApprove = request },
                            onReject = {
                                selectedRequestToReject = request
                                rejectionReasonText = ""
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                    }
                }
            }
        }
    }

    // Approve Confirmation Dialog
    selectedRequestToApprove?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestToApprove = null },
            title = {
                Text(
                    text = "Approve Move-Out Notice",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to approve the vacate notice for ${req.tenant_name ?: "resident"} for vacating date ${DateFormatter.formatDate(req.requested_vacating_date)}? This will schedule the completion of their room stay.",
                    style = AppTypography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                AppButton(
                    text = "Approve Move-Out",
                    onClick = {
                        viewModel.approveRequest(req.id)
                        selectedRequestToApprove = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestToApprove = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }

    // Reject Reason Dialog
    selectedRequestToReject?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestToReject = null },
            title = {
                Text(
                    text = "Reject Move-Out Notice",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Provide a reason for rejecting this vacate request (e.g. minimum notice period not fulfilled):",
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AppDimens.Spacing10))
                    AppTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = "Rejection Reason",
                        placeholder = "e.g. Notice period minimum not met",
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "Confirm Rejection",
                    variant = ButtonVariant.Destructive,
                    onClick = {
                        viewModel.rejectRequest(req.id, rejectionReasonText)
                        selectedRequestToReject = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestToReject = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}

@Composable
private fun VacateItemCard(
    request: VacateRequestResponseDto,
    isReviewing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing16
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Vacate request by ${request.tenant_name ?: "resident"} in room ${request.room_number ?: "-"}, requested date ${DateFormatter.formatDate(request.requested_vacating_date)}, status ${request.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.tenant_name ?: "Resident",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing6))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Room ${request.room_number ?: "-"}",
                    style = AppTypography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Vacating Date: ${DateFormatter.formatDate(request.requested_vacating_date)}",
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing6))

            Text(
                text = "Submitted ${DateFormatter.formatDateTime(request.requested_at)}",
                style = AppTypography.labelSmall,
                color = TextSecondary
            )

            if (!request.rejection_reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppDimens.Spacing6))
                Text(
                    text = "Reason: ${request.rejection_reason}",
                    style = AppTypography.bodySmall,
                    color = StatusOverdueText
                )
            }

            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(AppDimens.Spacing16))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                ) {
                    AppButton(
                        text = "Approve",
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        enabled = !isReviewing,
                        icon = Icons.Default.Check
                    )
                    AppButton(
                        text = "Reject",
                        variant = ButtonVariant.Secondary,
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        enabled = !isReviewing,
                        icon = Icons.Default.Close
                    )
                }
            }
        }
    }
}
