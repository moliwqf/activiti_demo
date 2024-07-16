package com.moli.activiti.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author moli
 * @time 2024-07-15 11:41:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class ReturnData<T> {
    /**
     * 响应标志
     */
    private boolean flag;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 返回的信息
     */
    private T data;

    public static <T> ReturnData<T> ok() {
        return result(true, 200, "响应成功", null);
    }

    public static <T> ReturnData<T> ok(T data) {
        return result(true, 200, "响应成功", data);
    }

    public static <T> ReturnData<T> ok(T data, String message) {
        return result(true, 200, message, data);
    }

    public static <T> ReturnData<T> fail() {
        return result(false, 500, "响应失败", null);
    }

    public static <T> ReturnData<T> fail(T data) {
        return result(false, 500, "响应失败", data);
    }

    private static <T> ReturnData<T> result(Boolean flag, Integer code, String message, T data) {
        return ReturnData.<T>builder()
                .flag(flag)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
