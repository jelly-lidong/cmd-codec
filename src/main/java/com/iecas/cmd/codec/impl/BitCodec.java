package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.EnumHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 二进制编解码器
 * 支持位级别的编解码操作
 */
public class BitCodec implements Codec {

    @Override
    public byte[] encode(INode node, Map<String, Object> context) throws CodecException {
        try {
            Object value = StringUtils.isNotEmpty(node.getFwdExpr()) ? node.getFwdExprResult() : node.getValue();
            if (value == null) {
                throw new CodecException("节点值不能为空");
            }

            // 处理枚举值
            value = EnumHelper.processEnumForEncode(node, value);

            // 获取节点属性
            int length = node.getLength();

            // 首先验证长度有效性
            if (length <= 0 || length > 64) {
                throw new CodecException("无效的位长度: " + length + ", 必须在1到64之间");
            }

            // 解析二进制值
            String strValue = value.toString();
            long longValue;

            // 处理不同格式的输入
            if (strValue.startsWith("0b") || strValue.startsWith("0B")) {
                // 二进制格式
                strValue = strValue.substring(2);
            }
            longValue = Long.parseLong(strValue, 2);
            // 验证值范围
            long maxValue = (1L << length) - 1;
            if (longValue < 0 || longValue > maxValue) {
                throw new CodecException("值超出范围: " + longValue + ", 最大值: " + maxValue + " (长度: " + length + "位)");
            }

            // 根据节点定义的长度生成固定长度的字节数组
            return bitsToBytes(longValue, length);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("二进制编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object decode(byte[] data, INode node, Map<String, Object> context) throws CodecException {
        try {
            if (data == null || data.length == 0) {
                throw new CodecException("数据不能为空");
            }

            // 获取节点属性
            int length = node.getLength();

            // 计算需要的最小字节数
            int requiredBytes = (length + 7) / 8; // 向上取整
            if (data.length < requiredBytes) {
                throw new CodecException("数据长度不足: 需要至少 " + requiredBytes + " 字节，但只有 " + data.length + " 字节");
            }

            // 将字节数组转换为整数值
            long value = 0;
            for (int i = 0; i < data.length; i++) {
                value = (value << 8) | (data[i] & 0xFF);
            }

            // 如果位长度小于总位数，需要截取指定长度的位
            if (length < data.length * 8) {
                // 从低位开始截取指定长度的位（与encode的bitsToBytes逻辑一致）
                // 清除高位多余的位，保留低位的指定长度位
                long mask = (1L << length) - 1;
                value &= mask;
            }

            // 对于1位的位字段，直接返回数值而不是二进制格式
            String result;
            if (length == 1) {
                result = String.valueOf(value);
            } else {
                result = getResult(value, length);
            }

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, result);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("二进制解码失败: " + e.getMessage(), e);
        }
    }

    private static String getResult(long value, int length) {
        String result;
        if (value == 0) {
            // 对于零值，根据长度生成相应的零字符串
            StringBuilder zeroStr = new StringBuilder();
            for (int i = 0; i < length; i++) {
                zeroStr.append('0');
            }
            result = "0b" + zeroStr;
        } else {
            StringBuilder binaryResult = new StringBuilder(Long.toBinaryString(value));
            // 对于非零值，补齐前导零到指定长度
            while (binaryResult.length() < length) {
                binaryResult.insert(0, "0");
            }
            result = "0b" + binaryResult;
        }
        return result;
    }

    /**
     * 将位值转换为指定长度的字节数组
     *
     * @param value     位值
     * @param bitLength 位长度
     * @return 字节数组，长度为 (bitLength + 7) / 8
     */
    private byte[] bitsToBytes(long value, int bitLength) {
        // 计算需要的字节数（向上取整）
        int byteLength = (bitLength + 7) / 8;
        byte[] result = new byte[byteLength];

        // 从低位到高位填充字节数组（大端序）
        for (int i = 0; i < byteLength; i++) {
            int byteIndex = byteLength - 1 - i; // 大端序：高位字节在前
            int shiftBits = i * 8;

            if (shiftBits < 64) { // 避免移位溢出
                result[byteIndex] = (byte) ((value >> shiftBits) & 0xFF);
            } else {
                result[byteIndex] = 0; // 超出long范围的高位填充0
            }
        }

        return result;
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.BIT};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.BIT;
    }
} 