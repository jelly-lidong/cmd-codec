package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 判断是否包含标志位：hasFlag(value, mask)
 * 返回 1 或 0
 */
public class HasFlagFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hasFlag";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long mask = FunctionUtils.getNumberValue(arg2, env).longValue();
        long r = (value & mask) == mask ? 1L : 0L;
        return AviatorLong.valueOf(r);
    }
}


