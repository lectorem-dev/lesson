from uuid import UUID

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.security import require_roles
from app.course.schemas import CourseResponse, LessonListItemResponse, LessonResponse, LessonSubmitRequest, LessonSubmitResponse
from app.course.service import get_course, get_lesson, get_lessons, submit_lesson
from app.db.session import get_db
from app.progress.schemas import CourseProgressResponse
from app.progress.service import get_course_progress
from app.users.models import User, UserRole


router = APIRouter(prefix="/api/course", tags=["Курс"])


@router.get(
    "",
    response_model=CourseResponse,
    summary="Получение информации о курсе",
    description="Возвращает краткое описание основного курса и прогресс текущего пользователя.",
)
def read_course(
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> CourseResponse:
    return get_course(db, user)


@router.get(
    "/lessons",
    response_model=list[LessonListItemResponse],
    summary="Получение списка уроков с учетом прогресса пользователя",
    description="Возвращает уроки курса, их статус доступности и лучший результат пользователя.",
)
def read_lessons(
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> list[LessonListItemResponse]:
    return get_lessons(db, user)


@router.get(
    "/lessons/{lesson_id}",
    response_model=LessonResponse,
    summary="Получение содержимого урока и теста",
    description="Возвращает markdown-материал урока и тест без признаков правильных ответов.",
)
def read_lesson(
    lesson_id: UUID,
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> LessonResponse:
    return get_lesson(db, user, lesson_id)


@router.post(
    "/lessons/{lesson_id}/submit",
    response_model=LessonSubmitResponse,
    summary="Отправка ответов на тест",
    description="Проверяет ответы пользователя, считает процент результата и при успехе обновляет прогресс урока.",
)
def submit_lesson_answers(
    lesson_id: UUID,
    request: LessonSubmitRequest,
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> LessonSubmitResponse:
    return submit_lesson(db, user, lesson_id, request)


@router.get(
    "/progress",
    response_model=CourseProgressResponse,
    summary="Получение текущего прогресса пользователя",
    description="Возвращает количество пройденных уроков, общий процент курса и признак завершения.",
)
def read_course_progress(
    db: Session = Depends(get_db),
    user: User = Depends(require_roles(UserRole.ADMIN, UserRole.STUDENT)),
) -> CourseProgressResponse:
    return get_course_progress(db, user)
