# TiDB Cloud Starter - Backup, Recovery & Disaster Runbook

## 1. Overview
The Hostel Management System relies on **TiDB Cloud Starter**, a distributed, MySQL-compatible relational database with native high availability, Raft consensus replication, and automated multi-zone replication.

---

## 2. Automated Backups (TiDB Cloud Console)
TiDB Cloud Starter provides automated daily physical snapshots.
1. Log in to the [TiDB Cloud Console](https://tidbcloud.com/).
2. Select the cluster hosting `hostel_management`.
3. Navigate to **Backup & Restore**.
4. TiDB maintains automated daily snapshots with point-in-time restore capability.
5. To take an on-demand snapshot before running significant production migrations:
   - Click **Manual Backup**.
   - Label the snapshot (e.g., `pre-m2-deployment-20261110`).
   - Execution is non-blocking and does not lock tables.

---

## 3. Logical Backups (Dumpling & mysqldump)

### A. Recommended: Dumpling (TiDB Official Tool)
Dumpling is optimized for TiDB's distributed architecture and extracts consistent snapshot data without table locks.

```bash
# Dump all tables in hostel_management to local directory
dumpling \
  --host ${TIDB_HOST} \
  --port ${TIDB_PORT} \
  --user ${TIDB_USER} \
  --password ${TIDB_PASSWORD} \
  --database hostel_management \
  --filetype sql \
  --threads 4 \
  --output ./tidb_backup_$(date +%Y%m%d_%H%M%S) \
  --ca /etc/ssl/certs/ca-certificates.crt
```

### B. Alternative: Standard mysqldump
If Dumpling is not installed on the administrator machine, standard `mysqldump` can be used:

```bash
mysqldump \
  --host=${TIDB_HOST} \
  --port=${TIDB_PORT} \
  --user=${TIDB_USER} \
  --password=${TIDB_PASSWORD} \
  --ssl-mode=REQUIRED \
  --single-transaction \
  --routines \
  --triggers \
  hostel_management > backup_hostel_management_$(date +%Y%m%d_%H%M%S).sql
```

---

## 4. Disaster Recovery & Restoration

### Restoring from a Logical SQL Dump
To restore into a clean or disaster-recovery TiDB cluster:

```bash
# 1. Connect to MySQL client
mysql -h ${TIDB_HOST} -P ${TIDB_PORT} -u ${TIDB_USER} -p --ssl-mode=REQUIRED

# 2. Re-create database if necessary
CREATE DATABASE IF NOT EXISTS hostel_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostel_management;

# 3. Source the backup file
SOURCE /path/to/backup_hostel_management.sql;
```

---

## 5. Alembic Database Migration Management

### Inspecting Current Migration Revision
```bash
cd backend
python -m alembic current
```
*Current verified head*: `c83f12a7d901 (head)`

### Applying Pending Migrations in Production
```bash
cd backend
python -m alembic upgrade head
```

### Rolling Back the Most Recent Migration
```bash
cd backend
python -m alembic downgrade -1
```

---

## 6. High Availability & Failover Architecture
- TiDB separates computing (TiDB Server) from storage (TiKV).
- TiKV uses the **Raft consensus algorithm** across 3 replicas. If a storage node goes offline, leader election is immediate and transparent (< 3 seconds) with zero data loss.
- PyMySQL connection pool utilizes:
  - `pool_recycle=300` (recycles connections before cloud gateway timeouts).
  - `pool_pre_ping=True` (verifies connection liveness before handing out sessions).
  - Explicit TLS with system CA bundle for encrypted transit.
