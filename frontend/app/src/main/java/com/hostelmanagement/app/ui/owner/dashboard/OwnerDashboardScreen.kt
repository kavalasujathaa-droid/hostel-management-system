package com.hostelmanagement.app.ui.owner.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.Slate100
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate400
import com.hostelmanagement.app.core.theme.Slate50
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate600
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun OwnerDashboardScreen(
    onNavigateToRooms: () -> Unit,
    onNavigateToTenants: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToPaymentQr: () -> Unit,
    onNavigateToComplaints: () -> Unit,
    onNavigateToVacate: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: OwnerDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Hostel Overview",
                subtitle = "Owner Operations Dashboard",
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if ((state.stats?.open_complaints_count ?: 0) > 0) {
                                    Badge(containerColor = StatusOverdueText)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Slate900
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Hostel Settings",
                            tint = Slate900
                        )
                    }
                }
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingView(message = "Loading hostel overview...")
            state.errorMessage != null && state.stats == null -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load dashboard",
                    onRetry = viewModel::loadDashboardData
                )
            }
            state.stats != null -> {
                val stats = state.stats!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
                ) {
                    // 1. Essential Metrics Strip
                    item {
                        AppCard {
                            Text(
                                text = "CURRENT CAPACITY & DUES",
                                style = AppTypography.labelSmall,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing12))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricColumn(
                                    title = "Bed Occupancy",
                                    primaryValue = "${stats.total_active_tenants} / ${stats.total_capacity}",
                                    secondaryValue = "${stats.occupancy_rate_percentage}% full"
                                )
                                MetricColumn(
                                    title = "Available Beds",
                                    primaryValue = "${stats.available_beds}",
                                    secondaryValue = "across ${stats.total_rooms} rooms"
                                )
                                MetricColumn(
                                    title = "Pending Approvals",
                                    primaryValue = "${stats.pending_payments_count}",
                                    secondaryValue = CurrencyFormatter.format(stats.pending_payments_amount)
                                )
                            }
                        }
                    }

                    // 2. Action Required (Pending Payment Approvals)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Action Required: Pending Payments",
                                style = AppTypography.titleMedium,
                                color = Slate900
                            )
                            Text(
                                text = "View All (${state.pendingPayments.size})",
                                style = AppTypography.bodyMedium,
                                color = Slate600,
                                modifier = Modifier.clickable(onClick = onNavigateToPayments)
                            )
                        }
                    }

                    if (state.pendingPayments.isEmpty()) {
                        item {
                            AppCard {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = AppDimens.Spacing4)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(AppDimens.Spacing8))
                                    Text(
                                        text = "No pending payments. All tenant receipts are verified.",
                                        style = AppTypography.bodyMedium,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.pendingPayments.take(3)) { payment ->
                            AppCard(onClick = onNavigateToPayments) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = payment.submitted_by_name ?: "Tenant #${payment.submitted_by}",
                                            style = AppTypography.titleMedium,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "UTR: ${payment.utr} · ${DateFormatter.formatIsoDateTime(payment.submitted_at)}",
                                            style = AppTypography.bodyMedium,
                                            color = Slate500
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.format(payment.amount),
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                        StatusBadge(status = payment.status)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Operational Navigation Shortcuts
                    item {
                        Text(
                            text = "Operations Hub",
                            style = AppTypography.titleMedium,
                            color = Slate900
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)) {
                            NavShortcutRow(
                                title = "Room Management & Vacancy",
                                subtitle = "${stats.total_rooms} rooms · ${stats.available_beds} beds available",
                                icon = Icons.Outlined.MeetingRoom,
                                onClick = onNavigateToRooms
                            )
                            NavShortcutRow(
                                title = "Tenant Directory & Stays",
                                subtitle = "${stats.total_active_tenants} active residents",
                                icon = Icons.Outlined.PeopleOutline,
                                onClick = onNavigateToTenants
                            )
                            NavShortcutRow(
                                title = "Rent Billing & Monthly Cycle",
                                subtitle = "Generate monthly charges and track balances",
                                icon = Icons.Outlined.ReceiptLong,
                                onClick = onNavigateToBilling
                            )
                            NavShortcutRow(
                                title = "UPI QR Code Settings",
                                subtitle = "Manage active QR code image for tenant payments",
                                icon = Icons.Outlined.QrCode,
                                onClick = onNavigateToPaymentQr
                            )
                            NavShortcutRow(
                                title = "Complaints Queue",
                                subtitle = "${stats.open_complaints_count} open maintenance issues",
                                icon = Icons.Outlined.ReportProblem,
                                onClick = onNavigateToComplaints
                            )
                            NavShortcutRow(
                                title = "Financial Reports & Revenue",
                                subtitle = "Monthly billing vs collection breakdown",
                                icon = Icons.Outlined.Assessment,
                                onClick = onNavigateToReports
                            )
                            NavShortcutRow(
                                title = "Vacate Notices & Approvals",
                                subtitle = "${stats.pending_vacate_requests_count} pending move-out requests",
                                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                                onClick = onNavigateToVacate
                            )
                            NavShortcutRow(
                                title = "Hostel Settings & Account",
                                subtitle = "Profile, password, and hostel policies",
                                icon = Icons.Outlined.Settings,
                                onClick = onNavigateToSettings
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    title: String,
    primaryValue: String,
    secondaryValue: String
) {
    Column {
        Text(
            text = title,
            style = AppTypography.bodyMedium,
            color = Slate500
        )
        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
        Text(
            text = primaryValue,
            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Slate900
        )
        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
        Text(
            text = secondaryValue,
            style = AppTypography.labelSmall,
            color = Slate600
        )
    }
}

@Composable
private fun NavShortcutRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Slate100, AppShapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.Spacing12))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.titleMedium,
                    color = Slate900
                )
                Text(
                    text = subtitle,
                    style = AppTypography.bodyMedium,
                    color = Slate500
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
