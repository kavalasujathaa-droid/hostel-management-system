package com.hostelmanagement.app.ui.owner.billing

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun OwnerBillingScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: OwnerBillingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var genYear by remember { mutableStateOf(state.selectedYear.toString()) }
    var genMonth by remember { mutableStateOf(state.selectedMonth.toString()) }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Rent Billing",
                subtitle = "${DateFormatter.getMonthName(state.selectedMonth)} ${state.selectedYear} Cycle",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = viewModel::openGenerateDialog) {
                        Icon(
                            imageVector = Icons.Outlined.ElectricBolt,
                            contentDescription = "Generate Monthly Dues",
                            tint = BrandPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingView(message = "Loading billing charges...")
            state.errorMessage != null && state.charges.isEmpty() -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load billing",
                    onRetry = viewModel::loadCharges
                )
            }
            state.charges.isEmpty() -> {
                EmptyStateView(
                    title = "No Billing Generated For This Cycle",
                    message = "Tap 'Generate Dues' to calculate server-side rent charges for active tenant stays.",
                    icon = Icons.Outlined.ReceiptLong,
                    actionButtonText = "+ Generate ${DateFormatter.getMonthName(state.selectedMonth)} Dues",
                    onActionClick = viewModel::openGenerateDialog,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                ) {
                    items(state.charges, key = { it.id }) { charge ->
                        AppCard(
                            modifier = Modifier.semantics(mergeDescendants = true) {},
                            padding = AppDimens.Spacing16
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = charge.tenant_name ?: "Stay #${charge.tenant_stay_id}",
                                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                    Text(
                                        text = "Room ${charge.room_number ?: "—"} • Due ${DateFormatter.formatIsoDate(charge.due_date)}",
                                        style = AppTypography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                    Text(
                                        text = "Billable Days: ${charge.billable_days} / ${charge.days_in_month}",
                                        style = AppTypography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyFormatter.format(charge.amount_due),
                                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    if (charge.balance != "0.00" && charge.balance != "0") {
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Bal: ${CurrencyFormatter.format(charge.balance)}",
                                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = StatusOverdueText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                    StatusBadge(status = charge.status)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(AppDimens.Spacing24)) }
                }
            }
        }
    }

    if (state.isGenerateDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeGenerateDialog,
            title = {
                Text(
                    text = "Generate Monthly Rent Charges",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    Text(
                        text = "This generates daily-prorated rent charges for all active stays in the given month.",
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )

                    AppTextField(
                        value = genYear,
                        onValueChange = { genYear = it },
                        label = "Year (2020 - 2100)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    AppTextField(
                        value = genMonth,
                        onValueChange = { genMonth = it },
                        label = "Month (1 - 12)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "Generate Dues",
                    onClick = {
                        val y = genYear.toIntOrNull() ?: state.selectedYear
                        val m = genMonth.toIntOrNull() ?: state.selectedMonth
                        viewModel.generateBilling(y, m)
                    },
                    isLoading = state.isGeneratingBills
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeGenerateDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}
