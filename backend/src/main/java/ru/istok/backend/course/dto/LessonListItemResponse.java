package ru.istok.backend.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LessonListItemResponse {

    private Long id;
    private Integer position;
    private String title;
    private LessonStatus status;
    private Boolean locked;
    private Integer bestScorePercent;
}
