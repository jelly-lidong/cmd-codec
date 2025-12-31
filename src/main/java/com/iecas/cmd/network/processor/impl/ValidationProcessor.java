package com.iecas.cmd.network.processor.impl;

import com.iecas.cmd.network.NetworkMessage;
import com.iecas.cmd.network.processor.MessageProcessor;
import com.iecas.cmd.network.processor.ProcessorException;
import com.iecas.cmd.network.processor.ProcessorResult;

/**
 * 数据验证处理器
 * 用于验证消息数据的完整性和有效性
 */
public class ValidationProcessor implements MessageProcessor {
    
    private static final String NAME = "ValidationProcessor";
    
    private final int minDataLength;
    private final int maxDataLength;
    private final boolean validateChecksum;
    
    public ValidationProcessor() {
        this(1, Integer.MAX_VALUE, false);
    }
    
    public ValidationProcessor(int minDataLength, int maxDataLength, boolean validateChecksum) {
        this.minDataLength = minDataLength;
        this.maxDataLength = maxDataLength;
        this.validateChecksum = validateChecksum;
    }
    
    @Override
    public ProcessorResult process(NetworkMessage input) throws ProcessorException {
        if (input == null) {
            return ProcessorResult.failed("输入消息为空", null);
        }
        
        byte[] data = input.getData();
        if (data == null) {
            return ProcessorResult.failed("消息数据为空", null);
        }
        
        // 1. 长度验证
        if (data.length < minDataLength) {
            return ProcessorResult.failed(
                    String.format("数据长度不足，最小长度=%d，实际长度=%d", minDataLength, data.length), 
                    null);
        }
        
        if (data.length > maxDataLength) {
            return ProcessorResult.failed(
                    String.format("数据长度超限，最大长度=%d，实际长度=%d", maxDataLength, data.length), 
                    null);
        }
        
        // 2. 校验和验证（如果启用）
        if (validateChecksum && !validateChecksum(data)) {
            return ProcessorResult.failed("校验和验证失败", null);
        }
        
        // 3. 基本格式验证
        if (!isValidFormat(data)) {
            return ProcessorResult.failed("数据格式无效", null);
        }
        
        // 验证通过，返回原始消息
        return ProcessorResult.success(input);
    }
    
    /**
     * 校验和验证
     */
    private boolean validateChecksum(byte[] data) {
        if (data.length < 2) {
            return false; // 数据太短，无法包含校验和
        }
        
        // 假设最后一个字节是校验和
        int expectedChecksum = data[data.length - 1] & 0xFF;
        
        // 计算实际校验和（除了最后一个字节）
        int actualChecksum = 0;
        for (int i = 0; i < data.length - 1; i++) {
            actualChecksum += data[i] & 0xFF;
        }
        actualChecksum &= 0xFF;
        
        return expectedChecksum == actualChecksum;
    }
    
    /**
     * 基本格式验证
     */
    private boolean isValidFormat(byte[] data) {
        // 示例验证：检查是否包含无效的控制字符
        for (byte b : data) {
            int value = b & 0xFF;
            // 排除某些控制字符（除了常见的换行、制表符等）
            if (value < 9 || (value > 13 && value < 32)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getDescription() {
        return String.format("数据验证处理器 - 长度范围[%d,%d]，校验和验证=%s", 
                minDataLength, maxDataLength, validateChecksum);
    }
    
    @Override
    public boolean supports(NetworkMessage message) {
        // 支持所有类型的消息
        return message != null && message.getData() != null;
    }
} 