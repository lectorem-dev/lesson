package ru.istok.backend.course.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionResponse {

    private Long id;
    private String text;
    private List<TestAnswerResponse> answers;
}
