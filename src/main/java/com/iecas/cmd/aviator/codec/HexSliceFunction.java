package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 截取HEX字符串的字节片段：hexSlice(hex, offsetBytes, lengthBytes)
 */
public class HexSliceFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexSlice";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        String hex = normalize(FunctionUtils.getStringValue(arg1, env));
        int off = FunctionUtils.getNumberValue(arg2, env).intValue();
        int len = FunctionUtils.getNumberValue(arg3, env).intValue();
        if (off < 0 || len < 0) {
            throw new IllegalArgumentException("offset/length must be non-negative");
        }
        int start = off * 2;
        int end = Math.min(hex.length(), start + len * 2);
        if (start > hex.length()) return new AviatorString("");
        return new AviatorString(hex.substring(start, end));
    }

    private String normalize(String hex) {
        return hex.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


