package com.charge.station.shared.exception;

/**
 * 资源不存在异常
 * 
 * @author 架构师团队
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
