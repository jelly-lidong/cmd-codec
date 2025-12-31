package com.iecas.cmd.aviator.tlv;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 解析简单TLV（1字节长度）并返回指定字段：parseTLV1(hex, field)
 * field 取值："T" | "L" | "V"
 */
public class ParseTLV1Function extends AbstractFunction {
    @Override
    public String getName() {
        return "parseTLV1";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String hex = normalize(FunctionUtils.getStringValue(arg1, env));
        String field = FunctionUtils.getStringValue(arg2, env).trim().toUpperCase(Locale.ROOT);
        if (hex.length() < 4) {
            throw new IllegalArgumentException("TLV最小长度不足");
        }
        String tag = hex.substring(0, hex.length() - 2 - getLength(hex) * 2);
        int len = getLength(hex);
        String value = hex.substring(hex.length() - len * 2);
        String lHex = String.format(Locale.ROOT, "%02X", len);
        String out;
        switch (field) {
            case "T": out = tag; break;
            case "L": out = lHex; break;
            case "V": out = value; break;
            default: throw new IllegalArgumentException("field 仅支持 T/L/V");
        }
        return new AviatorString(out);
    }

    private int getLength(String hex) {
        String lHex = hex.substring(hex.length() - 2 - 2, hex.length() - 2);
        return Integer.parseInt(lHex, 16);
    }

    private String normalize(String h) {
        return h.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


