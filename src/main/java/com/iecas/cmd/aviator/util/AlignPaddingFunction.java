package com.iecas.cmd.aviator.util;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 计算对齐需要的填充字节数：alignPadding(value, boundary)
 * 例：alignPadding(13, 4) -> 3
 */
public class AlignPaddingFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "alignPadding";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        long boundary = FunctionUtils.getNumberValue(arg2, env).longValue();
        if (boundary <= 0) {
            return AviatorLong.valueOf(0);
        }
        long remainder = value % boundary;
        long padding = remainder == 0 ? 0 : boundary - remainder;
        return AviatorLong.valueOf(padding);
    }
}


