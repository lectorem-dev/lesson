package ru.istok.backend.common.exception;

import org.springframework.http.HttpStatus;

public class SubmitValidationException extends AppException {

    public SubmitValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.SUBMIT_VALIDATION_ERROR, message);
    }
}
