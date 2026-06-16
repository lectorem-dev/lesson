package ru.istok.backend.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Integer progressPercent;
    private Integer completedLessons;
    private Integer totalLessons;
}
