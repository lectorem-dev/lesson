from random import shuffle
from uuid import UUID

from sqlalchemy.orm import Session

from app.common.exceptions import ContentImportException, CourseNotFoundException, LessonLockedException, LessonNotFoundException, SubmitValidationException
from app.course import repository
from app.course.models import Lesson, TestAnswer, TestQuestion
from app.course.schemas import (
    CourseResponse,
    LessonListItemResponse,
    LessonResponse,
    LessonStatus,
    LessonSubmitRequest,
    LessonSubmitResponse,
    LessonTestResponse,
    TestAnswerResponse,
    TestQuestionResponse,
)
from app.progress.models import LessonProgress
from app.progress.repository import count_completed_lessons, get_progress_by_lesson_id, get_progress_for_lesson
from app.progress.service import calculate_progress_percent, java_round
from app.users.models import User


def get_course(db: Session, user: User) -> CourseResponse:
    course, lessons = get_course_with_lessons(db)
    completed_lessons = count_completed_lessons(db, user.id, course.id)
    total_lessons = len(lessons)
    return CourseResponse(
        id=course.id,
        title=course.title,
        description=course.description,
        progressPercent=calculate_progress_percent(completed_lessons, total_lessons),
        completedLessons=completed_lessons,
        totalLessons=total_lessons,
    )


def get_lessons(db: Session, user: User) -> list[LessonListItemResponse]:
    _, lessons = get_course_with_lessons(db)
    progress_by_lesson_id = get_progress_by_lesson_id(db, user.id, [lesson.id for lesson in lessons])

    return [
        LessonListItemResponse(
            id=lesson.id,
            position=lesson.position,
            title=lesson.title,
            status=get_lesson_status(lesson, lessons, progress_by_lesson_id),
            locked=get_lesson_status(lesson, lessons, progress_by_lesson_id) == LessonStatus.LOCKED,
            bestScorePercent=progress_by_lesson_id[lesson.id].best_score_percent if lesson.id in progress_by_lesson_id else None,
        )
        for lesson in lessons
    ]


def get_lesson(db: Session, user: User, lesson_id: UUID) -> LessonResponse:
    _, lessons = get_course_with_lessons(db)
    lesson = find_lesson_in_course(lesson_id, lessons)
    progress_by_lesson_id = get_progress_by_lesson_id(db, user.id, [item.id for item in lessons])

    if get_lesson_status(lesson, lessons, progress_by_lesson_id) == LessonStatus.LOCKED:
        raise LessonLockedException()

    rule = get_pass_rule_or_fail(db, lesson.id)
    questions = repository.get_lesson_questions(db, lesson.id)
    answers_by_question_id = repository.get_answers_for_questions(db, [question.id for question in questions])
    question_responses = [to_question_response(question, answers_by_question_id.get(question.id, [])) for question in questions]

    return LessonResponse(
        id=lesson.id,
        position=lesson.position,
        title=lesson.title,
        markdownContent=lesson.markdown_content,
        test=LessonTestResponse(passPercent=rule.pass_percent, questions=question_responses),
    )


def submit_lesson(db: Session, user: User, lesson_id: UUID, request: LessonSubmitRequest) -> LessonSubmitResponse:
    course, lessons = get_course_with_lessons(db)
    lesson = find_lesson_in_course(lesson_id, lessons)
    progress_by_lesson_id = get_progress_by_lesson_id(db, user.id, [item.id for item in lessons])

    if get_lesson_status(lesson, lessons, progress_by_lesson_id) == LessonStatus.LOCKED:
        raise LessonLockedException()

    rule = get_pass_rule_or_fail(db, lesson.id)
    questions = repository.get_lesson_questions(db, lesson.id)
    answers_by_question_id = repository.get_answers_for_questions(db, [question.id for question in questions])

    validate_submit_request(request, {question.id for question in questions}, answers_by_question_id)
    selected_answer_by_question_id = {answer.question_id: answer.answer_id for answer in request.answers}

    correct_answers = sum(
        1
        for question in questions
        if is_correct_answer(question.id, selected_answer_by_question_id[question.id], answers_by_question_id)
    )
    score_percent = java_round(correct_answers * 100.0 / len(questions))
    passed = score_percent >= rule.pass_percent

    if passed:
        save_passed_progress(db, user, lesson, score_percent)

    next_lesson_id = get_next_lesson_id(lesson, lessons) if passed else None
    course_completed = passed and count_completed_lessons(db, user.id, course.id) == len(lessons)
    db.commit()

    return LessonSubmitResponse(
        passed=passed,
        scorePercent=score_percent,
        passPercent=rule.pass_percent,
        nextLessonId=next_lesson_id,
        courseCompleted=course_completed,
    )


