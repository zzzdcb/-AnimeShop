package com.dong.common;

import lombok.Data;
import java.time.Instant;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private Long timestamp;
    private T data;

    private Result() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功（有数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功（自定义消息，无数据）
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    // 成功（自定义消息，有数据）
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // 失败（自定义错误码）
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}