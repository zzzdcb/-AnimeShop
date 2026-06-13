package com.dong.exception;

import lombok.Getter;

/**
 * Token异常
 * 用于JWT Token相关的异常
 */
@Getter
public class TokenException extends RuntimeException {

    private Integer code;
    private String message;

    public TokenException(String message) {
        super(message);
        this.code = ErrorCode.TOKEN_INVALID.getCode();
        this.message = message;
    }

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.TOKEN_INVALID.getCode();
        this.message = message;
    }
}