package ru.istok.backend.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Полное содержимое урока")
public class LessonResponse {

    @Schema(description = "Идентификатор урока", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Порядковый номер урока", example = "2")
    private Integer position;

    @Schema(description = "Название урока", example = "Урок 2")
    private String title;

    @Schema(description = "Markdown-содержимое урока")
    private String markdownContent;

    @Schema(description = "Тест урока")
    private LessonTestResponse test;
}
