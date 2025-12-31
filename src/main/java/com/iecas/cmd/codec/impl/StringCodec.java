package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.EnumHelper;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 字符串编解码器
 */
public class StringCodec implements Codec {
    /**
     * 编码字符串为字节数组
     *
     * @param node    协议节点
     * @param context 上下文信息
     * @return 编码后的字节数组
     * @throws CodecException 编码异常
     */
    @Override
    public byte[] encode(INode node, Map<String, Object> context) throws CodecException {
        try {
            Object value = StringUtils.isNotEmpty(node.getFwdExpr()) ? node.getFwdExprResult() : node.getValue();
            if (value == null) {
                throw new CodecException("节点值不能为空");
            }

            // 处理枚举值
            value = EnumHelper.processEnumForEncode(node, value);

            String charset = node.getCharset();

            // 获取字符集
            Charset charsetObj = getCharset(charset);

            // 转换为字符串
            String strValue = value.toString();

            byte[] bytes = strValue.getBytes(charsetObj);

            // 获取节点定义的长度（字节）
            int requiredLength = node.getLength();

            int lengthInBytes = (requiredLength + 7) / 8; // 转换为字节长度

            if (bytes.length > lengthInBytes) {
                throw new CodecException("字符串长度超过最大限制: " + bytes.length + " > " + requiredLength);
            }

            // 如果长度不足，填充零字节（null字符）
            if (bytes.length < lengthInBytes) {
                byte[] paddedBytes = new byte[lengthInBytes];
                System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
                // 剩余位置自动填充为0（null字符）
                return paddedBytes;
            }

            // 长度刚好匹配
            return bytes;
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("字符串编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object decode(byte[] data, INode node, Map<String, Object> context) throws CodecException {
        try {
            if (data == null || data.length == 0) {
                throw new CodecException("数据不能为空");
            }

            String charset = node.getCharset();

            // 获取字符集
            Charset charsetObj = getCharset(charset);

            String result = new String(data, charsetObj);
            
            // 移除尾部的空字符（null字符）
            result = result.replaceAll("\0+$", "");

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, result);
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException("字符串解码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.STRING};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.STRING;
    }

    /**
     * 获取字符集
     *
     * @param charset 字符集名称
     * @return 字符集对象
     */
    private Charset getCharset(String charset) {
        if (charset == null || charset.isEmpty()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(charset);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }
} 