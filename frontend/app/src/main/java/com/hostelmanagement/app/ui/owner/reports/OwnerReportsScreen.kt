package com.hostelmanagement.app.ui.owner.reports

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.MonthlyFinancialRecordDto
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import java.util.Calendar

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
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
            val years = remember(currentYear) { listOf(currentYear, currentYear - 1, currentYear - 2) }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing8),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)
            ) {
                items(years) { year ->
                    val isSelected = uiState.selectedYear == year
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setYear(year) },
                        label = {
                            Text(
                                text = year.toString(),
                                style = AppTypography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = PureWhite,
                            containerColor = PureWhite,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) BrandPrimary else BorderSubtle
                        ),
                        shape = AppShapes.small
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
                    icon = Icons.Outlined.Assessment,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val report = uiState.report!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimens.Spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                        SectionHeader(title = "Annual Summary (${uiState.selectedYear})")
                    }

                    item {
                        AnnualSummaryCard(summary = report.summary)
                    }

                    item {
                        SectionHeader(title = "Monthly Breakdown")
                    }

                    if (report.monthly_breakdown.isEmpty()) {
                        item {
                            Text(
                                text = "No monthly records found for this year.",
                                style = AppTypography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = AppDimens.Spacing8)
                            )
                        }
                    } else {
                        items(report.monthly_breakdown, key = { "${it.year}-${it.month}" }) { record ->
                            MonthlyRecordCard(record = record)
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

@Composable
private fun AnnualSummaryCard(summary: MonthlyFinancialRecordDto) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing16
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Annual summary: Total billed ${CurrencyFormatter.format(summary.total_billed)}, collected ${CurrencyFormatter.format(summary.total_collected)}, outstanding ${CurrencyFormatter.format(summary.total_outstanding)}"
                }
        ) {
            ReportKpiRow(
                icon = Icons.Outlined.Assessment,
                label = "Total Billed",
                value = CurrencyFormatter.format(summary.total_billed),
                valueColor = TextPrimary
            )
            HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle, modifier = Modifier.padding(vertical = AppDimens.Spacing12))
            ReportKpiRow(
                icon = Icons.Outlined.CheckCircle,
                label = "Total Collected",
                value = CurrencyFormatter.format(summary.total_collected),
                valueColor = StatusPaidText
            )
            HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle, modifier = Modifier.padding(vertical = AppDimens.Spacing12))
            ReportKpiRow(
                icon = Icons.Outlined.PendingActions,
                label = "Outstanding Balance",
                value = CurrencyFormatter.format(summary.total_outstanding),
                valueColor = StatusOverdueText
            )
        }
    }
}

@Composable
private fun ReportKpiRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
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
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.width(AppDimens.Spacing8))
            Text(
                text = label,
                style = AppTypography.bodyMedium,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
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

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing14
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "$monthName financial record: Billed ${CurrencyFormatter.format(record.total_billed)}, collected ${CurrencyFormatter.format(record.total_collected)}, outstanding ${CurrencyFormatter.format(record.total_outstanding)}"
                }
        ) {
            Text(
                text = monthName,
                style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Billed", style = AppTypography.labelSmall, color = TextSecondary)
                    Text(
                        text = CurrencyFormatter.format(record.total_billed),
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }
                Column {
                    Text(text = "Collected", style = AppTypography.labelSmall, color = TextSecondary)
                    Text(
                        text = CurrencyFormatter.format(record.total_collected),
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusPaidText
                    )
                }
                Column {
                    Text(text = "Outstanding", style = AppTypography.labelSmall, color = TextSecondary)
                    Text(
                        text = CurrencyFormatter.format(record.total_outstanding),
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusOverdueText
                    )
                }
            }
        }
    }
}
