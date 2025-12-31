package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 32位大小端交换：swapEndian32(value)
 */
public class SwapEndian32Function extends AbstractFunction {
    @Override
    public String getName() {
        return "swapEndian32";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        long v = FunctionUtils.getNumberValue(arg1, env).longValue() & 0xFFFFFFFFL;
        long r = ((v & 0x000000FFL) << 24) |
                 ((v & 0x0000FF00L) << 8)  |
                 ((v & 0x00FF0000L) >>> 8) |
                 ((v & 0xFF000000L) >>> 24);
        return AviatorLong.valueOf(r & 0xFFFFFFFFL);
    }
}


