package com.iecas.cmd.engine;

import cn.hutool.core.collection.CollectionUtil;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.*;
import com.iecas.cmd.util.ExpressionValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 协议依赖图构建器
 *
 * <p>该类负责将协议结构、节点、依赖关系等添加到DependencyGraph中，并校验表达式的正确性。
 * 主要功能包括：</p>
 * <ul>
 *   <li>递归遍历协议结构，注册所有节点到依赖图</li>
 *   <li>建立节点之间的依赖关系</li>
 *   <li>验证节点表达式的语法和引用有效性</li>
 *   <li>构建完整的依赖图用于后续的拓扑排序和依赖分析</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>注册阶段：将所有协议节点添加到依赖图中，建立ID映射</li>
 *   <li>验证阶段：检查表达式语法和引用有效性</li>
 *   <li>依赖构建阶段：根据表达式和结构关系建立依赖边</li>
 * </ol>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
public class ProtocolDependencyBuilder {

    /**
     * 表达式解析器，用于解析节点表达式中的依赖引用
     */
    private final ExpressionParser expressionParser;

    /**
     * 表达式验证器，用于验证表达式的语法和引用有效性
     */
    private final ExpressionValidator expressionValidator;

    /**
     * 依赖图对象，存储所有节点和依赖关系
     */
    private final DependencyGraph dependencyGraph;
    /**
     * 所有叶子节点列表（不包含Header、Body、Tail等容器节点）
     */
    private final List<INode> allLeafNodes;

    private String currentProtocolId;

    /**
     * 记录已处理过的结构体节点ID，避免重复处理
     * 主要用于防止循环引用和重复注册
     */
    private final Set<String> processedNodeIds = new HashSet<>();

    /**
     * 构造函数
     *
     * @param expressionParser    表达式解析器
     * @param expressionValidator 表达式验证器
     * @param dependencyGraph     依赖图对象
     * @param allLeafNodes        所有叶子节点列表
     */
    public ProtocolDependencyBuilder(ExpressionParser expressionParser, ExpressionValidator expressionValidator, DependencyGraph dependencyGraph, List<INode> allLeafNodes) {
        this.expressionParser = expressionParser;
        this.expressionValidator = expressionValidator;
        this.dependencyGraph = dependencyGraph;
        this.allLeafNodes = allLeafNodes;
        log.debug("[依赖构建器] 初始化完成 - 解析器: {}, 验证器: {}, 依赖图: {}",
                expressionParser.getClass().getSimpleName(),
                expressionValidator.getClass().getSimpleName(),
                dependencyGraph.getClass().getSimpleName());
    }

    /**
     * 构建依赖图的主入口方法
     *
     * <p>该方法执行完整的依赖图构建流程：</p>
     * <ol>
     *   <li>清空之前的构建状态</li>
     *   <li>注册所有协议节点到依赖图</li>
     *   <li>验证节点表达式的正确性</li>
     *   <li>建立节点间的依赖关系</li>
     *   <li>输出构建结果和调试信息</li>
     * </ol>
     *
     * @param protocol 要构建依赖图的协议对象
     * @throws CodecException 当构建过程中发生错误时抛出
     */
    public void build(Protocol protocol) throws CodecException {
        // 清空之前的构建状态
        processedNodeIds.clear();
        dependencyGraph.clear();
        this.currentProtocolId = protocol.getId();
        // 第一步：注册所有节点，保证依赖边的两端节点都已存在于依赖图中
        addProtocolToGraph(protocol);

        // 构建ID到节点的映射表，用于表达式验证（key为协议ID:节点ID）
        Map<String, INode> nodeMap = dependencyGraph.getNodeMap();

        // 第三步：建立依赖边，避免依赖目标节点还未注册导致依赖丢失或异常
        addDependenciesToGraph(protocol);

        // 第四步：验证节点表达式的正确性（在依赖关系建立之后）
        validateNodeExpressions(nodeMap);

        // 打印所有节点及其依赖关系
        log.debug("[依赖构建] 打印依赖图节点依赖关系:");
        for (Map.Entry<String, Set<String>> entry : dependencyGraph.getNodeDependenciesMap().entrySet()) {
            String node = entry.getKey();
            Set<String> dependencies = entry.getValue();
            if (!dependencies.isEmpty()) {
                log.debug("[依赖关系] - {}   依赖于: {}", node, String.join("｜ ", dependencies));
            } else {
                log.debug("[依赖关系] - {}   无依赖", node);
            }
        }

        // 调试：检查所有ID映射
//        log.debug("[依赖构建] ============================== 所有ID映射 ===============================");
//        Map<String, String> idMap = dependencyGraph.getNodeIdMap();
//        for (Map.Entry<String, String> entry : idMap.entrySet()) {
//            log.debug("[ID映射] ID: {} -> 路径: {}", entry.getKey(), entry.getValue());
//        }

        log.debug("============================== 协议依赖图构建完成 ===============================");
        log.debug("[依赖构建] 构建完成统计 - 总节点数: {}, 总依赖关系数: {}, 处理节点数: {}",
                dependencyGraph.getNodeMap().size(),
                dependencyGraph.getNodeDependenciesMap().size(),
                processedNodeIds.size());
    }

