package com.iecas.cmd.aviator.node;

import cn.hutool.core.collection.CollectionUtil;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.engine.ExpressionParser;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.util.BitBuffer;
import com.iecas.cmd.util.ByteUtil;

import java.util.List;
import java.util.Map;

/**
 * 辅助：在 Aviator env 中查找 INode
 */
public class NodeLookup {

    public static byte[] getBetweenBytes(Map<String, Object> env, String startNodeId, String endNodeId) throws CodecException {
        List<INode> betweenNodes = getBetweenNode(env, startNodeId, endNodeId);
        int totalBits = betweenNodes.stream().mapToInt(NodeLookup::bitLengthOf).sum();
        if (totalBits % 8 != 0) {
            throw new CodecException("节点 " + startNodeId + " 到 " + endNodeId + " 之间的总长度不是字节对齐的");
        }
        int totalBytes = totalBits / 8;
        byte[] data = new byte[totalBytes];
        ProtocolCodec protocolCodec = new ProtocolCodec();
        BitBuffer buffer = new BitBuffer();
        for (INode node : betweenNodes) {
            String nodeId = node.getId();
            String encodeHex = (String) env.get(nodeId + "_encoded");
            protocolCodec.writeNodeDataToBuffer((Node) node, ByteUtil.hexStringToBytes(encodeHex), buffer);
        }
        return buffer.toByteArray();
    }


    public static List<INode> getBetweenNode(Map<String, Object> env, String startNodeId, String endNodeId) throws CodecException {
        List<INode> allLeafNodes = (List<INode>) env.get("allLeafNodes");
        if (CollectionUtil.isEmpty(allLeafNodes)) {
            throw new CodecException("环境变量 allLeafNodes 为空");
        }

        int startIndex = -1;
        int endIndex = -1;
        for (INode iNode : allLeafNodes) {
            if (iNode.getId().equals(startNodeId)) {
                startIndex = allLeafNodes.indexOf(iNode);
            }
            if (iNode.getId().equals(endNodeId)) {
                endIndex = allLeafNodes.indexOf(iNode);
            }
        }
        if (startIndex == -1) {
            throw new CodecException("未找到起始节点 ID=" + startNodeId);
        }
        if (endIndex == -1) {
            throw new CodecException("未找到结束节点 ID=" + endNodeId);
        }

        if (startIndex > endIndex) {
            throw new CodecException("起始节点必须在结束节点之前");
        }
        return allLeafNodes.subList(startIndex, endIndex + 1);

    }

    /**
     * 在 env 中查找指定 ID 或 Name 的节点
     *
     * @param env          Aviator 表达式引擎的环境变量
     * @param nodeIdOrName 节点 ID 或 Name
     * @return 找到的节点，找不到返回 null
     */
    public static INode findNode(Map<String, Object> env, String nodeIdOrName) {
        if (env == null || nodeIdOrName == null) return null;
        Object direct = env.get(nodeIdOrName);
        if (direct instanceof INode) return (INode) direct;
        for (Object v : env.values()) {
            if (v instanceof INode) {
                INode n = (INode) v;
                if (nodeIdOrName.equals(n.getId()) || nodeIdOrName.equals(n.getName()) || nodeIdOrName.equals(n.getPath())) {
                    return n;
                }
            }
        }
        return null;
    }

    public static int bitLengthOf(INode node) {
        if (node == null) return 0;
        try {
            // 优先使用编码过程中记录的位范围
            int start = getStartBit(node);
            int end = getEndBit(node);
            if (start >= 0 && end >= 0 && end >= start) {
                return end - start;
            }
        } catch (Throwable ignore) {
        }
        // 回退：使用节点定义的 length（按比特）
        return Math.max(0, node.getLength());
    }

    public static int getStartBit(INode node) {
        try {
            // 通过反射调用 getStartBitPosition（仅在实现类 Node 上有效）
            java.lang.reflect.Method m = node.getClass().getMethod("getStartBitPosition");
            Object v = m.invoke(node);
            return v instanceof Integer ? (Integer) v : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getEndBit(INode node) {
        try {
            java.lang.reflect.Method m = node.getClass().getMethod("getEndBitPosition");
            Object v = m.invoke(node);
            return v instanceof Integer ? (Integer) v : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}


