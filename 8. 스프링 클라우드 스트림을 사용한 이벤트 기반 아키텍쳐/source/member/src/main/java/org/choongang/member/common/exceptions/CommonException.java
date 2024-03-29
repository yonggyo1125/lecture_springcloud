package org.choongang.member.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

public class CommonException extends RuntimeException {

    private HttpStatus status;
    private Errors errors;

    private boolean messageCode;

    public CommonException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CommonException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CommonException(Errors errors, HttpStatus status) {
        this.status = status;
        this.errors = errors;

    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setMessageCode(boolean messageCode) {
        this.messageCode = messageCode;
    }

    public boolean isMessageCode() {
        return messageCode;
    }
}