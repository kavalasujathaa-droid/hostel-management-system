package com.hostelmanagement.app

import com.hostelmanagement.app.core.datastore.SessionState
import com.hostelmanagement.app.core.datastore.UserSession
import com.hostelmanagement.app.ui.navigation.ROUTE_OWNER_ROOT
import com.hostelmanagement.app.ui.navigation.ROUTE_TENANT_ROOT
import com.hostelmanagement.app.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRoutingTest {

    @Test
    fun sessionState_ownerSession_routesToOwnerRoot() {
        val ownerSession = UserSession(
            accessToken = "token_owner",
            refreshToken = "refresh_owner",
            userId = 1L,
            fullName = "Owner Name",
            email = "owner@example.com",
            role = "OWNER",
            isActive = true
        )
        val state: SessionState = SessionState.Authenticated(ownerSession)

        assertTrue(ownerSession.isOwner)
        assertFalse(ownerSession.isTenant)
        assertTrue(ownerSession.isLoggedIn)

        val destination = when (state) {
            is SessionState.Authenticated -> {
                when {
                    state.session.isOwner -> ROUTE_OWNER_ROOT
                    state.session.isTenant -> ROUTE_TENANT_ROOT
                    else -> Screen.Login.route
                }
            }
            else -> Screen.Login.route
        }

        assertEquals("owner_root", destination)
    }

    @Test
    fun sessionState_tenantSession_routesToTenantRoot() {
        val tenantSession = UserSession(
            accessToken = "token_tenant",
            refreshToken = "refresh_tenant",
            userId = 2L,
            fullName = "Tenant Name",
            email = "tenant@example.com",
            role = "TENANT",
            isActive = true
        )
        val state: SessionState = SessionState.Authenticated(tenantSession)

        assertFalse(tenantSession.isOwner)
        assertTrue(tenantSession.isTenant)
        assertTrue(tenantSession.isLoggedIn)

        val destination = when (state) {
            is SessionState.Authenticated -> {
                when {
                    state.session.isOwner -> ROUTE_OWNER_ROOT
                    state.session.isTenant -> ROUTE_TENANT_ROOT
                    else -> Screen.Login.route
                }
            }
            else -> Screen.Login.route
        }

        assertEquals("tenant_root", destination)
    }

    @Test
    fun sessionState_unauthenticated_routesToLogin() {
        val state: SessionState = SessionState.Unauthenticated

        val destination = when (state) {
            is SessionState.Authenticated -> {
                when {
                    state.session.isOwner -> ROUTE_OWNER_ROOT
                    state.session.isTenant -> ROUTE_TENANT_ROOT
                    else -> Screen.Login.route
                }
            }
            else -> Screen.Login.route
        }

        assertEquals("login", destination)
    }

    @Test
    fun userSession_emptyToken_isNotLoggedIn() {
        val emptySession = UserSession(
            accessToken = "",
            refreshToken = "",
            userId = 0L,
            fullName = "",
            email = "",
            role = "GUEST",
            isActive = false
        )

        assertFalse(emptySession.isLoggedIn)
    }

    @Test
    fun complaintRoutes_verifyExactRouteStrings() {
        assertEquals("tenant_complaints", Screen.TenantComplaints.route)
        assertEquals("tenant_raise_complaint", Screen.TenantRaiseComplaint.route)
        assertEquals("owner_complaints", Screen.OwnerComplaints.route)
    }
}
