package com.hostelmanagement.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth Routes
    data object Login : Screen("login")
    data object OwnerInit : Screen("owner_init")

    // Owner Routes
    data object OwnerDashboard : Screen("owner_dashboard")
    data object OwnerRooms : Screen("owner_rooms")
    data object OwnerRoomDetail : Screen("owner_rooms/{roomId}") {
        fun createRoute(roomId: Long) = "owner_rooms/$roomId"
    }
    data object OwnerTenants : Screen("owner_tenants")
    data object OwnerTenantDetail : Screen("owner_tenants/{tenantId}") {
        fun createRoute(tenantId: Long) = "owner_tenants/$tenantId"
    }
    data object OwnerBilling : Screen("owner_billing")
    data object OwnerPayments : Screen("owner_payments")
    data object OwnerPaymentQr : Screen("owner_payment_qr")
    data object OwnerComplaints : Screen("owner_complaints")
    data object OwnerVacate : Screen("owner_vacate")
    data object OwnerReports : Screen("owner_reports")
    data object OwnerNotifications : Screen("owner_notifications")
    data object OwnerSettings : Screen("owner_settings")

    // Tenant Routes
    data object TenantDashboard : Screen("tenant_dashboard")
    data object TenantRentDetails : Screen("tenant_rent_details")
    data object TenantPay : Screen("tenant_pay/{rentChargeId}") {
        fun createRoute(rentChargeId: Long) = "tenant_pay/$rentChargeId"
    }
    data object TenantPayments : Screen("tenant_payments")
    data object TenantRoomInfo : Screen("tenant_room_info")
    data object TenantComplaints : Screen("tenant_complaints")
    data object TenantVacate : Screen("tenant_vacate")
    data object TenantNotifications : Screen("tenant_notifications")
    data object TenantProfile : Screen("tenant_profile")
}
