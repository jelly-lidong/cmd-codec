package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class NodeEndOffsetFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "nodeEndOffset";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        int endBit = node == null ? -1 : NodeLookup.getEndBit(node);
        int endByte = endBit < 0 ? -1 : ((endBit + 7) / 8);
        return AviatorLong.valueOf(endByte);
    }
}


