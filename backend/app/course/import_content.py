from dataclasses import dataclass
import json
from pathlib import Path

from pydantic import BaseModel, Field, ValidationError
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from app.common.exceptions import ContentImportException
from app.core.config import settings
from app.course.models import Course, Lesson, LessonPassRule, TestAnswer, TestQuestion


COURSE_TITLE = "Основной курс"
COURSE_DESCRIPTION = "Единый курс MVP платформы"

# TODO: заменить хардкод-манифест на настраиваемый файл курса
MANIFEST = [
    (1, "lesson-1.md", "test-1.json"),
    (2, "lesson-2.md", "test-2.json"),
    (3, "lesson-3.md", "test-3.json"),
]


class TestAnswerFile(BaseModel):
    text: str = Field(description="Текст варианта ответа")
    correct: bool = Field(description="Признак правильного ответа")


class TestQuestionFile(BaseModel):
    text: str = Field(description="Текст вопроса")
    answers: list[TestAnswerFile] = Field(min_length=2, description="Варианты ответа на вопрос")


class TestFile(BaseModel):
    title: str | None = Field(default=None, description="Название теста из файла контента")
    pass_percent: int = Field(alias="passPercent", ge=1, le=100, description="Минимальный процент для прохождения теста")
    questions: list[TestQuestionFile] = Field(min_length=1, description="Вопросы теста")


@dataclass
class LoadedLesson:
    position: int
    markdown: str
    test: TestFile
    test_file_name: str


def import_course_content(db: Session) -> None:
    loaded_lessons = load_content()
    course = upsert_course(db)

    for loaded_lesson in loaded_lessons:
        upsert_lesson(db, course, loaded_lesson)


def load_content() -> list[LoadedLesson]:
    root = settings.content_dir.expanduser().resolve()
    lessons_dir = root / "lessons"
    tests_dir = root / "tests"

    ensure_directory(lessons_dir, "lessons")
    ensure_directory(tests_dir, "tests")
    validate_exact_files(lessons_dir, {lesson_file for _, lesson_file, _ in MANIFEST})
    validate_exact_files(tests_dir, {test_file for _, _, test_file in MANIFEST})
    validate_manifest_numbers()

    loaded_lessons = []
    for position, lesson_file, test_file in MANIFEST:
        markdown = read_markdown(lessons_dir / lesson_file)
        test = read_test(tests_dir / test_file)
        validate_test(test, test_file)
        loaded_lessons.append(LoadedLesson(position=position, markdown=markdown, test=test, test_file_name=test_file))

    return loaded_lessons


def ensure_directory(directory: Path, name: str) -> None:
    if not directory.is_dir():
        raise ContentImportException(f"Не найдена директория '{name}': {directory}")


def validate_exact_files(directory: Path, expected_files: set[str]) -> None:
    try:
        actual_files = {path.name for path in directory.iterdir() if path.is_file()}
    except OSError as exception:
        raise ContentImportException(f"Не удалось прочитать содержимое директории: {directory}") from exception

    # Для MVP ожидаем строгий набор файлов: так сразу видны лишние и пропущенные материалы.
    missing_files = sorted(expected_files - actual_files)
    if missing_files:
        raise ContentImportException(f"В директории {directory} отсутствуют ожидаемые файлы: {missing_files}")

    extra_files = sorted(actual_files - expected_files)
    if extra_files:
        raise ContentImportException(f"В директории {directory} найдены лишние файлы: {extra_files}")


def validate_manifest_numbers() -> None:
    for position, lesson_file, test_file in MANIFEST:
        expected_lesson_file = f"lesson-{position}.md"
        expected_test_file = f"test-{position}.json"
        if lesson_file != expected_lesson_file or test_file != expected_test_file:
            raise ContentImportException(f"Номера lesson/test не совпадают с позицией {position} в манифесте")


def read_markdown(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exception:
        raise ContentImportException(f"Не удалось прочитать markdown-файл: {path}") from exception


def read_test(path: Path) -> TestFile:
    try:
        raw_data = json.loads(path.read_text(encoding="utf-8"))
        return TestFile.model_validate(raw_data)
    except json.JSONDecodeError as exception:
        raise ContentImportException(f"Некорректный JSON-файл теста '{path}': {exception.msg}") from exception
    except ValidationError as exception:
        raise ContentImportException(f"Некорректный JSON-файл теста '{path}': {exception}") from exception
    except OSError as exception:
        raise ContentImportException(f"Не удалось прочитать JSON-файл теста: {path}") from exception


def validate_test(test: TestFile, file_name: str) -> None:
    if not 1 <= test.pass_percent <= 100:
        raise ContentImportException(f"Тест '{file_name}' должен содержать passPercent от 1 до 100")
    if not test.questions:
        raise ContentImportException(f"Тест '{file_name}' должен содержать хотя бы один вопрос")

    # Проверка теста намеренно строгая: frontend не получает correct, поэтому ошибки контента ловим при старте.
    for question_index, question in enumerate(test.questions, start=1):
        if not question.text.strip():
            raise ContentImportException(f"Вопрос {question_index} в '{file_name}' должен содержать текст")
        if len(question.answers) < 2:
            raise ContentImportException(f"Вопрос {question_index} в '{file_name}' должен содержать минимум два ответа")

        correct_count = sum(1 for answer in question.answers if answer.correct)
        has_invalid_answer = any(not answer.text.strip() for answer in question.answers)
        if has_invalid_answer:
            raise ContentImportException(f"Вопрос {question_index} в '{file_name}' содержит некорректный ответ")
        if correct_count != 1:
            raise ContentImportException(f"Вопрос {question_index} в '{file_name}' должен содержать ровно один правильный ответ")


def upsert_course(db: Session) -> Course:
    course = db.scalar(select(Course).where(Course.title == COURSE_TITLE))
    if course is None:
        course = Course(title=COURSE_TITLE)
        db.add(course)

    course.title = COURSE_TITLE
    course.description = COURSE_DESCRIPTION
    db.flush()
    return course


def upsert_lesson(db: Session, course: Course, loaded_lesson: LoadedLesson) -> None:
    lesson = db.scalar(
        select(Lesson).where(
            Lesson.course_id == course.id,
            Lesson.position == loaded_lesson.position,
        )
    )
    if lesson is None:
        lesson = Lesson(course_id=course.id, position=loaded_lesson.position)
        db.add(lesson)

    lesson.title = f"Урок {loaded_lesson.position}"
    lesson.markdown_content = loaded_lesson.markdown
    db.flush()

    rule = db.scalar(select(LessonPassRule).where(LessonPassRule.lesson_id == lesson.id))
    if rule is None:
        rule = LessonPassRule(lesson_id=lesson.id)
        db.add(rule)
    rule.pass_percent = loaded_lesson.test.pass_percent

    # При переимпорте полностью пересоздаем вопросы и ответы урока, чтобы база совпадала с файлами.
    db.execute(delete(TestQuestion).where(TestQuestion.lesson_id == lesson.id))
    db.flush()

    for question_index, question_file in enumerate(loaded_lesson.test.questions, start=1):
        question = TestQuestion(
            lesson_id=lesson.id,
            position=question_index,
            text=question_file.text,
        )
        db.add(question)
        db.flush()

        for answer_index, answer_file in enumerate(question_file.answers, start=1):
            db.add(
                TestAnswer(
                    question_id=question.id,
                    position=answer_index,
                    text=answer_file.text,
                    correct=answer_file.correct,
                )
            )
