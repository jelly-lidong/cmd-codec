package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import com.iecas.cmd.model.proto.INode;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

public class DecodeNodeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "decodeNode";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg, AviatorObject charsetArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        String charset = FunctionUtils.getStringValue(charsetArg, env);
        INode node = NodeLookup.findNode(env, nodeId);
        if (node == null) return AviatorRuntimeJavaType.valueOf(null);
        byte[] bytes = node.getData();
        if (bytes == null) return AviatorRuntimeJavaType.valueOf(null);
        String s = new String(bytes, Charset.forName(charset));
        return AviatorRuntimeJavaType.valueOf(s);
    }
}


