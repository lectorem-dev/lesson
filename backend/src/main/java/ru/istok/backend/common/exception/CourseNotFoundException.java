package ru.istok.backend.common.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends AppException {

    public CourseNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorCode.COURSE_NOT_FOUND, "Course not found");
    }
}
