package ru.istok.backend.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Вопрос теста")
public class TestQuestionResponse {

    @Schema(description = "Идентификатор вопроса", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Текст вопроса")
    private String text;

    @Schema(description = "Список вариантов ответа")
    private List<TestAnswerResponse> answers;
}
