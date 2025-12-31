package com.iecas.cmd.aviator.util;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

/**
 * 右填充字符串到指定长度
 * 用法：padRight(str, len, ch)
 */
public class PadRightFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "padRight";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        String s = FunctionUtils.getStringValue(arg1, env);
        int len = FunctionUtils.getNumberValue(arg2, env).intValue();
        String ch = FunctionUtils.getStringValue(arg3, env);
        if (ch == null || ch.isEmpty()) ch = " ";
        if (s.length() >= len) return new AviatorString(s);
        StringBuilder sb = new StringBuilder(len);
        sb.append(s);
        for (int i = s.length(); i < len; i++) sb.append(ch.charAt(0));
        return new AviatorString(sb.toString());
    }
}


