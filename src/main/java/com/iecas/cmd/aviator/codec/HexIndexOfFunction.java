package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Locale;
import java.util.Map;

/**
 * 在HEX字符串中查找子串起始字节偏移：hexIndexOf(haystack, needle)
 * 返回字节偏移，找不到返回 -1。
 */
public class HexIndexOfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexIndexOf";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String hay = normalize(FunctionUtils.getStringValue(arg1, env));
        String nee = normalize(FunctionUtils.getStringValue(arg2, env));
        if ((hay.length() & 1) == 1 || (nee.length() & 1) == 1) {
            throw new IllegalArgumentException("HEX长度必须为偶数");
        }
        int idx = hay.indexOf(nee);
        int result = idx < 0 ? -1 : (idx / 2);
        return AviatorLong.valueOf(result);
    }

    private String normalize(String hex) {
        return hex.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


