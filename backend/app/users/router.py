from uuid import UUID

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.security import require_roles
from app.db.session import get_db
from app.users.models import User, UserRole
from app.users.schemas import UserCreateRequest, UserResponse, UserUpdateRequest
from app.users.service import archive_user, create_user, find_all_users, find_user_by_id, update_user


router = APIRouter(prefix="/api/admin/users", tags=["Пользователи"])


@router.get(
    "",
    response_model=list[UserResponse],
    summary="Получение списка пользователей",
    description="Возвращает всех пользователей платформы для административного раздела.",
)
def get_users(
    db: Session = Depends(get_db),
    _: User = Depends(require_roles(UserRole.ADMIN)),
) -> list[UserResponse]:
    return find_all_users(db)


@router.get(
    "/{id}",
    response_model=UserResponse,
    summary="Получение пользователя по идентификатору",
    description="Возвращает данные одного пользователя по UUID.",
)
def get_user(
    id: UUID,
    db: Session = Depends(get_db),
    _: User = Depends(require_roles(UserRole.ADMIN)),
) -> UserResponse:
    return find_user_by_id(db, id)


@router.post(
    "",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Создание пользователя администратором",
    description="Создает пользователя с ролью ADMIN или STUDENT и активным статусом.",
)
def create_admin_user(
    request: UserCreateRequest,
    db: Session = Depends(get_db),
    _: User = Depends(require_roles(UserRole.ADMIN)),
) -> UserResponse:
    return create_user(db, request)


@router.put(
    "/{id}",
    response_model=UserResponse,
    summary="Обновление пользователя",
    description="Обновляет имя, логин, роль, статус и при необходимости пароль пользователя.",
)
def update_admin_user(
    id: UUID,
    request: UserUpdateRequest,
    db: Session = Depends(get_db),
    _: User = Depends(require_roles(UserRole.ADMIN)),
) -> UserResponse:
    return update_user(db, id, request)


@router.delete(
    "/{id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Архивирование пользователя",
    description="Переводит пользователя в статус ARCHIVED без физического удаления записи.",
)
def delete_admin_user(
    id: UUID,
    db: Session = Depends(get_db),
    _: User = Depends(require_roles(UserRole.ADMIN)),
) -> None:
    archive_user(db, id)
