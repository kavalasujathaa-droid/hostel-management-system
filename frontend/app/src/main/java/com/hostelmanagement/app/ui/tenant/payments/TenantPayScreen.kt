package com.hostelmanagement.app.ui.tenant.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantPayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: TenantPayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Submit Rent Payment",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading payment details..."
            )
        } else if (uiState.paymentSubmitted) {
            PaymentSuccessView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                utr = uiState.utrInput,
                amount = uiState.amountInput,
                onViewHistoryClick = onNavigateToHistory
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // UPI QR Code Card
                item {
                    ActiveQrScanCard(qr = uiState.activeQr)
                }

                // Invoice Summary Card
                val charge = uiState.rentCharge
                if (charge != null) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Invoice #${charge.id}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Remaining Balance Due:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = CurrencyFormatter.format(charge.balance),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Form
                item {
                    Text(
                        text = "Enter Transaction Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    AppTextField(
                        value = uiState.amountInput,
                        onValueChange = { viewModel.onAmountChanged(it) },
                        label = "Payment Amount",
                        placeholder = "e.g. 5000",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = Slate500
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    AppTextField(
                        value = uiState.utrInput,
                        onValueChange = { viewModel.onUtrChanged(it) },
                        label = "Bank UTR / Transaction Reference",
                        placeholder = "12-digit transaction UTR number",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Slate500
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    AppButton(
                        text = "Submit Payment for Verification",
                        onClick = { viewModel.submitPayment() },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = uiState.isSubmitting,
                        enabled = uiState.amountInput.isNotBlank() && uiState.utrInput.length >= 4 && !uiState.isSubmitting
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
private fun ActiveQrScanCard(qr: com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto?) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "UPI QR Code",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scan with Google Pay, PhonePe, Paytm, or BHIM",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (qr != null) {
                val fullImageUrl = remember(qr.image_path) {
                    val base = BuildConfig.BACKEND_URL.trimEnd('/')
                    val path = if (qr.image_path.startsWith("/")) qr.image_path else "/${qr.image_path}"
                    "$base$path"
                }

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Hostel UPI QR code for payment",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate500
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No QR uploaded by owner",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessView(
    modifier: Modifier = Modifier,
    utr: String,
    amount: String,
    onViewHistoryClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Payment Submitted",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your payment of ${CurrencyFormatter.format(amount)} (UTR: $utr) has been submitted for owner verification.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppButton(
            text = "View Payment History",
            onClick = onViewHistoryClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
