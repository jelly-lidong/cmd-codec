package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.iecas.cmd.model.proto.INode;

import java.util.List;
import java.util.Map;

public class FieldFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "field";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject structArg, AviatorObject fieldArg) {
        String structId = FunctionUtils.getStringValue(structArg, env);
        String fieldName = FunctionUtils.getStringValue(fieldArg, env);
        INode structNode = NodeLookup.findNode(env, structId);
        if (structNode == null) return AviatorRuntimeJavaType.valueOf(null);
        INode child = structNode.getChild(fieldName);
        if (child != null) return AviatorRuntimeJavaType.valueOf(child.getValue());
        // fallback: 遍历 children 以 id/name 匹配
        List<?> children = structNode.getChildren();
        if (children != null) {
            for (Object c : children) {
                if (c instanceof INode) {
                    INode n = (INode) c;
                    if (fieldName.equals(n.getId()) || fieldName.equals(n.getName())) {
                        return AviatorRuntimeJavaType.valueOf(n.getValue());
                    }
                }
            }
        }
        return AviatorRuntimeJavaType.valueOf(null);
    }
}


