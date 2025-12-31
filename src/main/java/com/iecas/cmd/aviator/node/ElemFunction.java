package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import com.iecas.cmd.model.proto.INode;

import java.util.List;
import java.util.Map;

public class ElemFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "elem";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject groupArg, AviatorObject idxArg, AviatorObject fieldArg) {
        String groupId = FunctionUtils.getStringValue(groupArg, env);
        int idx = FunctionUtils.getNumberValue(idxArg, env).intValue();
        String fieldName = FunctionUtils.getStringValue(fieldArg, env);
        INode group = NodeLookup.findNode(env, groupId);
        if (group == null) return AviatorRuntimeJavaType.valueOf(null);
        List<?> children = group.getChildren();
        if (children == null || idx < 0 || idx >= children.size()) return AviatorRuntimeJavaType.valueOf(null);
        Object c = children.get(idx);
        if (!(c instanceof INode)) return AviatorRuntimeJavaType.valueOf(null);
        INode elem = (INode) c;
        INode child = elem.getChild(fieldName);
        if (child != null) return AviatorRuntimeJavaType.valueOf(child.getValue());
        // fallback: id/name
        List<?> fields = elem.getChildren();
        if (fields != null) {
            for (Object f : fields) {
                if (f instanceof INode) {
                    INode n = (INode) f;
                    if (fieldName.equals(n.getId()) || fieldName.equals(n.getName())) {
                        return AviatorRuntimeJavaType.valueOf(n.getValue());
                    }
                }
            }
        }
        return AviatorRuntimeJavaType.valueOf(null);
    }
}


