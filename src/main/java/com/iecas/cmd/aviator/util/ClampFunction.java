package com.iecas.cmd.aviator.util;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 将数值限制在[min, max]区间内：clamp(value, min, max)
 */
public class ClampFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "clamp";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long min = FunctionUtils.getNumberValue(arg2, env).longValue();
        long max = FunctionUtils.getNumberValue(arg3, env).longValue();
        if (min > max) {
            long t = min; min = max; max = t;
        }
        long r = value < min ? min : (value > max ? max : value);
        return AviatorLong.valueOf(r);
    }
}
