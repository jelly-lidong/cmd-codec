package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 连接两个HEX字符串：hexConcat(a, b)
 */
public class HexConcatFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexConcat";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String a = normalize(FunctionUtils.getStringValue(arg1, env));
        String b = normalize(FunctionUtils.getStringValue(arg2, env));
        return new AviatorString(a + b);
    }

    private String normalize(String hex) {
        return hex.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


