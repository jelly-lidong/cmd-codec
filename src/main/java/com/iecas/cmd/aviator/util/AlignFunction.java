package com.iecas.cmd.aviator.util;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 对齐到指定边界：align(value, boundary)
 * 例：align(13, 4) -> 16
 */
public class AlignFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "align";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long boundary = FunctionUtils.getNumberValue(arg2, env).longValue();
        if (boundary <= 0) {
            return AviatorLong.valueOf(value);
        }
        long aligned = ((value + boundary - 1) / boundary) * boundary;
        return AviatorLong.valueOf(aligned);
    }
}


