package org.example.catplatform.common.result;

import lombok.Data;

// 统一返回结果类
@Data
public class Result<T> {
    private Integer code;    // 状态码：200成功，其他失败
    private String message;
    private T data;

    // 构造方法
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}