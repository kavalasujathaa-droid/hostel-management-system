package com.hostelmanagement.app.ui.owner.tenants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextMuted
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    onNavigateToTenantDetail: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: TenantListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableLongStateOf(0L) }
    var selectedRoomNumber by remember { mutableStateOf("No Immediate Room Assignment") }
    var isRoomDropdownExpanded by remember { mutableStateOf(false) }
    var joiningDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var monthlyRent by remember { mutableStateOf("6500.00") }

    LaunchedEffect(state.actionSuccessMessage) {
        state.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Tenant Directory",
                subtitle = "${state.tenants.size} registered residents",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = viewModel::openRegisterDialog) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Register Tenant",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingView(message = "Loading tenants...")
            state.errorMessage != null && state.tenants.isEmpty() -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load tenants",
                    onRetry = viewModel::loadTenants
                )
            }
            state.tenants.isEmpty() -> {
                EmptyStateView(
                    title = "No Tenants Registered",
                    message = "Register tenants to assign beds and issue monthly billing.",
                    icon = Icons.Outlined.PeopleOutline,
                    actionButtonText = "+ Register First Tenant",
                    onActionClick = viewModel::openRegisterDialog,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing8)
                ) {
                    AppTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        label = "Search Directory",
                        placeholder = "Search by name, email, or phone...",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppDimens.Spacing12))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredTenants, key = { it.id }) { tenant ->
                            AppCard(
                                onClick = { onNavigateToTenantDetail(tenant.id) },
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
                                            text = tenant.full_name,
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = tenant.email,
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                        if (!tenant.phone.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                            Text(
                                                text = "Phone: ${tenant.phone}",
                                                style = AppTypography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    StatusBadge(status = if (tenant.is_active) "ACTIVE" else "INACTIVE")
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(AppDimens.Spacing24)) }
                    }
                }
            }
        }
    }

    if (state.isRegisterDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeRegisterDialog,
            title = {
                Text(
                    text = "Register New Tenant",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    AppTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "e.g., Ananya Roy"
                    )

                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "tenant@example.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    AppTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Initial Password",
                        placeholder = "Minimum 6 characters",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    AppTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone (Optional)",
                        placeholder = "+91 9876543210",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Text(
                        text = "Optional Room Assignment",
                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )

                    ExposedDropdownMenuBox(
                        expanded = isRoomDropdownExpanded,
                        onExpandedChange = { isRoomDropdownExpanded = !isRoomDropdownExpanded }
                    ) {
                        AppTextField(
                            value = selectedRoomNumber,
                            onValueChange = {},
                            label = "Assign Room Now",
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = isRoomDropdownExpanded,
                            onDismissRequest = { isRoomDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Immediate Room Assignment") },
                                onClick = {
                                    selectedRoomId = 0L
                                    selectedRoomNumber = "No Immediate Room Assignment"
                                    isRoomDropdownExpanded = false
                                }
                            )
                            state.availableRooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text("Room ${room.room_number} (${room.available_beds} beds free)") },
                                    onClick = {
                                        selectedRoomId = room.id
                                        selectedRoomNumber = "Room ${room.room_number}"
                                        isRoomDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedRoomId > 0) {
                        AppTextField(
                            value = joiningDate,
                            onValueChange = { joiningDate = it },
                            label = "Joining Date (YYYY-MM-DD)",
                            placeholder = "2026-11-10"
                        )

                        AppTextField(
                            value = monthlyRent,
                            onValueChange = { monthlyRent = it },
                            label = "Monthly Rent (₹)",
                            placeholder = "6500.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = "Register Tenant",
                    onClick = {
                        val rent = if (selectedRoomId > 0) monthlyRent.toDoubleOrNull() else null
                        val rId = if (selectedRoomId > 0) selectedRoomId else null
                        val jDate = if (selectedRoomId > 0) joiningDate else null
                        viewModel.registerTenant(
                            fullName = fullName,
                            email = email,
                            password = password,
                            phone = phone,
                            roomId = rId,
                            joiningDate = jDate,
                            monthlyRent = rent
                        )
                    },
                    isLoading = state.isSubmittingTenant
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeRegisterDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}
