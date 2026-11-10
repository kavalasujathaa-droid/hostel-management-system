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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.Slate50
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
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
        containerColor = Slate50
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
                        AppCard {
                            Text(
                                text = "PERSONAL DETAILS",
                                style = AppTypography.labelSmall,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                            Text(
                                text = tenant.full_name,
                                style = AppTypography.titleLarge,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "Email: ${tenant.email}",
                                style = AppTypography.bodyMedium,
                                color = Slate500
                            )
                            if (!tenant.phone.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                Text(
                                    text = "Phone: ${tenant.phone}",
                                    style = AppTypography.bodyMedium,
                                    color = Slate500
                                )
                            }
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "Account Registered: ${DateFormatter.formatIsoDate(tenant.created_at)}",
                                style = AppTypography.bodyMedium,
                                color = Slate500
                            )
                        }
                    }

                    // Active Stay Card
                    item {
                        Text(
                            text = "Current Active Stay",
                            style = AppTypography.titleMedium,
                            color = Slate900
                        )
                    }

                    item {
                        if (activeStay != null) {
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Room ${activeStay.room_number ?: activeStay.room_id}",
                                            style = AppTypography.titleMedium,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Joined on ${DateFormatter.formatIsoDate(activeStay.joining_date)}",
                                            style = AppTypography.bodyMedium,
                                            color = Slate500
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Monthly Rent: ${CurrencyFormatter.format(activeStay.monthly_rent)}",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Slate900
                                        )
                                    }
                                    StatusBadge(status = activeStay.status)
                                }
                            }
                        } else {
                            AppCard {
                                Text(
                                    text = "No active room stay assigned. Assign a vacant room from the Rooms screen.",
                                    style = AppTypography.bodyMedium,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    // Historical Stays
                    if (history.isNotEmpty()) {
                        item {
                            Text(
                                text = "Stay History (${history.size})",
                                style = AppTypography.titleMedium,
                                color = Slate900
                            )
                        }

                        items(history) { stay ->
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Room ${stay.room_number ?: stay.room_id}",
                                            style = AppTypography.titleMedium,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "From: ${DateFormatter.formatIsoDate(stay.joining_date)}" +
                                                (stay.approved_vacating_date?.let { " to ${DateFormatter.formatIsoDate(it)}" } ?: ""),
                                            style = AppTypography.bodyMedium,
                                            color = Slate500
                                        )
                                    }
                                    StatusBadge(status = stay.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
