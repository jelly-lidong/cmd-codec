package com.iecas.cmd.engine;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.ConditionalDependency;
import com.iecas.cmd.model.proto.ConditionalDependency.ConditionalAction;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.model.proto.INode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 条件依赖处理器
 * 负责处理节点的条件依赖逻辑
 */
@Slf4j
public class ConditionalDependencyProcessor {

    private final ExpressionEngine expressionEngine;
    private final DependencyGraph dependencyGraph;

    public ConditionalDependencyProcessor(ExpressionEngine expressionEngine, DependencyGraph dependencyGraph) {
        this.expressionEngine = expressionEngine;
        this.dependencyGraph = dependencyGraph;
    }

    /**
     * 处理协议中所有节点的条件依赖
     *
     * @param context      上下文信息
     * @param allLeafNodes 协议中所有叶子节点
     * @throws CodecException 处理异常
     */
    public void processConditionalDependencies(Map<String, Object> context, List<INode> allLeafNodes) throws CodecException {
        log.debug("[条件依赖] 开始处理协议条件依赖");
        // 收集所有有条件依赖的节点
        List<INode> conditionalNodes = allLeafNodes.stream()
                .filter(node -> node.getConditionalDependencies() != null && !node.getConditionalDependencies().isEmpty())
                .collect(Collectors.toList());

        log.debug("[条件依赖] 发现 {} 个有条件依赖的节点", conditionalNodes.size());

        // 按优先级处理条件依赖
        for (INode node : conditionalNodes) {
            processNodeConditionalDependencies(node, allLeafNodes, context);
        }

        log.debug("[条件依赖] 条件依赖处理完成");
    }

    /**
     * 处理单个节点的条件依赖
     *
     * @param node     目标节点
     * @param allNodes 所有节点列表
     * @param context  上下文信息
     * @throws CodecException 处理异常
     */
    private void processNodeConditionalDependencies(INode node, List<INode> allNodes,
                                                    Map<String, Object> context) throws CodecException {
        List<ConditionalDependency> dependencies = node.getConditionalDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        log.debug("[条件依赖] 处理节点 '{}' 的 {} 个条件依赖", node.getName(), dependencies.size());

        // 按优先级排序
        dependencies.sort(Comparator.comparingInt(ConditionalDependency::getPriority));

        boolean finalEnabled = true;
        String finalReason = "默认启用";

        for (ConditionalDependency dependency : dependencies) {
            try {
                boolean conditionResult = evaluateCondition(dependency, allNodes, context);
                ConditionalAction action = conditionResult ? dependency.getAction() : dependency.getElseAction();

                log.debug("[条件依赖] 节点 '{}' 条件 '{}' 评估结果: {}, 执行动作: {}",
                        node.getName(), dependency.getCondition(), conditionResult, action);

                switch (action) {
                    case ENABLE:
                        finalReason = String.format("条件依赖启用: %s", dependency.getCondition());
                        break;
                    case DISABLE:
                        finalEnabled = false;
                        finalReason = String.format("条件依赖禁用: %s", dependency.getCondition());
                        break;
                    case SET_DEFAULT:
                        // 设置默认值逻辑
                        setNodeDefaultValue(node);
                        finalReason = String.format("条件依赖设置默认值: %s", dependency.getCondition());
                        break;
                    case CLEAR_VALUE:
                        // 清空值逻辑
                        clearNodeValue(node);
                        finalReason = String.format("条件依赖清空值: %s", dependency.getCondition());
                        break;
                }

                // 如果被禁用，后续条件依赖不再处理
                if (!finalEnabled) {
                    break;
                }

            } catch (Exception e) {
                log.error("[条件依赖] 处理节点 '{}' 的条件依赖时发生错误: {}", node.getName(), e.getMessage());
                throw new CodecException("处理条件依赖失败: " + e.getMessage(), e);
            }
        }

        // 设置最终状态
        node.setEnabled(finalEnabled);
        node.setEnabledReason(finalReason);

        log.debug("[条件依赖] 节点 '{}' 最终状态: {} ({})", node.getName(),
                finalEnabled ? "启用" : "禁用", finalReason);
    }

