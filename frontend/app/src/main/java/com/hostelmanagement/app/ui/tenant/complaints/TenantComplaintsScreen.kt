package com.hostelmanagement.app.ui.tenant.complaints

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantComplaintsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRaiseComplaint: () -> Unit = {},
    viewModel: TenantComplaintsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var viewingAttachmentComplaint by remember { mutableStateOf<ComplaintResponseDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadComplaints()
    }

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
                title = "Complaints & Maintenance",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRaiseComplaint,
                containerColor = BrandPrimary,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite) },
                text = { Text("Raise Complaint", fontWeight = FontWeight.Bold, color = PureWhite) }
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
            // Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing8),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)
            ) {
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
                item {
                    val isSelected = uiState.selectedStatusFilter == "OPEN"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("OPEN") },
                        label = { Text("Open", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                    val isSelected = uiState.selectedStatusFilter == "IN PROGRESS"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("IN PROGRESS") },
                        label = { Text("In Progress", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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
                    val isSelected = uiState.selectedStatusFilter == "RESOLVED"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter("RESOLVED") },
                        label = { Text("Resolved", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
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

            if (uiState.isLoading && uiState.complaints.isEmpty()) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading complaints..."
                )
            } else if (uiState.complaints.isEmpty()) {
                EmptyStateView(
                    title = if (uiState.selectedStatusFilter != null) "No ${uiState.selectedStatusFilter} Complaints" else "No Complaints Logged",
                    description = if (uiState.selectedStatusFilter != null) {
                        "You don't have any complaints with status: ${uiState.selectedStatusFilter}."
                    } else {
                        "Everything looks good! If you experience any maintenance or facility issue, raise a ticket."
                    },
                    actionLabel = "Raise Complaint",
                    onAction = onNavigateToRaiseComplaint,
                    icon = Icons.Outlined.ReportProblem,
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

                    items(uiState.complaints, key = { it.id }) { complaint ->
                        TenantComplaintItemCard(
                            complaint = complaint,
                            onViewAttachment = { viewingAttachmentComplaint = complaint }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp)) // Clearance for FAB
                    }
                }
            }
        }
    }

    // Modal Attachment Lightbox Dialog
    viewingAttachmentComplaint?.let { complaint ->
        Dialog(onDismissRequest = { viewingAttachmentComplaint = null }) {
            Card(
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.Spacing16),
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
                                color = TextPrimary
                            )
                            Text(
                                text = complaint.title.ifBlank { complaint.category },
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { viewingAttachmentComplaint = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close preview", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimens.Spacing12))

                    val attachmentUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/complaints/${complaint.id}/attachment"
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = "Full Complaint Attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .clip(AppShapes.medium)
                            .border(1.dp, BorderSubtle, AppShapes.medium),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(AppDimens.Spacing16))

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
private fun TenantComplaintItemCard(
    complaint: ComplaintResponseDto,
    onViewAttachment: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing16
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Ticket #${complaint.id}, ${complaint.title}, category ${complaint.category}, status ${complaint.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ticket #${complaint.id}",
                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing8))
                    Box(
                        modifier = Modifier
                            .clip(AppShapes.small)
                            .background(BrandContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = complaint.category,
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary
                        )
                    }
                }
                StatusBadge(status = complaint.status)
            }

            if (complaint.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                Text(
                    text = complaint.title,
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing4))

            Text(
                text = complaint.description,
                style = AppTypography.bodyMedium,
                color = TextSecondary
            )

            // Optional Photo Thumbnail
            if (!complaint.attachment_path.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppDimens.Spacing12))
                val attachmentUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/complaints/${complaint.id}/attachment"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.small)
                        .background(BrandContainer)
                        .clickable(onClick = onViewAttachment)
                        .padding(AppDimens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = "Complaint thumbnail",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(AppShapes.small)
                            .border(1.dp, BorderSubtle, AppShapes.small),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing10))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Photo Attached",
                            style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap to view full image",
                            style = AppTypography.bodySmall,
                            color = BrandPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "View photo",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing10))

            Text(
                text = "Submitted ${DateFormatter.formatDateTime(complaint.created_at)}",
                style = AppTypography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
