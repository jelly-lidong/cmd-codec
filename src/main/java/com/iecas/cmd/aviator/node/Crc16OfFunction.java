package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class Crc16OfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "crc16Of";
    }

    private int crc16(byte[] data) {
        if (data == null) return 0;
        int crc = 0xFFFF;
        for (byte b : data) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xFFFF;
            crc ^= (b & 0xFF);
            crc ^= ((crc & 0xFF) >> 4);
            crc ^= (crc << 12) & 0xFFFF;
            crc ^= ((crc & 0xFF) << 5) & 0xFFFF;
        }
        return crc & 0xFFFF;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        int crc = crc16(node != null ? node.getData() : null);
        return AviatorLong.valueOf(crc);
    }
}


