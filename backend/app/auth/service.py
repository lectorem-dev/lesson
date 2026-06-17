from sqlalchemy.orm import Session

from app.auth.schemas import LoginRequest, LoginResponse
from app.common.exceptions import ArchivedUserException, InvalidCredentialsException
from app.core.security import create_access_token, verify_password
from app.users.models import UserStatus
from app.users.repository import get_user_by_login


def login(db: Session, request: LoginRequest) -> LoginResponse:
    user = get_user_by_login(db, request.login)
    if user is None:
        raise InvalidCredentialsException()
    if user.status == UserStatus.ARCHIVED.value:
        raise ArchivedUserException()
    if not verify_password(request.password, user.password_hash):
        raise InvalidCredentialsException()

    return LoginResponse(
        token=create_access_token(user),
        userId=user.id,
        login=user.login,
        name=user.name,
        role=user.role,
    )