def get_course_with_lessons(db: Session) -> tuple:
    course = repository.get_main_course(db)
    if course is None:
        raise CourseNotFoundException()
    return course, repository.get_course_lessons(db, course)


def to_question_response(question: TestQuestion, answers: list[TestAnswer]) -> TestQuestionResponse:
    answer_responses = [TestAnswerResponse(id=answer.id, text=answer.text) for answer in answers]
    # Ответы перемешиваются на каждой выдаче урока, чтобы позиция правильного варианта не запоминалась.
    shuffle(answer_responses)
    return TestQuestionResponse(id=question.id, text=question.text, answers=answer_responses)


def validate_submit_request(
    request: LessonSubmitRequest,
    expected_question_ids: set[UUID],
    answers_by_question_id: dict[UUID, list[TestAnswer]],
) -> None:
    if not request.answers:
        raise SubmitValidationException("Нужно передать ответы на все вопросы")

    # Проверяем, что клиент прислал ровно один ответ на каждый вопрос текущего урока.
    request_question_ids: set[UUID] = set()
    for answer in request.answers:
        if answer.question_id in request_question_ids:
            raise SubmitValidationException(f"В запросе найден повторяющийся questionId: {answer.question_id}")
        request_question_ids.add(answer.question_id)

    missing_question_ids = expected_question_ids - request_question_ids
    if missing_question_ids:
        raise SubmitValidationException(f"Нет ответов для вопросов: {sorted(str(item) for item in missing_question_ids)}")

    extra_question_ids = request_question_ids - expected_question_ids
    if extra_question_ids:
        raise SubmitValidationException(f"В запросе есть лишние вопросы: {sorted(str(item) for item in extra_question_ids)}")

    for answer in request.answers:
        belongs_to_question = any(candidate.id == answer.answer_id for candidate in answers_by_question_id.get(answer.question_id, []))
        if not belongs_to_question:
            raise SubmitValidationException(f"Ответ {answer.answer_id} не относится к вопросу {answer.question_id}")


def is_correct_answer(question_id: UUID, answer_id: UUID, answers_by_question_id: dict[UUID, list[TestAnswer]]) -> bool:
    return any(answer.id == answer_id and answer.correct for answer in answers_by_question_id.get(question_id, []))


def save_passed_progress(db: Session, user: User, lesson: Lesson, score_percent: int) -> None:
    progress = get_progress_for_lesson(db, user.id, lesson.id)
    if progress is None:
        progress = LessonProgress(
            user_id=user.id,
            lesson_id=lesson.id,
            best_score_percent=score_percent,
            passed=True,
            completed_at=None,
        )
        db.add(progress)

    progress.passed = True
    # completed_at фиксируется при первой успешной попытке, а лучший балл может повышаться позже.
    if progress.completed_at is None:
        from datetime import datetime

        progress.completed_at = datetime.now()
    progress.best_score_percent = max(progress.best_score_percent, score_percent)
    db.flush()


def get_next_lesson_id(lesson: Lesson, lessons: list[Lesson]) -> UUID | None:
    next_position = lesson.position + 1
    return next((candidate.id for candidate in lessons if candidate.position == next_position), None)


def get_lesson_status(
    lesson: Lesson,
    lessons: list[Lesson],
    progress_by_lesson_id: dict[UUID, LessonProgress],
) -> LessonStatus:
    current_progress = progress_by_lesson_id.get(lesson.id)
    if current_progress is not None and current_progress.passed:
        return LessonStatus.PASSED

    if lesson.position == 1:
        return LessonStatus.AVAILABLE

    # Урок открывается только после прохождения предыдущего урока по порядковому номеру.
    previous_lesson = next((candidate for candidate in lessons if candidate.position == lesson.position - 1), None)
    previous_progress = progress_by_lesson_id.get(previous_lesson.id) if previous_lesson is not None else None
    return LessonStatus.AVAILABLE if previous_progress is not None and previous_progress.passed else LessonStatus.LOCKED


def find_lesson_in_course(lesson_id: UUID, lessons: list[Lesson]) -> Lesson:
    lesson = next((candidate for candidate in lessons if candidate.id == lesson_id), None)
    if lesson is None:
        raise LessonNotFoundException()
    return lesson


def get_pass_rule_or_fail(db: Session, lesson_id: UUID):
    rule = repository.get_pass_rule(db, lesson_id)
    if rule is None:
        raise ContentImportException(f"Для урока {lesson_id} не найдено правило прохождения")
    return rule
