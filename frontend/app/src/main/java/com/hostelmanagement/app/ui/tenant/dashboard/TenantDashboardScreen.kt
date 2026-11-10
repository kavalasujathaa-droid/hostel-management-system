package com.hostelmanagement.app.ui.tenant.dashboard

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
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.hostelmanagement.app.core.theme.StatusOverdueBg
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.StatusPaidBg
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.StatusPendingBg
import com.hostelmanagement.app.core.theme.StatusPendingText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDashboardScreen(
    onNavigateToPay: (Long) -> Unit,
    onNavigateToRentDetails: () -> Unit,
    onNavigateToRoomInfo: () -> Unit,
    onNavigateToComplaints: () -> Unit,
    onNavigateToRaiseComplaint: () -> Unit = onNavigateToComplaints,
    onNavigateToVacate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPaymentHistory: () -> Unit,
    viewModel: TenantDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val stay = uiState.activeStay
    val roomDisplay = if (stay?.room_number != null) "Room ${stay.room_number}" else "Hostel Resident"

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Hi, Resident 👋",
                                style = AppTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = roomDisplay,
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = onNavigateToRoomInfo) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrandContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "My Stay Info",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        if (uiState.isLoading && uiState.activeStay == null && uiState.recentPayments.isEmpty()) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading dashboard..."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
            ) {
                // 1. "This Month's Rent" Hero Card
                item {
                    val unpaid = uiState.unpaidRentCharge
                    if (unpaid != null && uiState.totalOutstanding > BigDecimal.ZERO) {
                        TenantRentDueHeroCard(
                            rentCharge = unpaid,
                            totalOutstanding = uiState.totalOutstanding,
                            onPayClick = { onNavigateToPay(unpaid.id) }
                        )
                    } else {
                        TenantAllDuesClearedCard(onViewHistoryClick = onNavigateToRentDetails)
                    }
                }

                // 2. Quick Actions
                item {
                    SectionHeader(title = "Quick Actions")
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                    ) {
                        val unpaidChargeId = uiState.unpaidRentCharge?.id
                        TenantQuickActionCard(
                            title = "My Payments",
                            icon = Icons.Outlined.History,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPaymentHistory
                        )
                        TenantQuickActionCard(
                            title = "Upload Proof",
                            icon = Icons.Outlined.Image,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (unpaidChargeId != null) {
                                    onNavigateToPay(unpaidChargeId)
                                } else {
                                    onNavigateToPaymentHistory()
                                }
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing10)
                    ) {
                        TenantQuickActionCard(
                            title = "Raise Complaint",
                            icon = Icons.Outlined.AddComment,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRaiseComplaint
                        )
                        TenantQuickActionCard(
                            title = "Room & Stay",
                            icon = Icons.Outlined.MeetingRoom,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRoomInfo
                        )
                    }
                }

                // 3. Active Room Stay Summary
                if (stay != null) {
                    item {
                        AppCard(
                            onClick = onNavigateToRoomInfo,
                            padding = AppDimens.Spacing16
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(BrandContainer, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MeetingRoom,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                                    Column {
                                        Text(
                                            text = "Room ${stay.room_number ?: "-"}",
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Monthly Rent: ${CurrencyFormatter.format(stay.monthly_rent)}",
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                StatusBadge(status = stay.status)
                            }
                        }
                    }
                }

                // 4. Recent Activity
                item {
                    SectionHeader(
                        title = "Recent Activity",
                        actionText = if (uiState.recentPayments.isNotEmpty()) "View All" else null,
                        onActionClick = onNavigateToPaymentHistory
                    )
                }

                if (uiState.recentPayments.isEmpty()) {
                    item {
                        AppCard(padding = AppDimens.Spacing16) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Payment,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(AppDimens.Spacing10))
                                Text(
                                    text = "No recent payment transactions recorded yet.",
                                    style = AppTypography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.recentPayments, key = { it.id }) { payment ->
                        TenantActivityCard(
                            payment = payment,
                            onClick = onNavigateToPaymentHistory
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

@Composable
private fun TenantRentDueHeroCard(
    rentCharge: RentChargeResponseDto,
    totalOutstanding: BigDecimal,
    onPayClick: () -> Unit
) {
    AppCard(
        padding = AppDimens.Spacing20,
        containerColor = PureWhite,
        borderColor = BorderSubtle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Month's Rent",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )
            Surface(
                color = StatusOverdueBg,
                shape = AppShapes.small,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF9B6BC))
            ) {
                Text(
                    text = "Due Soon",
                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusOverdueText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.Spacing8))

        Text(
            text = CurrencyFormatter.format(totalOutstanding),
            style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing4))

        Text(
            text = "Billing Period: ${rentCharge.period_start} to ${rentCharge.period_end}",
            style = AppTypography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(AppDimens.Spacing16))

        AppButton(
            text = "Pay Rent",
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.Payment
        )
    }
}

@Composable
private fun TenantAllDuesClearedCard(onViewHistoryClick: () -> Unit) {
    AppCard(padding = AppDimens.Spacing20) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(StatusPaidBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = StatusPaidText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                Column {
                    Text(
                        text = "All Dues Cleared",
                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "No pending rent balances this month.",
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = "Bills",
                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = BrandPrimary,
                modifier = Modifier.clickable(onClick = onViewHistoryClick)
            )
        }
    }
}

@Composable
private fun TenantQuickActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier,
        padding = AppDimens.Spacing16,
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
                    .size(44.dp)
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
private fun TenantActivityCard(
    payment: PaymentResponseDto,
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
                    .background(
                        if (payment.status.equals("PAID", ignoreCase = true) || payment.status.equals("APPROVED", ignoreCase = true))
                            StatusPaidBg
                        else if (payment.status.equals("PENDING", ignoreCase = true))
                            StatusPendingBg
                        else
                            StatusOverdueBg,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (payment.status.equals("PAID", ignoreCase = true) || payment.status.equals("APPROVED", ignoreCase = true))
                        Icons.Outlined.Check
                    else if (payment.status.equals("PENDING", ignoreCase = true))
                        Icons.Outlined.History
                    else
                        Icons.Outlined.ReportProblem,
                    contentDescription = null,
                    tint = if (payment.status.equals("PAID", ignoreCase = true) || payment.status.equals("APPROVED", ignoreCase = true))
                        StatusPaidText
                    else if (payment.status.equals("PENDING", ignoreCase = true))
                        StatusPendingText
                    else
                        StatusOverdueText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.Spacing12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Payment • ${CurrencyFormatter.format(payment.amount)}",
                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = "${payment.status.lowercase().replaceFirstChar { it.uppercase() }} • ${DateFormatter.formatDateTime(payment.submitted_at)}",
                    style = AppTypography.bodySmall,
                    color = TextSecondary
                )
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
