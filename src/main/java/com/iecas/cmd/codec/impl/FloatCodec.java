package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.EnumHelper;
import com.iecas.cmd.util.HexHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * 浮点数编解码器
 */
public class FloatCodec implements Codec {


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
            boolean bigEndian = node.isBigEndian();

            // 解析浮点数值
            double doubleValue;
            if (value instanceof Number) {
                doubleValue = ((Number) value).doubleValue();
            } else {
                try {
                    doubleValue = Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    if (HexHelper.isHexFloat(value.toString())) {
                        try{
                            value = HexHelper.normalizeHexString(value.toString());
                            if (((String) value).length() == 8){
                                doubleValue =  ByteUtil.bytesToFloat(ByteUtil.hexStringToBytes(value.toString()));
                            }else{
                                doubleValue =  ByteUtil.bytesToDouble(ByteUtil.hexStringToBytes(value.toString()));
                            }
                            if (Double.isNaN(doubleValue)){
                                return ByteUtil.hexStringToBytes(value.toString());
                            }
                        }catch (Exception ex){
                            throw new CodecException("无效的浮点数值: " + value);
                        }
                    }else{
                        throw new CodecException("无效的浮点数值: " + value);
                    }
                }
            }

            // 根据长度选择单精度或双精度
            ByteBuffer buffer;
            if (length == 32) {
                buffer = ByteBuffer.allocate(4);
                // 先设置字节序，再写入数据
                buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                buffer.putFloat((float) doubleValue);
            } else if (length == 64) {
                buffer = ByteBuffer.allocate(8);
                // 先设置字节序，再写入数据
                buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                buffer.putDouble(doubleValue);
            } else {
                throw new CodecException("不支持的浮点数长度: " + length);
            }
            byte[] bytes = buffer.array();
            if (!node.isBigEndian()) {
                ArrayUtils.reverse(bytes);
            }
            return bytes;
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("浮点数编码失败: " + e.getMessage(), e);
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
            boolean bigEndian = node.isBigEndian();

            // 验证数据长度
            if (data.length * 8 < length) {
                throw new CodecException("数据长度不足: " + data.length + " < " + length);
            }

            // 根据长度选择单精度或双精度
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

            Object result;
            if (length == 32) {
                result = buffer.getFloat();
            } else if (length == 64) {
                result = buffer.getDouble();
            } else {
                throw new CodecException("不支持的浮点数长度: " + length);
            }

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, result);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("浮点数解码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.FLOAT};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.FLOAT;
    }
} 