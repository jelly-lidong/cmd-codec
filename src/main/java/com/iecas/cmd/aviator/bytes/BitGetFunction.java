package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 获取指定位的比特值（LSB为第0位）
 * 用法：getBit(number, index)
 */
public class BitGetFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "getBit";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long index = FunctionUtils.getNumberValue(arg2, env).longValue();
        if (index < 0 || index >= 64) {
            throw new IllegalArgumentException("index out of range [0,64): " + index);
        }
        long bit = (value >>> index) & 1L;
        return AviatorLong.valueOf(bit);
    }
}


