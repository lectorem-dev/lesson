from io import BytesIO
from pathlib import Path

from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from sqlalchemy.orm import Session

from app.common.exceptions import CourseNotCompletedException, CourseNotFoundException
from app.course.repository import get_course_lessons, get_main_course
from app.progress.repository import count_completed_lessons
from app.users.models import User


FONT_CANDIDATES = [
    Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    Path("/usr/share/fonts/dejavu/DejaVuSans.ttf"),
    Path("/Library/Fonts/Arial Unicode.ttf"),
    Path("/System/Library/Fonts/Supplemental/Arial Unicode.ttf"),
    Path("/System/Library/Fonts/Supplemental/Arial.ttf"),
    Path("C:/Windows/Fonts/arial.ttf"),
]


def generate_certificate(db: Session, user: User) -> bytes:
    course = get_main_course(db)
    if course is None:
        raise CourseNotFoundException()

    total_lessons = len(get_course_lessons(db, course))
    completed_lessons = count_completed_lessons(db, user.id, course.id)
    if total_lessons == 0 or completed_lessons < total_lessons:
        raise CourseNotCompletedException()

    return generate_pdf(user.name)


def generate_pdf(user_name: str) -> bytes:
    output = BytesIO()
    page_width, page_height = A4
    font_name = "CertificateFont"
    font_path = resolve_font_path()

    # Для кириллицы в PDF регистрируем TrueType-шрифт, иначе имя студента может отобразиться квадратиками.
    pdfmetrics.registerFont(TTFont(font_name, str(font_path)))

    pdf = canvas.Canvas(output, pagesize=A4)
    pdf.setFillColorRGB(1, 1, 1)
    pdf.rect(0, 0, page_width, page_height, fill=1, stroke=0)
    pdf.setFillColorRGB(0, 0, 0)
    pdf.setFont(font_name, 22)
    pdf.drawString(40, page_height - 62, user_name)
    pdf.showPage()
    pdf.save()

    return output.getvalue()


def resolve_font_path() -> Path:
    for path in FONT_CANDIDATES:
        if path.is_file():
            return path
    raise RuntimeError("Не найден TrueType-шрифт для генерации PDF-сертификата")
