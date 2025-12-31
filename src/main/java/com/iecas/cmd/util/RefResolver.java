package com.iecas.cmd.util;

import com.iecas.cmd.exception.CodecException;

/**
 * 依赖引用解析工具，支持两种格式：
 * - #nodeId                 （默认当前协议）
 * - #protocolId:nodeId      （跨协议）
 */
public class RefResolver {

    public static class RefKey {
        public final String protocolId;
        public final String nodeId;

        public RefKey(String protocolId, String nodeId) {
            this.protocolId = protocolId;
            this.nodeId = nodeId;
        }
    }

    /**
     * 解析条件/表达式中的引用。
     */
    public static RefKey parse(String raw, String currentProtocolId) throws CodecException {
        if (raw == null || raw.isEmpty()) {
            throw new CodecException("引用不能为空");
        }
        if (!raw.startsWith("#")) {
            throw new CodecException("非法引用格式: " + raw + ", 必须以#开头");
        }
        String s = raw.substring(1);
        if (s.isEmpty()) {
            throw new CodecException("非法引用格式: " + raw);
        }
        int idx = s.indexOf(':');
        if (idx < 0) {
            // #nodeId
            return new RefKey(currentProtocolId, s);
        }
        String ns = s.substring(0, idx);
        String nodeId = s.substring(idx + 1);
        if (ns.isEmpty() || nodeId.isEmpty()) {
            throw new CodecException("非法引用格式: " + raw);
        }
        return new RefKey(ns, nodeId);
    }
}


