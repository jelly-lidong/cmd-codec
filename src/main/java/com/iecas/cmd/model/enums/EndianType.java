package com.iecas.cmd.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 字节序枚举
 */
@Getter
public enum EndianType {
    /**
     * 小端
     */
    LITTLE("小端", 1),
    /**
     * 大端：
     */
    BIG("大端", 2);

    @JsonValue
    private final String value;

    @EnumValue
    private final int code;

    EndianType(String value, int code) {
        this.value = value;
        this.code = code;
    }

    /**
     * 根据字符串获取对应的枚举值
     * @param value 字符串值
     * @return 枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static EndianType fromString(String value) {
        for (EndianType endianType : EndianType.values()) {
            if (endianType.value.equals(value)) {
                return endianType;
            }
        }
        throw new IllegalArgumentException("无效的字节序: " + value);
    }

    /**
     * 根据编码获取对应的枚举值
     * @param code 编码值
     * @return 枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static EndianType fromCode(int code) {
        for (EndianType endianType : EndianType.values()) {
            if (endianType.code == code) {
                return endianType;
            }
        }
        throw new IllegalArgumentException("无效的字节序编码: " + code);
    }

    @Override
    public String toString() {
        return value;
    }
} 