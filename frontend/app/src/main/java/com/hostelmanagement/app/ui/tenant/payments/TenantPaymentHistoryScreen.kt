package com.hostelmanagement.app.ui.tenant.payments

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.HorizontalDivider
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
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.Slate500
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
        containerColor = MaterialTheme.colorScheme.background
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
                icon = Icons.Default.Payment,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(uiState.payments, key = { it.id }) { payment ->
                    PaymentHistoryCard(payment = payment)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: PaymentResponseDto) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Payment of ${CurrencyFormatter.format(payment.amount)}, UTR ${payment.utr}, status ${payment.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.format(payment.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(text = payment.status, status = payment.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "UTR: ${payment.utr}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Submitted: ${DateFormatter.formatDateTime(payment.submitted_at)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                if (payment.reviewed_at != null) {
                    Text(
                        text = "Reviewed: ${DateFormatter.formatDateTime(payment.reviewed_at)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }

            if (!payment.rejection_reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rejection Reason: ${payment.rejection_reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
