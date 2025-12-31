package com.iecas.cmd.aviator.network;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * IPv4地址转换为HEX字符串（大端，4字节）。
 */
public class IpToHexFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "ipToHex";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String ip = FunctionUtils.getStringValue(arg1, env).trim();
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            if (addr.length != 4) {
                throw new IllegalArgumentException("仅支持IPv4");
            }
            StringBuilder sb = new StringBuilder(8);
            for (byte b : addr) sb.append(String.format("%02X", b));
            return new AviatorString(sb.toString());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("非法IP地址: " + ip, e);
        }
    }
}


