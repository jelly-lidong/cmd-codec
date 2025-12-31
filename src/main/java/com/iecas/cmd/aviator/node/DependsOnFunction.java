package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * dependsOn(nodeId, depNodeId): 简单依赖判断（当前仅存在性+启用性）
 */
public class DependsOnFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "dependsOn";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a1, AviatorObject a2) {
        String nodeId = FunctionUtils.getStringValue(a1, env);
        String depId = FunctionUtils.getStringValue(a2, env);
        INode node = NodeLookup.findNode(env, nodeId);
        INode dep = NodeLookup.findNode(env, depId);
        long r = (node != null && dep != null && dep.isEnabled()) ? 1L : 0L;
        return AviatorLong.valueOf(r);
    }
}


