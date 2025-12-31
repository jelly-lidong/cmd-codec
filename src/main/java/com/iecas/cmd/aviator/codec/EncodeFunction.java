package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.nio.charset.Charset;

/**
 * 将字符串按指定字符集编码为HEX：encode(str, charset)
 */
public class EncodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "encode";
    }

    @Override
    public AviatorObject call(java.util.Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String s = FunctionUtils.getStringValue(arg1, env);
        String charsetName = FunctionUtils.getStringValue(arg2, env);
        byte[] bytes = s.getBytes(Charset.forName(charsetName));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return new AviatorString(sb.toString());
    }
}
