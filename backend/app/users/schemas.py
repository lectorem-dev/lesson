from datetime import datetime
from uuid import UUID

from pydantic import Field, field_validator

from app.common.responses import ApiModel
from app.users.models import UserRole, UserStatus


class UserResponse(ApiModel):
    id: UUID = Field(description="Идентификатор пользователя", examples=["550e8400-e29b-41d4-a716-446655440000"])
    name: str = Field(description="Имя пользователя", examples=["Иван Иванов"])
    login: str = Field(description="Логин пользователя", examples=["student"])
    role: UserRole = Field(description="Роль пользователя: ADMIN или STUDENT", examples=["STUDENT"])
    status: UserStatus = Field(description="Статус пользователя: ACTIVE или ARCHIVED", examples=["ACTIVE"])
    created_at: datetime = Field(alias="createdAt", description="Дата создания пользователя")
    updated_at: datetime = Field(alias="updatedAt", description="Дата последнего обновления пользователя")


class UserCreateRequest(ApiModel):
    name: str = Field(min_length=1, description="Имя пользователя", examples=["Иван Иванов"])
    login: str = Field(min_length=1, description="Логин пользователя", examples=["student"])
    password: str = Field(min_length=1, description="Пароль пользователя", examples=["secret"])
    role: UserRole = Field(description="Роль пользователя: ADMIN или STUDENT", examples=["STUDENT"])

    @field_validator("name", "login", "password")
    @classmethod
    def validate_not_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("поле обязательно")
        return value


class UserUpdateRequest(ApiModel):
    name: str = Field(min_length=1, description="Имя пользователя", examples=["Иван Иванов"])
    login: str = Field(min_length=1, description="Логин пользователя", examples=["student"])
    password: str | None = Field(default=None, description="Новый пароль пользователя. Можно не передавать, чтобы оставить старый")
    role: UserRole = Field(description="Роль пользователя: ADMIN или STUDENT", examples=["STUDENT"])
    status: UserStatus = Field(description="Статус пользователя: ACTIVE или ARCHIVED", examples=["ACTIVE"])

    @field_validator("name", "login")
    @classmethod
    def validate_not_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("поле обязательно")
        return value
