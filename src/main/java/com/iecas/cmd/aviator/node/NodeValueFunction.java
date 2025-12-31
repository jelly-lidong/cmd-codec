package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class NodeValueFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "nodeValue";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        Object val = node != null ? node.getValue() : null;
        return AviatorRuntimeJavaType.valueOf(val);
    }
}


