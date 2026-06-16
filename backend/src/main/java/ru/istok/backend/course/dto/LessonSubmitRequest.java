package ru.istok.backend.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonSubmitRequest {

    @Valid
    @NotEmpty
    private List<LessonSubmitAnswerRequest> answers;
}
