package com.manhnv.security.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(200, "SUCCESS", data);
    }

    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>(401, "UNAUTHORIZED", data);
    }

    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>(403, "FORBIDDEN", data);
    }

    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(500, message, null);
    }

    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>(404, message, null);
    }
}
