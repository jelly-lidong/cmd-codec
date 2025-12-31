package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.HashMap;
import java.util.Map;

/**
 * when(nodeId, expr): 基于表达式的启用检查，向上下文注入 value/node
 */
public class WhenFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "when";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a1, AviatorObject a2) {
        String nodeId = FunctionUtils.getStringValue(a1, env);
        String expr = FunctionUtils.getStringValue(a2, env);
        INode node = NodeLookup.findNode(env, nodeId);
        if (node == null) return AviatorLong.valueOf(0);
        Map<String, Object> ctx = new HashMap<>(env);
        ctx.put("value", node.getValue());
        ctx.put("node", node);
        Object r = AviatorEvaluator.execute(expr, ctx);
        boolean b = r instanceof Boolean ? (Boolean) r : (r instanceof Number ? ((Number) r).doubleValue() != 0.0 : r != null);
        return AviatorLong.valueOf(b ? 1L : 0L);
    }
}


