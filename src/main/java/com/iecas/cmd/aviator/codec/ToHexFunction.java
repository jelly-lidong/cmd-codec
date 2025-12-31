package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 将字符串或数字转为HEX字符串（大写，无0x前缀）
 * 用法：toHex(str) 或 toHex(str, charset) 或 toHex(number)
 */
public class ToHexFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "toHex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object v = arg1.getValue(env);
        if (v instanceof Number) {
            long n = ((Number) v).longValue();
            return new AviatorString(Long.toHexString(n).toUpperCase());
        }
        String s = FunctionUtils.getStringValue(arg1, env);
        return new AviatorString(bytesToHex(s.getBytes(StandardCharsets.UTF_8)));
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String s = FunctionUtils.getStringValue(arg1, env);
        String charsetName = FunctionUtils.getStringValue(arg2, env);
        Charset cs = Charset.forName(charsetName);
        return new AviatorString(bytesToHex(s.getBytes(cs)));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}


