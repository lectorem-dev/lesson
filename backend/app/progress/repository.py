from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.course.models import Lesson
from app.progress.models import LessonProgress


def get_progress_by_lesson_id(db: Session, user_id: UUID, lesson_ids: list[UUID]) -> dict[UUID, LessonProgress]:
    if not lesson_ids:
        return {}
    progress_items = db.scalars(
        select(LessonProgress).where(
            LessonProgress.user_id == user_id,
            LessonProgress.lesson_id.in_(lesson_ids),
        )
    )
    return {progress.lesson_id: progress for progress in progress_items}


def get_progress_for_lesson(db: Session, user_id: UUID, lesson_id: UUID) -> LessonProgress | None:
    return db.scalar(
        select(LessonProgress).where(
            LessonProgress.user_id == user_id,
            LessonProgress.lesson_id == lesson_id,
        )
    )


def count_completed_lessons(db: Session, user_id: UUID, course_id: UUID) -> int:
    return int(
        db.scalar(
            select(func.count(LessonProgress.id))
            .join(Lesson, LessonProgress.lesson_id == Lesson.id)
            .where(
                LessonProgress.user_id == user_id,
                LessonProgress.passed.is_(True),
                Lesson.course_id == course_id,
            )
        )
        or 0
    )
