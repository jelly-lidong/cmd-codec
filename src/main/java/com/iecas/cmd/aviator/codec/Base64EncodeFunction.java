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
 * Base64 编码
 * 用法：base64Encode(str) 或 base64Encode(str, charset)
 */
public class Base64EncodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "base64Encode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String s = FunctionUtils.getStringValue(arg1, env);
        String out = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        return new AviatorString(out);
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String s = FunctionUtils.getStringValue(arg1, env);
        String charset = FunctionUtils.getStringValue(arg2, env);
        String out = Base64.getEncoder().encodeToString(s.getBytes(Charset.forName(charset)));
        return new AviatorString(out);
    }
}