    /**
     * 打印节点及其依赖关系的递归方法
     *
     * <p>该方法用于调试时打印依赖图的层次结构，显示每个节点的依赖关系。</p>
     *
     * <p><b>核心设计：</b>使用协议ID:节点ID格式查找依赖关系。</p>
     *
     * @param node  要打印的节点
     * @param level 当前节点的层级深度，用于生成缩进
     */
    @SuppressWarnings("unused")
    private void printNodeDependencies(INode node, int level) {
        // 根据层级生成缩进，使用Java 8兼容的方式
        String indent = String.join("", Collections.nCopies(level, "    "));

        // 构建协议作用域的节点ID（协议ID:节点ID）来查找依赖关系
        String nodeId = node.getId();
        String scopedNodeId = nodeId != null && !nodeId.isEmpty() && currentProtocolId != null
                ? currentProtocolId + ":" + nodeId
                : nodeId;

        // 获取当前节点的依赖关系（依赖图使用协议ID:节点ID作为key）
        Set<String> dependencies = scopedNodeId != null
                ? dependencyGraph.getNodeDependenciesMap().get(scopedNodeId)
                : null;

        if (dependencies != null && !dependencies.isEmpty()) {
            log.debug("[依赖层次] {}节点: {} (ID: {}) 有 {} 个依赖",
                    indent, node.getName(), scopedNodeId, dependencies.size());

            // 递归打印每个依赖节点（依赖ID已经是协议ID:节点ID格式）
            for (String depScopedId : dependencies) {
                INode depNode = dependencyGraph.getNodeMap().get(depScopedId);
                if (depNode != null) {
                    log.debug("[依赖层次] {}- 依赖节点: {} (ID: {})", indent, depNode.getName(), depScopedId);
                    printNodeDependencies(depNode, level + 1); // 递归打印依赖节点
                } else {
                    log.warn("[依赖层次] {}警告: 找不到依赖节点ID: {}", indent, depScopedId);
                }
            }
        } else {
            log.debug("[依赖层次] {}节点: {} (ID: {}) 无依赖", indent, node.getName(), scopedNodeId);
        }
    }

    /**
     * 验证节点表达式的正确性
     *
     * <p>该方法遍历所有节点，验证其正向和反向表达式的语法正确性，
     * 以及表达式中的引用（ID引用）是否在依赖图中存在。</p>
     *
     * <p><b>核心设计：</b>nodeMap的key为协议ID:节点ID格式。</p>
     *
     * @param nodeMap 节点ID（协议ID:节点ID）到节点对象的映射表
     * @throws CodecException 当表达式验证失败时抛出
     */
    private void validateNodeExpressions(Map<String, INode> nodeMap) throws CodecException {
        log.debug("[表达式验证] 开始验证 {} 个节点的表达式", nodeMap.size());

        for (Map.Entry<String, INode> entry : nodeMap.entrySet()) {
            INode node = entry.getValue();
            String nodeId = entry.getKey(); // 协议ID:节点ID格式
            //log.debug("[表达式验证] 验证节点: {} (ID: {})", node.getName(), nodeId);

            // 验证正向表达式
            String fwdExpr = node.getFwdExpr();
            if (fwdExpr != null && !fwdExpr.trim().isEmpty()) {
                log.debug("[表达式验证] 验证正向表达式: {} = {}", nodeId, fwdExpr);
                try {
                    // 验证正向表达式的语法
                    expressionValidator.validateSyntax(fwdExpr);
                    log.debug("[表达式验证] 正向表达式语法验证通过: {}", fwdExpr);

                    // 验证正向表达式中的引用（ID引用）是否有效，ID引用必须存在于依赖图中
                    expressionValidator.validateReferencesInGraph(fwdExpr, dependencyGraph, currentProtocolId);
                    log.debug("[表达式验证] 正向表达式引用验证通过: {}", fwdExpr);
                } catch (Exception e) {
                    log.error("[表达式验证] 正向表达式验证失败: {} = {}, 错误: {}", nodeId, fwdExpr, e.getMessage());
                    throw new CodecException("正向表达式验证失败: " + e.getMessage(), e);
                }
            }

            // 验证反向表达式
            String bwdExpr = node.getBwdExpr();
            if (bwdExpr != null && !bwdExpr.trim().isEmpty()) {
                log.debug("[表达式验证] 验证反向表达式: {} = {}", nodeId, bwdExpr);
                try {
                    // 验证反向表达式的语法
                    expressionValidator.validateSyntax(bwdExpr);
                    log.debug("[表达式验证] 反向表达式语法验证通过: {}", bwdExpr);

                    // 验证反向表达式中的引用（ID引用）是否有效，ID引用必须存在于依赖图中
                    expressionValidator.validateReferencesInGraph(bwdExpr, dependencyGraph, currentProtocolId);
                    log.debug("[表达式验证] 反向表达式引用验证通过: {}", bwdExpr);
                } catch (Exception e) {
                    log.error("[表达式验证] 反向表达式验证失败: {} = {}, 错误: {}", nodeId, bwdExpr, e.getMessage());
                    throw new CodecException("反向表达式验证失败: " + e.getMessage(), e);
                }
            }
        }

        log.debug("[表达式验证] 所有节点表达式验证完成，共验证 {} 个节点", nodeMap.size());
    }

