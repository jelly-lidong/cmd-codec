package com.iecas.cmd.validator;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 值域验证器
 */
public class RangeValidator {

    /**
     * 验证范围表达式是否合法
     *
     * @param range 范围字符串，支持多段范围，格式如"[1,200] || (300,400] || [500,600) || [700]"
     * @return true 如果范围表达式合法，false 否则
     */
    public static boolean isValidRange(String range) {
        if (range == null || range.isEmpty()) {
            return true; // 空范围视为合法
        }

        try {
            parseMultiRange(range);
            return true;
        } catch (CodecException e) {
            return false;
        }
    }

    /**
     * 验证值是否在指定范围内
     *
     * @param value     要验证的值
     * @param range     范围字符串，支持多段范围，格式如"[1,200] || (300,400] || [500,600) || [700]"
     * @param valueType 值类型
     * @throws CodecException 如果值超出范围
     */
    public static void validateRange(Object value, String range, ValueType valueType) throws CodecException {
        if (range == null || range.isEmpty()) {
            return;
        }

        if (isValidRange(range)) {
            throw new CodecException("无效的范围表达式: " + range);
        }

        // 解析多段范围
        List<Range> ranges = parseMultiRange(range);

        // 根据值类型进行验证
        switch (valueType) {
            case INT:
            case UINT:
                validateIntegerRange(value.toString(), ranges);
                break;
            case FLOAT:
                validateFloatRange(Double.parseDouble(value.toString()), ranges);
                break;
            case STRING:
                validateStringRange((String) value, ranges);
                break;
            case HEX:
                validateHexRange((String) value, ranges);
                break;
            case BIT:
                validateBitRange((String) value, ranges);
                break;
            default:
                // 其他类型暂不验证
                break;
        }
    }

    /**
     * 解析多段范围字符串
     * 支持格式: "[1,200] || (300,400] || [500,600) || [700]"
     */
    private static List<Range> parseMultiRange(String range) throws CodecException {
        List<Range> ranges = new ArrayList<>();

        try {
            // 检查是否以 || 结尾
            if (range.trim().endsWith("||")) {
                throw new CodecException("范围表达式不能以 || 结尾: " + range);
            }
            
            // 按 || 分割多个范围
            String[] rangeParts = range.split("\\|\\|");

            for (String rangePart : rangeParts) {
                rangePart = rangePart.trim();
                if (rangePart.isEmpty()) {
                    throw new CodecException("范围表达式不能包含空的段: " + range);
                }
                ranges.add(parseSingleRange(rangePart));
            }

            if (ranges.isEmpty()) {
                throw new CodecException("无效的范围格式: " + range);
            }

            return ranges;
        } catch (Exception e) {
            throw new CodecException("解析范围失败: " + range, e);
        }
    }

    /**
     * 解析单个范围字符串
     * 支持格式: "[1,200]", "(300,400]", "[500,600)", "[700]"
     */
    private static Range parseSingleRange(String range) throws CodecException {
        try {
            range = range.trim();

            // 检查是否为单点值 [x]
            if (range.matches("\\[\\s*[^,\\]]+\\s*]")) {
                String value = range.substring(1, range.length() - 1).trim();
                return new Range(value, value, true, true);
            }

            // 检查范围格式
            if (!range.matches("[(\\[].*[,\\s]+.*[)\\]]")) {
                throw new CodecException("无效的范围格式: " + range);
            }

            // 提取边界符号
            char leftBracket = range.charAt(0);
            char rightBracket = range.charAt(range.length() - 1);

            boolean minInclusive = (leftBracket == '[');
            boolean maxInclusive = (rightBracket == ']');

            // 移除边界符号
            String content = range.substring(1, range.length() - 1);
            String[] parts = content.split(",");

            if (parts.length != 2) {
                throw new CodecException("无效的范围格式: " + range);
            }

            String min = parts[0].trim();
            String max = parts[1].trim();

            // 检查值是否为空
            if (min.isEmpty() || max.isEmpty()) {
                throw new CodecException("范围值不能为空: " + range);
            }

            return new Range(min, max, minInclusive, maxInclusive);
        } catch (Exception e) {
            throw new CodecException("解析范围失败: " + range, e);
        }
    }

    /**
     * 验证整数范围
     */
    private static void validateIntegerRange(String value, List<Range> ranges) throws CodecException {
        long longValue = Long.parseLong(value);

        for (Range range : ranges) {
            if (isValueInRange(longValue, range)) {
                return; // 值在某个范围内，验证通过
            }
        }

        // 值不在任何范围内，抛出异常
        throw new CodecException(
                String.format("值 %d 超出所有范围: %s", longValue, formatRangesForDisplay(ranges)));
    }

    /**
     * 验证浮点数范围
     */
    private static void validateFloatRange(Number value, List<Range> ranges) throws CodecException {
        double doubleValue = value.doubleValue();

        for (Range range : ranges) {
            if (isValueInRange(doubleValue, range)) {
                return; // 值在某个范围内，验证通过
            }
        }

        // 值不在任何范围内，抛出异常
        throw new CodecException(String.format("值 %.2f 超出所有范围: %s", doubleValue, formatRangesForDisplay(ranges)));
    }

