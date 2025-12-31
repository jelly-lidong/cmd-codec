package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * 按节点对齐后返回对齐后的结束偏移（字节）
 * alignNode(nodeId, boundary)
 */
public class AlignNodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "alignNode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        String nodeId = FunctionUtils.getStringValue(arg1, env);
        long boundary = FunctionUtils.getNumberValue(arg2, env).longValue();
        INode node = NodeLookup.findNode(env, nodeId);
        if (node == null || boundary <= 0) return AviatorLong.valueOf(-1);
        int endByte = NodeLookup.getEndBit(node);
        if (endByte < 0) return AviatorLong.valueOf(-1);
        long end = (endByte + 7) / 8;
        long aligned = ((end + boundary - 1) / boundary) * boundary;
        return AviatorLong.valueOf(aligned);
    }
}


