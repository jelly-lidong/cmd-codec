package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class LengthFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "length";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String id = FunctionUtils.getStringValue(arg1, env);
        byte[] bytes = (byte[]) env.get(id);
        return AviatorLong.valueOf(bytes.length);
    }
} 