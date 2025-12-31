package com.iecas.cmd.aviator.network;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * MAC地址（"AA:BB:CC:DD:EE:FF" 或 "AA-BB-CC-DD-EE-FF"）转HEX字符串。
 */
public class MacToHexFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "macToHex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String mac = FunctionUtils.getStringValue(arg1, env).trim().toUpperCase(Locale.ROOT);
        String clean = mac.replace(":", "").replace("-", "");
        if (!clean.matches("[0-9A-F]{12}")) {
            throw new IllegalArgumentException("非法MAC地址");
        }
        return new AviatorString(clean);
    }
}


