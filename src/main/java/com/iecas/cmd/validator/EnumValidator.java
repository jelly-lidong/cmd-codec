package com.iecas.cmd.validator;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.EnumRange;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.util.HexHelper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 枚举值验证器
 */
public class EnumValidator {
    
    /**
     * 验证值是否为有效的枚举值
     * @param value 要验证的值
     * @param node 协议节点
     * @throws CodecException 如果值不是有效的枚举值
     */
    public static void validateEnum(Object value, INode node) throws CodecException {
        // 获取枚举范围
        List<EnumRange> enumRanges = getEnumRanges(node);
        if (CollectionUtils.isEmpty(enumRanges)) {
            return; // 没有枚举定义，跳过验证
        }

        String strValue = value.toString();
        boolean valid = enumRanges.stream().anyMatch(enumRange -> HexHelper.compareHexStrings(enumRange.getValue(),strValue));
            
        if (!valid) {
            throw new CodecException(
                String.format("值 '%s' 不是有效的枚举值，有效值包括: %s",
                    strValue,
                    enumRanges.stream()
                        .map(e -> String.format("%s(%s)", e.getValue(), e.getDesc()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("无")));
        }
    }

    /**
     * 获取枚举值的描述
     * @param value 枚举值
     * @param node 协议节点
     * @return 枚举值的描述
     * @throws CodecException 如果值不是有效的枚举值
     */
    public static String getEnumDesc(Object value, INode node) throws CodecException {
        List<EnumRange> enumRanges = getEnumRanges(node);
        if (enumRanges == null || enumRanges.isEmpty()) {
            return value.toString();
        }

        return enumRanges.stream()
            .filter(enumRange -> HexHelper.compareHexStrings(enumRange.getValue(),value.toString()))
            .findFirst()
            .map(EnumRange::getDesc)
            .orElseThrow(() -> new CodecException(
                String.format("值 '%s' 不是有效的枚举值", value.toString())));
    }

    /**
     * 根据描述获取枚举值
     * @param desc 枚举值描述
     * @param node 协议节点
     * @return 枚举值
     * @throws CodecException 如果描述不是有效的枚举值描述
     */
    public static String getEnumValue(String desc, INode node) throws CodecException {
        List<EnumRange> enumRanges = getEnumRanges(node);
        if (enumRanges == null || enumRanges.isEmpty()) {
            return desc;
        }

        return enumRanges.stream()
            .filter(enumRange -> enumRange.getDesc().equals(desc))
            .findFirst()
            .map(EnumRange::getValue)
            .orElseThrow(() -> new CodecException(
                String.format("描述 '%s' 不是有效的枚举值描述", desc)));
    }

    /**
     * 检查节点是否有枚举定义
     * @param node 协议节点
     * @return 是否有枚举定义
     */
    public static boolean hasEnumDefinition(INode node) {
        List<EnumRange> enumRanges = getEnumRanges(node);
        return enumRanges != null && !enumRanges.isEmpty();
    }

    /**
     * 获取所有枚举值
     * @param node 协议节点
     * @return 枚举值列表
     */
    public static List<String> getAllEnumValues(INode node) {
        List<EnumRange> enumRanges = getEnumRanges(node);
        if (enumRanges == null || enumRanges.isEmpty()) {
            return Collections.emptyList();
        }

        return enumRanges.stream()
            .map(EnumRange::getValue)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有枚举描述
     * @param node 协议节点
     * @return 枚举描述列表
     */
    public static List<String> getAllEnumDescs(INode node) {
        List<EnumRange> enumRanges = getEnumRanges(node);
        if (enumRanges == null || enumRanges.isEmpty()) {
            return Collections.emptyList();
        }

        return enumRanges.stream()
            .map(EnumRange::getDesc)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 从协议节点获取枚举范围
     * @param node 协议节点
     * @return 枚举范围列表
     */
    private static List<EnumRange> getEnumRanges(INode node) {
        if (node instanceof Node) {
            return node.getEnumRanges();
        }
        return new ArrayList<>();
    }
} 