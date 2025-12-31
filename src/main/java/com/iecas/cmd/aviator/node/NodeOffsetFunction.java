package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class NodeOffsetFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "nodeOffset";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        int startBit = node == null ? -1 : NodeLookup.getStartBit(node);
        int startByte = startBit < 0 ? -1 : (startBit / 8);
        return AviatorLong.valueOf(startByte);
    }
}


