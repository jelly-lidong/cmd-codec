package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 从数值中抽取位段（LSB为第0位）
 * 用法：extractBits(number, offset, length)
 */
public class BitsExtractFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "extractBits";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        int offset = FunctionUtils.getNumberValue(arg2, env).intValue();
        int length = FunctionUtils.getNumberValue(arg3, env).intValue();
        if (offset < 0 || offset >= 64) {
            throw new IllegalArgumentException("offset out of range [0,64): " + offset);
        }
        if (length <= 0 || length > 64 - offset) {
            throw new IllegalArgumentException("length out of range: " + length);
        }
        long mask = (length == 64) ? -1L : ((1L << length) - 1);
        long result = (value >>> offset) & mask;
        return AviatorLong.valueOf(result);
    }
}


