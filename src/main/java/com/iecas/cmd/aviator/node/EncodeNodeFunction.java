package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import com.iecas.cmd.model.proto.INode;

import java.nio.charset.Charset;
import java.util.Map;

public class EncodeNodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "encodeNode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg, AviatorObject charsetArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        String charset = FunctionUtils.getStringValue(charsetArg, env);
        INode node = NodeLookup.findNode(env, nodeId);
        if (node == null) return new AviatorString("");
        Object v = node.getValue();
        if (v == null) return new AviatorString("");
        byte[] bytes = v.toString().getBytes(Charset.forName(charset));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return new AviatorString(sb.toString());
    }
}


