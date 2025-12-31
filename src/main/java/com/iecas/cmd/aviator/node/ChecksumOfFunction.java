package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class ChecksumOfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "checksumOf";
    }

    private int sum(byte[] data) {
        if (data == null) return 0;
        int s = 0;
        for (byte b : data) s += (b & 0xFF);
        return s & 0xFF;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        int v = sum(node != null ? node.getData() : null);
        return AviatorLong.valueOf(v);
    }
}


