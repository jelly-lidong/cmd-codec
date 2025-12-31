package com.iecas.cmd.network.processor;

import com.iecas.cmd.network.NetworkMessage;

import java.util.List;
import java.util.Map;

/**
 * 消息打包器接口
 * 用于将分段后的数据重新打包成新的消息
 */
public interface MessagePacker {
    
    /**
     * 打包规则配置
     */
    class PackRule {
        private final String name;
        private final String description;
        private final PackStrategy strategy;
        private final Map<String, Object> parameters;
        
        public PackRule(String name, String description, PackStrategy strategy, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.strategy = strategy;
            this.parameters = parameters;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public PackStrategy getStrategy() { return strategy; }
        public Map<String, Object> getParameters() { return parameters; }
        
        @SuppressWarnings("unchecked")
        public <T> T getParameter(String key, Class<T> type, T defaultValue) {
            Object value = parameters.get(key);
            if (type.isInstance(value)) {
                return (T) value;
            }
            return defaultValue;
        }
        
        @Override
        public String toString() {
            return String.format("PackRule{name='%s', strategy=%s, parameters=%s}", 
                    name, strategy, parameters);
        }
    }
    
    /**
     * 打包策略枚举
     */
    enum PackStrategy {
        CONCATENATE,         // 简单拼接
        LENGTH_PREFIX,       // 添加长度前缀
        HEADER_PAYLOAD,      // 头部+载荷结构
        FRAMED,             // 帧结构
        COMPRESSED,         // 压缩打包
        ENCRYPTED,          // 加密打包
        CUSTOM              // 自定义打包
    }
    
    /**
     * 打包结果
     */
    class PackResult {
        private final List<NetworkMessage> messages;
        private final String metadata;
        private final long totalSize;
        
        public PackResult(List<NetworkMessage> messages, String metadata, long totalSize) {
            this.messages = messages;
            this.metadata = metadata;
            this.totalSize = totalSize;
        }
        
        public List<NetworkMessage> getMessages() { return messages; }
        public String getMetadata() { return metadata; }
        public long getTotalSize() { return totalSize; }
        
        public int getMessageCount() { return messages.size(); }
        
        public boolean isEmpty() { return messages.isEmpty(); }
        
        @Override
        public String toString() {
            return String.format("PackResult{messageCount=%d, totalSize=%d, metadata='%s'}", 
                    messages.size(), totalSize, metadata);
        }
    }
    
    /**
     * 将分段数据打包成新的消息
     * 
     * @param segments 分段数据列表
     * @param segmentTypes 分段类型列表（可选）
     * @param rule 打包规则
     * @param originalMessage 原始消息（用于复制元数据）
     * @return 打包结果
     * @throws ProcessorException 打包异常
     */
    PackResult pack(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, 
                   NetworkMessage originalMessage) throws ProcessorException;
    
    /**
     * 获取打包器名称
     */
    String getName();
    
    /**
     * 检查是否支持指定的打包策略
     */
    boolean supports(PackStrategy strategy);
    
    /**
     * 获取支持的打包策略列表
     */
    List<PackStrategy> getSupportedStrategies();
} 