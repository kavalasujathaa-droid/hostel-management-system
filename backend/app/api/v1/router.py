from fastapi import APIRouter
from app.api.v1.endpoints import (
    auth,
    billing,
    complaints,
    health,
    notifications,
    payment_qr,
    payments,
    reports,
    rooms,
    stays,
    tenants,
    users,
    vacate,
)

api_v1_router = APIRouter()

api_v1_router.include_router(health.router, prefix="/health", tags=["Health"])
api_v1_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_v1_router.include_router(users.router, prefix="/users", tags=["Users"])
api_v1_router.include_router(rooms.router, prefix="/rooms", tags=["Rooms"])
api_v1_router.include_router(tenants.router, prefix="/tenants", tags=["Tenants"])
api_v1_router.include_router(stays.router, prefix="/stays", tags=["Tenant Stays"])
api_v1_router.include_router(billing.router, prefix="/billing", tags=["Billing & Rent"])
api_v1_router.include_router(payments.router, prefix="/payments", tags=["Payments"])
api_v1_router.include_router(payment_qr.router, prefix="/payment-qr", tags=["Payment QR"])
api_v1_router.include_router(complaints.router, prefix="/complaints", tags=["Complaints"])
api_v1_router.include_router(vacate.router, prefix="/vacate-requests", tags=["Vacate Requests"])
api_v1_router.include_router(notifications.router, prefix="/notifications", tags=["Notifications"])
api_v1_router.include_router(reports.router, prefix="/reports", tags=["Reports & Analytics"])