    /**
     * 验证字符串范围
     */
    private static void validateStringRange(String value, List<Range> ranges) throws CodecException {
        int length = value.length();

        for (Range range : ranges) {
            if (isValueInRange((long) length, range)) {
                return; // 值在某个范围内，验证通过
            }
        }

        // 值不在任何范围内，抛出异常
        throw new CodecException(
                String.format("字符串长度 %d 超出所有范围: %s", length, formatRangesForDisplay(ranges)));
    }

    /**
     * 验证十六进制范围
     */
    private static void validateHexRange(String value, List<Range> ranges) throws CodecException {
        // 移除0x前缀
        String cleanValue = value.startsWith("0x") || value.startsWith("0X") ?
                value.substring(2) : value;
        long longValue = Long.parseLong(cleanValue, 16);

        for (Range range : ranges) {
            if (isValueInRange(longValue, range)) {
                return; // 值在某个范围内，验证通过
            }
        }

        // 值不在任何范围内，抛出异常
        throw new CodecException(
                String.format("十六进制值 0x%X 超出所有范围: %s", longValue, formatRangesForDisplay(ranges)));
    }

    /**
     * 验证二进制范围
     */
    private static void validateBitRange(String value, List<Range> ranges) throws CodecException {
        // 移除0b前缀
        String cleanValue = value.startsWith("0b") || value.startsWith("0B") ?
                value.substring(2) : value;
        long longValue = Long.parseLong(cleanValue, 2);

        for (Range range : ranges) {
            if (isValueInRange(longValue, range)) {
                return; // 值在某个范围内，验证通过
            }
        }

        // 值不在任何范围内，抛出异常
        throw new CodecException(
                String.format("二进制值 0b%s 超出所有范围: %s",
                        Long.toBinaryString(longValue), formatRangesForDisplay(ranges)));
    }

    /**
     * 解析长整数值（支持十六进制、二进制和十进制）
     */
    private static long parseLong(String value) {
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return Long.parseLong(value.substring(2), 16);
        } else if (value.startsWith("0b") || value.startsWith("0B")) {
            return Long.parseLong(value.substring(2), 2);
        } else {
            return Long.parseLong(value);
        }
    }

    /**
     * 解析双精度浮点数值（支持十六进制、二进制和十进制）
     */
    private static double parseDouble(String value) {
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return (double) Long.parseLong(value.substring(2), 16);
        } else if (value.startsWith("0b") || value.startsWith("0B")) {
            return (double) Long.parseLong(value.substring(2), 2);
        } else {
            return Double.parseDouble(value);
        }
    }

    /**
     * 检查值是否在指定范围内（支持开闭区间）
     */
    private static boolean isValueInRange(long value, Range range) {
        long min = parseLong(range.getMin());
        long max = parseLong(range.getMax());

        boolean minCheck = range.isMinInclusive() ? (value >= min) : (value > min);
        boolean maxCheck = range.isMaxInclusive() ? (value <= max) : (value < max);

        return minCheck && maxCheck;
    }

    /**
     * 检查值是否在指定范围内（支持开闭区间）
     */
    private static boolean isValueInRange(double value, Range range) {
        double min = parseDouble(range.getMin());
        double max = parseDouble(range.getMax());

        boolean minCheck = range.isMinInclusive() ? (value >= min) : (value > min);
        boolean maxCheck = range.isMaxInclusive() ? (value <= max) : (value < max);

        return minCheck && maxCheck;
    }

    /**
     * 格式化范围列表用于显示
     */
    private static String formatRangesForDisplay(List<Range> ranges) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ranges.size(); i++) {
            if (i > 0) {
                sb.append(" || ");
            }
            Range range = ranges.get(i);
            sb.append(formatRangeForDisplay(range));
        }
        return sb.toString();
    }

    /**
     * 格式化单个范围用于显示
     */
    private static String formatRangeForDisplay(Range range) {
        String minBracket = range.isMinInclusive() ? "[" : "(";
        String maxBracket = range.isMaxInclusive() ? "]" : ")";

        // 如果是单点值
        if (range.getMin().equals(range.getMax())) {
            return "[" + range.getMin() + "]";
        }

        return minBracket + range.getMin() + "," + range.getMax() + maxBracket;
    }

    /**
     * 范围验证结果类
     */
    public static class RangeValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public RangeValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            if (valid) {
                return "RangeValidationResult{valid=true}";
            } else {
                return "RangeValidationResult{valid=false, errorMessage='" + errorMessage + "'}";
            }
        }
    }

    /**
     * 范围类
     */
    @Data
    private static class Range {
        private final String min;
        private final String max;
        private final boolean minInclusive;  // 最小值是否包含在范围内
        private final boolean maxInclusive;  // 最大值是否包含在范围内

        public Range(String min, String max, boolean minInclusive, boolean maxInclusive) {
            this.min = min;
            this.max = max;
            this.minInclusive = minInclusive;
            this.maxInclusive = maxInclusive;
        }
    }
} 