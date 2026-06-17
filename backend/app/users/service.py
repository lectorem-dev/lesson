from uuid import UUID

from sqlalchemy.orm import Session

from app.common.exceptions import LoginAlreadyExistsException, UserNotFoundException
from app.core.config import settings
from app.core.security import hash_password
from app.users import repository
from app.users.models import User, UserRole, UserStatus
from app.users.schemas import UserCreateRequest, UserResponse, UserUpdateRequest


def to_user_response(user: User) -> UserResponse:
    return UserResponse.model_validate(user)


def find_all_users(db: Session) -> list[UserResponse]:
    return [to_user_response(user) for user in repository.get_users(db)]


def find_user_by_id(db: Session, user_id: UUID) -> UserResponse:
    user = repository.get_user_by_id(db, user_id)
    if user is None:
        raise UserNotFoundException()
    return to_user_response(user)


def create_user(db: Session, request: UserCreateRequest) -> UserResponse:
    if repository.exists_by_login(db, request.login):
        raise LoginAlreadyExistsException()

    user = User(
        name=request.name,
        login=request.login,
        password_hash=hash_password(request.password),
        role=to_enum_value(request.role),
        status=UserStatus.ACTIVE.value,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return to_user_response(user)


def update_user(db: Session, user_id: UUID, request: UserUpdateRequest) -> UserResponse:
    user = repository.get_user_by_id(db, user_id)
    if user is None:
        raise UserNotFoundException()
    if repository.exists_by_login_except_user(db, request.login, user_id):
        raise LoginAlreadyExistsException()

    user.name = request.name
    user.login = request.login
    user.role = to_enum_value(request.role)
    user.status = to_enum_value(request.status)
    if request.password is not None and request.password.strip():
        user.password_hash = hash_password(request.password)

    db.commit()
    db.refresh(user)
    return to_user_response(user)


def archive_user(db: Session, user_id: UUID) -> None:
    user = repository.get_user_by_id(db, user_id)
    if user is None:
        raise UserNotFoundException()

    user.status = UserStatus.ARCHIVED.value
    db.commit()


def seed_admin(db: Session) -> None:
    admin = repository.get_user_by_login(db, settings.admin_login)
    if admin is None:
        admin = User(login=settings.admin_login)
        db.add(admin)

    admin.name = "Администратор"
    admin.login = settings.admin_login
    admin.password_hash = hash_password(settings.admin_password)
    admin.role = "ADMIN"
    admin.status = UserStatus.ACTIVE.value


def to_enum_value(value: UserRole | UserStatus | str) -> str:
    return value.value if hasattr(value, "value") else value
