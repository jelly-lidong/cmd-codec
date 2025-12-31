package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 将 BCD 的 HEX 字符串解码为数字字符串。
 * 示例：fromBCD("0123") -> "123"，会去掉可能的前导0。
 */
public class FromBCDFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "fromBCD";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if ((hex.length() & 1) == 1) {
            throw new IllegalArgumentException("BCD HEX长度必须为偶数");
        }
        StringBuilder digits = new StringBuilder(hex.length());
        for (int i = 0; i < hex.length(); i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || hi > 9 || lo < 0 || lo > 9) {
                throw new IllegalArgumentException("非法BCD HEX");
            }
            digits.append((char) ('0' + hi)).append((char) ('0' + lo));
        }
        // 去掉可能的前导0
        String out = digits.toString().replaceFirst("^0+(?!$)", "");
        return new AviatorString(out);
    }
}


