package com.hostelmanagement.app.ui.tenant.billing

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
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantRentDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPay: (Long) -> Unit,
    viewModel: TenantRentDetailsViewModel = hiltViewModel()
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
                title = "Rent & Invoices",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.rentCharges.isEmpty()) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading rent charges..."
            )
        } else if (uiState.rentCharges.isEmpty()) {
            EmptyStateView(
                title = "No Invoices Found",
                description = "No monthly rent charges have been billed to your account yet.",
                actionLabel = "Refresh",
                onAction = { viewModel.loadRentCharges() },
                icon = Icons.Default.ReceiptLong,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(uiState.rentCharges, key = { it.id }) { charge ->
                    TenantRentChargeCard(
                        charge = charge,
                        onPayClick = { onNavigateToPay(charge.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TenantRentChargeCard(
    charge: RentChargeResponseDto,
    onPayClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Invoice #${charge.id}, due ${CurrencyFormatter.format(charge.amount_due)}, balance ${CurrencyFormatter.format(charge.balance)}, status ${charge.status}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invoice #${charge.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(text = charge.status, status = charge.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Period: ${charge.period_start} to ${charge.period_end}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Amount Due", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(charge.amount_due),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(text = "Amount Paid", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(charge.amount_paid),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(text = "Balance", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(charge.balance),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (charge.status == "PAID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Payment Due Date: ${DateFormatter.formatDate(charge.due_date)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )

            if (charge.status != "PAID") {
                Spacer(modifier = Modifier.height(14.dp))
                AppButton(
                    text = "Submit Payment",
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Payment
                )
            }
        }
    }
}
