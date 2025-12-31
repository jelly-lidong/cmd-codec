package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 将HEX字符串转为原始字符串（按UTF-8解码）
 * 用法：fromHex(hexStr)
 */
public class FromHexFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "fromHex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env);
        byte[] bytes = hexToBytes(hex);
        return new AviatorString(new String(bytes));
    }

    private byte[] hexToBytes(String hex) {
        String clean = hex.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if ((clean.length() & 1) != 0) {
            throw new IllegalArgumentException("HEX长度必须为偶数");
        }
        int len = clean.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            int hi = Character.digit(clean.charAt(2 * i), 16);
            int lo = Character.digit(clean.charAt(2 * i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("非法HEX字符");
            }
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}


