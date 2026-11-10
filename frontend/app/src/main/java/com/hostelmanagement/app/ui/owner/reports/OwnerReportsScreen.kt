package com.hostelmanagement.app.ui.owner.reports

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.data.remote.dto.MonthlyFinancialRecordDto
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import java.util.Calendar
import java.util.Locale

@Composable
fun OwnerReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerReportsViewModel = hiltViewModel()
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
                title = "Financial Reports",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Year filter chips
            val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
            val years = remember(currentYear) { listOf(currentYear, currentYear - 1, currentYear - 2) }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(years) { year ->
                    FilterChip(
                        selected = uiState.selectedYear == year,
                        onClick = { viewModel.setYear(year) },
                        label = { Text(year.toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (uiState.isLoading && uiState.report == null) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading annual financial report..."
                )
            } else if (uiState.report == null) {
                EmptyStateView(
                    title = "No Data Available",
                    description = "No financial records found for year ${uiState.selectedYear}.",
                    actionLabel = "Retry",
                    onAction = { viewModel.loadReport() },
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val report = uiState.report!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Annual Summary (${uiState.selectedYear})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    item {
                        AnnualSummaryCard(summary = report.summary)
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Monthly Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (report.monthly_breakdown.isEmpty()) {
                        item {
                            Text(
                                text = "No monthly records found for this year.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(report.monthly_breakdown, key = { "${it.year}-${it.month}" }) { record ->
                            MonthlyRecordCard(record = record)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnualSummaryCard(summary: MonthlyFinancialRecordDto) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Annual summary: Total billed ${CurrencyFormatter.format(summary.total_billed)}, collected ${CurrencyFormatter.format(summary.total_collected)}, outstanding ${CurrencyFormatter.format(summary.total_outstanding)}"
                }
        ) {
            ReportKpiRow(
                icon = Icons.Default.Assessment,
                label = "Total Billed",
                value = CurrencyFormatter.format(summary.total_billed),
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            ReportKpiRow(
                icon = Icons.Default.CheckCircle,
                label = "Total Collected",
                value = CurrencyFormatter.format(summary.total_collected),
                valueColor = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            ReportKpiRow(
                icon = Icons.Default.PendingActions,
                label = "Outstanding Balance",
                value = CurrencyFormatter.format(summary.total_outstanding),
                valueColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ReportKpiRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Slate500
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
private fun MonthlyRecordCard(record: MonthlyFinancialRecordDto) {
    val monthName = remember(record.month) {
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        if (record.month in 1..12) monthNames[record.month - 1] else "Month ${record.month}"
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$monthName financial record: Billed ${CurrencyFormatter.format(record.total_billed)}, collected ${CurrencyFormatter.format(record.total_collected)}, outstanding ${CurrencyFormatter.format(record.total_outstanding)}"
                }
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Billed", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(record.total_billed),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(text = "Collected", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(record.total_collected),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(text = "Outstanding", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text(
                        text = CurrencyFormatter.format(record.total_outstanding),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
