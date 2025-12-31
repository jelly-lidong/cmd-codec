package com.iecas.cmd.network.processor.impl;

import com.iecas.cmd.network.processor.MessageSegmenter;
import com.iecas.cmd.network.processor.ProcessorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 默认消息分段器实现
 */
public class DefaultMessageSegmenter implements MessageSegmenter {

    private static final String NAME = "DefaultMessageSegmenter";

    @Override
    public SegmentResult segment(byte[] data, SegmentRule rule) throws ProcessorException {
        if (data == null || data.length == 0) {
            return new SegmentResult(Collections.emptyList(), Collections.emptyList(), "空数据，无需分段");
        }

        if (rule == null) {
            throw new ProcessorException(NAME, "分段规则不能为空");
        }

        try {
            switch (rule.getStrategy()) {
                case FIXED_LENGTH:
                    return segmentByFixedLength(data, rule);
                case DELIMITER:
                    return segmentByDelimiter(data, rule);
                case LENGTH_PREFIX:
                    return segmentByLengthPrefix(data, rule);
                case PATTERN_MATCH:
                    return segmentByPattern(data, rule);
                case CUSTOM:
                    return segmentByCustom(data, rule);
                default:
                    throw new ProcessorException(NAME, "不支持的分段策略: " + rule.getStrategy());
            }
        } catch (Exception e) {
            throw new ProcessorException(NAME, "分段处理失败", e);
        }
    }

    /**
     * 固定长度分段
     */
    private SegmentResult segmentByFixedLength(byte[] data, SegmentRule rule) throws ProcessorException {
        Integer segmentLength = getParameterAsInteger(rule.getParameter());
        if (segmentLength == null || segmentLength <= 0) {
            throw new ProcessorException(NAME, "固定长度分段需要正整数参数");
        }

        List<byte[]> segments = new ArrayList<>();
        List<Integer> segmentTypes = new ArrayList<>();

        for (int i = 0; i < data.length; i += segmentLength) {
            int endIndex = Math.min(i + segmentLength, data.length);
            byte[] segment = Arrays.copyOfRange(data, i, endIndex);
            segments.add(segment);
            segmentTypes.add(0); // 默认类型
        }

        return new SegmentResult(segments, segmentTypes, String.format("固定长度分段完成，长度=%d，分段数=%d", segmentLength, segments.size()));
    }

    /**
     * 分隔符分段
     */
    private SegmentResult segmentByDelimiter(byte[] data, SegmentRule rule) throws ProcessorException {
        byte[] delimiter = getParameterAsBytes(rule.getParameter());
        if (delimiter == null || delimiter.length == 0) {
            throw new ProcessorException(NAME, "分隔符分段需要非空字节数组参数");
        }

        List<byte[]> segments = new ArrayList<>();
        List<Integer> segmentTypes = new ArrayList<>();

        int start = 0;
        int delimiterIndex;

        while ((delimiterIndex = indexOf(data, delimiter, start)) != -1) {
            if (delimiterIndex > start) {
                byte[] segment = Arrays.copyOfRange(data, start, delimiterIndex);
                segments.add(segment);
                segmentTypes.add(0);
            }
            start = delimiterIndex + delimiter.length;
        }

        // 处理最后一段
        if (start < data.length) {
            byte[] segment = Arrays.copyOfRange(data, start, data.length);
            segments.add(segment);
            segmentTypes.add(0);
        }

        return new SegmentResult(segments, segmentTypes, String.format("分隔符分段完成，分隔符长度=%d，分段数=%d", delimiter.length, segments.size()));
    }

