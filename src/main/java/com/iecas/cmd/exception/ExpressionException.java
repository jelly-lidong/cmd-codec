package com.iecas.cmd.exception;

/**
 * 表达式计算异常
 */
public class ExpressionException extends RuntimeException {
    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
} 