    /**
     * 评估条件表达式
     *
     * @param dependency 条件依赖配置
     * @param allNodes   所有节点列表
     * @param context    上下文信息
     * @return 条件评估结果
     * @throws CodecException 评估异常
     */
    private boolean evaluateCondition(ConditionalDependency dependency, List<INode> allNodes,
                                      Map<String, Object> context) throws CodecException {
        // 查找条件节点
        INode conditionNode = findConditionNode(dependency.getConditionNode(), allNodes, context);
        if (conditionNode == null) {
            String currentProtocolId = context == null ? null : (String) context.get("protocolId");
            throw new CodecException("未找到条件节点: " + dependency.getConditionNode() +
                    (currentProtocolId != null ? (" (protocolId=" + currentProtocolId + ")") : ""));
        }

        // 获取条件节点的值
        Object conditionValue = conditionNode.getValue();
        log.debug("[条件依赖] 条件节点 '{}' 的值: {}", conditionNode.getName(), conditionValue);

        // 准备表达式上下文
        Map<String, Object> expressionContext = new HashMap<>(context);
        expressionContext.put("value", conditionValue);
        expressionContext.put("node", conditionNode);

        // 评估条件表达式
        try {
            Object result = expressionEngine.evaluate(dependency.getCondition(), expressionContext);

            // 转换为布尔值
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0.0;
            } else if (result instanceof String) {
                return Boolean.parseBoolean((String) result);
            } else {
                return result != null;
            }

        } catch (Exception e) {
            log.error("[条件依赖] 评估条件表达式失败: {}", dependency.getCondition());
            throw new CodecException("评估条件表达式失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找条件节点
     *
     * <p><b>核心设计：</b>节点查找基于协议ID:节点ID格式。</p>
     *
     * @param conditionNodeRef 条件节点引用（支持 #nodeId 或 #protocolId:nodeId 格式）
     * @param allNodes         所有节点列表
     * @param context          上下文信息（应包含protocolId）
     * @return 找到的节点，如果未找到返回null
     */
    private INode findConditionNode(String conditionNodeRef, List<INode> allNodes, Map<String, Object> context) throws CodecException {
        if (conditionNodeRef == null || conditionNodeRef.trim().isEmpty()) {
            return null;
        }
        // 解析引用：支持 #nodeId 或 #protocolId:nodeId 格式
        String currentProtocolId = context == null ? null : (String) context.get("protocolId");
        com.iecas.cmd.util.RefResolver.RefKey ref = com.iecas.cmd.util.RefResolver.parse(conditionNodeRef, currentProtocolId);

        // 构建协议作用域的节点ID（协议ID:节点ID）
        String scopedNodeId;
        if (ref.protocolId != null) {
            scopedNodeId = ref.protocolId + ":" + ref.nodeId;
        } else if (currentProtocolId != null) {
            scopedNodeId = currentProtocolId + ":" + ref.nodeId;
        } else {
            // 如果没有协议ID，尝试直接使用节点ID（向后兼容）
            scopedNodeId = ref.nodeId;
        }

        // 总是优先尝试在当前协议的节点集合中查找（使用简单的节点ID匹配，因为allNodes中的节点可能只有简单的ID）
        if (ref.protocolId == null || (currentProtocolId != null && currentProtocolId.equals(ref.protocolId))) {
            for (INode node : allNodes) {
                // 先尝试简单的节点ID匹配
                if (ref.nodeId.equals(node.getId())) {
                    return node;
                }
                // 如果节点有协议ID，尝试协议ID:节点ID格式匹配
                String nodeScopedId = currentProtocolId != null ? currentProtocolId + ":" + node.getId() : node.getId();
                if (scopedNodeId.equals(nodeScopedId)) {
                    return node;
                }
            }
            // 使用依赖图查找（依赖图使用协议ID:节点ID作为key）
            if (dependencyGraph != null) {
                INode n = dependencyGraph.getNodeById(scopedNodeId);
                if (n != null) return n;
            }
        }

        // 仅当显式指定了协议ID且与当前协议不同，才尝试跨协议注册表
        if (ref.protocolId != null && (currentProtocolId == null || !currentProtocolId.equals(ref.protocolId))) {
            com.iecas.cmd.registry.ProtocolRegistry registry = com.iecas.cmd.registry.ProtocolRegistry.getInstance();
            return registry.getNode(ref.protocolId, ref.nodeId);
        }

        return null;
    }

    /**
     * 设置节点默认值
     *
     * @param node 目标节点
     */
    private void setNodeDefaultValue(INode node) {
        // 这里可以根据节点类型设置合适的默认值
        if (node.getValue() == null) {
            switch (node.getValueType()) {
                case HEX:
                    node.setValue("0x00");
                    break;
                case BIT:
                    node.setValue("0b0");
                    break;
                case INT:
                case UINT:
                    node.setValue("0");
                    break;
                case FLOAT:
                    node.setValue("0.0");
                    break;
                case STRING:
                    node.setValue("");
                    break;
                default:
                    node.setValue("0");
                    break;
            }
            log.debug("[条件依赖] 为节点 '{}' 设置默认值: {}", node.getName(), node.getValue());
        }
    }

    /**
     * 清空节点值
     *
     * @param node 目标节点
     */
    private void clearNodeValue(INode node) {
        node.setValue(null);
        log.debug("[条件依赖] 清空节点 '{}' 的值", node.getName());
    }

    /**
     * 获取协议中的所有节点
     *
     * @param protocol 协议对象
     * @return 所有节点列表
     */
    private List<INode> getAllNodes(Protocol protocol) {
        List<INode> allNodes = new ArrayList<>();
        collectNodes(protocol, allNodes);
        return allNodes;
    }

    /**
     * 递归收集所有节点
     *
     * @param node      当前节点
     * @param collector 节点收集器
     */
    private void collectNodes(INode node, List<INode> collector) {
        if (node == null) {
            return;
        }

        collector.add(node);

        // 递归收集子节点
        if (node.getChildren() != null) {
            for (INode child : node.getChildren()) {
                collectNodes(child, collector);
            }
        }

        // 处理特殊结构节点
        if (node instanceof Protocol) {
            Protocol protocol = (Protocol) node;
            if (protocol.getHeader() != null) {
                collectNodes(protocol.getHeader(), collector);
            }
            if (protocol.getBody() != null) {
                collectNodes(protocol.getBody(), collector);
            }
            if (protocol.getTail() != null) {
                collectNodes(protocol.getTail(), collector);
            }
        } else if (node instanceof com.iecas.cmd.model.proto.Body) {
            com.iecas.cmd.model.proto.Body body = (com.iecas.cmd.model.proto.Body) node;
            if (body.getHeader() != null) {
                collectNodes(body.getHeader(), collector);
            }
            if (body.getBody() != null) {
                collectNodes(body.getBody(), collector);
            }
            if (body.getTail() != null) {
                collectNodes(body.getTail(), collector);
            }
        }
    }
} 