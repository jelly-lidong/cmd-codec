package com.iecas.cmd.aviator.tlv;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 解析TLV（2字节长度，大端）：parseTLV2(hex, field)；field取"T"/"L"/"V"
 */
public class ParseTLV2Function extends AbstractFunction {
    @Override
    public String getName() {
        return "parseTLV2";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String hex = normalize(FunctionUtils.getStringValue(arg1, env));
        String field = FunctionUtils.getStringValue(arg2, env).trim().toUpperCase(Locale.ROOT);
        if (hex.length() < 6) throw new IllegalArgumentException("TLV最小长度不足");
        int len = Integer.parseInt(hex.substring(hex.length() - 4, hex.length() - 2) + hex.substring(hex.length() - 2), 16);
        String tag = hex.substring(0, hex.length() - 4 - len * 2);
        String value = hex.substring(hex.length() - len * 2);
        String lHex = String.format(Locale.ROOT, "%04X", len);
        String out;
        switch (field) {
            case "T": out = tag; break;
            case "L": out = lHex; break;
            case "V": out = value; break;
            default: throw new IllegalArgumentException("field 仅支持 T/L/V");
        }
        return new AviatorString(out);
    }

    private String normalize(String h) {
        return h.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


