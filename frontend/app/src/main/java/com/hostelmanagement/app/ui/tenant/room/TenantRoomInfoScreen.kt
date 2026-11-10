package com.hostelmanagement.app.ui.tenant.room

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MeetingRoom
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmanagement.app.core.common.CurrencyFormatter
import com.hostelmanagement.app.core.common.DateFormatter
import com.hostelmanagement.app.core.theme.Slate500
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.ui.components.AppCard
import com.hostelmanagement.app.ui.components.AppTopBar
import com.hostelmanagement.app.ui.components.EmptyStateView
import com.hostelmanagement.app.ui.components.LoadingView
import com.hostelmanagement.app.ui.components.StatusBadge

@Composable
fun TenantRoomInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: TenantRoomInfoViewModel = hiltViewModel()
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
                title = "Room Information",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.activeStay == null) {
            LoadingView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "Loading room information..."
            )
        } else if (uiState.activeStay == null && uiState.pastStays.isEmpty()) {
            EmptyStateView(
                title = "No Room Assignment",
                description = "You do not currently have an active or past room assignment.",
                actionLabel = "Refresh",
                onAction = { viewModel.loadRoomInfo() },
                icon = Icons.Default.MeetingRoom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                    Text(
                        text = "Current Room Assignment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val active = uiState.activeStay
                if (active != null) {
                    item {
                        ActiveStayDetailCard(stay = active)
                    }
                } else {
                    item {
                        Text(
                            text = "No active room stay assigned.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                    }
                }

                if (uiState.pastStays.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stay History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(uiState.pastStays, key = { it.id }) { stay ->
                        PastStayCard(stay = stay)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveStayDetailCard(stay: TenantStayResponseDto) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Active room ${stay.room_number ?: "-"}, monthly rent ${CurrencyFormatter.format(stay.monthly_rent)}, joined ${DateFormatter.formatDate(stay.joining_date)}"
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room ${stay.room_number ?: "-"}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(text = stay.status, status = stay.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StayInfoRow(
                icon = Icons.Default.CurrencyRupee,
                label = "Agreed Monthly Rent",
                value = CurrencyFormatter.format(stay.monthly_rent)
            )

            Spacer(modifier = Modifier.height(10.dp))

            StayInfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Check-in / Joining Date",
                value = DateFormatter.formatDate(stay.joining_date)
            )

            if (!stay.approved_vacating_date.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                StayInfoRow(
                    icon = Icons.Default.EventBusy,
                    label = "Approved Vacating Date",
                    value = DateFormatter.formatDate(stay.approved_vacating_date)
                )
            }
        }
    }
}

@Composable
private fun StayInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Slate500
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PastStayCard(stay: TenantStayResponseDto) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room ${stay.room_number ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${DateFormatter.formatDate(stay.joining_date)} to ${stay.approved_vacating_date?.let { DateFormatter.formatDate(it) } ?: "Vacated"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
            StatusBadge(text = stay.status, status = stay.status)
        }
    }
}
