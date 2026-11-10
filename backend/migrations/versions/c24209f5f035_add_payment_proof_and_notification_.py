"""add payment proof and notification category

Revision ID: c24209f5f035
Revises: c83f12a7d901
Create Date: 2026-11-10 16:39:40.780362

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'c24209f5f035'
down_revision: Union[str, Sequence[str], None] = 'c83f12a7d901'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column('payments', sa.Column('proof_image_path', sa.String(length=500), nullable=True))
    op.add_column('notifications', sa.Column('category', sa.String(length=50), server_default=sa.text("'GENERAL'"), nullable=False))
    op.execute("UPDATE notifications SET category = 'PAYMENT' WHERE notification_type IN ('PAYMENT_APPROVED', 'PAYMENT_REJECTED', 'PAYMENT_SUBMITTED', 'RENT_BILL')")
    op.execute("UPDATE notifications SET category = 'REMINDER' WHERE notification_type IN ('RENT_DUE_REMINDER')")
    op.execute("UPDATE notifications SET category = 'ALERT' WHERE notification_type IN ('RENT_OVERDUE', 'VACATE_REQUEST')")


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_column('notifications', 'category')
    op.drop_column('payments', 'proof_image_path')
