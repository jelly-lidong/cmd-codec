package com.iecas.cmd.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 值类型枚举
 */
@Getter
public enum ValueType {
    HEX("十六进制", 1),
    BIT("二进制", 2),
    INT("有符号整数", 3),
    UINT("无符号整数", 4),
    FLOAT("浮点数", 5),
    STRING("字符串", 6),
    TIME("时间",7);

    @JsonValue
    private final String desc;
    @EnumValue
    private final int code;

    ValueType(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    public static ValueType parse(String cmdLengthAndType) {
        if (cmdLengthAndType.contains("U")) {
            return UINT;
        }
        if (cmdLengthAndType.contains("H")) {
            return HEX;
        }
        if (cmdLengthAndType.contains("B")) {
            return BIT;
        }
        if (cmdLengthAndType.contains("Protocol")) {
            return INT;
        }
        if (cmdLengthAndType.contains("F")) {
            return FLOAT;
        }
        if (cmdLengthAndType.contains("S")) {
            return STRING;
        }

        throw new UnsupportedOperationException("不支持类型：" + cmdLengthAndType);
    }

    /**
     * 根据字符串获取对应的枚举值
     *
     * @param value 字符串值
     * @return 枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static ValueType fromString(String value) {
        for (ValueType type : ValueType.values()) {
            if (type.desc.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的值类型: " + value);
    }

    /**
     * 根据编码获取对应的枚举值
     *
     * @param code 编码值
     * @return 枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static ValueType fromCode(int code) {
        for (ValueType type : ValueType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的值类型编码: " + code);
    }

    public static ValueType fromDesc(String desc) {
        for (ValueType type : ValueType.values()) {
            if (type.desc.equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的值类型编码: " + desc);
    }

    @Override
    public String toString() {
        return desc;
    }
} 