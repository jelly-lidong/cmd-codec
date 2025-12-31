package com.iecas.cmd.codec;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.iecas.cmd.codec.impl.*;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 编解码器工厂
 * 用于管理和获取各种类型的编解码器
 */
@Slf4j
public class CodecFactory {
    private final Map<ValueType, Codec> codecMap = new HashMap<>();


    public CodecFactory() {
        // 自动注册所有编解码器
        registerCodec();
    }

    /**
     * 自动注册aviator包下的所有自定义函数
     */
    private void registerCodec() {
        registerCodec(new BitCodec());
        registerCodec(new FloatCodec());
        registerCodec(new HexCodec());
        registerCodec(new SignedIntCodec());
        registerCodec(new StringCodec());
        registerCodec(new UnsignedIntCodec());
        // 时间类型使用无符号整型编解码器
        codecMap.put(ValueType.TIME, new UnsignedIntCodec());
    }

    /**
     * 注册编解码器
     *
     * @param codec 编解码器实例
     */
    public void registerCodec(Codec codec) {
        for (ValueType type : codec.getSupportedValueTypes()) {
            codecMap.put(type, codec);
        }
    }

    /**
     * 获取指定值类型的编解码器
     *
     * @param valueType 值类型
     * @return 编解码器实例
     * @throws CodecException 如果找不到对应的编解码器
     */
    public Codec getCodec(ValueType valueType) throws CodecException {
        Codec codec = codecMap.get(valueType);
        if (codec == null) {
            throw new CodecException("未找到值类型 " + valueType + " 的编解码器");
        }
        return codec;
    }
} 