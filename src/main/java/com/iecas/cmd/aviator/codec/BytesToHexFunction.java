package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * byte[] 转 HEX：bytesToHex(bytes)
 */
public class BytesToHexFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "bytesToHex";
    }

    @Override
    public AviatorObject call(java.util.Map<String, Object> env, AviatorObject arg1) {
        Object v = arg1.getValue(env);
        if (!(v instanceof byte[])) {
            throw new IllegalArgumentException("参数必须为byte[]");
        }
        byte[] bytes = (byte[]) v;
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return new AviatorString(sb.toString());
    }
}
