package com.hostelmanagement.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hostelmanagement.app.core.theme.SurfaceNeutral
import com.hostelmanagement.app.ui.components.AppBottomBar
import com.hostelmanagement.app.ui.components.BottomNavItem
import com.hostelmanagement.app.ui.owner.billing.OwnerBillingScreen
import com.hostelmanagement.app.ui.owner.complaints.OwnerComplaintsScreen
import com.hostelmanagement.app.ui.owner.dashboard.OwnerDashboardScreen
import com.hostelmanagement.app.ui.owner.notifications.OwnerNotificationsScreen
import com.hostelmanagement.app.ui.owner.payments.OwnerPaymentReviewScreen
import com.hostelmanagement.app.ui.owner.qr.OwnerPaymentQrScreen
import com.hostelmanagement.app.ui.owner.reports.OwnerReportsScreen
import com.hostelmanagement.app.ui.owner.rooms.RoomDetailScreen
import com.hostelmanagement.app.ui.owner.rooms.RoomListScreen
import com.hostelmanagement.app.ui.owner.settings.OwnerSettingsScreen
import com.hostelmanagement.app.ui.owner.tenants.TenantDetailScreen
import com.hostelmanagement.app.ui.owner.tenants.TenantListScreen
import com.hostelmanagement.app.ui.owner.vacate.OwnerVacateScreen

@Composable
fun OwnerNavHost(
    onLogoutSuccess: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 5-Tab Owner Bottom Navigation: Home, Rooms, Payments, Complaints, Profile
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.OwnerDashboard.route,
            title = "Home",
            icon = Icons.Default.Dashboard
        ),
        BottomNavItem(
            route = Screen.OwnerRooms.route,
            title = "Rooms",
            icon = Icons.Default.MeetingRoom
        ),
        BottomNavItem(
            route = Screen.OwnerPayments.route,
            title = "Payments",
            icon = Icons.Default.Payment
        ),
        BottomNavItem(
            route = Screen.OwnerComplaints.route,
            title = "Complaints",
            icon = Icons.Default.ReportProblem
        ),
        BottomNavItem(
            route = Screen.OwnerSettings.route,
            title = "Profile",
            icon = Icons.Default.Person
        )
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = SurfaceNeutral,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.OwnerDashboard.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            }
        ) {
            // Tab 1: Dashboard Overview
            composable(Screen.OwnerDashboard.route) {
                OwnerDashboardScreen(
                    onNavigateToRooms = { navController.navigate(Screen.OwnerRooms.route) },
                    onNavigateToTenants = { navController.navigate(Screen.OwnerTenants.route) },
                    onNavigateToBilling = { navController.navigate(Screen.OwnerBilling.route) },
                    onNavigateToPayments = { navController.navigate(Screen.OwnerPayments.route) },
                    onNavigateToPaymentQr = { navController.navigate(Screen.OwnerPaymentQr.route) },
                    onNavigateToComplaints = { navController.navigate(Screen.OwnerComplaints.route) },
                    onNavigateToVacate = { navController.navigate(Screen.OwnerVacate.route) },
                    onNavigateToReports = { navController.navigate(Screen.OwnerReports.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.OwnerNotifications.route) },
                    onNavigateToSettings = { navController.navigate(Screen.OwnerSettings.route) }
                )
            }

            // Tab 2: Rooms Management
            composable(Screen.OwnerRooms.route) {
                RoomListScreen(
                    onNavigateToRoomDetail = { roomId ->
                        navController.navigate(Screen.OwnerRoomDetail.createRoute(roomId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.OwnerRoomDetail.route,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                RoomDetailScreen(
                    roomId = roomId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 3: Payments Review & Management
            composable(Screen.OwnerPayments.route) {
                OwnerPaymentReviewScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OwnerPaymentQr.route) {
                OwnerPaymentQrScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 4: Complaints Portal
            composable(Screen.OwnerComplaints.route) {
                OwnerComplaintsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 5: Account, Profile & Settings
            composable(Screen.OwnerSettings.route) {
                OwnerSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutSuccess = onLogoutSuccess
                )
            }

            // Secondary Owner Screens
            composable(Screen.OwnerTenants.route) {
                TenantListScreen(
                    onNavigateToTenantDetail = { tenantId ->
                        navController.navigate(Screen.OwnerTenantDetail.createRoute(tenantId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.OwnerTenantDetail.route,
                arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
                TenantDetailScreen(
                    tenantId = tenantId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OwnerBilling.route) {
                OwnerBillingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OwnerReports.route) {
                OwnerReportsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OwnerVacate.route) {
                OwnerVacateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OwnerNotifications.route) {
                OwnerNotificationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
