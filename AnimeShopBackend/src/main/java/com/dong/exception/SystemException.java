package com.dong.exception;

import lombok.Getter;

/**
 * 系统异常
 * 用于系统级错误（数据库、网络、文件等）
 */
@Getter
public class SystemException extends RuntimeException {

    private Integer code;
    private String message;

    public SystemException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}