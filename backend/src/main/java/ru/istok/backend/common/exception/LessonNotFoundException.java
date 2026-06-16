package ru.istok.backend.common.exception;

import org.springframework.http.HttpStatus;

public class LessonNotFoundException extends AppException {

    public LessonNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.LESSON_NOT_FOUND, "Lesson with id %d not found".formatted(id));
    }
}
