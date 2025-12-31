package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Locale;
import java.util.Map;

/**
 * HEX 转 byte[]：hexToBytes(hex)
 */
public class HexToBytesFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexToBytes";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if ((hex.length() & 1) == 1) throw new IllegalArgumentException("HEX长度必须为偶数");
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return AviatorRuntimeJavaType.valueOf(bytes);
    }
}


