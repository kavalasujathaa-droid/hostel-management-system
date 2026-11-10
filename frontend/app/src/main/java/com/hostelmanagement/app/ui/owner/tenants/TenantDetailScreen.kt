package com.hostelmanagement.app.ui.owner.tenants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantDetailScreen(
    tenantId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TenantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.details?.tenant?.full_name ?: "Tenant Profile",
                subtitle = "Resident Details & Stay History",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingView(message = "Loading resident profile...")
            state.errorMessage != null && state.details == null -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load tenant details",
                    onRetry = viewModel::loadTenantDetails
                )
            }
            state.details != null -> {
                val tenant = state.details!!.tenant
                val activeStay = state.details!!.active_stay
                val history = state.details!!.stay_history ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
                ) {
                    // Profile Info Card
                    item {
                        AppCard(padding = AppDimens.Spacing20) {
                            Text(
                                text = "PERSONAL DETAILS",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                            Text(
                                text = tenant.full_name,
                                style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "Email: ${tenant.email}",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                            if (!tenant.phone.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                Text(
                                    text = "Phone: ${tenant.phone}",
                                    style = AppTypography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "Registered on: ${DateFormatter.formatIsoDate(tenant.created_at)}",
                                style = AppTypography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Active Stay Card
                    item {
                        SectionHeader(title = "Current Active Stay")
                    }

                    item {
                        if (activeStay != null) {
                            AppCard(padding = AppDimens.Spacing16) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Room ${activeStay.room_number ?: activeStay.room_id}",
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Joined: ${DateFormatter.formatIsoDate(activeStay.joining_date)}",
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Monthly Rent: ${CurrencyFormatter.format(activeStay.monthly_rent)}",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextPrimary
                                        )
                                    }
                                    StatusBadge(status = activeStay.status)
                                }
                            }
                        } else {
                            AppCard(padding = AppDimens.Spacing16) {
                                Text(
                                    text = "No active room stay assigned. Assign a vacant room from the Rooms screen.",
                                    style = AppTypography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Historical Stays
                    if (history.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Stay History (${history.size})")
                        }

                        items(history) { stay ->
                            AppCard(padding = AppDimens.Spacing14) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Room ${stay.room_number ?: stay.room_id}",
                                            style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "From: ${DateFormatter.formatIsoDate(stay.joining_date)}" +
                                                    (stay.approved_vacating_date?.let { " to ${DateFormatter.formatIsoDate(it)}" } ?: ""),
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    StatusBadge(status = stay.status)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                    }
                }
            }
        }
    }
}
