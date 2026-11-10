package com.hostelmanagement.app.ui.tenant.notifications

import androidx.compose.runtime.Composable
import com.hostelmanagement.app.ui.owner.notifications.OwnerNotificationsScreen

@Composable
fun TenantNotificationsScreen(
    onNavigateBack: () -> Unit
) {
    // Both owner and tenant share the same backend notification contract (/api/v1/notifications)
    OwnerNotificationsScreen(onNavigateBack = onNavigateBack)
}
