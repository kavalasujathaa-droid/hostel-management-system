package com.hostelmanagement.app.ui.owner.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.BuildConfig
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
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
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
    var viewingProofPayment by remember { mutableStateOf<PaymentResponseDto?>(null) }

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
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = PureWhite,
                contentColor = BrandPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = BrandPrimary
                    )
                }
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Text(
                            text = "Pending (${state.pendingPayments.size})",
                            style = AppTypography.titleSmall.copy(
                                fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (state.selectedTab == 0) BrandPrimary else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Text(
                            text = "History (${state.historyPayments.size})",
                            style = AppTypography.titleSmall.copy(
                                fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (state.selectedTab == 1) BrandPrimary else TextSecondary
                        )
                    }
                )
            }

            when {
                state.isLoading -> {
                    LoadingView(modifier = Modifier.fillMaxSize(), message = "Loading payments...")
                }
                state.errorMessage != null && state.pendingPayments.isEmpty() && state.historyPayments.isEmpty() -> {
                    ErrorStateView(
                        modifier = Modifier.fillMaxSize(),
                        message = state.errorMessage ?: "Unknown error occurred",
                        onRetry = viewModel::loadPayments
                    )
                }
                else -> {
                    val currentList = if (state.selectedTab == 0) state.pendingPayments else state.historyPayments

                    if (currentList.isEmpty()) {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            title = if (state.selectedTab == 0) "No Pending Payments" else "No Payment History",
                            message = if (state.selectedTab == 0)
                                "All submitted tenant payments have been verified."
                            else "No past approved or rejected payments on record.",
                            icon = Icons.Outlined.Payment
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                        ) {
                            items(currentList, key = { it.id }) { payment ->
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
                                                text = payment.submitted_by_name ?: "Tenant #${payment.submitted_by}",
                                                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "UTR: ${payment.utr}",
                                                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    color = TextPrimary
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("UTR", payment.utr))
                                                        scope.launch { snackbarHostState.showSnackbar("Copied UTR to clipboard") }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.ContentCopy,
                                                        contentDescription = "Copy UTR",
                                                        tint = BrandPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Submitted: ${DateFormatter.formatIsoDateTime(payment.submitted_at)}",
                                                style = AppTypography.labelSmall,
                                                color = TextSecondary
                                            )
                                            if (!payment.rejection_reason.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                                Text(
                                                    text = "Reason: ${payment.rejection_reason}",
                                                    style = AppTypography.bodySmall,
                                                    color = StatusOverdueText
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = CurrencyFormatter.format(payment.amount),
                                                style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppDimens.Spacing6))
                                            StatusBadge(status = payment.status)
                                        }
                                    }

                                    // Payment Screenshot Proof Preview
                                    if (!payment.proof_image_path.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing12))
                                        val proofUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/payments/${payment.id}/proof"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(AppShapes.small)
                                                .background(BrandContainer)
                                                .clickable { viewingProofPayment = payment }
                                                .padding(AppDimens.Spacing8),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = proofUrl,
                                                contentDescription = "Payment Screenshot Thumbnail",
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(AppShapes.small)
                                                    .border(1.dp, BorderSubtle, AppShapes.small),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(AppDimens.Spacing10))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Screenshot Attached",
                                                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Tap to view full receipt",
                                                    style = AppTypography.bodySmall,
                                                    color = BrandPrimary
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = "Enlarge proof",
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
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
    }

    // Modal Dialog: Full Screenshot Lightbox
    viewingProofPayment?.let { proofPayment ->
        Dialog(onDismissRequest = { viewingProofPayment = null }) {
            Card(
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.Spacing16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payment Screenshot",
                                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "UTR: ${proofPayment.utr} • ₹${proofPayment.amount}",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { viewingProofPayment = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close preview", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimens.Spacing12))

                    val fullProofUrl = "${BuildConfig.BACKEND_URL.trimEnd('/')}/api/v1/payments/${proofPayment.id}/proof"
                    AsyncImage(
                        model = fullProofUrl,
                        contentDescription = "Full payment screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 420.dp)
                            .clip(AppShapes.medium)
                            .border(1.dp, BorderSubtle, AppShapes.medium),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                    AppButton(
                        text = "Dismiss",
                        onClick = { viewingProofPayment = null },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary
                    )
                }
            }
        }
    }

    if (state.isRejectDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeRejectDialog,
            title = {
                Text(text = "Reject Payment", style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    Text(
                        text = "Specify the reason for payment rejection (e.g., UTR not matching bank statement):",
                        style = AppTypography.bodyMedium,
                        color = TextSecondary
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
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}
