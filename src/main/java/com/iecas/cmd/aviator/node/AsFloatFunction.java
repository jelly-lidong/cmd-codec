package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import com.iecas.cmd.model.proto.INode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * asFloat(nodeId, endian) -> 字符串（保持兼容性），endian: BIG/LITTLE
 * 自动根据节点长度取 32 或 64 位浮点（字节数 4/8）。
 */
public class AsFloatFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "asFloat";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg, AviatorObject endianArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        String endian = FunctionUtils.getStringValue(endianArg, env);
        INode node = NodeLookup.findNode(env, nodeId);
        byte[] data = node != null ? node.getData() : null;
        if (data == null) return new AviatorString("");
        ByteOrder order = "LITTLE".equalsIgnoreCase(endian) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer buf = ByteBuffer.wrap(data).order(order);
        String out;
        if (data.length >= 8) {
            out = String.valueOf(buf.getDouble(0));
        } else if (data.length >= 4) {
            out = String.valueOf(buf.getFloat(0));
        } else {
            out = "";
        }
        return new AviatorString(out);
    }
}


