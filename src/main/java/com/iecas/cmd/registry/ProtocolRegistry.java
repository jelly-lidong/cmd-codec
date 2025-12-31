package com.iecas.cmd.registry;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.model.proto.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局协议注册表
 * 维护 (protocolId, nodeId) 到节点对象的索引，支持跨协议引用解析。
 */
@Slf4j
public class ProtocolRegistry {

    private static final ProtocolRegistry INSTANCE = new ProtocolRegistry();

    // protocolId -> (nodeId -> node)
    private final Map<String, Map<String, INode>> protocolNodeIndex = new HashMap<>();

    private ProtocolRegistry() {
    }

    public static ProtocolRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册协议的节点索引。
     * 检查同协议内 nodeId 的唯一性，不同协议可重复。
     *
     * @throws CodecException 如果协议为null、协议ID为空，或同协议内存在重复的 nodeId
     */
    public synchronized void registerProtocol(Protocol protocol, List<INode> allLeafNodes) throws CodecException {
        if (protocol == null) {
            throw new CodecException("协议不能为空，无法注册到协议注册表");
        }
        String protocolId = protocol.getId();
        if (protocolId == null || protocolId.isEmpty()) {
            throw new CodecException("协议ID不能为空，协议必须配置ID才能注册到协议注册表，以支持跨协议引用功能");
        }
        Map<String, INode> nodeMap = protocolNodeIndex.computeIfAbsent(protocolId, k -> new HashMap<>());

        // 检查同协议内 nodeId 的唯一性
        Map<String, INode> newNodes = new HashMap<>();
        for (INode node : allLeafNodes) {
            String nodeId = node.getId();
            if (nodeId == null || nodeId.isEmpty()) continue;

            // 检查是否与已注册的节点冲突（同协议内）
            if (nodeMap.containsKey(nodeId)) {
                throw new CodecException(String.format(
                        "协议 '%s' 内存在重复的 nodeId '%s'。该 nodeId 已在协议注册表中存在，请确保同一协议内所有节点ID唯一。",
                        protocolId, nodeId));
            }

            // 检查本次注册的节点之间是否冲突
            if (newNodes.containsKey(nodeId)) {
                throw new CodecException(String.format(
                        "协议 '%s' 内存在重复的 nodeId '%s'。本次注册的节点中存在重复，请确保同一协议内所有节点ID唯一。",
                        protocolId, nodeId));
            }

            newNodes.put(nodeId, node);
        }

        // 所有检查通过后，批量注册
        nodeMap.putAll(newNodes);
        log.debug("[ProtocolRegistry] 协议已注册: {} (nodes={})", protocolId, nodeMap.size());
    }

    public INode getNode(String protocolId, String nodeId) {
        Map<String, INode> nodeMap = protocolNodeIndex.get(protocolId);
        if (nodeMap == null) return null;
        return nodeMap.get(nodeId);
    }
}


