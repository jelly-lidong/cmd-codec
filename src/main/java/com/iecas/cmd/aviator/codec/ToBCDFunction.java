package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 将纯数字字符串编码为 BCD 的 HEX 字符串（高位在前）。
 * 示例：toBCD("123") -> "0123"；toBCD("1234") -> "1234"。
 */
public class ToBCDFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "toBCD";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String digits = FunctionUtils.getStringValue(arg1, env).trim();
        if (!digits.matches("\\d+")) {
            throw new IllegalArgumentException("BCD仅支持数字字符");
        }
        if ((digits.length() & 1) == 1) {
            digits = "0" + digits;
        }
        StringBuilder sb = new StringBuilder(digits.length());
        for (int i = 0; i < digits.length(); i += 2) {
            int hi = digits.charAt(i) - '0';
            int lo = digits.charAt(i + 1) - '0';
            sb.append(String.format(Locale.ROOT, "%X%X", hi, lo));
        }
        return new AviatorString(sb.toString());
    }
}


