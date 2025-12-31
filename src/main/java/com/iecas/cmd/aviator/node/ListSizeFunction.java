package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.List;
import java.util.Map;

public class ListSizeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "listSize";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject groupArg) {
        String groupId = FunctionUtils.getStringValue(groupArg, env);
        INode group = NodeLookup.findNode(env, groupId);
        if (group == null) return AviatorLong.valueOf(0);
        List<?> children = group.getChildren();
        int size = children == null ? 0 : children.size();
        return AviatorLong.valueOf(size);
    }
}


