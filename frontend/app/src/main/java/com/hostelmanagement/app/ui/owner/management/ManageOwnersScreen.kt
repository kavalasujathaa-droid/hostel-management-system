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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun ManageOwnersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddOwner: () -> Unit,
    viewModel: ManageOwnersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Owners",
                subtitle = "Co-owners with equal administration access",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.loadOwners() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh owners list",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddOwner,
                containerColor = BrandPrimary,
                contentColor = PureWhite
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new owner"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        if (uiState.isLoading && uiState.owners.isEmpty()) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading owner accounts..."
            )
        } else if (uiState.owners.isEmpty()) {
            EmptyStateView(
                title = "No Co-Owners Added",
                description = "You are currently the only owner account for this hostel. Add co-owners to share hostel management.",
                actionLabel = "Add Co-Owner",
                onAction = onNavigateToAddOwner,
                icon = Icons.Default.Person,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
            ) {
                item {
                    SectionHeader(title = "Hostel Owners (${uiState.owners.size})")
                }

                items(uiState.owners, key = { it.id }) { owner ->
                    OwnerAccountCard(owner = owner)
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                }
            }
        }
    }
}

@Composable
private fun OwnerAccountCard(owner: UserResponseDto) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing16
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = owner.full_name.trim().take(2).uppercase().ifEmpty { "OW" }
                        Text(
                            text = initials,
                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                    Column {
                        Text(
                            text = owner.full_name,
                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Hostel Administrator",
                            style = AppTypography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                StatusBadge(status = if (owner.is_active) "ACTIVE" else "INACTIVE")
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing12))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(AppDimens.Spacing8))
                Text(
                    text = owner.email,
                    style = AppTypography.bodySmall,
                    color = TextPrimary
                )
            }

            if (!owner.phone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppDimens.Spacing6))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.Spacing8))
                    Text(
                        text = owner.phone,
                        style = AppTypography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing8))

            Text(
                text = "Account created on ${DateFormatter.formatDate(owner.created_at)}",
                style = AppTypography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
