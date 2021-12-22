package com.jslink.wc.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends DataCheckException {

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(HttpStatus status) {
        super(status);
    }

    public ForbiddenException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }

    public ForbiddenException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public ForbiddenException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
