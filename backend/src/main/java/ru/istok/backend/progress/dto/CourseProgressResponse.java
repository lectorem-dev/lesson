package ru.istok.backend.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {

    private Integer completedLessons;
    private Integer totalLessons;
    private Integer progressPercent;
    private Boolean courseCompleted;
}
