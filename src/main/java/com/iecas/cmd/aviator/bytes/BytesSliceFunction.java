package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 截取 byte[] 片段：bytesSlice(bytes, offset, length)
 */
public class BytesSliceFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "bytesSlice";
    }

    private static byte[] requireBytes(AviatorObject obj, Map<String, Object> env) {
        Object val = obj.getValue(env);
        if (val instanceof byte[]) return (byte[]) val;
        throw new IllegalArgumentException("参数必须为byte[]，请使用 toBytes()/hexToBytes() 进行转换");
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arr, AviatorObject off, AviatorObject len) {
        byte[] src = requireBytes(arr, env);
        int offset = FunctionUtils.getNumberValue(off, env).intValue();
        int length = FunctionUtils.getNumberValue(len, env).intValue();
        if (offset < 0) offset = 0;
        if (length < 0) length = 0;
        if (offset > src.length) return AviatorRuntimeJavaType.valueOf(new byte[0]);
        int end = Math.min(src.length, offset + length);
        byte[] out = new byte[end - offset];
        System.arraycopy(src, offset, out, 0, out.length);
        return AviatorRuntimeJavaType.valueOf(out);
    }
}


