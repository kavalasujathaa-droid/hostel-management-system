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
import androidx.compose.material.icons.filled.Notifications
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
import com.hostelmanagement.app.ui.tenant.billing.TenantRentDetailsScreen
import com.hostelmanagement.app.ui.tenant.complaints.TenantComplaintsScreen
import com.hostelmanagement.app.ui.tenant.complaints.TenantRaiseComplaintScreen
import com.hostelmanagement.app.ui.tenant.dashboard.TenantDashboardScreen
import com.hostelmanagement.app.ui.tenant.notifications.TenantNotificationsScreen
import com.hostelmanagement.app.ui.tenant.payments.TenantPayScreen
import com.hostelmanagement.app.ui.tenant.payments.TenantPaymentHistoryScreen
import com.hostelmanagement.app.ui.tenant.profile.TenantProfileScreen
import com.hostelmanagement.app.ui.tenant.room.TenantRoomInfoScreen
import com.hostelmanagement.app.ui.tenant.vacate.TenantVacateScreen

@Composable
fun TenantNavHost(
    onLogoutSuccess: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 5-Tab Tenant Bottom Navigation: Home, Payments, Complaints, Alerts, Profile
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.TenantDashboard.route,
            title = "Home",
            icon = Icons.Default.Dashboard
        ),
        BottomNavItem(
            route = Screen.TenantPayments.route,
            title = "Payments",
            icon = Icons.Default.Payment
        ),
        BottomNavItem(
            route = Screen.TenantComplaints.route,
            title = "Complaints",
            icon = Icons.Default.ReportProblem
        ),
        BottomNavItem(
            route = Screen.TenantNotifications.route,
            title = "Alerts",
            icon = Icons.Default.Notifications
        ),
        BottomNavItem(
            route = Screen.TenantProfile.route,
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
            startDestination = Screen.TenantDashboard.route,
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
            // Tab 1: Tenant Dashboard
            composable(Screen.TenantDashboard.route) {
                TenantDashboardScreen(
                    onNavigateToPay = { rentChargeId ->
                        navController.navigate(Screen.TenantPay.createRoute(rentChargeId))
                    },
                    onNavigateToRentDetails = { navController.navigate(Screen.TenantRentDetails.route) },
                    onNavigateToRoomInfo = { navController.navigate(Screen.TenantRoomInfo.route) },
                    onNavigateToComplaints = { navController.navigate(Screen.TenantComplaints.route) },
                    onNavigateToRaiseComplaint = { navController.navigate(Screen.TenantRaiseComplaint.route) },
                    onNavigateToVacate = { navController.navigate(Screen.TenantVacate.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.TenantNotifications.route) },
                    onNavigateToPaymentHistory = { navController.navigate(Screen.TenantPayments.route) }
                )
            }

            // Tab 2: Payments History
            composable(Screen.TenantPayments.route) {
                TenantPaymentHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 3: Complaints Portal
            composable(Screen.TenantComplaints.route) {
                TenantComplaintsScreen(
                    onNavigateToRaiseComplaint = { navController.navigate(Screen.TenantRaiseComplaint.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 4: Alerts & Notifications Center
            composable(Screen.TenantNotifications.route) {
                TenantNotificationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 5: Resident Profile & Settings
            composable(Screen.TenantProfile.route) {
                TenantProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutSuccess = onLogoutSuccess
                )
            }

            // Dedicated Tenant Flow Screens
            composable(Screen.TenantRaiseComplaint.route) {
                TenantRaiseComplaintScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onComplaintSubmitted = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TenantPay.route,
                arguments = listOf(navArgument("rentChargeId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) {
                TenantPayScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHistory = { navController.navigate(Screen.TenantPayments.route) }
                )
            }

            composable(Screen.TenantRoomInfo.route) {
                TenantRoomInfoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TenantRentDetails.route) {
                TenantRentDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPay = { rentChargeId ->
                        navController.navigate(Screen.TenantPay.createRoute(rentChargeId))
                    }
                )
            }

            composable(Screen.TenantVacate.route) {
                TenantVacateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
