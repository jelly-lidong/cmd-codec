package com.iecas.cmd.aviator;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

public class ReturnSrcValueFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "returnSrcValue";
    }

    @Override
    public AviatorObject call(Map<String, Object> env) {
        env.get("");
        return new AviatorString("");
    }
}
