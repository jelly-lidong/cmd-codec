package com.iecas.cmd.network.processor;

import java.util.List;

/**
 * 消息分段器接口
 * 用于将字节数据按照特定规则分段
 */
public interface MessageSegmenter {
    
    /**
     * 分段规则配置
     */
    class SegmentRule {
        private final String name;
        private final String description;
        private final SegmentStrategy strategy;
        private final Object parameter;
        
        public SegmentRule(String name, String description, SegmentStrategy strategy, Object parameter) {
            this.name = name;
            this.description = description;
            this.strategy = strategy;
            this.parameter = parameter;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public SegmentStrategy getStrategy() { return strategy; }
        public Object getParameter() { return parameter; }
        
        @Override
        public String toString() {
            return String.format("SegmentRule{name='%s', strategy=%s, parameter=%s}", 
                    name, strategy, parameter);
        }
    }
    
    /**
     * 分段策略枚举
     */
    enum SegmentStrategy {
        FIXED_LENGTH,        // 固定长度分段
        DELIMITER,           // 分隔符分段
        LENGTH_PREFIX,       // 长度前缀分段
        PATTERN_MATCH,       // 模式匹配分段
        CUSTOM              // 自定义分段
    }
    
    /**
     * 分段结果
     */
    class SegmentResult {
        private final List<byte[]> segments;
        private final List<Integer> segmentTypes;
        private final String metadata;
        
        public SegmentResult(List<byte[]> segments, List<Integer> segmentTypes, String metadata) {
            this.segments = segments;
            this.segmentTypes = segmentTypes;
            this.metadata = metadata;
        }
        
        public List<byte[]> getSegments() { return segments; }
        public List<Integer> getSegmentTypes() { return segmentTypes; }
        public String getMetadata() { return metadata; }
        
        public int getSegmentCount() { return segments.size(); }
        
        @Override
        public String toString() {
            return String.format("SegmentResult{segmentCount=%d, metadata='%s'}", 
                    segments.size(), metadata);
        }
    }
    
    /**
     * 对字节数据进行分段
     * 
     * @param data 原始字节数据
     * @param rule 分段规则
     * @return 分段结果
     * @throws ProcessorException 分段异常
     */
    SegmentResult segment(byte[] data, SegmentRule rule) throws ProcessorException;
    
    /**
     * 获取分段器名称
     */
    String getName();
    
    /**
     * 检查是否支持指定的分段策略
     */
    boolean supports(SegmentStrategy strategy);
    
    /**
     * 获取支持的分段策略列表
     */
    List<SegmentStrategy> getSupportedStrategies();
} 