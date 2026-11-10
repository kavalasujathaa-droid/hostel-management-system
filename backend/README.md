# Hostel Management System — Backend

Production-grade, secure, and fully verified backend for the Android Hostel Management System built with **Python 3.11**, **FastAPI**, **SQLAlchemy 2.x**, **TiDB Cloud Starter** (MySQL-compatible distributed SQL), **Argon2id**, **PyJWT**, **Gunicorn**, **Docker**, and **Caddy** (Automated HTTPS).

---

## 1. System Architecture Overview

```
backend/
├── app/
│   ├── main.py                  # FastAPI application factory, CORS, static uploads, security headers, lifespan
│   ├── core/
│   │   ├── config.py            # Centralized Pydantic Settings (TiDB, JWT, CORS, App configuration)
│   │   ├── dependencies.py      # RBAC dependencies (get_current_user, require_owner, require_tenant)
│   │   ├── exceptions.py        # Custom application exceptions and uniform JSON error handlers
│   │   ├── logging.py           # Centralized structured logging with sensitive data sanitization
│   │   └── security.py          # Argon2id password hashing, JWT signing, refresh token rotation
│   ├── db/
│   │   ├── base.py              # SQLAlchemy 2.x Declarative Base
│   │   └── session.py           # Engine with TLS/SSL, connection pooling, sessionmaker, and safe ping
│   ├── api/
│   │   ├── router.py            # Root API router mounting /api/v1
│   │   └── v1/
│   │       ├── router.py        # Version 1 router mounting 13 endpoint modules
│   │       └── endpoints/       # 48 REST API endpoints across 13 domains
│   │           ├── audit.py             # System audit logs (Owner only)
│   │           ├── auth.py              # Login, token refresh, logout, password change, bootstrap
│   │           ├── billing.py           # Monthly rent generation & charge retrieval
│   │           ├── complaints.py        # Tenant complaints submission and status tracking
│   │           ├── health.py            # Liveness (/health/live) and Readiness (/health/ready) probes
│   │           ├── notifications.py     # In-app notifications & device push token registration
│   │           ├── payment_qr.py        # UPI QR upload, retrieval, and lifecycle management
│   │           ├── payments.py          # Payment submissions, UTR checks, approval & rejection
│   │           ├── reports.py           # Owner analytics dashboard & monthly financial statements
│   │           ├── rooms.py             # Room CRUD, capacity management & occupancy rates
│   │           ├── stays.py             # Tenant room assignments & stay histories
│   │           ├── users.py             # User profile, directory, and status management
│   │           └── vacate.py            # Vacate notice workflow & review
│   ├── models/
│   │   └── models.py            # 13 SQLAlchemy models (12 core + refresh_tokens)
│   ├── schemas/                 # Pydantic v2 validation models for all request/responses
│   ├── services/                # Pure business logic and database transaction services
│   └── utils/                   # Common helper utilities
├── docs/
│   ├── API_DOCUMENTATION.md     # Complete REST API reference table
│   └── TIDB_BACKUP_AND_RECOVERY.md # TiDB Cloud backup and disaster recovery runbook
├── migrations/                  # Alembic database migrations (current revision: c83f12a7d901)
├── tests/                       # 38 passing automated tests (100% TiDB verified)
├── uploads/qr/                  # Persistent storage directory for active UPI QR code images
├── .env.example                 # Safe placeholder environment template
├── alembic.ini                  # Alembic migration configuration
├── Dockerfile                   # Multi-stage production container with non-root user
├── gunicorn_conf.py             # Production ASGI multi-worker configuration
└── requirements.txt             # Pinned production dependencies
```

---

## 2. Technology Stack & Key Choices

