package com.iecas.cmd.aviator.codec;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Base64 解码
 * 用法：base64Decode(b64) 或 base64Decode(b64, charset)
 */
public class Base64DecodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "base64Decode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String b64 = FunctionUtils.getStringValue(arg1, env);
        String out = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
        return new AviatorString(out);
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String b64 = FunctionUtils.getStringValue(arg1, env);
        String charset = FunctionUtils.getStringValue(arg2, env);
        String out = new String(Base64.getDecoder().decode(b64), Charset.forName(charset));
        return new AviatorString(out);
    }
}


