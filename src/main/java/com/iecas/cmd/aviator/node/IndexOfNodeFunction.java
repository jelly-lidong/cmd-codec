package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.List;
import java.util.Map;

public class IndexOfNodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "indexOfNode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject groupArg, AviatorObject nodeArg) {
        String groupId = FunctionUtils.getStringValue(groupArg, env);
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        INode group = NodeLookup.findNode(env, groupId);
        if (group == null) return AviatorLong.valueOf(-1);
        List<?> children = group.getChildren();
        if (children == null) return AviatorLong.valueOf(-1);
        int idx = 0;
        for (Object c : children) {
            if (c instanceof INode) {
                INode n = (INode) c;
                if (nodeId.equals(n.getId()) || nodeId.equals(n.getName()) || nodeId.equals(n.getPath())) {
                    return AviatorLong.valueOf(idx);
                }
                idx++;
            }
        }
        return AviatorLong.valueOf(-1);
    }
}


