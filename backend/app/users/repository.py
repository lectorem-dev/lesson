from uuid import UUID

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.course import models as course_models
from app.progress import models as progress_models
from app.users.models import User

_ = (course_models, progress_models)


def get_users(db: Session) -> list[User]:
    return list(db.scalars(select(User).order_by(User.created_at.asc())))


def get_user_by_id(db: Session, user_id: UUID) -> User | None:
    return db.get(User, user_id)


def get_user_by_login(db: Session, login: str) -> User | None:
    return db.scalar(select(User).where(User.login == login))


def exists_by_login(db: Session, login: str) -> bool:
    return db.scalar(select(User.id).where(User.login == login).limit(1)) is not None


def exists_by_login_except_user(db: Session, login: str, user_id: UUID) -> bool:
    return db.scalar(select(User.id).where(User.login == login, User.id != user_id).limit(1)) is not None
