package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * 计算节点对齐需要的补齐字节数
 * paddingForNode(nodeId, boundary)
 */
public class PaddingForNodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "paddingForNode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        long boundary = FunctionUtils.getNumberValue(arg2, env).longValue();
        INode node = NodeLookup.findNode(env, nodeId);
        if (node == null || boundary <= 0) return AviatorLong.valueOf(0);
        int endBit = NodeLookup.getEndBit(node);
        if (endBit < 0) return AviatorLong.valueOf(0);
        long end = (endBit + 7) / 8;
        long rem = end % boundary;
        long padding = rem == 0 ? 0 : boundary - rem;
        return AviatorLong.valueOf(padding);
    }
}


