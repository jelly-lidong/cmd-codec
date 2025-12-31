package com.iecas.cmd.engine;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 填充处理器
 * 负责处理节点的填充逻辑，支持多种填充模式
 */
@Slf4j
public class PaddingProcessor {

    private final ExpressionEngine expressionEngine;

    public PaddingProcessor(ExpressionEngine expressionEngine) {
        this.expressionEngine = expressionEngine;
    }

    /**
     * 处理协议中所有节点的填充
     *
     * @param protocol 协议对象
     * @param context  上下文信息
     * @throws CodecException 处理异常
     */
    public void processPadding(Protocol protocol, Map<String, Object> context) throws CodecException {
        log.debug("[填充处理] 开始处理协议填充");

        // 获取所有节点
        List<INode> allNodes = getAllNodes(protocol);

        // 收集所有填充节点
        List<INode> paddingNodes = new ArrayList<>();
        for (INode node : allNodes) {
            if (node.isPaddingNode()) {
                paddingNodes.add(node);
            }
        }

        log.debug("[填充处理] 共{} 个节点。发现 {} 个填充节点", allNodes.size(), paddingNodes.size());

        // 处理每个填充节点
        for (INode node : paddingNodes) {
            processPaddingNode(node, allNodes, context);
        }
        log.debug("[填充处理] 填充处理完成");
    }

    /**
     * 处理单个填充节点
     *
     * @param node     填充节点
     * @param allNodes 所有节点列表
     * @param context  上下文信息
     * @throws CodecException 处理异常
     */
    private void processPaddingNode(INode node, List<INode> allNodes, Map<String, Object> context) throws CodecException {
        PaddingConfig config = node.getPaddingConfig();
        if (config == null || !config.isValid()) {
            log.debug("[填充处理] 节点 '{}' 的填充配置无效", node.getName());
            return;
        }

        log.debug("[填充处理] 处理填充节点: {}", node.getName());
        log.debug("[填充处理] 填充配置: {}", config);

        // 检查填充启用条件
        if (!isPaddingEnabled(config, context)) {
            log.debug("[填充处理] 节点 '{}' 的填充被禁用", node.getName());
            return;
        }

        // 计算填充长度
        int paddingLength = calculatePaddingLength(config, node, allNodes, context);
        log.debug("[填充处理] 节点 '{}' 计算出的填充长度: {} 位", node.getName(), paddingLength);

        // 应用长度限制
        paddingLength = Math.max(paddingLength, config.getMinPaddingLength());
        paddingLength = Math.min(paddingLength, config.getMaxPaddingLength());

        if (paddingLength <= 0) {
            log.debug("[填充处理] 节点 '{}' 不需要填充", node.getName());
            return;
        }

        // 生成填充数据
        byte[] paddingData = generatePaddingData(config, paddingLength);

        // 更新节点信息
        updatePaddingNode(node, paddingLength, paddingData);

        log.debug("[填充处理] 节点 '{}' 填充完成，填充长度: {} 位", node.getName(), paddingLength);
    }

