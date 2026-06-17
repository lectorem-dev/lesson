from collections.abc import Callable
from datetime import datetime, timedelta
import uuid

from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.common.exceptions import AccessDeniedException, ArchivedUserException, AuthRequiredException
from app.core.config import settings
from app.db.session import get_db
from app.users.models import User, UserRole, UserStatus
from app.users.repository import get_user_by_id


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
bearer_scheme = HTTPBearer(auto_error=False)
ALGORITHM = "HS256"


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    return pwd_context.verify(password, password_hash)


def create_access_token(user: User) -> str:
    now = datetime.utcnow()
    expires_at = now + timedelta(minutes=settings.jwt_expiration_minutes)
    # В JWT кладем только поля, нужные для быстрой авторизации без серверной сессии.
    payload = {
        "sub": user.login,
        "userId": str(user.id),
        "login": user.login,
        "role": user.role,
        "iat": now,
        "exp": expires_at,
    }
    return jwt.encode(payload, settings.jwt_secret, algorithm=ALGORITHM)


def parse_access_token(token: str) -> dict[str, str]:
    try:
        # Здесь одновременно проверяются подпись JWT и срок действия токена.
        payload = jwt.decode(token, settings.jwt_secret, algorithms=[ALGORITHM])
        user_id = str(uuid.UUID(str(payload["userId"])))
        role = str(payload["role"])
        login = str(payload["login"])
    except (KeyError, ValueError, JWTError):
        raise AuthRequiredException() from None

    return {"userId": user_id, "login": login, "role": role}


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
    db: Session = Depends(get_db),
) -> User:
    # FastAPI-зависимость заменяет Spring Security filter: достает Bearer JWT и загружает активного пользователя.
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise AuthRequiredException()

    token_data = parse_access_token(credentials.credentials)
    user = get_user_by_id(db, uuid.UUID(token_data["userId"]))
    if user is None or user.status != UserStatus.ACTIVE.value:
        raise AuthRequiredException()

    return user


def require_roles(*roles: UserRole) -> Callable[[User], User]:
    allowed_roles = {role.value for role in roles}

    def dependency(current_user: User = Depends(get_current_user)) -> User:
        # Проверка ролей вынесена в зависимость, чтобы правила доступа были видны рядом с роутерами.
        if current_user.status == UserStatus.ARCHIVED.value:
            raise ArchivedUserException()
        if current_user.role not in allowed_roles:
            raise AccessDeniedException()
        return current_user

    return dependency
