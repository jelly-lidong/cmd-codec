package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class NodeLengthFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "nodeLength";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        // getLength 语义：比特长度
        int bits = node == null ? 0 : NodeLookup.bitLengthOf(node);
        int bytes = (bits + 7) / 8;
        return AviatorLong.valueOf(bytes);
    }
}


