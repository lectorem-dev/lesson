package ru.istok.backend.common.exception;

import org.springframework.http.HttpStatus;

public class LessonLockedException extends AppException {

    public LessonLockedException(Long id) {
        super(HttpStatus.FORBIDDEN, ErrorCode.LESSON_LOCKED, "Lesson with id %d is locked".formatted(id));
    }
}
