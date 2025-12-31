package com.iecas.cmd.aviator.util;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

/**
 * 子串提取
 * 用法：substr(str, start, len)
 */
public class SubstrFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "substr";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        String s = FunctionUtils.getStringValue(arg1, env);
        int start = FunctionUtils.getNumberValue(arg2, env).intValue();
        int len = FunctionUtils.getNumberValue(arg3, env).intValue();
        if (start < 0) start = 0;
        if (len < 0) len = 0;
        if (start > s.length()) return new AviatorString("");
        int end = Math.min(s.length(), start + len);
        return new AviatorString(s.substring(start, end));
    }
}


