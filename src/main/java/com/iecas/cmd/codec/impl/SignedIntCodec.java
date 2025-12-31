package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.EnumHelper;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Map;

/**
 * 有符号整数编解码器
 * <p>
 * 计算公式
 * 对于n位有符号整数，取值范围是：
 * 最小值：-2^(n-1)
 * 最大值：2^(n-1) - 1
 * 对于3位：
 * 最小值：-2^(3-1) = -2^2 = -4
 * 最大值：2^(3-1) - 1 = 2^2 - 1 = 4 - 1 = 3
 * 为什么会这样？
 * 在二进制补码表示中：
 * 最高位是符号位：0表示正数，1表示负数
 * 正数：直接用二进制表示（000=0, 001=1, 010=2, 011=3）
 * 负数：用补码表示
 * 100（二进制）= -4
 * 101（二进制）= -3
 * 110（二进制）= -2
 * 111（二进制）= -1
 * 为什么负数比正数多一个？
 * 这是因为0占用了一个正数位置。在3位中：
 * 正数：0, 1, 2, 3（4个数）
 * 负数：-1, -2, -3, -4（4个数）
 * 总共：8个数（2^3 = 8）
 * 所以3位有符号整数的范围确实是 -4 到 3，这是二进制补码表示的标准特性。
 * </p>
 */
public class SignedIntCodec implements Codec {

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
            long longValue;
            if (value instanceof Number) {
                longValue = ((Number) value).longValue();
            } else {
                String strValue = value.toString();
                try {
                    // 处理十六进制格式
                    if (strValue.startsWith("0x") || strValue.startsWith("0X")) {
                        longValue = Long.parseLong(strValue.substring(2), 16);
                    } else {
                        longValue = (long) Double.parseDouble(strValue);
                    }
                } catch (NumberFormatException e) {
                    try {
                        longValue = (long) Double.parseDouble(strValue);
                    }catch (Exception e2) {
                        throw new CodecException("无效的有符号整数值: " + value);
                    }
                }
            }//011  111   100     100

            // 验证值范围（按位计算）
            long maxValue = (1L << (lengthInBits - 1)) - 1;
            long minValue = -(1L << (lengthInBits - 1));
            if (longValue > maxValue || longValue < minValue) {
                throw new CodecException("有符号整数值超出范围: " + longValue + ", 范围: [" + minValue + ", " + maxValue + "]");
            }

            // 转换为字节数组
            return ByteUtil.signedIntToBytes(longValue, lengthInBytes, bigEndian);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("有符号整数编码失败: " + e.getMessage(), e);
        }
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

            // 先按无符号整数读取，避免字节级符号扩展
            long value = ByteUtil.bytesToUnsignedInt(data, bigEndian);

            // 处理有符号整数的位级符号扩展
            if (lengthInBits < 64) {
                // 清除超出位长度的高位
                long mask = (1L << lengthInBits) - 1;
                value &= mask;
                
                // 检查符号位并进行符号扩展
                long signBit = 1L << (lengthInBits - 1);
                if ((value & signBit) != 0) {
                    // 负数：进行符号扩展
                    value |= -(1L << lengthInBits);
                }
            }

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, value);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("有符号整数解码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.INT};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.INT;
    }
} 