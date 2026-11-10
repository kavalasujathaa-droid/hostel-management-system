"""upgrade complaints portal with title category attachment and resolved status

Revision ID: 087c109209dd
Revises: c24209f5f035
Create Date: 2026-11-10 17:15:21.646753

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '087c109209dd'
down_revision: Union[str, Sequence[str], None] = 'c24209f5f035'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column('complaints', sa.Column('title', sa.String(length=150), server_default=sa.text("''"), nullable=False))
    op.add_column('complaints', sa.Column('category', sa.String(length=50), server_default=sa.text("'Other'"), nullable=False))
    op.add_column('complaints', sa.Column('attachment_path', sa.String(length=500), nullable=True))

    try:
        op.drop_constraint('ck_complaints_status', 'complaints', type_='check')
    except Exception:
        pass

    op.create_check_constraint(
        'ck_complaints_status',
        'complaints',
        "status IN ('OPEN', 'IN PROGRESS', 'RESOLVED')"
    )
    op.create_check_constraint(
        'ck_complaints_category',
        'complaints',
        "category IN ('Plumbing', 'Electrical', 'Cleaning', 'Maintenance', 'Other')"
    )


def downgrade() -> None:
    """Downgrade schema."""
    try:
        op.drop_constraint('ck_complaints_category', 'complaints', type_='check')
        op.drop_constraint('ck_complaints_status', 'complaints', type_='check')
    except Exception:
        pass

    op.create_check_constraint(
        'ck_complaints_status',
        'complaints',
        "status IN ('OPEN', 'IN PROGRESS')"
    )
    op.drop_column('complaints', 'attachment_path')
    op.drop_column('complaints', 'category')
    op.drop_column('complaints', 'title')
