package com.charge.station.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一API响应对象
 * 
 * @author 架构师团队
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(
    int code,
    String message,
    T data
) {
    
    /**
     * 成功响应
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "success", data);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "success", null);
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
    
    /**
     * 错误响应（默认错误码）
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }
}
