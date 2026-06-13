package com.dong.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 格式：模块(2位) + 错误码(2位)
 */
@Getter
public enum ErrorCode {

    // 通用错误 (00)
    SUCCESS(20000, "成功"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后再试"),
    PARAM_ERROR(40000, "参数错误"),
    ILLEGAL_STATE(40001, "非法状态"),

    // 用户模块错误 (01)
    USER_NOT_FOUND(40101, "用户不存在"),
    USER_ALREADY_EXISTS(40102, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(40103, "用户名或密码错误"),
    USER_DISABLED(40104, "账号已被禁用"),
    USER_LOCKED(40105, "账号已被锁定"),
    USER_EXPIRED(40106, "账号已过期"),

    // 权限错误 (02)
    UNAUTHORIZED(40201, "未登录或登录已过期"),
    FORBIDDEN(40203, "没有权限访问"),
    TOKEN_EXPIRED(40204, "Token已过期"),
    TOKEN_INVALID(40205, "Token无效"),
    TOKEN_MISSING(40206, "缺少Token"),

    // 业务错误 (03)
    LOGIN_FAILED(40301, "登录失败"),
    LOGOUT_FAILED(40302, "登出失败"),

    // 数据库错误 (04)
    DATA_NOT_FOUND(40401, "数据不存在"),
    DATA_ALREADY_EXISTS(40402, "数据已存在"),
    DATA_SAVE_FAILED(40403, "数据保存失败"),
    DATA_UPDATE_FAILED(40404, "数据更新失败"),
    DATA_DELETE_FAILED(40405, "数据删除失败"),

    // 文件错误 (05)
    FILE_UPLOAD_ERROR(40501, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(40502, "文件下载失败"),
    FILE_NOT_FOUND(40503, "文件不存在"),
    FILE_FORMAT_ERROR(40504, "文件格式错误"),

    // 验证码错误 (06)
    CAPTCHA_ERROR(40601, "验证码错误"),
    CAPTCHA_EXPIRED(40602, "验证码已过期"),

    // 第三方服务错误 (07)
    THIRD_PARTY_ERROR(40701, "第三方服务异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     */
    public static ErrorCode getByCode(Integer code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
