package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

public class IsEmptyFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "isEmpty";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        INode node = NodeLookup.findNode(env, nodeId);
        boolean empty = true;
        if (node != null) {
            Object v = node.getValue();
            if (v instanceof String) {
                empty = ((String) v).isEmpty();
            } else if (v instanceof byte[]) {
                empty = ((byte[]) v).length == 0;
            } else {
                empty = (v == null);
            }
        }
        return AviatorLong.valueOf(empty ? 1L : 0L);
    }
}


