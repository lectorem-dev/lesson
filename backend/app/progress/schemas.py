from pydantic import Field

from app.common.responses import ApiModel


class CourseProgressResponse(ApiModel):
    completed_lessons: int = Field(alias="completedLessons", description="Количество пройденных уроков", examples=[2])
    total_lessons: int = Field(alias="totalLessons", description="Общее количество уроков", examples=[3])
    progress_percent: int = Field(alias="progressPercent", description="Процент прохождения курса", examples=[67])
    course_completed: bool = Field(alias="courseCompleted", description="Признак завершения курса", examples=[False])
