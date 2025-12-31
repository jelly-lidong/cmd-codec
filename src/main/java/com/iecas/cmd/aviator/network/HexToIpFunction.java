package com.iecas.cmd.aviator.network;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * HEX字符串转换为IPv4点分十进制。
 */
public class HexToIpFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexToIp";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (hex.length() != 8) {
            throw new IllegalArgumentException("IPv4 HEX长度必须为8");
        }
        int a = Integer.parseInt(hex.substring(0, 2), 16);
        int b = Integer.parseInt(hex.substring(2, 4), 16);
        int c = Integer.parseInt(hex.substring(4, 6), 16);
        int d = Integer.parseInt(hex.substring(6, 8), 16);
        return new AviatorString(a + "." + b + "." + c + "." + d);
    }
}


