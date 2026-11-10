from sqlalchemy import inspect, text
from app.db.session import engine

EXPECTED_TABLES = {
    "users",
    "rooms",
    "tenant_stays",
    "rent_charges",
    "payments",
    "payment_qr",
    "complaints",
    "vacate_requests",
    "notifications",
    "device_tokens",
    "audit_logs",
    "hostel_settings",
}

def test_all_tables_exist():
    inspector = inspect(engine)
    actual = set(inspector.get_table_names())

    missing = EXPECTED_TABLES - actual

    assert not missing, f"Missing tables: {missing}"

    print(f"\nPASS: All {len(EXPECTED_TABLES)} application tables exist.")

def test_rent_and_payment_decimal_precision():
    inspector = inspect(engine)

    for table, column in [
        ("tenant_stays", "monthly_rent"),
        ("rent_charges", "monthly_rent_snapshot"),
        ("rent_charges", "amount_due"),
        ("rent_charges", "amount_paid"),
        ("payments", "amount"),
    ]:
        columns = {
            item["name"]: item
            for item in inspector.get_columns(table)
        }

        assert column in columns, f"{table}.{column} missing"

        sql_type = columns[column]["type"]

        assert sql_type.precision == 10, (
            f"{table}.{column}: incorrect precision {sql_type.precision}"
        )
        assert sql_type.scale == 2, (
            f"{table}.{column}: incorrect scale {sql_type.scale}"
        )

    print("PASS: All monetary columns use DECIMAL(10,2).")

def test_payment_utr_unique():
    inspector = inspect(engine)

    indexes = inspector.get_indexes("payments")
    unique_constraints = inspector.get_unique_constraints("payments")

    unique_utr_index = any(
        index.get("unique")
        and index.get("column_names") == ["utr"]
        for index in indexes
    )

    unique_utr_constraint = any(
        constraint.get("column_names") == ["utr"]
        for constraint in unique_constraints
    )

    assert unique_utr_index or unique_utr_constraint, (
        "UTR unique constraint is missing"
    )

    print("PASS: Payment UTR is unique.")

def test_foreign_keys():
    inspector = inspect(engine)

    expected = {
        "tenant_stays": {"users", "rooms"},
        "rent_charges": {"tenant_stays"},
        "payments": {"rent_charges", "users"},
        "complaints": {"tenant_stays"},
        "vacate_requests": {"tenant_stays", "users"},
        "notifications": {"users"},
        "device_tokens": {"users"},
        "payment_qr": {"users"},
        "hostel_settings": {"users"},
        "audit_logs": {"users"},
    }

    for table, expected_targets in expected.items():
        actual_targets = {
            fk["referred_table"]
            for fk in inspector.get_foreign_keys(table)
        }

        missing = expected_targets - actual_targets
        assert not missing, f"{table}: missing foreign keys to {missing}"

    print("PASS: Expected foreign-key relationships exist.")

def test_rent_constraints():
    with engine.connect() as connection:
        result = connection.execute(
            text("SHOW CREATE TABLE rent_charges")
        ).fetchone()

        create_sql = result[1]

    required = [
        "ck_rent_no_overpayment",
        "ck_rent_billable_within_month",
        "ck_rent_valid_period",
        "uq_rent_stay_billing_period",
    ]

    for constraint in required:
        assert constraint in create_sql, (
            f"Missing rent constraint: {constraint}"
        )

    print("PASS: Rent constraints exist in TiDB.")

def test_room_capacity_constraint():
    with engine.connect() as connection:
        result = connection.execute(
            text("SHOW CREATE TABLE rooms")
        ).fetchone()

    assert "ck_rooms_capacity_positive" in result[1]

    print("PASS: Room capacity constraint exists.")

if __name__ == "__main__":
    tests = [
        test_all_tables_exist,
        test_rent_and_payment_decimal_precision,
        test_payment_utr_unique,
        test_foreign_keys,
        test_rent_constraints,
        test_room_capacity_constraint,
    ]

    for test in tests:
        test()

    print("\nDATABASE SCHEMA AUDIT PASSED.")
