package com.jslink.wc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DataCheckException extends ResponseStatusException {

    private Object content;

    public DataCheckException(HttpStatus status) {
        super(status);
    }

    public DataCheckException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public DataCheckException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    public DataCheckException(HttpStatus status, Object content) {
        super(status);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
