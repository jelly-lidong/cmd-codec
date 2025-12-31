package com.iecas.cmd.aviator.network;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.util.ByteUtil;

import java.util.Map;

/**
 * HEX字符串转换为IPv4点分十进制。
 */
public class IpToIntFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "ipToInt";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String ip = FunctionUtils.getStringValue(arg1, env);
        String[] split = ip.split("\\.");

        int a = Integer.parseInt(split[0]);
        int b = Integer.parseInt(split[1]);
        int c = Integer.parseInt(split[2]);
        int d = Integer.parseInt(split[3]);

        byte[] bytes = {(byte) a, (byte) b, (byte) c, (byte) d};

        long value = ByteUtil.bytesToUnsignedInt(bytes);

        return AviatorLong.valueOf(value);
    }
}


