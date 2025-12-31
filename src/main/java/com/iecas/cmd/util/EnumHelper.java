package com.iecas.cmd.util;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.EnumRange;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.validator.EnumValidator;

import java.util.List;

/**
 * 枚举处理工具类
 * 用于在编解码过程中处理枚举值的转换
 */
public class EnumHelper {

    /**
     * 处理编码时的枚举值
     * 如果节点有枚举定义，则验证值的有效性
     * 如果传入的是枚举描述，则转换为对应的枚举值
     *
     * @param node  协议节点
     * @param value 原始值
     * @return 处理后的值
     */
    public static Object processEnumForEncode(INode node, Object value) throws CodecException {
        if (value == null || !EnumValidator.hasEnumDefinition(node)) {
            return value;
        }

        String strValue = value.toString();
        List<EnumRange> enumRanges = node.getEnumRanges();

        // 检查是否是有效的枚举值
        boolean isValidValue = enumRanges.stream()
                .anyMatch(enumRange -> enumRange.getValue().equals(strValue));

        if (isValidValue) {
            return value;
        }

        // 检查是否是枚举描述，如果是则转换为枚举值
        boolean isValidDesc = enumRanges.stream()
                .anyMatch(enumRange -> enumRange.getDesc().equals(strValue));

        if (isValidDesc) {
            return EnumValidator.getEnumValue(strValue, node);
        }

        // 如果既不是有效的枚举值也不是有效的描述，则进行验证（会抛出异常）
        EnumValidator.validateEnum(value, node);
        return value;
    }

    /**
     * 处理解码时的枚举值
     * 如果节点有枚举定义，则验证值的有效性并可选择性地返回描述信息
     *
     * @param node  协议节点
     * @param value 解码得到的值
     * @return 处理后的值
     */
    public static Object processEnumForDecode(INode node, Object value) throws CodecException {
        if (value == null || !EnumValidator.hasEnumDefinition(node)) {
            return value;
        }

        // System.out.println("节点 " + node.getName() + " 的枚举值 " + value + " 对应描述 " + desc);
        return EnumValidator.getEnumDesc(value, node);

    }

    /**
     * 获取枚举信息字符串（用于日志和调试）
     *
     * @param node 协议节点
     * @return 枚举信息字符串
     */
    public static String getEnumInfo(INode node) {
        if (!EnumValidator.hasEnumDefinition(node)) {
            return "无枚举定义";
        }

        List<EnumRange> enumRanges = node.getEnumRanges();
        StringBuilder sb = new StringBuilder();
        sb.append("枚举定义: ");

        for (int i = 0; i < enumRanges.size(); i++) {
            EnumRange enumRange = enumRanges.get(i);
            sb.append(enumRange.getValue())
                    .append("(")
                    .append(enumRange.getDesc())
                    .append(")");

            if (i < enumRanges.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    /**
     * 检查值是否为有效的枚举值或枚举描述
     *
     * @param node  协议节点
     * @param value 要检查的值
     * @return 检查结果
     */
    public static EnumCheckResult checkEnumValue(INode node, Object value) {
        if (value == null || !EnumValidator.hasEnumDefinition(node)) {
            return new EnumCheckResult(false, false, false, value.toString());
        }

        String strValue = value.toString();
        List<EnumRange> enumRanges = node.getEnumRanges();

        boolean isValidValue = enumRanges.stream()
                .anyMatch(enumRange -> enumRange.getValue().equals(strValue));

        boolean isValidDesc = enumRanges.stream()
                .anyMatch(enumRange -> enumRange.getDesc().equals(strValue));

        return new EnumCheckResult(true, isValidValue, isValidDesc, strValue);
    }

    /**
     * 枚举检查结果
     */
    public static class EnumCheckResult {
        private final boolean hasEnumDefinition;
        private final boolean isValidValue;
        private final boolean isValidDescription;
        private final String inputValue;

        public EnumCheckResult(boolean hasEnumDefinition, boolean isValidValue,
                               boolean isValidDescription, String inputValue) {
            this.hasEnumDefinition = hasEnumDefinition;
            this.isValidValue = isValidValue;
            this.isValidDescription = isValidDescription;
            this.inputValue = inputValue;
        }

        public boolean hasEnumDefinition() {
            return hasEnumDefinition;
        }

        public boolean isValidValue() {
            return isValidValue;
        }

        public boolean isValidDescription() {
            return isValidDescription;
        }

        public boolean isValid() {
            return !hasEnumDefinition || isValidValue || isValidDescription;
        }

        public String getInputValue() {
            return inputValue;
        }

        @Override
        public String toString() {
            return String.format("EnumCheckResult{hasEnum=%s, validValue=%s, validDesc=%s, input='%s'}",
                    hasEnumDefinition, isValidValue, isValidDescription, inputValue);
        }
    }
} 