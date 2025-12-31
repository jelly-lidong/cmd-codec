package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 设置或清除标志位：setFlag(value, mask, enabled)
 * enabled 非0表示置位，否则清零
 */
public class SetFlagFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "setFlag";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long mask = FunctionUtils.getNumberValue(arg2, env).longValue();
        long enabled = FunctionUtils.getNumberValue(arg3, env).longValue();
        long r = enabled != 0 ? (value | mask) : (value & ~mask);
        return AviatorLong.valueOf(r);
    }
}


