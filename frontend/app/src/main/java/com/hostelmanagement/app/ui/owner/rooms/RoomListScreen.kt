package com.hostelmanagement.app.ui.owner.rooms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.hostelmanagement.app.core.theme.AppDimens
import com.hostelmanagement.app.core.theme.AppShapes
import com.hostelmanagement.app.core.theme.AppTypography
import com.hostelmanagement.app.core.theme.BorderSubtle
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
fun RoomListScreen(
    onNavigateToRoomDetail: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: RoomListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var newRoomNumber by remember { mutableStateOf("") }
    var newRoomFloor by remember { mutableStateOf("") }
    var newRoomCapacity by remember { mutableStateOf("2") }

    LaunchedEffect(state.actionSuccessMessage) {
        state.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Room Management",
                subtitle = "${state.rooms.size} rooms configured",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = viewModel::openAddRoomDialog) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Room",
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
            state.isLoading -> LoadingView(message = "Loading rooms...")
            state.errorMessage != null && state.rooms.isEmpty() -> {
                ErrorStateView(
                    message = state.errorMessage ?: "Failed to load rooms",
                    onRetry = viewModel::loadRooms
                )
            }
            state.rooms.isEmpty() -> {
                EmptyStateView(
                    title = "No Rooms Added Yet",
                    message = "Add rooms and set their bed capacity to start assigning tenants.",
                    icon = Icons.Outlined.MeetingRoom,
                    actionButtonText = "+ Add First Room",
                    onActionClick = viewModel::openAddRoomDialog,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val floors = remember(state.rooms) {
                        listOf(null) + state.rooms.mapNotNull { it.floor }.distinct().sorted()
                    }

                    if (floors.size > 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = AppDimens.Spacing16, vertical = AppDimens.Spacing8),
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing8)
                        ) {
                            floors.forEach { floor ->
                                val selected = state.selectedFloor == floor
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setFloorFilter(floor) },
                                    label = {
                                        Text(
                                            text = floor ?: "All Floors",
                                            style = AppTypography.labelMedium.copy(
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
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
                                        selected = selected,
                                        borderColor = if (selected) BrandPrimary else BorderSubtle
                                    ),
                                    shape = AppShapes.small
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppDimens.Spacing16),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                        }

                        items(state.rooms, key = { it.id }) { room ->
                            AppCard(
                                onClick = { onNavigateToRoomDetail(room.id) },
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
                                            text = "Room ${room.room_number}",
                                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing2))
                                        Text(
                                            text = "Floor: ${room.floor ?: "Standard"} • Total Beds: ${room.capacity}",
                                            style = AppTypography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${room.active_occupants} / ${room.capacity} Occupied",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (room.available_beds > 0) TextPrimary else StatusOverdueText
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.Spacing4))
                                        StatusBadge(
                                            status = if (room.available_beds > 0) "${room.available_beds} Free" else "Full"
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(AppDimens.Spacing24)) }
                    }
                }
            }
        }
    }

    if (state.isAddRoomDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeAddRoomDialog,
            title = {
                Text(text = "Add New Room", style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing12)) {
                    AppTextField(
                        value = newRoomNumber,
                        onValueChange = { newRoomNumber = it },
                        label = "Room Number",
                        placeholder = "e.g., 101, 202-A"
                    )

                    AppTextField(
                        value = newRoomFloor,
                        onValueChange = { newRoomFloor = it },
                        label = "Floor (Optional)",
                        placeholder = "e.g., Ground, 1st Floor"
                    )

                    AppTextField(
                        value = newRoomCapacity,
                        onValueChange = { newRoomCapacity = it },
                        label = "Bed Capacity",
                        placeholder = "Number of beds (e.g. 2, 3)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "Create Room",
                    onClick = {
                        val cap = newRoomCapacity.toIntOrNull() ?: 2
                        viewModel.createRoom(newRoomNumber, newRoomFloor, cap)
                        newRoomNumber = ""
                        newRoomFloor = ""
                        newRoomCapacity = "2"
                    },
                    isLoading = state.isSubmittingRoom
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddRoomDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = PureWhite,
            shape = AppShapes.large
        )
    }
}
