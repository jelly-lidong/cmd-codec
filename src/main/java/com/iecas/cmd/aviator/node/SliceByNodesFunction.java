package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * 按节点边界截取字节：sliceByNodes(startNodeId, endNodeId)
 * 包含 startNode 起始字节，直到 endNode 结束字节（不含）。
 */
public class SliceByNodesFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "sliceByNodes";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject startArg, AviatorObject endArg) {
        String sId = FunctionUtils.getStringValue(startArg, env);
        String eId = FunctionUtils.getStringValue(endArg, env);
        INode s = NodeLookup.findNode(env, sId);
        INode e = NodeLookup.findNode(env, eId);
        if (s == null || e == null) return AviatorRuntimeJavaType.valueOf(null);
        int sByte = NodeLookup.getStartBit(s);
        int eByte = NodeLookup.getEndBit(e);
        if (sByte < 0 || eByte < 0) return AviatorRuntimeJavaType.valueOf(null);
        int start = sByte / 8;
        int end = (eByte + 7) / 8;
        byte[] src = s.getData(); // 假设上下文相同缓冲，按需要可改为从整体缓冲获取
        if (src == null) return AviatorRuntimeJavaType.valueOf(null);
        if (start < 0) start = 0;
        if (end > src.length) end = src.length;
        if (start >= end) return AviatorRuntimeJavaType.valueOf(new byte[0]);
        byte[] out = new byte[end - start];
        System.arraycopy(src, start, out, 0, out.length);
        return AviatorRuntimeJavaType.valueOf(out);
    }
}


