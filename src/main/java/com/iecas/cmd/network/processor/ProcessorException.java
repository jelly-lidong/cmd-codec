package com.iecas.cmd.network.processor;

/**
 * 消息处理器异常
 */
public class ProcessorException extends Exception {
    
    private final String processorName;
    private final String errorCode;
    
    public ProcessorException(String message) {
        super(message);
        this.processorName = null;
        this.errorCode = null;
    }
    
    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
        this.processorName = null;
        this.errorCode = null;
    }
    
    public ProcessorException(String processorName, String message) {
        super(String.format("[%s] %s", processorName, message));
        this.processorName = processorName;
        this.errorCode = null;
    }
    
    public ProcessorException(String processorName, String message, Throwable cause) {
        super(String.format("[%s] %s", processorName, message), cause);
        this.processorName = processorName;
        this.errorCode = null;
    }
    
    public ProcessorException(String processorName, String errorCode, String message) {
        super(String.format("[%s:%s] %s", processorName, errorCode, message));
        this.processorName = processorName;
        this.errorCode = errorCode;
    }
    
    public ProcessorException(String processorName, String errorCode, String message, Throwable cause) {
        super(String.format("[%s:%s] %s", processorName, errorCode, message), cause);
        this.processorName = processorName;
        this.errorCode = errorCode;
    }
    
    public String getProcessorName() {
        return processorName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 