    /**
     * 检查填充是否启用
     *
     * @param config  填充配置
     * @param context 上下文信息
     * @return 是否启用填充
     * @throws CodecException 处理异常
     */
    private boolean isPaddingEnabled(PaddingConfig config, Map<String, Object> context) throws CodecException {
        if (!config.isEnabled()) {
            return false;
        }

        String enableCondition = config.getEnableCondition();
        if (enableCondition == null || enableCondition.trim().isEmpty()) {
            return true;
        }

        try {
            Object result = expressionEngine.evaluate(enableCondition, context);
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0.0;
            } else {
                return result != null;
            }
        } catch (Exception e) {
            log.error("[填充处理] 评估填充启用条件失败: {}", enableCondition);
            throw new CodecException("评估填充启用条件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算填充长度
     *
     * @param config   填充配置
     * @param node     当前节点
     * @param allNodes 所有节点列表
     * @param context  上下文信息
     * @return 填充长度（位数）
     * @throws CodecException 计算异常
     */
    private int calculatePaddingLength(PaddingConfig config, INode node, List<INode> allNodes, Map<String, Object> context) throws CodecException {
        switch (config.getPaddingType()) {
            case FIXED_LENGTH:
                return calculateFixedLengthPadding(config, node);
            case ALIGNMENT:
                return calculateAlignmentPadding(config, node, allNodes);
            case DYNAMIC:
                return calculateDynamicPadding(config, node, context);
            case FILL_CONTAINER:
                return calculateFillContainerPadding(config, node, allNodes, context);
            default:
                throw new CodecException("不支持的填充类型: " + config.getPaddingType());
        }
    }

    /**
     * 计算固定长度填充
     */
    private int calculateFixedLengthPadding(PaddingConfig config, INode node) {
        int currentLength = node.getActualDataLength();
        int targetLength = config.getTargetLength();
        return Math.max(0, targetLength - currentLength);
    }

    /**
     * 计算对齐填充
     */
    private int calculateAlignmentPadding(PaddingConfig config, INode node, List<INode> allNodes) {
        int alignmentBits = config.getTargetLength();

        if (alignmentBits <= 0) {
            return 0;
        }

        // 计算当前节点之前所有数据的累计长度
        int cumulativeLength = calculateCumulativeLengthBeforeNode(node, allNodes);

        log.debug("[对齐填充] 节点 '{}' 之前的累计长度: {} 位", node.getName(), cumulativeLength);
        log.debug("[对齐填充] 对齐长度: {} 位", alignmentBits);

        int remainder = cumulativeLength % alignmentBits;
        int paddingLength = remainder == 0 ? 0 : alignmentBits - remainder;

        log.debug("[对齐填充] 余数: {} 位，需要填充: {} 位", remainder, paddingLength);

        return paddingLength;
    }

    /**
     * 计算当前节点之前所有数据的累计长度
     *
     * @param currentNode 当前节点
     * @param allNodes    所有节点列表
     * @return 累计长度（位数）
     */
    private int calculateCumulativeLengthBeforeNode(INode currentNode, List<INode> allNodes) {
        int cumulativeLength = 0;

        // 获取按编码顺序排列的叶子节点列表
        List<INode> orderedLeafNodes = getOrderedLeafNodes(allNodes);

        log.debug("[累计长度] 开始计算节点 '{}' 之前的累计长度", currentNode.getName());

        // 遍历有序的叶子节点，累计当前节点之前的节点长度
        for (INode node : orderedLeafNodes) {
            if (node == currentNode) {
                // 找到当前节点，停止累计
                log.debug("[累计长度] 找到目标节点 '{}'，停止累计", currentNode.getName());
                break;
            }

            // 跳过填充节点（避免循环依赖）
            if (node.isPaddingNode()) {
                log.debug("[累计长度] 跳过填充节点 '{}'", node.getName());
                continue;
            }

            // 跳过结构节点（Protocol、Header、Body、Check等）
            if (node.isStructureNode()) {
                log.debug("[累计长度] 跳过结构节点 '{}'", node.getName());
                continue;
            }

            // 累计节点长度
            int nodeLength = node.getActualDataLength();
            if (nodeLength <= 0) {
                nodeLength = node.getLength();
            }

            if (nodeLength > 0) {
                cumulativeLength += nodeLength;
                log.debug("[累计长度] 节点 '{}': {} 位，累计: {} 位", node.getName(), nodeLength, cumulativeLength);
            }
        }

        //log.debug("[累计长度] 节点 '{}' 之前的最终累计长度: {} 位", currentNode.getName(), cumulativeLength);
        return cumulativeLength;
    }

    /**
     * 获取按编码顺序排列的叶子节点列表
     *
     * @param allNodes 所有节点列表
     * @return 有序的叶子节点列表
     */
    private List<INode> getOrderedLeafNodes(List<INode> allNodes) {
        List<INode> leafNodes = new ArrayList<>();

        // 找到Protocol根节点
        Protocol protocol = null;
        for (INode node : allNodes) {
            if (node instanceof Protocol) {
                protocol = (Protocol) node;
                break;
            }
        }

        if (protocol != null) {
            // 按照协议结构顺序收集叶子节点：Header -> Body -> Check
            if (protocol.getHeader() != null) {
                collectLeafNodes(protocol.getHeader(), leafNodes);
            }
            if (protocol.getBody() != null) {
                collectLeafNodes(protocol.getBody(), leafNodes);
            }
            if (protocol.getTail() != null) {
                collectLeafNodes(protocol.getTail(), leafNodes);
            }
        }

        return leafNodes;
    }

    /**
     * 递归收集叶子节点（实际数据节点）
     *
     * @param node      当前节点
     * @param collector 叶子节点收集器
     */
    private void collectLeafNodes(INode node, List<INode> collector) {
        if (node == null) {
            return;
        }

        // 如果是叶子节点（没有子节点或子节点为空），则添加到收集器
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            collector.add(node);
        } else {
            // 递归收集子节点，按order排序
            List<INode> children = new ArrayList<>(node.getChildren());
            children.sort((a, b) -> {
                float orderA = getNodeOrder(a);
                float orderB = getNodeOrder(b);
                return Float.compare(orderA, orderB);
            });

            for (INode child : children) {
                collectLeafNodes(child, collector);
            }
        }
    }

    /**
     * 获取节点的order值
     */
    private float getNodeOrder(INode node) {
        if (node instanceof Node) {
            return ((Node) node).getOrder();
        }
        return 0;
    }

    /**
     * 判断是否为结构节点
     */
    private boolean isStructureNode(INode node) {
        return node instanceof Protocol || node instanceof Header || node instanceof Body || node instanceof Tail || (node.getChildren() != null && !node.getChildren().isEmpty());
    }

    /**
     * 计算动态填充
     */
    private int calculateDynamicPadding(PaddingConfig config, INode node, Map<String, Object> context) throws CodecException {
        String expression = config.getLengthExpression();

        // 准备表达式上下文
        Map<String, Object> expressionContext = new HashMap<>(context);
        expressionContext.put("currentLength", node.getActualDataLength());
        expressionContext.put("targetLength", config.getTargetLength());
        expressionContext.put("node", node);

        try {
            Object result = expressionEngine.evaluate(expression, expressionContext);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            } else {
                throw new CodecException("填充长度表达式结果不是数字: " + result);
            }
        } catch (Exception e) {
            log.error("[填充处理] 评估填充长度表达式失败: {}", expression);
            throw new CodecException("评估填充长度表达式失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算补齐容器空间填充
     */
    private int calculateFillContainerPadding(PaddingConfig config, INode node, List<INode> allNodes, Map<String, Object> context) throws CodecException {

        // 获取容器固定长度
        int containerFixedLength;
        if (config.isAutoCalculateContainerLength()) {
            // 自动从容器节点获取长度
            String containerNodeRef = config.getContainerNode();
            INode containerNode = findContainerNode(containerNodeRef, allNodes);
            if (containerNode == null) {
                throw new CodecException("未找到容器节点: " + containerNodeRef);
            }
            containerFixedLength = containerNode.getLength();

            // 计算容器中所有子节点的实际长度之和（排除当前填充节点）
            int usedLength = calculateUsedLengthInContainer(containerNode, node);

            log.debug("[容器填充] 容器节点: {}, 固定长度: {}, 已使用长度: {}", containerNode.getName(), containerFixedLength, usedLength);

            return Math.max(0, containerFixedLength - usedLength);

        } else {
            // 使用指定的固定长度
            containerFixedLength = config.getContainerFixedLength();

            // 查找父容器节点来计算已使用长度
            INode parentContainer = findParentContainer(node, allNodes);
            if (parentContainer == null) {
                throw new CodecException("无法找到填充节点的父容器: " + node.getName());
            }

            int usedLength = calculateUsedLengthInContainer(parentContainer, node);

            log.debug("[容器填充] 指定固定长度: {}, 已使用长度: {}", containerFixedLength, usedLength);

            return Math.max(0, containerFixedLength - usedLength);
        }
    }

    /**
     * 查找节点的父容器
     *
     * @param node     当前节点
     * @param allNodes 所有节点列表
     * @return 父容器节点
     */
    private INode findParentContainer(INode node, List<INode> allNodes) {
        for (INode candidate : allNodes) {
            List<String> childrenIds = candidate.getChildren().stream().map(Node::getId).collect(Collectors.toList());
            if (candidate.getChildren() != null && childrenIds.contains(node.getId())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 计算容器中已使用的长度（改进版本）
     *
     * @param containerNode 容器节点
     * @param excludeNode   要排除的节点（通常是填充节点）
     * @return 已使用的长度（位数）
     */
    private int calculateUsedLengthInContainer(INode containerNode, INode excludeNode) {
        int usedLength = 0;

        if (containerNode.getChildren() != null) {
            for (INode child : containerNode.getChildren()) {
                if (child != excludeNode) {
                    // 使用实际数据长度，如果没有设置则使用节点长度
                    int childLength = child.getActualDataLength();
                    if (childLength <= 0) {
                        childLength = child.getLength();
                    }
                    usedLength += childLength;

                    log.debug("[容器填充] 子节点: {}, 长度: {}", child.getName(), childLength);
                }
            }
        }

        log.debug("[容器填充] 容器 '{}' 已使用长度: {}", containerNode.getName(), usedLength);
        return usedLength;
    }

    /**
     * 计算补齐剩余空间填充
     */
    private int calculateFillRemainingPadding(PaddingConfig config, INode node, List<INode> allNodes, Map<String, Object> context) throws CodecException {
        String containerNodeRef = config.getContainerNode();

        // 查找容器节点
        INode containerNode = findContainerNode(containerNodeRef, allNodes);
        if (containerNode == null) {
            throw new CodecException("未找到容器节点: " + containerNodeRef);
        }

        // 计算容器中已使用的长度
        int usedLength = calculateUsedLengthInContainer(containerNode, node);
        int containerLength = containerNode.getLength();

        return Math.max(0, containerLength - usedLength);
    }

    /**
     * 查找容器节点
     *
     * <p><b>核心设计：</b>支持通过节点名称、简单节点ID或协议ID:节点ID格式查找节点。</p>
     *
     * @param containerNodeRef 容器节点引用（支持节点名称、#nodeId 或 #protocolId:nodeId 格式）
     * @param allNodes         所有节点列表
     * @return 找到的容器节点，如果未找到返回null
     */
    private INode findContainerNode(String containerNodeRef, List<INode> allNodes) {
        if (containerNodeRef == null || containerNodeRef.trim().isEmpty()) {
            return null;
        }

        // 如果是引用格式（以#开头），解析协议ID:节点ID
        String scopedNodeId = null;
        String simpleNodeId = null;
        if (containerNodeRef.startsWith("#")) {
            try {
                // 尝试解析为引用格式（这里没有currentProtocolId，先尝试简单匹配）
                String ref = containerNodeRef.substring(1);
                if (ref.contains(":")) {
                    // 格式：#protocolId:nodeId
                    scopedNodeId = ref;
                } else {
                    // 格式：#nodeId（需要在查找时添加协议ID）
                    simpleNodeId = ref;
                }
            } catch (Exception e) {
                // 解析失败，继续使用原始逻辑
            }
        }

        // 查找节点：优先使用协议ID:节点ID，其次使用简单节点ID，最后使用名称
        for (INode node : allNodes) {
            // 1. 按协议ID:节点ID查找（如果提供了scopedNodeId）
            if (scopedNodeId != null) {
                // 尝试构造节点的协议ID:节点ID格式（需要知道协议ID）
                // 由于这里没有协议ID上下文，先尝试直接匹配节点ID
                String nodeId = node.getId();
                if (nodeId != null) {
                    // 如果scopedNodeId格式是 protocolId:nodeId，提取nodeId部分
                    int colonIdx = scopedNodeId.indexOf(':');
                    if (colonIdx > 0) {
                        String refNodeId = scopedNodeId.substring(colonIdx + 1);
                        if (refNodeId.equals(nodeId)) {
                            return node;
                        }
                    } else if (scopedNodeId.equals(nodeId)) {
                        return node;
                    }
                }
            }
            
            // 2. 按简单节点ID查找
            if (simpleNodeId != null && simpleNodeId.equals(node.getId())) {
                return node;
            }
            
            // 3. 按原始引用格式匹配（向后兼容）
            if (containerNodeRef.equals(node.getName()) || 
                containerNodeRef.equals("#" + node.getId()) || 
                containerNodeRef.equals(node.getId())) {
                return node;
            }
        }

        return null;
    }

    /**
     * 生成填充数据
     *
     * @param config            填充配置
     * @param paddingLengthBits 填充长度（位数）
     * @return 填充数据
     */
    private byte[] generatePaddingData(PaddingConfig config, int paddingLengthBits) {
        if (paddingLengthBits <= 0) {
            return new byte[0];
        }

        byte[] paddingPattern = config.getPaddingBytes();
        int paddingLengthBytes = (paddingLengthBits + 7) / 8; // 向上取整到字节

        if (!config.isRepeatPattern()) {
            // 不重复模式，只填充一次模式
            int copyLength = Math.min(paddingPattern.length, paddingLengthBytes);
            byte[] result = new byte[paddingLengthBytes];
            System.arraycopy(paddingPattern, 0, result, 0, copyLength);
            return result;
        } else {
            // 重复模式，重复填充模式直到达到目标长度
            byte[] result = new byte[paddingLengthBytes];
            for (int i = 0; i < paddingLengthBytes; i++) {
                result[i] = paddingPattern[i % paddingPattern.length];
            }
            return result;
        }
    }

    /**
     * 更新填充节点信息
     *
     * @param node          填充节点
     * @param paddingLength 填充长度（位数）
     * @param paddingData   填充数据
     */
    private void updatePaddingNode(INode node, int paddingLength, byte[] paddingData) {
        // 保存原始数据长度（只在第一次处理时保存）
        if (node.getActualDataLength() == 0 || node.getActualDataLength() == node.getLength()) {
            node.setActualDataLength(node.getLength());
        }

        // 更新节点长度为填充后的实际长度
        int newLength = paddingLength;
        if (node instanceof Node) {
            ((Node) node).setLength(newLength);
        }

        // 设置填充数据作为节点值（如果需要）
        if (paddingData.length > 0) {
            StringBuilder hexValue = new StringBuilder("0x");
            for (byte b : paddingData) {
                hexValue.append(String.format("%02X", b & 0xFF));
            }
            node.setValue(hexValue.toString());
        }

        log.debug("[填充处理] 节点 '{}' 更新完成: 原长度={}, 填充长度={}, 新长度={}", node.getName(), node.getActualDataLength(), paddingLength, newLength);
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
        } else if (node instanceof Body) {
            Body body = (Body) node;
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