    /**
     * 长度前缀分段
     */
    private SegmentResult segmentByLengthPrefix(byte[] data, SegmentRule rule) throws ProcessorException {
        Integer prefixLength = getParameterAsInteger(rule.getParameter());
        if (prefixLength == null || prefixLength <= 0 || prefixLength > 4) {
            throw new ProcessorException(NAME, "长度前缀分段需要1-4字节的前缀长度参数");
        }

        List<byte[]> segments = new ArrayList<>();
        List<Integer> segmentTypes = new ArrayList<>();

        int offset = 0;
        while (offset < data.length) {
            if (offset + prefixLength > data.length) {
                // 剩余数据不足以包含长度前缀
                break;
            }

            // 读取长度前缀
            int segmentLength = readLength(data, offset, prefixLength);
            if (segmentLength < 0 || offset + prefixLength + segmentLength > data.length) {
                // 长度无效或数据不足
                break;
            }

            // 提取数据段（不包含长度前缀）
            byte[] segment = Arrays.copyOfRange(data, offset + prefixLength, offset + prefixLength + segmentLength);
            segments.add(segment);
            segmentTypes.add(0);

            offset += prefixLength + segmentLength;
        }

        return new SegmentResult(segments, segmentTypes, String.format("长度前缀分段完成，前缀长度=%d字节，分段数=%d", prefixLength, segments.size()));
    }

    /**
     * 模式匹配分段
     */
    private SegmentResult segmentByPattern(byte[] data, SegmentRule rule) throws ProcessorException {
        String patternStr = getParameterAsString(rule.getParameter());
        if (patternStr == null || patternStr.trim().isEmpty()) {
            throw new ProcessorException(NAME, "模式匹配分段需要有效的正则表达式参数");
        }

        // 将字节数据转换为字符串进行模式匹配
        String dataStr = new String(data);
        Pattern pattern = Pattern.compile(patternStr);

        List<byte[]> segments = new ArrayList<>();
        List<Integer> segmentTypes = new ArrayList<>();

        String[] parts = pattern.split(dataStr);
        for (String part : parts) {
            if (!part.isEmpty()) {
                segments.add(part.getBytes());
                segmentTypes.add(0);
            }
        }

        return new SegmentResult(segments, segmentTypes, String.format("模式匹配分段完成，模式=%s，分段数=%d", patternStr, segments.size()));
    }

    /**
     * 自定义分段（简单示例实现）
     */
    private SegmentResult segmentByCustom(byte[] data, SegmentRule rule) throws ProcessorException {
        // 这里可以根据具体业务需求实现自定义分段逻辑
        // 示例：按照特定的协议头进行分段

        List<byte[]> segments = new ArrayList<>();
        List<Integer> segmentTypes = new ArrayList<>();

        // 简单示例：每256字节为一段
        int segmentSize = 256;
        for (int i = 0; i < data.length; i += segmentSize) {
            int endIndex = Math.min(i + segmentSize, data.length);
            byte[] segment = Arrays.copyOfRange(data, i, endIndex);
            segments.add(segment);
            segmentTypes.add(1); // 自定义类型
        }

        return new SegmentResult(segments, segmentTypes, String.format("自定义分段完成，分段数=%d", segments.size()));
    }

    /**
     * 在字节数组中查找子数组的位置
     */
    private int indexOf(byte[] data, byte[] pattern, int start) {
        if (pattern.length == 0) return start;
        if (start >= data.length) return -1;

        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 从字节数组中读取长度值
     */
    private int readLength(byte[] data, int offset, int lengthBytes) {
        int length = 0;
        for (int i = 0; i < lengthBytes; i++) {
            length = (length << 8) | (data[offset + i] & 0xFF);
        }
        return length;
    }

    /**
     * 获取参数作为整数
     */
    private Integer getParameterAsInteger(Object parameter) {
        if (parameter instanceof Integer) {
            return (Integer) parameter;
        } else if (parameter instanceof String) {
            try {
                return Integer.parseInt((String) parameter);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取参数作为字节数组
     */
    private byte[] getParameterAsBytes(Object parameter) {
        if (parameter instanceof byte[]) {
            return (byte[]) parameter;
        } else if (parameter instanceof String) {
            return ((String) parameter).getBytes();
        }
        return null;
    }

    /**
     * 获取参数作为字符串
     */
    private String getParameterAsString(Object parameter) {
        if (parameter instanceof String) {
            return (String) parameter;
        } else if (parameter instanceof byte[]) {
            return new String((byte[]) parameter);
        }
        return parameter != null ? parameter.toString() : null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supports(SegmentStrategy strategy) {
        return strategy != null; // 支持所有策略
    }

    @Override
    public List<SegmentStrategy> getSupportedStrategies() {
        return Arrays.asList(SegmentStrategy.values());
    }
} 