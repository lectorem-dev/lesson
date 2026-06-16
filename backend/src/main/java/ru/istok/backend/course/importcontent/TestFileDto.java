package ru.istok.backend.course.importcontent;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestFileDto {

    private String title;
    private Integer passPercent;
    private List<TestQuestionFileDto> questions;
}
