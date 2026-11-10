package com.hostelmanagement.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hostelmanagement.app.core.datastore.SessionManager
import com.hostelmanagement.app.core.datastore.SessionState
import com.hostelmanagement.app.ui.auth.LoginScreen
import com.hostelmanagement.app.ui.auth.OwnerInitScreen
import com.hostelmanagement.app.ui.components.LoadingView

const val ROUTE_OWNER_ROOT = "owner_root"
const val ROUTE_TENANT_ROOT = "tenant_root"

@Composable
fun AppNavGraph(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle(initialValue = SessionState.Loading)

    // Wait until DataStore loads the session from disk
    if (sessionState is SessionState.Loading) {
        LoadingView(
            modifier = Modifier.fillMaxSize(),
            message = "Initializing application session..."
        )
        return
    }

    val startDestination = when (val state = sessionState) {
        is SessionState.Authenticated -> {
            when {
                state.session.isOwner -> ROUTE_OWNER_ROOT
                state.session.isTenant -> ROUTE_TENANT_ROOT
                else -> Screen.Login.route
            }
        }
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication Flow
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role.equals("OWNER", ignoreCase = true)) ROUTE_OWNER_ROOT else ROUTE_TENANT_ROOT
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOwnerInit = {
                    navController.navigate(Screen.OwnerInit.route)
                }
            )
        }

        composable(Screen.OwnerInit.route) {
            OwnerInitScreen(
                onNavigateBack = { navController.popBackStack() },
                onInitSuccess = {
                    navController.navigate(ROUTE_OWNER_ROOT) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Owner Root Flow
        composable(ROUTE_OWNER_ROOT) {
            OwnerNavHost(
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Tenant Root Flow
        composable(ROUTE_TENANT_ROOT) {
            TenantNavHost(
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
