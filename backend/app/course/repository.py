from uuid import UUID

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.course.models import Course, Lesson, LessonPassRule, TestAnswer, TestQuestion


COURSE_TITLE = "Основной курс"


def get_main_course(db: Session) -> Course | None:
    return db.scalar(select(Course).where(Course.title == COURSE_TITLE))


def get_course_lessons(db: Session, course: Course) -> list[Lesson]:
    return list(db.scalars(select(Lesson).where(Lesson.course_id == course.id).order_by(Lesson.position.asc())))


def get_lesson_questions(db: Session, lesson_id: UUID) -> list[TestQuestion]:
    return list(db.scalars(select(TestQuestion).where(TestQuestion.lesson_id == lesson_id).order_by(TestQuestion.position.asc())))


def get_answers_for_questions(db: Session, question_ids: list[UUID]) -> dict[UUID, list[TestAnswer]]:
    if not question_ids:
        return {}

    answers_by_question_id: dict[UUID, list[TestAnswer]] = {}
    answers = db.scalars(
        select(TestAnswer)
        .where(TestAnswer.question_id.in_(question_ids))
        .order_by(TestAnswer.question_id.asc(), TestAnswer.position.asc())
    )
    for answer in answers:
        answers_by_question_id.setdefault(answer.question_id, []).append(answer)
    return answers_by_question_id


def get_pass_rule(db: Session, lesson_id: UUID) -> LessonPassRule | None:
    return db.scalar(select(LessonPassRule).where(LessonPassRule.lesson_id == lesson_id))
