package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import com.iecas.cmd.model.proto.INode;

import java.security.MessageDigest;
import java.util.Map;

public class HashOfFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "hashOf";
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg, AviatorObject algoArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        String algo = FunctionUtils.getStringValue(algoArg, env);
        INode node = NodeLookup.findNode(env, nodeId);
        byte[] data = node != null ? node.getData() : null;
        if (data == null) return new AviatorString("");
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            md.update(data);
            return new AviatorString(toHex(md.digest()));
        } catch (Exception e) {
            return new AviatorString("");
        }
    }
}