    /**
     * 将协议及其组成部分（头、体、尾）添加到依赖图中
     *
     * <p>该方法负责将协议的结构信息注册到依赖图中，以便后续的依赖关系分析。
     * 这是依赖图构建的第一步，只负责节点注册，不建立依赖关系。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>注册协议头部（Header）</li>
     *   <li>注册协议主体（Body）</li>
     *   <li>注册协议尾部（Tail）</li>
     *   <li>注册协议中的其他节点</li>
     * </ol>
     *
     * @param protocol 要添加到图中的协议对象
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addProtocolToGraph(Protocol protocol) throws CodecException {
        // 定义协议的根名称
        String protocolName = protocol.getName();

        // 获取并注册协议的头部信息
        Header header = protocol.getHeader();
        if (header != null) {
            addHeaderToGraph(protocolName, header);
        }

        // 获取并注册协议的主体信息
        Body body = protocol.getBody();
        if (body != null) {
            addBodyToGraph(protocolName, body);
        }

        // 获取并注册协议的尾部信息
        Tail tail = protocol.getTail();
        if (tail != null) {
            addTailToGraph(protocolName, tail);
        }

        // 获取并注册协议中所有的其他节点
        List<Node> nodes = protocol.getNodes();
        if (CollectionUtil.isNotEmpty(nodes)) {
            addChildNodesToGraph(nodes, protocolName);
        }
    }

    /**
     * 将协议头部添加到依赖图中
     *
     * <p>该方法处理协议头部的注册，包括：</p>
     * <ul>
     *   <li>检查头部是否已处理过（避免重复处理）</li>
     *   <li>将头部节点添加到依赖图</li>
     *   <li>建立ID映射关系</li>
     *   <li>递归处理头部下的所有子节点</li>
     * </ul>
     *
     * @param path   当前节点的路径
     * @param header 要添加的协议头部
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addHeaderToGraph(String path, Header header) throws CodecException {
        if (header == null) {
            log.debug("[头部注册] 头部为空，跳过注册");
            return;
        }

        String headerId = header.getId();
        String headerPath = path + "." + header.getName();
        header.setPath(headerPath);
        log.debug("[头部注册] 注册头部: {} -> {}", header.getName(), headerPath);

        // 如果头部有ID，进行重复检查和注册
        if (headerId != null && !headerId.isEmpty()) {
            String scopedHeaderId = currentProtocolId + ":" + headerId;
            if (processedNodeIds.contains(scopedHeaderId)) {
                log.debug("[头部注册] 头部ID已处理过，跳过: {}", scopedHeaderId);
                return;
            }

            // 添加到依赖图和ID映射（使用协议ID:节点ID）
            dependencyGraph.addNode(header, headerPath, currentProtocolId);
            dependencyGraph.addNodeIdMapping(scopedHeaderId, headerPath);
            processedNodeIds.add(scopedHeaderId);
            log.debug("[头部注册] 头部注册成功: ID={}, 路径={}", scopedHeaderId, headerPath);
        } else {
            log.debug("[头部注册] 头部无ID，仅添加到依赖图: {}", headerPath);
            dependencyGraph.addNode(header, headerPath, currentProtocolId);
        }

        // 递归处理头部下的所有子节点
        List<Node> headerNodes = header.getNodes();
        if (CollectionUtil.isNotEmpty(headerNodes)) {
            log.debug("[头部注册] 头部 {} 有 {} 个子节点，开始递归注册", header.getName(), headerNodes.size());
            for (INode node : headerNodes) {
                addNodeToGraph(headerPath, node);
            }
        } else {
            log.debug("[头部注册] 头部 {} 无子节点", header.getName());
        }
    }

    /**
     * 将协议主体添加到依赖图中
     *
     * <p>该方法处理协议主体的注册，协议主体是最复杂的部分，可能包含：</p>
     * <ul>
     *   <li>嵌套的头部、主体、尾部</li>
     *   <li>节点组容器</li>
     *   <li>普通节点</li>
     * </ul>
     *
     * @param path 当前节点的路径
     * @param body 要添加的协议主体
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addBodyToGraph(String path, Body body) throws CodecException {
        if (body == null) {
            log.debug("[主体注册] 主体为空，跳过注册");
            return;
        }

        String bodyId = body.getId();
        String bodyPath = path + "." + body.getName();
        log.debug("[主体注册] 注册主体: {} -> {}", body.getName(), bodyPath);

        // 检查是否已处理过
        if (bodyId != null && !bodyId.isEmpty()) {
            String scopedBodyId = currentProtocolId + ":" + bodyId;
            if (processedNodeIds.contains(scopedBodyId)) {
                log.debug("[主体注册] 主体ID已处理过，跳过: {}", scopedBodyId);
                return;
            }
        }

        // 添加到依赖图和ID映射（使用协议ID:节点ID）
        if (bodyId != null && !bodyId.isEmpty()) {
            String scopedBodyId = currentProtocolId + ":" + bodyId;
            processedNodeIds.add(scopedBodyId);
            dependencyGraph.addNodeIdMapping(scopedBodyId, bodyPath);
            log.debug("[主体注册] 主体ID映射建立: {} -> {}", scopedBodyId, bodyPath);
        }

        dependencyGraph.addNode(body, bodyPath, currentProtocolId);
        log.debug("[主体注册] 主体注册成功: {}", bodyPath);

        // 递归处理嵌套的头部
        if (body.getHeader() != null) {
            //log.debug("[主体注册] 主体 {} 有嵌套头部，开始注册", body.getName());
            addHeaderToGraph(bodyPath, body.getHeader());
        }

        // 递归处理嵌套的主体（支持多层嵌套）
        if (body.getBody() != null) {
            // log.debug("[主体注册] 主体 {} 有嵌套主体，开始注册", body.getName());
            addBodyToGraph(bodyPath, body.getBody());
        }

        // 递归处理嵌套的尾部
        if (body.getTail() != null) {
            log.debug("[主体注册] 主体 {} 有嵌套尾部，开始注册", body.getName());
            addTailToGraph(bodyPath, body.getTail());
        }

        // 处理主体下的所有节点
        List<Node> bodyNodes = body.getNodes();
        if (CollectionUtil.isNotEmpty(bodyNodes)) {
            log.debug("[主体注册] 主体 {} 有 {} 个节点，开始注册", body.getName(), bodyNodes.size());
            addChildNodesToGraph(bodyNodes, bodyPath);
        } else {
            log.debug("[主体注册] 主体 {} 无节点", body.getName());
        }
    }

    /**
     * 将子节点列表添加到依赖图中
     *
     * <p>该方法统一处理子节点的注册，根据节点类型选择不同的处理策略：</p>
     * <ul>
     *   <li>NodeGroup：节点组容器，需要特殊处理</li>
     *   <li>普通Node：直接添加到依赖图</li>
     * </ul>
     *
     * @param childNodes 子节点列表
     * @param path       父节点的路径
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addChildNodesToGraph(List<Node> childNodes, String path) throws CodecException {

        for (Node node : childNodes) {
            if (node instanceof NodeGroup) {
                addGroupContainerToGraph(path, (NodeGroup) node);
            } else {
                addNodeToGraph(path, node);
            }
        }

    }

    /**
     * 将节点组容器添加到依赖图中
     *
     * <p>节点组容器是一种特殊的节点类型，可以包含其他结构体节点。
     * 该方法递归处理容器内的所有节点，支持多层嵌套。</p>
     *
     * @param bodyPath  父节点的路径
     * @param nodeGroup 要添加的节点组容器
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addGroupContainerToGraph(String bodyPath, NodeGroup nodeGroup) throws CodecException {
        if (nodeGroup == null) {
            log.debug("[组容器注册] 组容器为空，跳过注册");
            return;
        }

        String groupContainerId = nodeGroup.getId();
        String groupContainerPath = bodyPath + "." + nodeGroup.getName();
        log.debug("[组容器注册] 注册组容器: {} -> {}", nodeGroup.getName(), groupContainerPath);

        // 如果组容器有ID，进行重复检查和注册
        if (groupContainerId != null && !groupContainerId.isEmpty()) {
            String scopedGroupContainerId = currentProtocolId + ":" + groupContainerId;
            if (processedNodeIds.contains(scopedGroupContainerId)) {
                log.debug("[组容器注册] 组容器ID已处理过，跳过: {}", scopedGroupContainerId);
                return;
            }

            // 添加到依赖图和ID映射（使用协议ID:节点ID）
            dependencyGraph.addNode(nodeGroup, groupContainerPath, currentProtocolId);
            dependencyGraph.addNodeIdMapping(scopedGroupContainerId, groupContainerPath);
            processedNodeIds.add(scopedGroupContainerId);
            log.debug("[组容器注册] 组容器注册成功: ID={}, 路径={}", scopedGroupContainerId, groupContainerPath);
        } else {
            log.debug("[组容器注册] 组容器无ID，仅添加到依赖图: {}", groupContainerPath);
            dependencyGraph.addNode(nodeGroup, groupContainerPath, currentProtocolId);
        }

        // 递归处理组容器内的所有节点
        List<Node> groupNodes = nodeGroup.getGroupNodes();
        if (CollectionUtil.isNotEmpty(groupNodes)) {
            log.debug("[组容器注册] 组容器 {} 有 {} 个节点，开始递归注册",
                    nodeGroup.getName(), groupNodes.size());

            for (INode node : groupNodes) {
                // 根据节点类型选择不同的处理策略
                if (node instanceof Header) {
                    Header header = (Header) node;
                    log.debug("[组容器注册] 处理嵌套头部: {}", header.getName());
                    addHeaderToGraph(groupContainerPath, header);
                } else if (node instanceof Body) {
                    Body body = (Body) node;
                    log.debug("[组容器注册] 处理嵌套主体: {}", body.getName());
                    addBodyToGraph(groupContainerPath, body);
                } else if (node instanceof Tail) {
                    Tail tail = (Tail) node;
                    log.debug("[组容器注册] 处理嵌套尾部: {}", tail.getName());
                    addTailToGraph(groupContainerPath, tail);
                } else if (node instanceof NodeGroup) {
                    NodeGroup subNodeGroup = (NodeGroup) node;
                    log.debug("[组容器注册] 处理嵌套组容器: {}", subNodeGroup.getName());
                    addGroupContainerToGraph(groupContainerPath, subNodeGroup);
                } else if (node instanceof Node) {
                    log.debug("[组容器注册] 处理普通节点: {}", node.getName());
                    addNodeToGraph(groupContainerPath, node);
                } else {
                    log.warn("[组容器注册] 未知节点类型: {} - {}", node.getClass().getSimpleName(), node.getName());
                }
            }
        } else {
            log.debug("[组容器注册] 组容器 {} 无节点", nodeGroup.getName());
        }

        // 确保节点组容器本身也被添加到依赖图中（如果尚未添加）
        String scopedGroupContainerId = groupContainerId != null && !groupContainerId.isEmpty()
                ? currentProtocolId + ":" + groupContainerId
                : null;
        if (scopedGroupContainerId != null && !dependencyGraph.getNodeMap().containsKey(scopedGroupContainerId)) {
            dependencyGraph.addNode(nodeGroup, groupContainerPath, currentProtocolId);
            log.debug("[组容器注册] 节点组容器添加到依赖图: {}", groupContainerPath);
        }
    }

    /**
     * 将协议尾部添加到依赖图中
     *
     * <p>该方法处理协议尾部的注册，尾部通常包含校验信息。</p>
     *
     * @param path 当前节点的路径
     * @param tail 要添加的协议尾部
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addTailToGraph(String path, Tail tail) throws CodecException {
        if (tail == null) {
            log.debug("[尾部注册] 尾部为空，跳过注册");
            return;
        }

        String checkId = tail.getId();
        String checkPath = path + "." + tail.getName();
        log.debug("[尾部注册] 注册尾部: {} -> {}", tail.getName(), checkPath);

        // 如果尾部有ID，进行重复检查和注册
        if (checkId != null && !checkId.isEmpty()) {
            String scopedCheckId = currentProtocolId + ":" + checkId;
            if (processedNodeIds.contains(scopedCheckId)) {
                log.debug("[尾部注册] 尾部ID已处理过，跳过: {}", scopedCheckId);
                return;
            }

            // 添加到依赖图和ID映射（使用协议ID:节点ID）
            processedNodeIds.add(scopedCheckId);
            dependencyGraph.addNode(tail, checkPath, currentProtocolId);
            dependencyGraph.addNodeIdMapping(scopedCheckId, checkPath);
            log.debug("[尾部注册] 尾部注册成功: ID={}, 路径={}", scopedCheckId, checkPath);
        } else {
            log.debug("[尾部注册] 尾部无ID，仅添加到依赖图: {}", checkPath);
            dependencyGraph.addNode(tail, checkPath, currentProtocolId);
        }

        // 递归处理尾部下的所有子节点
        List<Node> tailNodes = tail.getNodes();
        if (CollectionUtil.isNotEmpty(tailNodes)) {
            log.debug("[尾部注册] 尾部 {} 有 {} 个子节点，开始递归注册", tail.getName(), tailNodes.size());
            for (INode node : tailNodes) {
                addNodeToGraph(checkPath, node);
            }
        } else {
            log.debug("[尾部注册] 尾部 {} 无子节点", tail.getName());
        }
    }

    /**
     * 将普通节点添加到依赖图中
     *
     * <p>该方法处理普通节点的注册，普通节点是最基本的节点类型。</p>
     *
     * @param path 父节点的路径
     * @param node 要添加的节点
     * @throws CodecException 当注册节点过程中发生错误时抛出
     */
    private void addNodeToGraph(String path, INode node) throws CodecException {
        String nodePath = path + "." + node.getName();
        dependencyGraph.addNode(node, nodePath, currentProtocolId);
    }

