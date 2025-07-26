package com.charge.station.shared.exception;

/**
 * 业务异常
 * 
 * @author 架构师团队
 * @version 1.0
 */
public class BusinessException extends RuntimeException {
    
    private final int errorCode;
    
    public BusinessException(String message) {
        this(40001, message);
    }
    
    public BusinessException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        this(40001, message, cause);
    }
    
    public BusinessException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
