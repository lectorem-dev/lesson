from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.common.exceptions import AppException, ErrorCode
from app.common.responses import error_response


def register_error_handlers(app: FastAPI) -> None:
    @app.exception_handler(AppException)
    async def handle_app_exception(_, exception: AppException) -> JSONResponse:
        return JSONResponse(
            status_code=exception.status_code,
            content=error_response(
                exception.status_code,
                exception.code,
                exception.message,
                exception.details,
            ),
        )

    @app.exception_handler(RequestValidationError)
    async def handle_validation_exception(_, exception: RequestValidationError) -> JSONResponse:
        details = [
            f"{'.'.join(str(item) for item in error['loc'] if item != 'body')}: {error['msg']}"
            for error in exception.errors()
        ]
        return JSONResponse(
            status_code=400,
            content=error_response(400, ErrorCode.VALIDATION_ERROR, "Ошибка валидации", details),
        )