    /**
     * 添加依赖关系到依赖图
     *
     * <p><b>核心设计：</b>依赖关系完全基于 协议ID:节点ID，path 仅用于调试和节点对象属性设置。</p>
     *
     * <p>该方法在节点注册完成后，建立节点之间的依赖关系。
     * 依赖关系主要来源于：</p>
     * <ul>
     *   <li>结构层次关系（父子关系）- 基于协议ID:节点ID</li>
     *   <li>表达式引用关系（#节点ID 或 #协议ID:节点ID）</li>
     *   <li>节点组容器关系 - 基于协议ID:节点ID</li>
     * </ul>
     *
     * @param protocol 要建立依赖关系的协议对象
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addDependenciesToGraph(Protocol protocol) throws CodecException {
        String protocolName = protocol.getName();

        // 建立头部依赖关系
        Header header = protocol.getHeader();
        if (header != null) {
            String headerPath = protocolName + "." + header.getName();
            addHeaderDependenciesToGraph(header, headerPath);
        }

        // 建立主体依赖关系
        Body body = protocol.getBody();
        if (body != null) {
            String bodyPath = protocolName + "." + body.getName();
            addBodyDependenciesToGraph(body, bodyPath);
        }

        // 建立尾部依赖关系
        Tail tail = protocol.getTail();
        if (tail != null) {
            String checkPath = protocolName + "." + tail.getName();
            addTailDependenciesToGraph(tail, checkPath);
        }

        // 建立其他节点的依赖关系
        List<Node> nodes = protocol.getNodes();
        if (CollectionUtil.isNotEmpty(nodes)) {
            for (INode node : nodes) {
                String nodePath = protocolName + "." + node.getName();
                if (node instanceof NodeGroup) {
                    log.debug("[依赖关系] 处理节点组容器: {}", nodePath);
                    addGroupContainerDependencies((NodeGroup) node, nodePath);
                } else if (node instanceof Node) {
                    log.debug("[依赖关系] 处理普通节点: {}", nodePath);
                    addNodeDependencies(node, nodePath);
                }
            }
        }

        log.debug("[依赖关系] 协议依赖关系建立完成: {}", protocol.getName());
    }

    /**
     * 建立节点组容器的依赖关系
     *
     * <p>节点组容器与其子节点之间建立父子依赖关系，
     * 父节点依赖子节点，确保子节点先于父节点处理。</p>
     *
     * <p><b>注意：</b>依赖关系完全基于协议ID:节点ID，groupContainerPath仅用于调试日志。</p>
     *
     * @param nodeGroup          节点组容器
     * @param groupContainerPath 节点组容器的路径（仅用于调试，不用于依赖关系建立）
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addGroupContainerDependencies(NodeGroup nodeGroup, String groupContainerPath) throws CodecException {
        List<Node> groupNodes = nodeGroup.getGroupNodes();
        if (CollectionUtil.isEmpty(groupNodes)) {
            log.warn("[依赖关系] 分组容器无子节点: {}", groupContainerPath);
            return;
        }

        for (INode childNode : groupNodes) {
            String childNodePath = groupContainerPath + "." + childNode.getName();

            // 获取协议作用域的节点ID
            String scopedNodeGroupId = nodeGroup.getId() != null && !nodeGroup.getId().isEmpty()
                    ? currentProtocolId + ":" + nodeGroup.getId()
                    : null;
            String scopedChildNodeId = childNode.getId() != null && !childNode.getId().isEmpty()
                    ? currentProtocolId + ":" + childNode.getId()
                    : null;

            // 将子节点添加到依赖图中（如果尚未添加）
            if (scopedChildNodeId != null && !dependencyGraph.getNodeMap().containsKey(scopedChildNodeId)) {
                dependencyGraph.addNode(childNode, childNodePath, currentProtocolId);
            }

            // 建立父子依赖关系：父节点依赖子节点（使用协议ID:节点ID）
            if (scopedNodeGroupId != null && scopedChildNodeId != null) {
                dependencyGraph.addDependency(scopedNodeGroupId, scopedChildNodeId);
            }

            // 递归处理子节点的依赖关系
            if (childNode instanceof Header) {
                addHeaderDependenciesToGraph((Header) childNode, childNodePath);
            } else if (childNode instanceof Body) {
                addBodyDependenciesToGraph((Body) childNode, childNodePath);
            } else if (childNode instanceof Tail) {
                addTailDependenciesToGraph((Tail) childNode, childNodePath);
            } else if (childNode instanceof NodeGroup) {
                addGroupContainerDependencies((NodeGroup) childNode, childNodePath);
            } else if (childNode instanceof Node) {
                // 递归处理子节点的子节点
                addNodeDependencies(childNode, childNodePath);
            } else {
                throw new CodecException("未知节点类型，无法建立依赖关系: " + childNode.getClass().getSimpleName());
            }
        }
    }

    /**
     * 建立头部节点的依赖关系
     *
     * <p>头部节点与其子节点之间建立父子依赖关系。</p>
     *
     * <p><b>注意：</b>依赖关系完全基于协议ID:节点ID，headerPath仅用于调试日志。</p>
     *
     * @param header     头部节点
     * @param headerPath 头部节点的路径（仅用于调试，不用于依赖关系建立）
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addHeaderDependenciesToGraph(Header header, String headerPath) throws CodecException {
        List<Node> headerNodes = header.getNodes();
        if (CollectionUtil.isEmpty(headerNodes)) {
            return;
        }

        for (INode node : headerNodes) {
            String nodePath = headerPath + "." + node.getName();
            // 获取协议作用域的节点ID
            String scopedHeaderId = header.getId() != null && !header.getId().isEmpty()
                    ? currentProtocolId + ":" + header.getId()
                    : null;
            String scopedNodeId = node.getId() != null && !node.getId().isEmpty()
                    ? currentProtocolId + ":" + node.getId()
                    : null;

            // 建立父子依赖关系：父依赖子（使用协议ID:节点ID）
            if (scopedHeaderId != null && scopedNodeId != null) {
                dependencyGraph.addDependency(scopedHeaderId, scopedNodeId);
            }
            // 处理节点表达式中的依赖引用，建立基于表达式的依赖关系
            addNodeDependencies(node, nodePath);
        }
    }

    /**
     * 建立主体节点的依赖关系
     *
     * <p>主体节点是最复杂的部分，可能包含嵌套的结构体和节点。</p>
     *
     * <p><b>注意：</b>依赖关系完全基于协议ID:节点ID，bodyPath仅用于调试日志。</p>
     *
     * @param body     主体节点
     * @param bodyPath 主体节点的路径（仅用于调试，不用于依赖关系建立）
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addBodyDependenciesToGraph(Body body, String bodyPath) throws CodecException {
        if (body == null) {
            log.debug("[依赖关系] 主体为空，跳过依赖关系建立: {}", bodyPath);
            return;
        }

        log.debug("[依赖关系] 建立主体依赖关系: {}", bodyPath);

        // 获取协议作用域的主体ID
        String scopedBodyId = body.getId() != null && !body.getId().isEmpty()
                ? currentProtocolId + ":" + body.getId()
                : null;

        // 确保协议体节点本身被添加到依赖图中
        if (scopedBodyId != null && !dependencyGraph.getNodeMap().containsKey(scopedBodyId)) {
            dependencyGraph.addNode(body, bodyPath, currentProtocolId);
            log.debug("[依赖关系] 主体节点添加到依赖图: {}", bodyPath);

            // 如果协议体有ID，建立ID映射
            if (scopedBodyId != null) {
                dependencyGraph.addNodeIdMapping(scopedBodyId, bodyPath);
                log.debug("[依赖关系] 主体ID映射建立: {} -> {}", scopedBodyId, bodyPath);
            }
        }

        // 建立与嵌套头部的依赖关系
        Header header = body.getHeader();
        if (header != null) {
            String headerPath = bodyPath + "." + header.getName();
            log.debug("[依赖关系] 建立主体与嵌套头部依赖: {} -> {}", bodyPath, headerPath);
            addHeaderDependenciesToGraph(header, headerPath);
            String scopedHeaderId = header.getId() != null && !header.getId().isEmpty()
                    ? currentProtocolId + ":" + header.getId()
                    : null;
            if (scopedBodyId != null && scopedHeaderId != null) {
                dependencyGraph.addDependency(scopedBodyId, scopedHeaderId);
            }
        }

        // 建立与嵌套主体的依赖关系（支持多层嵌套）
        Body childBody = body.getBody();
        if (childBody != null) {
            String childBodyPath = bodyPath + "." + childBody.getName();
            log.debug("[依赖关系] 建立主体与嵌套主体依赖: {} -> {}", bodyPath, childBodyPath);
            addBodyDependenciesToGraph(childBody, childBodyPath);
            // 父主体依赖子主体（使用协议ID:节点ID）
            String scopedChildBodyId = childBody.getId() != null && !childBody.getId().isEmpty()
                    ? currentProtocolId + ":" + childBody.getId()
                    : null;
            if (scopedBodyId != null && scopedChildBodyId != null) {
                dependencyGraph.addDependency(scopedBodyId, scopedChildBodyId);
            }
        }

        // 建立与嵌套尾部的依赖关系
        Tail tail = body.getTail();
        if (tail != null) {
            String checkPath = bodyPath + "." + tail.getName();
            log.debug("[依赖关系] 建立主体与嵌套尾部依赖: {} -> {}", bodyPath, checkPath);
            addTailDependenciesToGraph(tail, checkPath);
            // 父主体依赖子尾部（使用协议ID:节点ID）
            String scopedTailId = tail.getId() != null && !tail.getId().isEmpty()
                    ? currentProtocolId + ":" + tail.getId()
                    : null;
            if (scopedBodyId != null && scopedTailId != null) {
                dependencyGraph.addDependency(scopedBodyId, scopedTailId);
            }
        }

        // 建立与直接子节点的依赖关系
        List<Node> nodes = body.getNodes();
        if (CollectionUtil.isEmpty(nodes)) {
            log.debug("[依赖关系] 主体无直接子节点: {}", bodyPath);
            return;
        }

        log.debug("[依赖关系] 建立主体与 {} 个直接子节点的依赖关系: {}", nodes.size(), bodyPath);
        for (INode node : nodes) {
            String nodePath = bodyPath + "." + node.getName();
            log.debug("[依赖关系] 建立主体与子节点依赖: {} -> {}", bodyPath, nodePath);

            // 获取协议作用域的节点ID（使用方法开头已定义的 scopedBodyId）
            String scopedNodeId = node.getId() != null && !node.getId().isEmpty()
                    ? currentProtocolId + ":" + node.getId()
                    : null;

            // 建立父子依赖关系（使用协议ID:节点ID）
            if (scopedBodyId != null && scopedNodeId != null) {
                dependencyGraph.addDependency(scopedBodyId, scopedNodeId);
            }

            // 递归处理子节点的依赖关系
            if (node instanceof NodeGroup) {
                addGroupContainerDependencies((NodeGroup) node, nodePath);
            } else if (node instanceof Node) {
                addNodeDependencies(node, nodePath);
            }
        }

        log.debug("[依赖关系] 主体依赖关系建立完成: {}", bodyPath);
    }

    /**
     * 建立尾部节点的依赖关系
     *
     * <p>尾部节点通常不依赖其子节点，子节点可以独立处理。</p>
     *
     * <p><b>注意：</b>依赖关系完全基于协议ID:节点ID，checkPath仅用于调试日志。</p>
     *
     * @param tail      尾部节点
     * @param checkPath 尾部节点的路径（仅用于调试，不用于依赖关系建立）
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addTailDependenciesToGraph(Tail tail, String checkPath) throws CodecException {
        if (tail == null) {
            log.debug("[依赖关系] 尾部为空，跳过依赖关系建立: {}", checkPath);
            return;
        }

        List<Node> tailNodes = tail.getNodes();
        if (CollectionUtil.isEmpty(tailNodes)) {
            log.debug("[依赖关系] 尾部无子节点: {}", checkPath);
            return;
        }

        log.debug("[依赖关系] 尾部 {} 有 {} 个子节点，建立依赖关系", tail.getName(), tailNodes.size());
        for (INode node : tailNodes) {
            String nodePath = checkPath + "." + node.getName();
            log.debug("[依赖关系] 处理尾部子节点: {} -> {}", checkPath, nodePath);

            // 获取协议作用域的节点ID
            String scopedTailId = tail.getId() != null && !tail.getId().isEmpty()
                    ? currentProtocolId + ":" + tail.getId()
                    : null;
            String scopedNodeId = node.getId() != null && !node.getId().isEmpty()
                    ? currentProtocolId + ":" + node.getId()
                    : null;

            // 建立父子依赖关系：父依赖子（使用协议ID:节点ID）
            if (scopedTailId != null && scopedNodeId != null) {
                dependencyGraph.addDependency(scopedTailId, scopedNodeId);
            }
            log.debug("[依赖关系] 建立尾部父子依赖: {} 依赖 {}", checkPath, nodePath);
            // 尾部节点不依赖其子节点，子节点可以独立处理
            // 只递归处理子节点的依赖关系
            addNodeDependencies(node, nodePath);
        }

        log.debug("[依赖关系] 尾部依赖关系建立完成: {}", checkPath);
    }

    /**
     * 建立普通节点的依赖关系
     *
     * <p>该方法主要处理节点表达式中的依赖引用，建立基于表达式的依赖关系。</p>
     *
     * <p><b>注意：</b>依赖关系完全基于协议ID:节点ID（表达式中的 #节点ID 或 #协议ID:节点ID），
     * nodePath仅用于调试日志。</p>
     *
     * @param node     要建立依赖关系的节点
     * @param nodePath 节点的路径（仅用于调试，不用于依赖关系建立）
     * @throws CodecException 当建立依赖关系过程中发生错误时抛出
     */
    private void addNodeDependencies(INode node, String nodePath) throws CodecException {
        //log.debug("[依赖关系] 建立节点依赖关系: {} (ID: {})", nodePath, node.getId());

        // 1. 处理节点的表达式依赖
        String fwdExpr = node.getFwdExpr();
        if (fwdExpr != null && !fwdExpr.trim().isEmpty()) {
            addExprDependencies(node, nodePath, fwdExpr);
        }

        // 2. 处理反向表达式依赖（如果有的话）
        String bwdExpr = node.getBwdExpr();
        if (bwdExpr != null && !bwdExpr.trim().isEmpty()) {
            addExprDependencies(node, nodePath, bwdExpr);
        }
    }

