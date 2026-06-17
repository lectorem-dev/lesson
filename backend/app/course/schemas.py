from enum import Enum
from uuid import UUID

from pydantic import Field

from app.common.responses import ApiModel


class LessonStatus(str, Enum):
    LOCKED = "LOCKED"
    AVAILABLE = "AVAILABLE"
    PASSED = "PASSED"


class CourseResponse(ApiModel):
    id: UUID = Field(description="Идентификатор курса", examples=["550e8400-e29b-41d4-a716-446655440000"])
    title: str = Field(description="Название курса", examples=["Основной курс"])
    description: str | None = Field(description="Описание курса", examples=["Единый курс MVP платформы"])
    progress_percent: int = Field(alias="progressPercent", description="Процент прохождения курса", examples=[33])
    completed_lessons: int = Field(alias="completedLessons", description="Количество пройденных уроков", examples=[1])
    total_lessons: int = Field(alias="totalLessons", description="Общее количество уроков", examples=[3])


class LessonListItemResponse(ApiModel):
    id: UUID = Field(description="Идентификатор урока", examples=["550e8400-e29b-41d4-a716-446655440000"])
    position: int = Field(description="Порядковый номер урока", examples=[2])
    title: str = Field(description="Название урока", examples=["Урок 2"])
    status: LessonStatus = Field(description="Статус урока", examples=["AVAILABLE"])
    locked: bool = Field(description="Признак блокировки урока", examples=[False])
    best_score_percent: int | None = Field(alias="bestScorePercent", description="Лучший результат по тесту в процентах", examples=[100])


class TestAnswerResponse(ApiModel):
    id: UUID = Field(description="Идентификатор ответа", examples=["550e8400-e29b-41d4-a716-446655440001"])
    text: str = Field(description="Текст ответа", examples=["Markdown используется для описания учебного материала"])


class TestQuestionResponse(ApiModel):
    id: UUID = Field(description="Идентификатор вопроса", examples=["550e8400-e29b-41d4-a716-446655440000"])
    text: str = Field(description="Текст вопроса")
    answers: list[TestAnswerResponse] = Field(description="Список вариантов ответа")


class LessonTestResponse(ApiModel):
    pass_percent: int = Field(alias="passPercent", description="Минимальный процент для прохождения теста", examples=[70])
    questions: list[TestQuestionResponse] = Field(description="Вопросы теста")


class LessonResponse(ApiModel):
    id: UUID = Field(description="Идентификатор урока", examples=["550e8400-e29b-41d4-a716-446655440000"])
    position: int = Field(description="Порядковый номер урока", examples=[2])
    title: str = Field(description="Название урока", examples=["Урок 2"])
    markdown_content: str = Field(alias="markdownContent", description="Markdown-содержимое урока")
    test: LessonTestResponse = Field(description="Тест урока")


class LessonSubmitAnswerRequest(ApiModel):
    question_id: UUID = Field(alias="questionId", description="Идентификатор вопроса", examples=["550e8400-e29b-41d4-a716-446655440000"])
    answer_id: UUID = Field(alias="answerId", description="Идентификатор выбранного ответа", examples=["550e8400-e29b-41d4-a716-446655440001"])


class LessonSubmitRequest(ApiModel):
    answers: list[LessonSubmitAnswerRequest] = Field(min_length=1, description="Ответы по вопросам урока")


class LessonSubmitResponse(ApiModel):
    passed: bool = Field(description="Признак успешного прохождения теста", examples=[True])
    score_percent: int = Field(alias="scorePercent", description="Результат теста в процентах", examples=[100])
    pass_percent: int = Field(alias="passPercent", description="Минимальный процент для прохождения", examples=[70])
    next_lesson_id: UUID | None = Field(alias="nextLessonId", description="Идентификатор следующего урока, если тест пройден")
    course_completed: bool = Field(alias="courseCompleted", description="Признак завершения всего курса", examples=[False])
