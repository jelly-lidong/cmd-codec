package com.iecas.cmd.codec;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * 编解码器接口
 * 定义了编解码的基本方法
 */
public interface Codec {
    /**
     * 编码方法
     * @param node 协议节点
     * @param context 上下文信息
     * @return 编码后的字节数组
     * @throws CodecException 编解码异常
     */
    byte[] encode(INode node, Map<String, Object> context) throws CodecException;

    /**
     * 解码方法
     * @param data 待解码的字节数组
     * @param node 协议节点
     * @param context 上下文信息
     * @return 解码后的对象
     * @throws CodecException 编解码异常
     */
    Object decode(byte[] data, INode node, Map<String, Object> context) throws CodecException;

    /**
     * 获取编解码器支持的值类型
     * @return 支持的值类型数组
     */
    ValueType[] getSupportedValueTypes();

    /**
     * 判断是否支持指定的值类型
     * @param valueType 值类型
     * @return 是否支持
     */
    boolean supportsValueType(ValueType valueType);
}
