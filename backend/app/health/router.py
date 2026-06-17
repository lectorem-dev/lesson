from fastapi import APIRouter


router = APIRouter(prefix="/api/health", tags=["Сервис"])


@router.get(
    "",
    summary="Проверка состояния сервиса",
    description="Возвращает простой статус, по которому можно проверить доступность Python-backend.",
)
def health() -> dict[str, str]:
    return {"status": "OK"}
