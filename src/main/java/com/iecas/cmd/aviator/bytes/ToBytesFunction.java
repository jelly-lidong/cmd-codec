package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 字符串转 byte[]：toBytes(str[, charset])，默认UTF-8
 */
public class ToBytesFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "toBytes";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String s = FunctionUtils.getStringValue(arg1, env);
        return AviatorRuntimeJavaType.valueOf(s.getBytes(StandardCharsets.UTF_8));
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String s = FunctionUtils.getStringValue(arg1, env);
        String charset = FunctionUtils.getStringValue(arg2, env);
        return AviatorRuntimeJavaType.valueOf(s.getBytes(Charset.forName(charset)));
    }
}


