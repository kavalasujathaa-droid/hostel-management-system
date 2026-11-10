package com.hostelmanagement.app.ui.owner.complaints

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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun OwnerComplaintsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerComplaintsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = "Complaints & Issues",
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
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "OPEN",
                        onClick = { viewModel.setStatusFilter("OPEN") },
                        label = { Text("Open") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "IN PROGRESS",
                        onClick = { viewModel.setStatusFilter("IN PROGRESS") },
                        label = { Text("In Progress") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (uiState.isLoading && uiState.complaints.isEmpty()) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading complaints..."
                )
            } else if (uiState.complaints.isEmpty()) {
                EmptyStateView(
                    title = "No Complaints Found",
                    description = if (uiState.selectedStatusFilter != null) {
                        "No complaints matching status: ${uiState.selectedStatusFilter}"
                    } else {
                        "All complaints have been managed or none reported."
                    },
                    actionLabel = "Refresh",
                    onAction = { viewModel.loadComplaints() },
                    icon = Icons.Default.ReportProblem,
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

                    items(uiState.complaints, key = { it.id }) { complaint ->
                        ComplaintItemCard(
                            complaint = complaint,
                            isUpdating = uiState.isUpdating,
                            onUpdateStatus = { newStatus ->
                                viewModel.updateStatus(complaint.id, newStatus)
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
}

@Composable
private fun ComplaintItemCard(
    complaint: ComplaintResponseDto,
    isUpdating: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Complaint #${complaint.id} by ${complaint.tenant_name ?: "Tenant"} in room ${complaint.room_number ?: "Unknown"}, status ${complaint.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket #${complaint.id}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(text = complaint.status, status = complaint.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tenant and Room Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Slate500
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = complaint.tenant_name ?: "Unknown Tenant",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Slate500
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Room ${complaint.room_number ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = complaint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Reported ${DateFormatter.formatDateTime(complaint.created_at)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (complaint.status == "OPEN") {
                AppButton(
                    text = "Mark In Progress",
                    onClick = { onUpdateStatus("IN PROGRESS") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    icon = Icons.Default.AssignmentTurnedIn
                )
            } else if (complaint.status == "IN PROGRESS") {
                AppButton(
                    text = "Revert to Open",
                    onClick = { onUpdateStatus("OPEN") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating
                )
            }
        }
    }
}
