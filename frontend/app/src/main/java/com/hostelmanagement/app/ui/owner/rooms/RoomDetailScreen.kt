package com.hostelmanagement.app.ui.owner.rooms

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BrandPrimary
import com.hostelmanagement.app.core.theme.PureWhite
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.core.theme.TextPrimary
import com.hostelmanagement.app.core.theme.TextSecondary
import com.hostelmanagement.app.ui.components.AppButton
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTextField
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.ButtonVariant
import com.hostelmanagement.app.ui.components.ErrorStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.SectionHeader
import com.hostelmanagement.app.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: Long,
    onNavigateBack: () -> Unit,
    viewModel: RoomDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTenantId by remember { mutableLongStateOf(0L) }
    var selectedTenantName by remember { mutableStateOf("") }
    var isTenantDropdownExpanded by remember { mutableStateOf(false) }
    var joiningDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var monthlyRent by remember { mutableStateOf("6500.00") }

    LaunchedEffect(state.isRoomDeleted) {
        if (state.isRoomDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.room?.let { "Room ${it.room_number}" } ?: "Room Details",
                subtitle = state.room?.floor?.let { "Floor: $it" } ?: "Room Overview",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceNeutral
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingView(message = "Loading room details...")
            state.errorMessage != null && state.room == null -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load room",
                    onRetry = viewModel::loadRoomDetails
                )
            }
            state.room != null -> {
                val room = state.room!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing12),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing16)
                ) {
                    // Room Summary Card
                    item {
                        AppCard(padding = AppDimens.Spacing20) {
                            Text(
                                text = "CAPACITY & STATUS",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${room.active_occupants} / ${room.capacity} Beds Occupied",
                                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                    Text(
                                        text = "${room.available_beds} bed(s) available for assignment",
                                        style = AppTypography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                StatusBadge(status = if (room.available_beds > 0) "AVAILABLE" else "FULL")
                            }

                            if (room.available_beds > 0) {
                                Spacer(modifier = Modifier.height(AppDimens.Spacing16))
                                AppButton(
                                    text = "+ Assign Vacant Bed",
                                    onClick = viewModel::openAssignBedDialog,
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Occupants Section
                    item {
                        SectionHeader(title = "Current Occupants (${room.occupants?.size ?: 0})")
                    }

                    if (room.occupants.isNullOrEmpty()) {
                        item {
                            AppCard(padding = AppDimens.Spacing16) {
                                Text(
                                    text = "No tenants currently assigned to this room.",
                                    style = AppTypography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(room.occupants, key = { it.stay_id }) { occupant ->
                            AppCard(padding = AppDimens.Spacing14) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = occupant.full_name,
                                            style = AppTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = occupant.email,
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Joined: ${DateFormatter.formatIsoDate(occupant.joining_date)}",
                                            style = AppTypography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    StatusBadge(status = "ACTIVE")
                                }
                            }
                        }
                    }

                    // Deactivate/Delete Room Option (Only if empty)
                    if (room.active_occupants == 0) {
                        item {
                            Spacer(modifier = Modifier.height(AppDimens.Spacing8))
                            AppButton(
                                text = "Deactivate Room",
                                onClick = viewModel::deleteRoom,
                                variant = ButtonVariant.Destructive,
                                isLoading = state.isDeletingRoom,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.Spacing24))
                    }
                }
            }
        }
    }

    // Assign Bed Dialog
    if (state.isAssignBedDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeAssignBedDialog,
            title = {
                Text(
                    text = "Assign Bed in Room ${state.room?.room_number}",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    Text(
                        text = "Select Tenant",
                        style = AppTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )

                    ExposedDropdownMenuBox(
                        expanded = isTenantDropdownExpanded,
                        onExpandedChange = { isTenantDropdownExpanded = !isTenantDropdownExpanded }
                    ) {
                        AppTextField(
                            value = selectedTenantName.ifBlank { "Select a registered tenant" },
                            onValueChange = {},
                            label = "Tenant",
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = isTenantDropdownExpanded,
                            onDismissRequest = { isTenantDropdownExpanded = false }
                        ) {
                            state.availableTenants.forEach { tenant ->
                                DropdownMenuItem(
                                    text = { Text("${tenant.full_name} (${tenant.email})") },
                                    onClick = {
                                        selectedTenantId = tenant.id
                                        selectedTenantName = tenant.full_name
                                        isTenantDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

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
            },
            confirmButton = {
                AppButton(
                    text = "Confirm Assignment",
                    onClick = {
                        if (selectedTenantId > 0) {
                            viewModel.assignBed(selectedTenantId, joiningDate, monthlyRent)
                        }
                    },
                    isLoading = state.isAssigningBed
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAssignBedDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}
