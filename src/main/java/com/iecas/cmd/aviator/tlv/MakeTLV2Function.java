package com.iecas.cmd.aviator.tlv;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Locale;
import java.util.Map;

/**
 * 组装TLV（2字节长度，大端）。Length为V的字节数。
 * 输入：makeTLV2(tagHex, valueHex)
 */
public class MakeTLV2Function extends AbstractFunction {
    @Override
    public String getName() {
        return "makeTLV2";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String tag = normalize(FunctionUtils.getStringValue(arg1, env));
        String value = normalize(FunctionUtils.getStringValue(arg2, env));
        if ((value.length() & 1) == 1) throw new IllegalArgumentException("value HEX长度必须为偶数");
        int len = value.length() / 2;
        if (len > 0xFFFF) throw new IllegalArgumentException("长度超出2字节范围");
        String tlv = tag + String.format(Locale.ROOT, "%04X", len) + value;
        return new AviatorString(tlv);
    }

    private String normalize(String hex) {
        return hex.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}


