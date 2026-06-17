from datetime import datetime
from typing import Any

from pydantic import BaseModel, ConfigDict, Field

from app.common.exceptions import ErrorCode


class ApiModel(BaseModel):
    model_config = ConfigDict(
        from_attributes=True,
        populate_by_name=True,
        use_enum_values=True,
    )


class ErrorResponse(ApiModel):
    timestamp: datetime = Field(description="Время формирования ошибки")
    status: int = Field(description="HTTP-статус ответа", examples=[400])
    code: ErrorCode = Field(description="Код ошибки приложения", examples=["VALIDATION_ERROR"])
    message: str = Field(description="Основное сообщение об ошибке", examples=["Некорректное тело запроса"])
    details: list[str] = Field(default_factory=list, description="Дополнительные детали ошибки")


def error_response(status: int, code: ErrorCode, message: str, details: list[str] | None = None) -> dict[str, Any]:
    return ErrorResponse(
        timestamp=datetime.now(),
        status=status,
        code=code,
        message=message,
        details=details or [],
    ).model_dump(mode="json")
