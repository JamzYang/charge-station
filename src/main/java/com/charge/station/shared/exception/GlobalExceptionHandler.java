package com.charge.station.shared.exception;

import com.charge.station.interfaces.dto.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author 架构师团队
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        ApiResult<Void> result = ApiResult.error(e.getErrorCode(), e.getMessage());
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        ApiResult<Void> result = ApiResult.error(40404, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResult<Void>> handleSystemException(SystemException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        ApiResult<Void> result = ApiResult.error(50001, "系统暂时不可用");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        ApiResult<Void> result = ApiResult.error(40001, message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResult<Void>> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        ApiResult<Void> result = ApiResult.error(40001, message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反: {}", message);
        ApiResult<Void> result = ApiResult.error(40001, message);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        ApiResult<Void> result = ApiResult.error(50000, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
