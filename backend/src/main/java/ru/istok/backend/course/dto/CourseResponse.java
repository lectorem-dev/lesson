package ru.istok.backend.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Краткая информация о курсе")
public class CourseResponse {

    @Schema(description = "Идентификатор курса", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Название курса", example = "Основной курс")
    private String title;

    @Schema(description = "Описание курса", example = "Единый курс MVP платформы")
    private String description;

    @Schema(description = "Процент прохождения курса", example = "33")
    private Integer progressPercent;

    @Schema(description = "Количество пройденных уроков", example = "1")
    private Integer completedLessons;

    @Schema(description = "Общее количество уроков", example = "3")
    private Integer totalLessons;
}
