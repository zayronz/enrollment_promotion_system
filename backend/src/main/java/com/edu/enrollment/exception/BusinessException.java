package com.edu.enrollment.exception;

/**
 * 业务异常类，用于区分业务逻辑错误和系统异常
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
