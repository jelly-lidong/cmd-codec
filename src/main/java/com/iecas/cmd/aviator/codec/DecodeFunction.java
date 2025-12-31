package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * 将HEX按指定字符集解码为字符串：decode(hex, charset)
 */
public class DecodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "decode";
    }

    @Override
    public AviatorObject call(java.util.Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        String charsetName = FunctionUtils.getStringValue(arg2, env);
        if ((hex.length() & 1) == 1) throw new IllegalArgumentException("HEX长度必须为偶数");
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return new AviatorString(new String(bytes, Charset.forName(charsetName)));
    }
}


