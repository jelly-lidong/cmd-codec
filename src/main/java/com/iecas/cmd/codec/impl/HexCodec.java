package com.iecas.cmd.codec.impl;

import com.iecas.cmd.codec.Codec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.EnumHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 十六进制编解码器
 */
public class HexCodec implements Codec {

    @Override
    public byte[] encode(INode node, Map<String, Object> context) throws CodecException {
        try {
            byte[] data;
            Object value = StringUtils.isNotEmpty(node.getFwdExpr()) ? node.getFwdExprResult() : node.getValue();
            if (value == null) {
                throw new CodecException("节点值不能为空");
            }

            // 处理枚举值
            value = EnumHelper.processEnumForEncode(node, value);

            String hexStr = value.toString();
            // 移除可能存在的0x前缀
            if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
                hexStr = hexStr.substring(2);
            }

            if (hexStr.contains("H")){
                hexStr = hexStr.replace("H", "");
            }

            // 处理空字符串情况
            if (hexStr.isEmpty()) {
                hexStr = "0";
            }

            // 验证十六进制字符串
            if (!hexStr.matches("[0-9A-Fa-f]+")) {
                throw new CodecException("无效的十六进制字符串: " + hexStr);
            }

            // 确保字符串长度为偶数
            if (hexStr.length() % 2 != 0) {
                hexStr = "0" + hexStr;
            }

            // 获取节点定义的长度（位）
            int lengthInBits = node.getLength();

            if (lengthInBits == 0) {
                //todo 计算方式错误
                //lengthInBits = hexStr.length() * 8;
                lengthInBits = ByteUtil.hexStringToBytes(hexStr).length * 8;

                ((Node)node).setLength(lengthInBits);
            }

            int requiredBytes = (lengthInBits + 7) / 8; // 计算需要的字节数

            // 转换为字节数组
            byte[] originalBytes = ByteUtil.hexStringToBytes(hexStr);
            
            // 验证值是否超出范围
            if (originalBytes.length > requiredBytes) {
                // 检查高位字节是否都为0，如果不是则值超出范围
                boolean hasNonZeroHighBits = false;
                for (int i = 0; i < originalBytes.length - requiredBytes; i++) {
                    if (originalBytes[i] != 0) {
                        hasNonZeroHighBits = true;
                        break;
                    }
                }
                if (hasNonZeroHighBits) {
                    throw new CodecException("十六进制值超出范围，需要 " + requiredBytes + " 字节，但值需要 " + originalBytes.length + " 字节");
                }
                // 截取低位字节
                byte[] truncatedBytes = new byte[requiredBytes];
                System.arraycopy(originalBytes, originalBytes.length - requiredBytes, truncatedBytes, 0, requiredBytes);
                data = truncatedBytes;
            } else if (originalBytes.length < requiredBytes) {
                // 需要填充高位零字节
                byte[] paddedBytes = new byte[requiredBytes];
                System.arraycopy(originalBytes, 0, paddedBytes, requiredBytes - originalBytes.length, originalBytes.length);
                data =  paddedBytes;
            } else {
                // 长度刚好匹配
                data = originalBytes;
            }

            if (!node.isBigEndian()) {
                ArrayUtils.reverse(data);
            }
            return data;
        } catch (CodecException e) {
            throw e;
        } catch (Exception e) {
            throw new CodecException(node.getName() + ",十六进制编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Object decode(byte[] data, INode node, Map<String, Object> context) throws CodecException {
        try {
            if (data == null || data.length == 0) {
                throw new CodecException("数据不能为空");
            }

            // 转换为十六进制字符串并添加0x前缀
            String result = "0x" + ByteUtil.bytesToHexString(data);

            // 处理枚举值（解码时返回枚举值而不是描述，以便验证）
            return EnumHelper.processEnumForDecode(node, result);
        } catch (Exception e) {
            throw new CodecException(node.getName() + ",十六进制解码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ValueType[] getSupportedValueTypes() {
        return new ValueType[]{ValueType.HEX};
    }

    @Override
    public boolean supportsValueType(ValueType valueType) {
        return valueType == ValueType.HEX;
    }
} 