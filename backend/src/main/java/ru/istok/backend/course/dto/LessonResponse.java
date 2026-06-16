package ru.istok.backend.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {

    private Long id;
    private Integer position;
    private String title;
    private String markdownContent;
    private LessonTestResponse test;
}
