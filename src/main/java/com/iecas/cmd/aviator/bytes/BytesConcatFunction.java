package com.iecas.cmd.aviator.bytes;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 拼接两个或多个 byte[]：bytesConcat(b1, b2[, b3[, b4]])
 */
public class BytesConcatFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "bytesConcat";
    }

    private static byte[] requireBytes(AviatorObject obj, Map<String, Object> env) {
        Object val = obj.getValue(env);
        if (val instanceof byte[]) return (byte[]) val;
        throw new IllegalArgumentException("参数必须为byte[]，请使用 toBytes()/hexToBytes() 进行转换");
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a1, AviatorObject a2) {
        byte[] b1 = requireBytes(a1, env);
        byte[] b2 = requireBytes(a2, env);
        byte[] out = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, out, 0, b1.length);
        System.arraycopy(b2, 0, out, b1.length, b2.length);
        return AviatorRuntimeJavaType.valueOf(out);
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject a1, AviatorObject a2, AviatorObject a3) {
        byte[] b1 = requireBytes(a1, env);
        byte[] b2 = requireBytes(a2, env);
        byte[] b3 = requireBytes(a3, env);
        byte[] out = new byte[b1.length + b2.length + b3.length];
        int p = 0;
        System.arraycopy(b1, 0, out, p, b1.length); p += b1.length;
        System.arraycopy(b2, 0, out, p, b2.length); p += b2.length;
        System.arraycopy(b3, 0, out, p, b3.length);
        return AviatorRuntimeJavaType.valueOf(out);
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject a1, AviatorObject a2, AviatorObject a3, AviatorObject a4) {
        byte[] b1 = requireBytes(a1, env);
        byte[] b2 = requireBytes(a2, env);
        byte[] b3 = requireBytes(a3, env);
        byte[] b4 = requireBytes(a4, env);
        byte[] out = new byte[b1.length + b2.length + b3.length + b4.length];
        int p = 0;
        System.arraycopy(b1, 0, out, p, b1.length); p += b1.length;
        System.arraycopy(b2, 0, out, p, b2.length); p += b2.length;
        System.arraycopy(b3, 0, out, p, b3.length); p += b3.length;
        System.arraycopy(b4, 0, out, p, b4.length);
        return AviatorRuntimeJavaType.valueOf(out);
    }
}


