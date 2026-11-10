from alembic import op

revision = "ff04920ce5d6"
down_revision = "381da0b92060"
branch_labels = None
depends_on = None


CONSTRAINTS = [
    ("rooms", "ck_rooms_capacity_positive", "capacity > 0"),

    ("users", "ck_users_role", "role IN ('OWNER', 'TENANT')"),

    ("tenant_stays", "ck_stays_rent_nonnegative", "monthly_rent >= 0"),
    ("tenant_stays", "ck_stays_status", "status IN ('ACTIVE', 'COMPLETED')"),
    ("tenant_stays", "ck_stays_vacating_date",
     "approved_vacating_date IS NULL OR approved_vacating_date >= joining_date"),

    ("rent_charges", "ck_rent_billing_month", "billing_month BETWEEN 1 AND 12"),
    ("rent_charges", "ck_rent_days_in_month", "days_in_month BETWEEN 28 AND 31"),
    ("rent_charges", "ck_rent_billable_days", "billable_days BETWEEN 0 AND 31"),
    ("rent_charges", "ck_rent_snapshot_nonnegative", "monthly_rent_snapshot >= 0"),
    ("rent_charges", "ck_rent_due_nonnegative", "amount_due >= 0"),
    ("rent_charges", "ck_rent_paid_nonnegative", "amount_paid >= 0"),
    ("rent_charges", "ck_rent_status",
     "status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')"),
    ("rent_charges", "ck_rent_no_overpayment", "amount_paid <= amount_due"),
    ("rent_charges", "ck_rent_billable_within_month", "billable_days <= days_in_month"),
    ("rent_charges", "ck_rent_valid_period", "period_start <= period_end"),

    ("payments", "ck_payments_amount_positive", "amount > 0"),
    ("payments", "ck_payments_status",
     "status IN ('PENDING', 'APPROVED', 'REJECTED')"),

    ("device_tokens", "ck_device_platform", "platform IN ('ANDROID')"),

    ("hostel_settings", "ck_hostel_singleton", "id = 1"),
    ("hostel_settings", "ck_hostel_due_day", "rent_due_day BETWEEN 1 AND 28"),
    ("hostel_settings", "ck_hostel_currency", "currency = 'INR'"),

    ("complaints", "ck_complaints_status", "status IN ('OPEN', 'IN PROGRESS')"),

    ("vacate_requests", "ck_vacate_status",
     "status IN ('PENDING', 'APPROVED', 'REJECTED')"),
]


def upgrade():
    for table, name, condition in CONSTRAINTS:
        op.create_check_constraint(name, table, condition)


def downgrade():
    for table, name, condition in reversed(CONSTRAINTS):
        op.drop_constraint(name, table, type_="check")
