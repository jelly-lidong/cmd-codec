package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.EnumHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Map;

/**
 * 无符号整数编解码器
 */
@Slf4j
public class UnsignedIntCodec implements Codec {
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
            int lengthInBits = node.getLength(); // 长度单位为位
            int lengthInBytes = (lengthInBits + 7) / 8; // 转换为字节长度
            boolean bigEndian = node.isBigEndian();

            // 解析整数值
            BigInteger longValue;
            if (value instanceof Number) {
                longValue = BigInteger.valueOf(Long.parseLong(value.toString()));
            } else {
                String strValue = value.toString();
                try {

                    // 处理十六进制格式
                    if (strValue.startsWith("0x") || strValue.startsWith("0X")) {
                        longValue = BigInteger.valueOf(Long.parseLong(strValue.substring(2), 16));
                    } else {
                        longValue = BigInteger.valueOf(Long.parseLong(strValue));
                    }
                } catch (NumberFormatException e) {
                    try {
                        long parseDouble = (long) Double.parseDouble(strValue);
                        longValue = BigInteger.valueOf(parseDouble);
                    }catch (Exception e2) {
                        throw new CodecException("无效的整数值: " + value);
                    }
                }
            }


            BigInteger maxValue64 = BigInteger.valueOf(2).pow(lengthInBits).subtract(BigInteger.ONE);
            log.debug("无符号整数值: 最大值: {}, 长度: {} 位", maxValue64, lengthInBits);

            if (longValue.compareTo(BigInteger.valueOf(0L)) < 0 || longValue.compareTo(maxValue64) > 0) {
                throw new CodecException("无符号整数值超出范围: " + longValue + ", 最大值: " + maxValue64);
            }

            // 转换为字节数组
            return ByteUtil.signedIntToBytes(longValue.longValue(), lengthInBytes, bigEndian);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("无符号整数编码失败: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        long parseDouble = (long) Double.parseDouble("1565.232");
        BigInteger longValue = BigInteger.valueOf(parseDouble);
        System.out.println(longValue);
    }

    @Override
    public Object decode(byte[] data, INode node, Map<String, Object> context) throws CodecException {
        try {
            if (data == null || data.length == 0) {
                throw new CodecException("数据不能为空");
            }

            // 获取节点属性
            int lengthInBits = node.getLength(); // 长度单位为位
            int lengthInBytes = (lengthInBits + 7) / 8; // 转换为字节长度
            boolean bigEndian = node.isBigEndian();

            // 验证数据长度
            if (data.length < lengthInBytes) {
                throw new CodecException("数据长度不足: " + data.length + " < " + lengthInBytes);
            }

            // 转换为整数
            long value = ByteUtil.bytesToUnsignedInt(data, bigEndian);

            // 处理无符号整数（按位计算）
            if (lengthInBits == 64) {
                // 64位无符号整数，不需要清除高位，保持原值
                // 注意：对于64位，我们返回原始值，因为Java的long类型无法表示完整的64位无符号整数
                // 如果需要完整的64位无符号整数支持，建议使用BigInteger类型
                log.debug("64位无符号整数解码，保持原始值: {}", value);
            } else if (lengthInBits > 64) {
                // 对于超过64位的长度，抛出异常
                throw new CodecException("不支持超过64位的无符号整数长度: " + lengthInBits);
            } else {
                // 清除高位，确保值在指定位数范围内
                value &= (1L << lengthInBits) - 1;
            }

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, value);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("无符号整数解码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.UINT};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.UINT;
    }
} 