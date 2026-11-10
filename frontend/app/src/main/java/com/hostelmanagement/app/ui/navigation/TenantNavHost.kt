package com.hostelmanagement.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
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

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.TenantDashboard.route,
            title = "Dashboard",
            icon = Icons.Default.Dashboard
        ),
        BottomNavItem(
            route = Screen.TenantPayments.route,
            title = "Payments",
            icon = Icons.Default.Payment
        ),
        BottomNavItem(
            route = Screen.TenantProfile.route,
            title = "Profile",
            icon = Icons.Default.Person
        )
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
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
            modifier = Modifier.padding(paddingValues)
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

            // Tab 2: Payment History
            composable(Screen.TenantPayments.route) {
                TenantPaymentHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Tab 3: Tenant Profile
            composable(Screen.TenantProfile.route) {
                TenantProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutSuccess = onLogoutSuccess
                )
            }

            // Subroutes
            composable(Screen.TenantRentDetails.route) {
                TenantRentDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPay = { rentChargeId ->
                        navController.navigate(Screen.TenantPay.createRoute(rentChargeId))
                    }
                )
            }

            composable(
                route = Screen.TenantPay.route,
                arguments = listOf(navArgument("rentChargeId") { type = NavType.LongType })
            ) {
                TenantPayScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHistory = {
                        navController.navigate(Screen.TenantPayments.route) {
                            popUpTo(Screen.TenantDashboard.route)
                        }
                    }
                )
            }

            composable(Screen.TenantRoomInfo.route) {
                TenantRoomInfoScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TenantComplaints.route) {
                TenantComplaintsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRaiseComplaint = { navController.navigate(Screen.TenantRaiseComplaint.route) }
                )
            }

            composable(Screen.TenantRaiseComplaint.route) {
                TenantRaiseComplaintScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onComplaintSubmitted = { navController.popBackStack() }
                )
            }

            composable(Screen.TenantVacate.route) {
                TenantVacateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TenantNotifications.route) {
                TenantNotificationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
