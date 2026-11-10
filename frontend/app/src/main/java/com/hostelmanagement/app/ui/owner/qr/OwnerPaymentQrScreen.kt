package com.hostelmanagement.app.ui.owner.qr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCode2
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.StatusPaidText
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun OwnerPaymentQrScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerPaymentQrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadQrFromUri(context, it) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment QR Settings",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        if (uiState.isLoading && uiState.activeQr == null && uiState.qrHistory.isEmpty()) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading payment QR codes..."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppDimens.Spacing16),
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
            ) {
                item {
                    Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                    SectionHeader(title = "Active Payment QR")
                    Text(
                        text = "This QR code is displayed to residents when paying rent via UPI.",
                        style = AppTypography.bodySmall,
                        color = TextSecondary
                    )
                }

                item {
                    val activeQr = uiState.activeQr
                    if (activeQr != null) {
                        ActiveQrCard(qr = activeQr)
                    } else {
                        EmptyStateView(
                            title = "No Active QR Code",
                            description = "Upload a UPI QR code image so tenants can easily scan and pay.",
                            actionLabel = null,
                            onAction = null,
                            icon = Icons.Outlined.QrCode2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppDimens.Spacing16)
                        )
                    }
                }

                item {
                    AppButton(
                        text = if (uiState.isUploading) "Uploading QR..." else "Upload New QR Code",
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = uiState.isUploading,
                        icon = Icons.Outlined.AddPhotoAlternate
                    )
                }

                item {
                    SectionHeader(title = "QR Code History")
                }

                if (uiState.qrHistory.isEmpty()) {
                    item {
                        Text(
                            text = "No previous QR codes on file.",
                            style = AppTypography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = AppDimens.Spacing8)
                        )
                    }
                } else {
                    items(uiState.qrHistory, key = { it.id }) { qrItem ->
                        QrHistoryItem(qr = qrItem)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                }
            }
        }
    }
}

@Composable
private fun ActiveQrCard(qr: PaymentQRResponseDto) {
    val fullImageUrl = remember(qr.image_path) {
        val base = BuildConfig.BACKEND_URL.trimEnd('/')
        val path = if (qr.image_path.startsWith("/")) qr.image_path else "/${qr.image_path}"
        "$base$path"
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing20
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = "ACTIVE")
                Text(
                    text = "Added ${DateFormatter.formatDateTime(qr.created_at)}",
                    style = AppTypography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing16))

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(AppShapes.medium)
                    .background(PureWhite)
                    .border(1.dp, BorderSubtle, AppShapes.medium),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = "Active UPI QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.Spacing12))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = StatusPaidText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(AppDimens.Spacing6))
                Text(
                    text = "Active & visible to all tenants",
                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun QrHistoryItem(qr: PaymentQRResponseDto) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = AppDimens.Spacing14
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "QR Code #${qr.id}",
                    style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                Text(
                    text = DateFormatter.formatDateTime(qr.created_at),
                    style = AppTypography.labelSmall,
                    color = TextSecondary
                )
            }

            StatusBadge(
                status = if (qr.is_active) "ACTIVE" else "ARCHIVED"
            )
        }
    }
}
