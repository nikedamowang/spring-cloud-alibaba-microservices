package com.cloudDemo.api.dto;

import lombok.Data;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private boolean success;

    private Result() {
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        result.success = true;
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.success = true;
        return result;
    }

    /**
     * 错误响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = message;
        result.success = false;
        return result;
    }

    /**
     * 错误响应（自定义状态码）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.success = false;
        return result;
    }
}
