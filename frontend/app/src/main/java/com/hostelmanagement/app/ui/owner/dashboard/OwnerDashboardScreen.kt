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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.SurfaceVariantNeutral
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.MetricCard
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
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
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Hi, Owner 👋",
                                style = AppTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Hostel Overview",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
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
                                    tint = TextPrimary
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrandContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Hostel Settings",
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PureWhite,
                        titleContentColor = TextPrimary
                    )
                )
                HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle)
            }
        },
        containerColor = SurfaceNeutral
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
                    // 1. 2x2 Metric Cards Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                            ) {
                                MetricCard(
                                    title = "Tenants",
                                    value = "${stats.total_active_tenants}",
                                    icon = Icons.Outlined.People,
                                    iconBgColor = Color(0xFFEBF3FE),
                                    iconTintColor = Color(0xFF2563EB),
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToTenants
                                )
                                MetricCard(
                                    title = "Rooms",
                                    value = "${stats.total_rooms}",
                                    icon = Icons.Outlined.MeetingRoom,
                                    iconBgColor = Color(0xFFEFECFF),
                                    iconTintColor = BrandPrimary,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToRooms
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                            ) {
                                MetricCard(
                                    title = "Available Beds",
                                    value = "${stats.available_beds}",
                                    icon = Icons.Outlined.CheckCircle,
                                    iconBgColor = Color(0xFFE6F5EE),
                                    iconTintColor = Color(0xFF16845B),
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToRooms
                                )
                                MetricCard(
                                    title = "Pending (${stats.pending_payments_count})",
                                    value = CurrencyFormatter.format(stats.pending_payments_amount),
                                    icon = Icons.Outlined.ReceiptLong,
                                    iconBgColor = Color(0xFFFDECEE),
                                    iconTintColor = Color(0xFFD84A55),
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToPayments
                                )
                            }
                        }
                    }

                    // 2. Bed Occupancy Progress Card
                    item {
                        AppCard(padding = AppDimens.Spacing16) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hostel Bed Occupancy",
                                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${stats.occupancy_rate_percentage.toInt()}% Occupied",
                                    style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(AppDimens.Spacing10))
                            LinearProgressIndicator(
                                progress = { (stats.occupancy_rate_percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BrandPrimary,
                                trackColor = SurfaceVariantNeutral
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stats.total_active_tenants} active residents",
                                    style = AppTypography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${stats.available_beds} beds free / ${stats.total_capacity} total",
                                    style = AppTypography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // 3. Quick Actions 2x2 Grid
                    item {
                        SectionHeader(title = "Quick Actions")
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                            ) {
                                OwnerQuickActionButton(
                                    title = "Add Tenant",
                                    icon = Icons.Outlined.PersonAdd,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToTenants
                                )
                                OwnerQuickActionButton(
                                    title = "Add Room",
                                    icon = Icons.Outlined.MeetingRoom,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToRooms
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                            ) {
                                OwnerQuickActionButton(
                                    title = "Record Payment",
                                    icon = Icons.Outlined.Payment,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToPayments
                                )
                                OwnerQuickActionButton(
                                    title = "View Reports",
                                    icon = Icons.Outlined.Assessment,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToReports
                                )
                            }
                        }
                    }

                    // 4. Action Required: Pending Payments
                    item {
                        SectionHeader(
                            title = "Action Required: Payments",
                            actionText = if (state.pendingPayments.isNotEmpty()) "View All (${state.pendingPayments.size})" else null,
                            onActionClick = onNavigateToPayments
                        )
                    }

                    if (state.pendingPayments.isEmpty()) {
                        item {
                            AppCard(padding = AppDimens.Spacing16) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF16845B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(AppDimens.Spacing10))
                                    Text(
                                        text = "All tenant payment submissions are verified and up to date.",
                                        style = AppTypography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.pendingPayments.take(3), key = { it.id }) { payment ->
                            AppCard(
                                onClick = onNavigateToPayments,
                                padding = AppDimens.Spacing14
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = payment.submitted_by_name ?: "Tenant #${payment.submitted_by}",
                                            style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "UTR: ${payment.utr} • ${DateFormatter.formatIsoDateTime(payment.submitted_at)}",
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.format(payment.amount),
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                        StatusBadge(status = payment.status)
                                    }
                                }
                            }
                        }
                    }

                    // 5. Operations Hub
                    item {
                        SectionHeader(title = "Operations Hub")
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)) {
                            OwnerOperationsRow(
                                title = "Complaints & Maintenance",
                                subtitle = "${stats.open_complaints_count} open maintenance tickets",
                                icon = Icons.Outlined.ReportProblem,
                                badgeCount = stats.open_complaints_count,
                                onClick = onNavigateToComplaints
                            )
                            OwnerOperationsRow(
                                title = "Rent Billing Cycle",
                                subtitle = "Generate charges and track tenant dues",
                                icon = Icons.Outlined.ReceiptLong,
                                onClick = onNavigateToBilling
                            )
                            OwnerOperationsRow(
                                title = "UPI QR Code Settings",
                                subtitle = "Manage QR code for tenant payments",
                                icon = Icons.Outlined.QrCode,
                                onClick = onNavigateToPaymentQr
                            )
                            OwnerOperationsRow(
                                title = "Vacate Notices",
                                subtitle = "${stats.pending_vacate_requests_count} pending move-out requests",
                                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                                badgeCount = stats.pending_vacate_requests_count,
                                onClick = onNavigateToVacate
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing16))
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerQuickActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier,
        padding = AppDimens.Spacing14,
        onClick = onClick,
        shape = AppShapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(BrandContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
            Text(
                text = title,
                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun OwnerOperationsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    AppCard(
        onClick = onClick,
        padding = AppDimens.Spacing14
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(BrandContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.Spacing12))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = AppTypography.bodySmall,
                    color = TextSecondary
                )
            }
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StatusOverdueText)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(AppDimens.Spacing8))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
