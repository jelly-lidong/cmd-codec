package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class HexOfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hexOf";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        byte[] data = node != null ? node.getData() : null;
        if (data == null) return new AviatorString("");
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) sb.append(String.format("%02X", b));
        return new AviatorString(sb.toString());
    }
}


