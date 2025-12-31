package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 16位大小端交换：swapEndian16(value)
 */
public class SwapEndian16Function extends AbstractFunction {
    @Override
    public String getName() {
        return "swapEndian16";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        int v = FunctionUtils.getNumberValue(arg1, env).intValue();
        int r = ((v & 0xFF) << 8) | ((v >>> 8) & 0xFF);
        return AviatorLong.valueOf(r & 0xFFFF);
    }
}


