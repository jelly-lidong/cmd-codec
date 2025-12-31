package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;

/**
 * HEX -> Base64
 */
public class HexToBase64Function extends AbstractFunction {
    @Override
    public String getName() {
        return "hexToBase64";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if ((hex.length() & 1) == 1) {
            throw new IllegalArgumentException("HEX长度必须为偶数");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return new AviatorString(Base64.getEncoder().encodeToString(bytes));
    }
}


