from sqlalchemy import text
from app.db.session import engine


def test_tidb_connection():
    with engine.connect() as connection:
        result = connection.execute(
            text("SELECT VERSION(), DATABASE()")
        )

        row = result.fetchone()

        assert row is not None

        print("TiDB Version:", row[0])
        print("Database:", row[1])