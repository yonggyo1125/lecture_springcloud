package org.choongang.member.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

public class BadRequestException extends CommonException{

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String code, boolean messageCode) {
        this(code);
        setMessageCode(messageCode);
    }

    public BadRequestException(Errors errors) {
        super(errors, HttpStatus.BAD_REQUEST);
    }
}