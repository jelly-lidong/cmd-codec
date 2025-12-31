package com.iecas.cmd.aviator.varint;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.util.Map;

/**
 * Varint 编码为HEX字符串（LSB续位方案，7位一组，最高位为续位标志）。
 */
public class VarintEncodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "varintEncode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        long value = FunctionUtils.getNumberValue(arg1, env).longValue();
        if (value < 0) {
            throw new IllegalArgumentException("varint不支持负数");
        }
        StringBuilder sb = new StringBuilder();
        do {
            int bits = (int) (value & 0x7F);
            value >>>= 7;
            if (value != 0) bits |= 0x80; // 还有后续字节
            sb.append(String.format("%02X", bits));
        } while (value != 0);
        return new AviatorString(sb.toString());
    }
}


