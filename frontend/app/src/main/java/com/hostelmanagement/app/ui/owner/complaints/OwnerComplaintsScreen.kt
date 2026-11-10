package com.hostelmanagement.app.ui.owner.complaints

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate600
import com.hostelmanagement.app.core.theme.Slate700
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ButtonVariant
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
    var viewingAttachmentComplaint by remember { mutableStateOf<ComplaintResponseDto?>(null) }

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
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = PureWhite,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedStatusFilter == null,
                            borderColor = if (uiState.selectedStatusFilter == null) MaterialTheme.colorScheme.primary else Slate200
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
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = PureWhite,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedStatusFilter == "OPEN",
                            borderColor = if (uiState.selectedStatusFilter == "OPEN") MaterialTheme.colorScheme.primary else Slate200
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
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = PureWhite,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedStatusFilter == "IN PROGRESS",
                            borderColor = if (uiState.selectedStatusFilter == "IN PROGRESS") MaterialTheme.colorScheme.primary else Slate200
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == "RESOLVED",
                        onClick = { viewModel.setStatusFilter("RESOLVED") },
                        label = { Text("Resolved") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = PureWhite,
                            labelColor = Slate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedStatusFilter == "RESOLVED",
                            borderColor = if (uiState.selectedStatusFilter == "RESOLVED") MaterialTheme.colorScheme.primary else Slate200
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
                            },
                            onViewAttachment = { viewingAttachmentComplaint = complaint }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Modal Attachment Lightbox Dialog for Owner
    viewingAttachmentComplaint?.let { complaint ->
        Dialog(onDismissRequest = { viewingAttachmentComplaint = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ticket #${complaint.id} Photo",
                                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "Room ${complaint.room_number ?: "-"} • ${complaint.tenant_name ?: "Tenant"}",
                                style = AppTypography.bodySmall,
                                color = Slate500
                            )
                        }
                        IconButton(onClick = { viewingAttachmentComplaint = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close preview", tint = Slate600)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val attachmentUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/complaints/${complaint.id}/attachment"
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = "Full Complaint Attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate200),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        text = "Close",
                        onClick = { viewingAttachmentComplaint = null },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintItemCard(
    complaint: ComplaintResponseDto,
    isUpdating: Boolean,
    onUpdateStatus: (String) -> Unit,
    onViewAttachment: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Complaint #${complaint.id} by ${complaint.tenant_name ?: "Tenant"} in room ${complaint.room_number ?: "Unknown"}, category ${complaint.category}, status ${complaint.status}"
                }
        ) {
            // Header Row: Ticket #, Category Badge & StatusBadge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ticket #${complaint.id}",
                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = complaint.category,
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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

            // Title
            if (complaint.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = complaint.title,
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = complaint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Optional Photo Thumbnail Row
            if (!complaint.attachment_path.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val attachmentUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/complaints/${complaint.id}/attachment"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .clickable(onClick = onViewAttachment)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = "Complaint photo thumbnail",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Photo Attached",
                            style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to inspect full photo",
                            style = AppTypography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Inspect photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Reported ${DateFormatter.formatDateTime(complaint.created_at)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            when (complaint.status) {
                "OPEN" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = "Mark In Progress",
                            onClick = { onUpdateStatus("IN PROGRESS") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUpdating,
                            variant = ButtonVariant.Secondary,
                            icon = Icons.Default.AssignmentTurnedIn
                        )
                        AppButton(
                            text = "Resolve Ticket",
                            onClick = { onUpdateStatus("RESOLVED") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUpdating,
                            variant = ButtonVariant.Primary,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
                "IN PROGRESS" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = "Revert to Open",
                            onClick = { onUpdateStatus("OPEN") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUpdating,
                            variant = ButtonVariant.Secondary,
                            icon = Icons.Default.Refresh
                        )
                        AppButton(
                            text = "Mark Resolved",
                            onClick = { onUpdateStatus("RESOLVED") },
                            modifier = Modifier.weight(1f),
                            enabled = !isUpdating,
                            variant = ButtonVariant.Primary,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
                "RESOLVED" -> {
                    AppButton(
                        text = "Reopen Ticket",
                        onClick = { onUpdateStatus("OPEN") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        variant = ButtonVariant.Secondary,
                        icon = Icons.Default.Refresh
                    )
                }
            }
        }
    }
}
