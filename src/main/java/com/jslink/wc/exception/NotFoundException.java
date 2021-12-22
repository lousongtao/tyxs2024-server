package com.jslink.wc.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends DataCheckException {
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND, "Entity is not found");
    }

    public NotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public NotFoundException(HttpStatus status) {
        super(status);
    }

    public NotFoundException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public NotFoundException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
