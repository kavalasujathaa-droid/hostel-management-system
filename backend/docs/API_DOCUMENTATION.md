# Hostel Management System - Complete API Reference (v1)

Base URL: `/api/v1`  
Interactive Swagger UI: `/docs`  
ReDoc UI: `/redoc`

---

## 1. System Health & Probes
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/health/live` | Public | Kubernetes/container liveness probe (200 OK) |
| `GET` | `/health/ready` | Public | TiDB readiness probe via `SELECT 1` (200 OK or 503 Service Unavailable) |

---

## 2. Authentication & Session Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/bootstrap-owner` | Public (1-time) | One-time atomic bootstrap of hostel owner and settings |
| `POST` | `/auth/register-tenant` | Owner only | Account creation for a verified hostel tenant |
| `POST` | `/auth/login` | Public | Authenticate with email/password; returns access & refresh tokens |
| `POST` | `/auth/refresh` | Public | Rotate refresh token; revokes old token and issues fresh pair |
| `POST` | `/auth/logout` | Authenticated | Revoke refresh session and invalidate current tokens |
| `POST` | `/auth/change-password` | Authenticated | Change current user password with Argon2id re-hashing |

---

## 3. Users & Profiles
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/users/me` | Authenticated | Retrieve current user profile and role |
| `PUT` | `/users/me` | Authenticated | Update user profile details (name, phone) |
| `GET` | `/users` | Owner only | List all hostel users with pagination & role filtering |
| `PUT` | `/users/{id}/status` | Owner only | Activate or deactivate user account |

---

## 4. Room Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/rooms` | Authenticated | List all rooms with current occupancy and vacancy metrics |
| `POST` | `/rooms` | Owner only | Create a new room with capacity and monthly rent |
| `GET` | `/rooms/{id}` | Authenticated | Retrieve room details and occupants |
| `PUT` | `/rooms/{id}` | Owner only | Update room details, capacity, or rent rate |
| `DELETE` | `/rooms/{id}` | Owner only | Delete unoccupied room |

---

## 5. Tenant Stays & Assignments
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/stays/assign` | Owner only | Atomically assign tenant to room with row locking |
| `GET` | `/stays/my-stays` | Tenant only | Retrieve logged-in tenant's current and historical stays |
| `GET` | `/stays/tenant/{tenant_id}` | Owner only | Retrieve all stay history for a specific tenant |
| `GET` | `/stays/active` | Owner only | Retrieve all currently active stays across the hostel |
| `PUT` | `/stays/{id}/vacate` | Owner only | Mark stay as vacated, recording exact end date |

---

## 6. Rent Calculation & Monthly Billing
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/billing/generate` | Owner only | Generate monthly rent charges with exact Decimal proration |
| `GET` | `/billing/my-bills` | Tenant only | Retrieve current tenant's rent bills and balance |
| `GET` | `/billing/all` | Owner only | Retrieve all generated rent charges across the hostel |
| `GET` | `/billing/{id}` | Authenticated | Retrieve specific rent charge details |

---

## 7. Payments & UTR Verification
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/payments/submit` | Tenant only | Tenant submits payment amount and unique bank UTR |
| `GET` | `/payments/my-payments` | Tenant only | Retrieve tenant's submitted payment history |
| `GET` | `/payments/pending` | Owner only | Retrieve all unverified payments requiring manual review |
| `GET` | `/payments/all` | Owner only | Retrieve complete payment log across all statuses |
| `POST` | `/payments/{id}/approve` | Owner only | Atomically approve payment and deduct rent charge balance |
| `POST` | `/payments/{id}/reject` | Owner only | Reject invalid payment with rejection reason |

---

## 8. UPI QR Code Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/payment-qr/upload` | Owner only | Upload new UPI QR image and make active |
| `GET` | `/payment-qr/active` | Authenticated | Retrieve currently active UPI QR code for payments |
| `GET` | `/payment-qr/history` | Owner only | Retrieve history of all uploaded QR codes |
| `DELETE` | `/payment-qr/{id}` | Owner only | Delete QR code record |

---

## 9. Complaints Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/complaints/submit` | Tenant only | Submit a new complaint (plumbing, electrical, etc.) |
| `GET` | `/complaints/my` | Tenant only | Retrieve logged-in tenant's complaints |
| `GET` | `/complaints/all` | Owner only | Retrieve all complaints with status filtering |
| `PUT` | `/complaints/{id}/status` | Owner only | Update complaint status (`OPEN`, `IN PROGRESS`, `RESOLVED`) |

---

## 10. Vacate Requests
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/vacate-requests/submit` | Tenant only | Submit formal notice to vacate on expected date |
| `GET` | `/vacate-requests/my` | Tenant only | Retrieve tenant's vacate requests |
| `GET` | `/vacate-requests/all` | Owner only | Retrieve all pending/historical vacate notices |
| `PUT` | `/vacate-requests/{id}/review` | Owner only | Approve or reject notice, finalizing move-out date |

---

## 11. Notifications & Device Push Tokens
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/notifications/my` | Authenticated | List user notifications with unread count |
| `PUT` | `/notifications/{id}/read` | Authenticated | Mark notification as read |
| `POST` | `/notifications/device-token` | Authenticated | Register/refresh FCM device token for Android app |
| `DELETE` | `/notifications/device-token` | Authenticated | Deregister device push token on logout |

---

## 12. Financial Reports & Dashboard
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/reports/dashboard` | Owner only | Real-time overview: occupancy, revenue, pending rent, pending UTRs |
| `GET` | `/reports/financial` | Owner only | Monthly / annual financial statements with revenue and collections |

---

## 13. System Audit Log
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/audit/logs` | Owner only | Immutable audit trail of administrative & financial actions |
