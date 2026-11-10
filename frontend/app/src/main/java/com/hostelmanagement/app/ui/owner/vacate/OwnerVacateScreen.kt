package com.hostelmanagement.app.ui.owner.vacate

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "PENDING",
                        onClick = { viewModel.setStatusFilter("PENDING") },
                        label = { Text("Pending") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "APPROVED",
                        onClick = { viewModel.setStatusFilter("APPROVED") },
                        label = { Text("Approved") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "REJECTED",
                        onClick = { viewModel.setStatusFilter("REJECTED") },
                        label = { Text("Rejected") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
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
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
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
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Approve Confirmation Dialog
    selectedRequestToApprove?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestToApprove = null },
            title = { Text("Approve Move-Out Notice") },
            text = {
                Text(
                    "Are you sure you want to approve the vacate request for ${req.tenant_name ?: "tenant"} with vacating date ${DateFormatter.formatDate(req.requested_vacating_date)}? This will schedule the end date of their stay."
                )
            },
            confirmButton = {
                AppButton(
                    text = "Approve",
                    onClick = {
                        viewModel.approveRequest(req.id)
                        selectedRequestToApprove = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestToApprove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Reason Dialog
    selectedRequestToReject?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestToReject = null },
            title = { Text("Reject Move-Out Notice") },
            text = {
                Column {
                    Text("Provide a reason for rejecting this vacate request (optional):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Notice period minimum not met") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "Reject Request",
                    variant = ButtonVariant.Destructive,
                    onClick = {
                        viewModel.rejectRequest(req.id, rejectionReasonText)
                        selectedRequestToReject = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestToReject = null }) {
                    Text("Cancel")
                }
            }
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Vacate request by ${request.tenant_name ?: "tenant"} in room ${request.room_number ?: "-"}, requested date ${DateFormatter.formatDate(request.requested_vacating_date)}, status ${request.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.tenant_name ?: "Tenant",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(text = request.status, status = request.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Slate500
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Room ${request.room_number ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Slate500
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Requested Date: ${DateFormatter.formatDate(request.requested_vacating_date)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Submitted ${DateFormatter.formatDateTime(request.requested_at)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )

            if (!request.rejection_reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason: ${request.rejection_reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
