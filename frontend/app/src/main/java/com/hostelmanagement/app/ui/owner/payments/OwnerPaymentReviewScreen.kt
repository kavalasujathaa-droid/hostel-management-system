package com.hostelmanagement.app.ui.owner.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.hostelmanagement.app.core.theme.Slate200
import com.hostelmanagement.app.core.theme.Slate50
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.core.theme.Slate600
import com.hostelmanagement.app.core.theme.Slate900
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge
import kotlinx.coroutines.launch

@Composable
fun OwnerPaymentReviewScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: OwnerPaymentReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Verification",
                subtitle = "${state.pendingPayments.size} pending bank UTR reviews",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = PureWhite,
                contentColor = Slate900,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = Slate900,
                        height = 2.dp
                    )
                }
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Text(
                            text = "Pending (${state.pendingPayments.size})",
                            style = AppTypography.titleMedium
                        )
                    }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Text(
                            text = "Verified History (${state.reviewedPayments.size})",
                            style = AppTypography.titleMedium
                        )
                    }
                )
            }

            val currentList = if (state.selectedTab == 0) state.pendingPayments else state.reviewedPayments

            when {
                state.isLoading -> LoadingView(message = "Loading payments...")
                state.errorMessage != null && state.payments.isEmpty() -> {
                    ErrorStateView(
                        message = state.errorMessage ?: "Failed to load payments",
                        onRetry = viewModel::loadPayments
                    )
                }
                currentList.isEmpty() -> {
                    EmptyStateView(
                        title = if (state.selectedTab == 0) "No Pending Payments" else "No Payment History",
                        message = if (state.selectedTab == 0)
                            "All submitted tenant payments have been verified."
                        else "No past approved or rejected payments on record.",
                        icon = Icons.Outlined.Payment
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                    ) {
                        items(currentList, key = { it.id }) { payment ->
                            AppCard(
                                modifier = Modifier.semantics(mergeDescendants = true) {}
                            ) {
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "UTR: ${payment.utr}",
                                                style = AppTypography.bodyMedium,
                                                color = Slate900
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("UTR", payment.utr))
                                                    scope.launch { snackbarHostState.showSnackbar("Copied UTR to clipboard") }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ContentCopy,
                                                    contentDescription = "Copy UTR",
                                                    tint = Slate500
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Submitted: ${DateFormatter.formatIsoDateTime(payment.submitted_at)}",
                                            style = AppTypography.labelSmall,
                                            color = Slate500
                                        )
                                        if (!payment.rejection_reason.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                            Text(
                                                text = "Reason: ${payment.rejection_reason}",
                                                style = AppTypography.bodyMedium,
                                                color = StatusOverdueText
                                            )
                                        }
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

                                if (payment.status.equals("PENDING", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing16))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                                    ) {
                                        AppButton(
                                            text = "Reject",
                                            onClick = { viewModel.openRejectDialog(payment.id) },
                                            variant = ButtonVariant.Destructive,
                                            enabled = !state.isProcessingAction,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AppButton(
                                            text = "Approve & Mark Paid",
                                            onClick = { viewModel.approvePayment(payment.id) },
                                            variant = ButtonVariant.Primary,
                                            enabled = !state.isProcessingAction,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(AppDimens.Spacing16)) }
                    }
                }
            }
        }
    }

    if (state.isRejectDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeRejectDialog,
            title = {
                Text(text = "Reject Payment", style = AppTypography.titleMedium, color = Slate900)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    Text(
                        text = "Specify the reason for payment rejection (e.g., UTR not matching bank statement):",
                        style = AppTypography.bodyMedium,
                        color = Slate500
                    )
                    AppTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = "Rejection Reason",
                        placeholder = "UTR not received in bank account",
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "Confirm Rejection",
                    onClick = {
                        viewModel.confirmRejectPayment(rejectReason)
                        rejectReason = ""
                    },
                    variant = ButtonVariant.Destructive,
                    isLoading = state.isProcessingAction
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeRejectDialog) {
                    Text("Cancel", color = Slate600)
                }
            },
            containerColor = PureWhite,
            shape = com.hostelmanagement.app.core.theme.AppShapes.medium
        )
    }
}
