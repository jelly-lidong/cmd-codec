package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * asBCD(nodeId) -> 将节点字节(BCD编码)解析为数字字符串
 */
public class AsBCDFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "asBCD";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        INode node = NodeLookup.findNode(env, nodeId);
        byte[] data = node != null ? node.getData() : null;
        if (data == null || data.length == 0) return new AviatorString("");
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            int hi = (b >> 4) & 0x0F;
            int lo = b & 0x0F;
            if (hi > 9 || lo > 9) return new AviatorString("");
            sb.append((char)('0' + hi)).append((char)('0' + lo));
        }
        String out = sb.toString().replaceFirst("^0+(?!$)", "");
        return new AviatorString(out);
    }
}


