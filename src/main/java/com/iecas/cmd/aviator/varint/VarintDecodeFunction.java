package com.iecas.cmd.aviator.varint;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Locale;
import java.util.Map;

/**
 * 解析 Varint 的HEX字符串为数值。
 */
public class VarintDecodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "varintDecode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        long value = 0;
        int shift = 0;
        for (int i = 0; i < hex.length(); i += 2) {
            int b = Integer.parseInt(hex.substring(i, i + 2), 16);
            value |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) break;
            shift += 7;
            if (shift > 63) throw new IllegalArgumentException("varint过长");
        }
        return AviatorLong.valueOf(value);
    }
}


