package ru.istok.backend.course.importcontent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestAnswerFileDto {

    private String text;
    private Boolean correct;
}
