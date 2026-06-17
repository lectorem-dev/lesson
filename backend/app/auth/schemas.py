from uuid import UUID

from pydantic import Field, field_validator

from app.common.responses import ApiModel
from app.users.models import UserRole


class LoginRequest(ApiModel):
    login: str = Field(min_length=1, description="Логин пользователя", examples=["admin"])
    password: str = Field(min_length=1, description="Пароль пользователя", examples=["secret"])

    @field_validator("login", "password")
    @classmethod
    def validate_not_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("поле обязательно")
        return value


class LoginResponse(ApiModel):
    token: str = Field(description="JWT-токен для заголовка Authorization", examples=["eyJhbGciOiJIUzI1NiJ9..."])
    user_id: UUID = Field(alias="userId", description="Идентификатор пользователя", examples=["550e8400-e29b-41d4-a716-446655440000"])
    login: str = Field(description="Логин пользователя", examples=["admin"])
    name: str = Field(description="Имя пользователя", examples=["Администратор"])
    role: UserRole = Field(description="Роль пользователя: ADMIN или STUDENT", examples=["ADMIN"])
