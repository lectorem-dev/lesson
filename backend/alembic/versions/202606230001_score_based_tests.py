"""score based tests

Revision ID: 202606230001
Revises: 202606170001
Create Date: 2026-06-23 00:00:00.000000
"""

from alembic import op
import sqlalchemy as sa


revision = "202606230001"
down_revision = "202606170001"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.add_column("test_questions", sa.Column("points", sa.Integer(), nullable=False, server_default="1"))
    op.add_column("lesson_pass_rules", sa.Column("pass_score", sa.Integer(), nullable=False, server_default="1"))
    op.create_check_constraint("chk_test_questions_points", "test_questions", "points >= 1")
    op.create_check_constraint("chk_lesson_pass_rules_pass_score", "lesson_pass_rules", "pass_score >= 1")
    op.alter_column("test_questions", "points", server_default=None)
    op.alter_column("lesson_pass_rules", "pass_score", server_default=None)


def downgrade() -> None:
    op.drop_constraint("chk_lesson_pass_rules_pass_score", "lesson_pass_rules", type_="check")
    op.drop_constraint("chk_test_questions_points", "test_questions", type_="check")
    op.drop_column("lesson_pass_rules", "pass_score")
    op.drop_column("test_questions", "points")
