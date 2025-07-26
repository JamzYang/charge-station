package com.charge.station.shared.exception;

/**
 * 系统异常
 * 
 * @author 架构师团队
 * @version 1.0
 */
public class SystemException extends RuntimeException {
    
    public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
