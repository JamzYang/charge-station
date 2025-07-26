package com.charge.station.shared.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * 统一API响应格式
 * 
 * @param <T> 数据类型
 * @author 架构师团队
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String errorCode,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant timestamp
) {
    
    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data, null, Instant.now());
    }

    /**
     * 创建成功响应（带消息）
     * 
     * @param data 响应数据
     * @param message 成功消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }

    /**
     * 创建失败响应（带错误代码）
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, Instant.now());
    }

    /**
     * 创建失败响应（带数据和错误代码）
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return new ApiResponse<>(false, message, data, errorCode, Instant.now());
    }
}
