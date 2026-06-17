from enum import Enum
from typing import Sequence


class ErrorCode(str, Enum):
    USER_NOT_FOUND = "USER_NOT_FOUND"
    LOGIN_ALREADY_EXISTS = "LOGIN_ALREADY_EXISTS"
    INVALID_LOGIN_OR_PASSWORD = "INVALID_LOGIN_OR_PASSWORD"
    USER_ARCHIVED = "USER_ARCHIVED"
    ACCESS_DENIED = "ACCESS_DENIED"
    VALIDATION_ERROR = "VALIDATION_ERROR"
    COURSE_NOT_FOUND = "COURSE_NOT_FOUND"
    LESSON_NOT_FOUND = "LESSON_NOT_FOUND"
    LESSON_LOCKED = "LESSON_LOCKED"
    CONTENT_IMPORT_ERROR = "CONTENT_IMPORT_ERROR"
    SUBMIT_VALIDATION_ERROR = "SUBMIT_VALIDATION_ERROR"
    COURSE_NOT_COMPLETED = "COURSE_NOT_COMPLETED"


class AppException(Exception):
    def __init__(
        self,
        status_code: int,
        code: ErrorCode,
        message: str,
        details: Sequence[str] | None = None,
    ) -> None:
        self.status_code = status_code
        self.code = code
        self.message = message
        self.details = list(details or [])
        super().__init__(message)


class InvalidCredentialsException(AppException):
    def __init__(self) -> None:
        super().__init__(401, ErrorCode.INVALID_LOGIN_OR_PASSWORD, "Неверный логин или пароль")


class AuthRequiredException(AppException):
    def __init__(self) -> None:
        super().__init__(401, ErrorCode.INVALID_LOGIN_OR_PASSWORD, "Требуется авторизация")


class ArchivedUserException(AppException):
    def __init__(self) -> None:
        super().__init__(403, ErrorCode.USER_ARCHIVED, "Пользователь архивирован")


class AccessDeniedException(AppException):
    def __init__(self) -> None:
        super().__init__(403, ErrorCode.ACCESS_DENIED, "Недостаточно прав доступа")


class UserNotFoundException(AppException):
    def __init__(self) -> None:
        super().__init__(404, ErrorCode.USER_NOT_FOUND, "Пользователь не найден")


class LoginAlreadyExistsException(AppException):
    def __init__(self) -> None:
        super().__init__(409, ErrorCode.LOGIN_ALREADY_EXISTS, "Логин уже занят")


class CourseNotFoundException(AppException):
    def __init__(self) -> None:
        super().__init__(404, ErrorCode.COURSE_NOT_FOUND, "Курс не найден")


class LessonNotFoundException(AppException):
    def __init__(self) -> None:
        super().__init__(404, ErrorCode.LESSON_NOT_FOUND, "Урок не найден")


class LessonLockedException(AppException):
    def __init__(self) -> None:
        super().__init__(403, ErrorCode.LESSON_LOCKED, "Урок пока недоступен")


class CourseNotCompletedException(AppException):
    def __init__(self) -> None:
        super().__init__(403, ErrorCode.COURSE_NOT_COMPLETED, "Курс еще не завершен")


class SubmitValidationException(AppException):
    def __init__(self, message: str) -> None:
        super().__init__(400, ErrorCode.SUBMIT_VALIDATION_ERROR, message)


class ContentImportException(AppException):
    def __init__(self, message: str) -> None:
        super().__init__(500, ErrorCode.CONTENT_IMPORT_ERROR, message)
