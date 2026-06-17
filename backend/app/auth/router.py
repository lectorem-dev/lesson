from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.auth.schemas import LoginRequest, LoginResponse
from app.auth.service import login
from app.db.session import get_db


router = APIRouter(prefix="/api/auth", tags=["Авторизация"])


@router.post(
    "/login",
    response_model=LoginResponse,
    summary="Авторизация пользователя",
    description="Проверяет логин и пароль активного пользователя и возвращает JWT для защищенных запросов.",
)
def login_user(request: LoginRequest, db: Session = Depends(get_db)) -> LoginResponse:
    return login(db, request)