- **Language & Runtime**: Python 3.11.9
- **API Framework**: FastAPI 0.115+
- **Database Engine**: TiDB Cloud Starter (Distributed MySQL-compatible SQL)
- **Database Driver**: PyMySQL + Cryptography with TLS/SSL verification
- **ORM**: SQLAlchemy 2.0+ (Strict transactional safety with `with_for_update()` row locks)
- **Database Migrations**: Alembic (`c83f12a7d901 (head)`)
- **Password Hashing**: Argon2id via `argon2-cffi` (`time_cost=3`, `memory_cost=65536`, `parallelism=4`)
- **Token Security**: Dual-token architecture (Stateless HMAC-SHA256 JWT access tokens + Database-backed SHA-256 hashed refresh tokens with rotation and replay attack mitigation)
- **Rent Precision**: Python `decimal.Decimal` with `ROUND_HALF_UP` and ₹0.01 quantization (Zero floating-point arithmetic)
- **ASGI Production Server**: Gunicorn 23.0+ with `uvicorn.workers.UvicornWorker`
- **Reverse Proxy & HTTPS**: Caddy 2.8+ (Automated TLS via Let's Encrypt / ZeroSSL)

---

## 3. Quick Start (Development)

### 3.1 Prerequisites
- Python 3.11+
- Virtualenv
- Active TiDB Cloud Starter cluster credentials

### 3.2 Installation
```bash
cd backend
python -m venv .venv

# On Windows:
.\.venv\Scripts\Activate.ps1

# On Linux/macOS:
source .venv/bin/activate

pip install --upgrade pip
pip install -r requirements.txt
```

### 3.3 Environment Setup
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Fill in your TiDB connection parameters and secrets in `.env`.

### 3.4 Database Migrations
Ensure the database schema is at the latest revision:
```bash
python -m alembic upgrade head
```

### 3.5 Run Development Server
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
API Documentation:
- **Swagger UI**: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
- **ReDoc**: [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

---

## 4. Production Deployment

### Option A: Docker Compose & Caddy (Recommended)
This method provisions the FastAPI backend container (non-root `appuser`) and a Caddy reverse proxy that automatically handles Let's Encrypt TLS certificates:

1. Configure production environment in `backend/.env` (set `DOMAIN=api.yourhostel.com` and `ACME_EMAIL=admin@yourhostel.com`).
2. Run database migrations:
   ```bash
   cd backend && python -m alembic upgrade head
   ```
3. Start the production stack from the repository root:
   ```bash
   docker compose up -d --build
   ```
4. Verify running services:
   ```bash
   docker compose ps
   curl -f https://api.yourhostel.com/api/v1/health/live
   ```

### Option B: Bare-Metal / Systemd Linux Host
1. Install Python 3.11 and create a virtual environment in `/opt/hostel-backend`.
2. Install dependencies: `pip install -r requirements.txt`.
3. Create `/etc/systemd/system/hostel-backend.service`:
   ```ini
   [Unit]
   Description=Hostel Management API
   After=network.target

   [Service]
   User=www-data
   Group=www-data
   WorkingDirectory=/opt/hostel-backend
   EnvironmentFile=/opt/hostel-backend/.env
   ExecStart=/opt/hostel-backend/.venv/bin/gunicorn -c gunicorn_conf.py app.main:app
   Restart=always
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```
4. Enable and start:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now hostel-backend
   ```

### Option C: Render Cloud Deployment
1. Connect your repository to Render.
2. Render detects `render.yaml` at the root and provisions:
   - Web Service running `gunicorn -c gunicorn_conf.py app.main:app`
   - Persistent Disk mounted to `/app/uploads/qr` for QR codes
   - Automated zero-downtime deploys and health checks (`/api/v1/health/live`)
3. Add your `TIDB_*` environment variables in the Render Dashboard.

---

## 5. Automated Verification & Testing

The test suite covers 38 automated tests executing against the live TiDB Cloud database:

```bash
# Run all tests
cd backend
python -m pytest tests -v
```

### Test Coverage Highlights:
- **`test_auth.py`**: Argon2id hashing, owner bootstrap, login success/failure, JWT access tokens, database-backed refresh token rotation & replay attack detection, logout & password changes.
- **`test_rooms.py`**: Room CRUD, capacity validation, occupancy metrics, `with_for_update()` concurrency locking.
- **`test_billing.py`**: Exact Decimal rent calculations across 28, 29, 30, and 31-day months, leap year handling, bill generation, duplicate bill prevention.
- **`test_payments.py`**: Tenant payment submissions, unique UTR constraint, owner approval/rejection lifecycle, partial payments, overpayment rejection.
- **`test_concurrency_and_qr.py`**: Multithreaded concurrent room assignment over-booking prevention, multithreaded concurrent payment approval locking, UPI QR upload and image persistence.
- **`test_complaints_vacate.py`**: Complaint submission, lifecycle transitions, vacate request workflow and end-date updates.
- **`test_notifications_reports.py`**: In-app notifications, device token registration, financial report totals and dashboard accuracy.
- **`test_rbac.py`**: Unauthenticated requests blocked (401), tenant access to owner endpoints forbidden (403), owner allowed.
- **`test_health.py`**: Startup verification, `/health/live`, `/health/ready` success, simulated database outage 503 handling, security headers (`nosniff`, `DENY`), static file mounting.
- **`test_database_audit.py`**: Schema integrity, check constraints, foreign keys, decimal precision across all 13 tables.

---

## 6. Security Features

1. **Password Safety**: Argon2id with memory cost 64MB and time cost 3. Plaintext passwords never logged or returned.
2. **Token Rotation & Replay Attack Defense**: Refresh tokens are stored as SHA-256 hashes in `refresh_tokens`. Attempting to reuse an already-rotated token immediately revokes all sessions for that user (RFC 6819).
3. **Owner Bootstrapping**: One-time bootstrap endpoint atomically commits hostel settings and owner account. Subsequent calls are permanently rejected with HTTP 409 Conflict.
4. **Row-Level Transaction Safety**: Room assignments and payment approvals use SQLAlchemy `with_for_update()` pessimistic row locks to prevent race conditions and overbooking.
5. **Exact Decimal Monetary Arithmetic**: Zero floating-point arithmetic is used for financial calculations; all rents, balances, and charges use Python `Decimal` quantized to ₹0.01.
6. **Zero Secret Leakage**: Database passwords, JWT keys, and credentials are kept strictly in `.env`. Error handlers strip SQL and internal exception traces before sending JSON responses to clients.
7. **Security Headers**: All API responses automatically include `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `X-XSS-Protection: 1; mode=block`, and `Referrer-Policy: strict-origin-when-cross-origin`.
