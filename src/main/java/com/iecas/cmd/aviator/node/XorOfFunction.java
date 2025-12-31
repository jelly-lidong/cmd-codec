package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class XorOfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "xorOf";
    }

    private int xor(byte[] data) {
        if (data == null) return 0;
        int x = 0;
        for (byte b : data) x ^= (b & 0xFF);
        return x & 0xFF;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        int v = xor(node != null ? node.getData() : null);
        return AviatorLong.valueOf(v);
    }
}


