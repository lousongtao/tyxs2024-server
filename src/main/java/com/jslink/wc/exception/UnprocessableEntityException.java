package com.jslink.wc.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends DataCheckException {

    public UnprocessableEntityException(String string) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, string);
    }

    public UnprocessableEntityException(HttpStatus status, String string) {
        super(status, string);
    }

    public UnprocessableEntityException(HttpStatus status, String string, Throwable cause) {
        super(status, string, cause);
    }
}
