package ru.istok.backend.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на один вопрос теста")
public class LessonSubmitAnswerRequest {

    @Schema(description = "Идентификатор вопроса", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Идентификатор вопроса обязателен")
    private UUID questionId;

    @Schema(description = "Идентификатор выбранного ответа", example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull(message = "Идентификатор ответа обязателен")
    private UUID answerId;
}
