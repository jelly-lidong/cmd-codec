package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Base64;
import java.util.Map;

/**
 * Base64 -> HEX
 */
public class Base64ToHexFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "base64ToHex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String b64 = FunctionUtils.getStringValue(arg1, env);
        byte[] bytes = Base64.getDecoder().decode(b64);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return new AviatorString(sb.toString());
    }
}


