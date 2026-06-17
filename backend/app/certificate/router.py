from fastapi import APIRouter, Depends, Response
from sqlalchemy.orm import Session

from app.certificate.service import generate_certificate
from app.core.security import require_roles
from app.db.session import get_db
from app.users.models import User, UserRole


router = APIRouter(prefix="/api/certificate", tags=["Сертификат"])


@router.get(
    "",
    summary="Скачивание PDF-сертификата",
    description="Возвращает PDF-сертификат после полного прохождения курса текущим пользователем.",
    responses={200: {"content": {"application/pdf": {"schema": {"type": "string", "format": "binary"}}}}},
)
def download_certificate(
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> Response:
    pdf = generate_certificate(db, user)
    return Response(
        content=pdf,
        media_type="application/pdf",
        headers={"Content-Disposition": 'attachment; filename="certificate.pdf"'},
    )