    /**
     * 处理节点表达式中的依赖引用
     *
     * <p><b>核心设计：</b>依赖关系完全基于协议ID:节点ID。</p>
     * <p>表达式支持两种引用格式：</p>
     * <ul>
     *   <li><code>#节点ID</code> - 同协议内引用，自动转换为 协议ID:节点ID</li>
     *   <li><code>#协议ID:节点ID</code> - 跨协议引用（仅同协议时加入依赖图）</li>
     * </ul>
     *
     * @param node     节点对象
     * @param nodePath 节点路径（仅用于调试日志，不用于依赖关系建立）
     * @param fwdExpr  表达式字符串
     * @throws CodecException 当依赖解析失败时抛出
     */
    private void addExprDependencies(INode node, String nodePath, String fwdExpr) throws CodecException {
        log.debug("[表达式依赖] 处理节点 {} 的表达式: {}", nodePath, fwdExpr);

        // 获取当前节点的协议作用域ID（依赖关系的唯一标识）
        String scopedNodeId = node.getId() != null && !node.getId().isEmpty()
                ? currentProtocolId + ":" + node.getId()
                : null;

        if (scopedNodeId == null) {
            log.warn("[表达式依赖] 节点无ID，跳过依赖关系建立: {}", nodePath);
            return;
        }

        // 解析表达式中的依赖引用（支持 #节点ID 或 #协议ID:节点ID 格式）
        Set<String> rawDeps = expressionParser.parseDependencies(fwdExpr, allLeafNodes, currentProtocolId);

        // 将依赖转换为协议作用域的节点ID格式
        Set<String> graphDeps = new LinkedHashSet<>();
        for (String dep : rawDeps) {
            String scopedDepId;
            int idx = dep.indexOf(':');
            if (idx < 0) {
                // 同协议内引用：只有节点ID，需要添加协议ID前缀
                scopedDepId = currentProtocolId + ":" + dep;
                graphDeps.add(scopedDepId);
                log.debug("[表达式依赖] 同协议内引用: {} -> {}", dep, scopedDepId);
            } else {
                // 跨协议引用：已经是协议ID:节点ID格式
                String protocolId = dep.substring(0, idx);
                if (currentProtocolId != null && currentProtocolId.equals(protocolId)) {
                    // 同协议（虽然已经带了协议ID，但为了统一处理，仍然加入依赖图）
                    scopedDepId = dep;
                    graphDeps.add(scopedDepId);
                    log.debug("[表达式依赖] 同协议引用（带协议ID）: {}", scopedDepId);
                } else {
                    // 跨协议依赖：不在本协议依赖图中建边
                    log.debug("[表达式依赖] 跨协议引用跳过建边: {}", dep);
                    continue;
                }
            }
        }

        dependencyGraph.addForwardDependency(scopedNodeId, graphDeps);
        log.debug("[表达式依赖] 进入依赖图的ID: {}", String.join(", ", graphDeps));

        // 为每个依赖ID建立依赖关系（仅当前协议内或明确指定的协议）
        for (String dependency : graphDeps) {
            // 使用支持协议作用域的检查方法
            if (dependencyGraph.hasNodeId(dependency, currentProtocolId)) {
                dependencyGraph.addDependency(scopedNodeId, dependency);
            } else {
                throw new CodecException("找不到依赖ID: " + dependency);
            }
        }
    }
}