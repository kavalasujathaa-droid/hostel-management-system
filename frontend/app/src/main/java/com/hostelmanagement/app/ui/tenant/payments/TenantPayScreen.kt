package com.hostelmanagement.app.ui.tenant.payments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandContainer
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusOverdueText
import com.hostelmanagement.app.core.theme.StatusPaidBg
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.SurfaceVariantNeutral
import com.hostelmanagement.app.core.theme.TextMuted
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantPayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: TenantPayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri, context.contentResolver)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pay Rent",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
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
                    .padding(AppDimens.Spacing24),
                utr = uiState.utrInput,
                amount = uiState.amountInput,
                onViewHistoryClick = onNavigateToHistory
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
            ) {
                // 1. Amount Due Card
                val charge = uiState.rentCharge
                item {
                    AppCard(padding = AppDimens.Spacing20) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount Due",
                                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextSecondary
                            )
                            if (charge != null) {
                                StatusBadge(status = charge.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimens.Spacing6))

                        val displayAmount = if (charge != null) CurrencyFormatter.format(charge.balance) else "₹${uiState.amountInput}"
                        Text(
                            text = displayAmount,
                            style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        if (charge != null) {
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                            Text(
                                text = "Invoice #${charge.id} • ${charge.period_start} to ${charge.period_end}",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // 2. Payment Method Selector
                item {
                    SectionHeader(title = "Payment Method")
                }

                item {
                    AppCard(padding = AppDimens.Spacing12) {
                        PaymentMethodRow(
                            title = "UPI (GPay / PhonePe / Paytm / BHIM)",
                            selected = selectedPaymentMethod == "UPI",
                            onClick = { selectedPaymentMethod = "UPI" }
                        )
                        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle, modifier = Modifier.padding(vertical = AppDimens.Spacing8))
                        PaymentMethodRow(
                            title = "Bank Transfer (NEFT / IMPS)",
                            selected = selectedPaymentMethod == "Bank",
                            onClick = { selectedPaymentMethod = "Bank" }
                        )
                        HorizontalDivider(thickness = AppDimens.HairlineBorder, color = BorderSubtle, modifier = Modifier.padding(vertical = AppDimens.Spacing8))
                        PaymentMethodRow(
                            title = "Other Offline Mode",
                            selected = selectedPaymentMethod == "Other",
                            onClick = { selectedPaymentMethod = "Other" }
                        )
                    }
                }

                // 3. UPI QR Code Display (if UPI chosen)
                if (selectedPaymentMethod == "UPI") {
                    item {
                        ActiveQrCard(qr = uiState.activeQr)
                    }
                }

                // 4. Upload Payment Screenshot
                item {
                    SectionHeader(title = "Upload Payment Screenshot *")
                }

                item {
                    if (uiState.selectedImageUri != null) {
                        AppCard(padding = AppDimens.Spacing12) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = uiState.selectedImageUri,
                                    contentDescription = "Selected screenshot preview",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(AppShapes.small)
                                        .border(1.dp, BorderSubtle, AppShapes.small),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(AppDimens.Spacing12))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uiState.selectedImageName ?: "payment_screenshot.jpg",
                                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                    Text(
                                        text = "Attached for owner verification",
                                        style = AppTypography.bodySmall,
                                        color = BrandPrimary
                                    )
                                }
                                IconButton(onClick = { viewModel.onRemoveImage() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove screenshot",
                                        tint = StatusOverdueText
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppShapes.medium)
                                .border(1.dp, BorderSubtle, AppShapes.medium)
                                .background(PureWhite)
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(vertical = AppDimens.Spacing24, horizontal = AppDimens.Spacing16),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(BrandContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(AppDimens.Spacing10))
                                Text(
                                    text = "Tap to upload screenshot",
                                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandPrimary
                                )
                                Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                Text(
                                    text = "PNG, JPG (Max 5 MB)",
                                    style = AppTypography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // 5. Payment Transaction Details
                item {
                    SectionHeader(title = "Transaction Details")
                }

                item {
                    AppCard(padding = AppDimens.Spacing16) {
                        AppTextField(
                            value = uiState.amountInput,
                            onValueChange = { viewModel.onAmountChanged(it) },
                            label = "Payment Amount (₹)",
                            placeholder = "e.g. 6000",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CurrencyRupee,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(AppDimens.Spacing16))

                        AppTextField(
                            value = uiState.utrInput,
                            onValueChange = { viewModel.onUtrChanged(it) },
                            label = "Bank UTR / Transaction Reference",
                            placeholder = "12-digit UTR reference number",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 6. Submitting Progress
                if (uiState.isSubmitting) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = BrandPrimary,
                                trackColor = SurfaceVariantNeutral
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing6))
                            Text(
                                text = if (uiState.selectedImageUri != null) "Uploading proof & submitting payment..." else "Submitting payment...",
                                style = AppTypography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // 7. Submit Payment Button
                item {
                    AppButton(
                        text = "Submit Payment",
                        onClick = { viewModel.submitPayment() },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = uiState.isSubmitting,
                        enabled = uiState.amountInput.isNotBlank() && uiState.utrInput.length >= 4 && !uiState.isSubmitting
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppDimens.Spacing4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = BrandPrimary,
                unselectedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(AppDimens.Spacing8))
        Text(
            text = title,
            style = AppTypography.bodyLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun ActiveQrCard(qr: PaymentQRResponseDto?) {
    AppCard(padding = AppDimens.Spacing16) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan to Pay via UPI",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing2))
            Text(
                text = "Use Google Pay, PhonePe, Paytm, or BHIM",
                style = AppTypography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(AppDimens.Spacing14))

            if (qr != null) {
                val fullImageUrl = remember(qr.image_path) {
                    val base = BuildConfig.BACKEND_URL.trimEnd('/')
                    val path = if (qr.image_path.startsWith("/")) qr.image_path else "/${qr.image_path}"
                    "$base$path"
                }

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(AppShapes.medium)
                        .border(1.dp, BorderSubtle, AppShapes.medium)
                        .background(PureWhite),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = "Active UPI QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppDimens.Spacing8),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(AppShapes.medium)
                        .background(BrandContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(AppDimens.Spacing16)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                        Text(
                            text = "No UPI QR uploaded yet",
                            style = AppTypography.bodySmall,
                            color = TextSecondary
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(StatusPaidBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = StatusPaidText,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.Spacing20))
            Text(
                text = "Payment Submitted!",
                style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
            Text(
                text = "Your payment of ₹$amount (UTR: $utr) has been submitted for owner verification.",
                style = AppTypography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = AppDimens.Spacing16)
            )

            Spacer(modifier = Modifier.height(AppDimens.Spacing32))

            AppButton(
                text = "View Payment History",
                onClick = onViewHistoryClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
