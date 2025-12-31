package com.iecas.cmd.aviator.network;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * HEX字符串转换为带冒号分隔的MAC地址。
 */
public class HexToMacFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexToMac";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String hex = FunctionUtils.getStringValue(arg1, env).replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (!hex.matches("[0-9A-F]{12}")) {
            throw new IllegalArgumentException("MAC HEX长度必须为12");
        }
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 12; i += 2) {
            if (i > 0) sb.append(":");
            sb.append(hex, i, i + 2);
        }
        return new AviatorString(sb.toString());
    }
}


