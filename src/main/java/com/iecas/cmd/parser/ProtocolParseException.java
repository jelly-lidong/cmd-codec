package com.iecas.cmd.parser;

/**
 * 协议解析异常
 */
public class ProtocolParseException extends RuntimeException {
    public ProtocolParseException(String message) {
        super(message);
    }

    public ProtocolParseException(String message, Throwable cause) {
        super(message, cause);
    }
} 