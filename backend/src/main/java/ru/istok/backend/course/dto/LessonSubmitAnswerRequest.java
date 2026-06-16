package ru.istok.backend.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonSubmitAnswerRequest {

    @NotNull
    private Long questionId;

    @NotNull
    private Long answerId;
}
