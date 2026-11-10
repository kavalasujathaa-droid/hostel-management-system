package com.hostelmanagement.app.ui.tenant.payments

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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.StatusOverdueBg
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.StatusPaidBg
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.StatusPendingBg
import com.hostelmanagement.app.core.theme.StatusPendingText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantPaymentHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: TenantPaymentHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment History",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        if (uiState.isLoading && uiState.payments.isEmpty()) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading payment history..."
            )
        } else if (uiState.payments.isEmpty()) {
            EmptyStateView(
                title = "No Payments Found",
                description = "You have not submitted any rent payments yet.",
                actionLabel = "Refresh",
                onAction = { viewModel.loadPayments() },
                icon = Icons.Outlined.Payment,
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
                items(uiState.payments, key = { it.id }) { payment ->
                    PaymentHistoryCard(payment = payment)
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: PaymentResponseDto) {
    val isPaid = payment.status.equals("PAID", ignoreCase = true) || payment.status.equals("APPROVED", ignoreCase = true)
    val isPending = payment.status.equals("PENDING", ignoreCase = true)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
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
                        .background(
                            if (isPaid) StatusPaidBg else if (isPending) StatusPendingBg else StatusOverdueBg,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Outlined.Check else if (isPending) Icons.Outlined.History else Icons.Outlined.ReportProblem,
                        contentDescription = null,
                        tint = if (isPaid) StatusPaidText else if (isPending) StatusPendingText else StatusOverdueText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                Column {
                    Text(
                        text = CurrencyFormatter.format(payment.amount),
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "UTR: ${payment.utr}",
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            StatusBadge(status = payment.status)
        }

        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle, modifier = Modifier.padding(vertical = AppDimens.Spacing10))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Submitted: ${DateFormatter.formatDateTime(payment.submitted_at)}",
                style = AppTypography.labelSmall,
                color = TextSecondary
            )
            if (payment.reviewed_at != null) {
                Text(
                    text = "Reviewed: ${DateFormatter.formatDateTime(payment.reviewed_at)}",
                    style = AppTypography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (!payment.rejection_reason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
            Text(
                text = "Rejection Reason: ${payment.rejection_reason}",
                style = AppTypography.bodySmall,
                color = StatusOverdueText
            )
        }
    }
}
