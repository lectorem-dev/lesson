package ru.istok.backend.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Вариант ответа на вопрос")
public class TestAnswerResponse {

    @Schema(description = "Идентификатор ответа", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @Schema(description = "Текст ответа", example = "Markdown используется для описания учебного материала")
    private String text;
}
