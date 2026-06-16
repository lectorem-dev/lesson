package ru.istok.backend.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LessonSubmitResponse {

    private Boolean passed;
    private Integer scorePercent;
    private Integer passPercent;
    private Long nextLessonId;
    private Boolean courseCompleted;
}
