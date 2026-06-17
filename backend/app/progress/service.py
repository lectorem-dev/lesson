from math import floor

from sqlalchemy.orm import Session

from app.common.exceptions import CourseNotFoundException
from app.course.repository import get_course_lessons, get_main_course
from app.progress.repository import count_completed_lessons
from app.progress.schemas import CourseProgressResponse
from app.users.models import User


def java_round(value: float) -> int:
    return int(floor(value + 0.5))


def calculate_progress_percent(completed_lessons: int, total_lessons: int) -> int:
    if total_lessons == 0:
        return 0
    # Прогресс считаем по завершенным урокам, а не по попыткам прохождения тестов.
    return java_round(completed_lessons * 100.0 / total_lessons)


def get_course_progress(db: Session, user: User) -> CourseProgressResponse:
    course = get_main_course(db)
    if course is None:
        raise CourseNotFoundException()

    total_lessons = len(get_course_lessons(db, course))
    completed_lessons = count_completed_lessons(db, user.id, course.id)
    return CourseProgressResponse(
        completedLessons=completed_lessons,
        totalLessons=total_lessons,
        progressPercent=calculate_progress_percent(completed_lessons, total_lessons),
        courseCompleted=total_lessons > 0 and completed_lessons == total_lessons,
    )
