package com.iecas.cmd.engine;

import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.INode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 依赖图管理器
 *
 * <p>该类负责管理协议节点之间的依赖关系，提供依赖图的构建、查询和分析功能。
 * 主要功能包括：</p>
 * <ul>
 *   <li>节点管理：添加节点、查询节点、管理节点映射关系</li>
 *   <li>依赖关系管理：添加依赖、查询依赖、检查依赖关系</li>
 *   <li>拓扑排序：获取节点的拓扑排序结果，确保依赖顺序正确</li>
 *   <li>循环依赖检测：检测并报告依赖图中的循环依赖</li>
 *   <li>路径管理：维护节点的层级路径，支持嵌套结构</li>
 * </ul>
 *
 * <p><b>核心数据结构：</b></p>
 * <ul>
 *   <li>backwardDependencies: 反向依赖图，key为协议ID:节点ID</li>
 *   <li>forwardDependencies: 正向依赖图，key为协议ID:节点ID</li>
 *   <li>inDegree: 记录每个节点的入度，key为协议ID:节点ID，用于拓扑排序</li>
 *   <li>nodeMap: 节点对象映射表，key为协议ID:节点ID（依赖关系的唯一标识）</li>
 *   <li>nodePathMap: 节点路径映射（仅用于调试，不参与依赖关系）</li>
 * </ul>
 *
 * <b>使用说明：</b>
 * - 先通过addNode/addDependency等方法构建依赖图，再通过getTopologicalOrder/findCycle等方法进行依赖分析。
 * - 依赖图本身不负责节点的实际处理，仅负责依赖关系的维护和分析。
 */
@Slf4j
public class DependencyGraph {

    /**
     * 反向依赖图
     * key: 被依赖的节点名称
     * value: 所有依赖key节点的集合
     * 用于存储节点间的依赖关系，便于反向查找哪些节点依赖了这个节点
     */
    // 以节点ID为key的反向依赖图（存储依赖我的节点ID集合）
    private final Map<String, LinkedHashSet<String>> backwardDependencies = new LinkedHashMap<>();

    /**
     * 正向依赖图
     * key: 节点名称
     * value: 依赖的节点的集合
     * 用于存储节点间的依赖关系，便于查某个节点依赖的节点的集合
     */
    // 以节点ID为key的正向依赖图（我依赖的节点ID集合）
    private final Map<String, Set<String>> forwardDependencies = new LinkedHashMap<>();

    /**
     * 记录每个节点的入度（被依赖的次数）
     * key: 节点名称
     * value: 入度值
     * 用于拓扑排序，入度为0的节点表示没有依赖，可以优先处理
     */
    // 以节点ID为key的入度
    private final Map<String, Integer> inDegree = new LinkedHashMap<>();

    /**
     * 节点对象映射表（核心数据结构，用于依赖关系）
     * key: 协议ID:节点ID（依赖关系的唯一标识）
     * value: 节点对象
     * 用于存储和快速访问节点对象，所有依赖关系操作都基于此key
     */
    private final Map<String, INode> nodeMap = new LinkedHashMap<>();

    /**
     * 节点路径映射表（仅用于调试和反向查找，不参与依赖关系建立）
     * key: 协议ID:节点ID
     * value: 节点的完整路径（人类可读的层级表示）
     * 用于调试日志输出、错误信息显示，但不用于依赖关系计算
     */
    private final Map<String, String> nodePathMap = new LinkedHashMap<>();

    /**
     * 节点ID到路径的映射（仅用于调试和反向查找）
     * key: 协议ID:节点ID
     * value: 节点路径
     * 用于通过ID快速查找对应的路径，仅用于调试和日志，不参与依赖关系
     */
    private final Map<String, String> nodeIdMap = new LinkedHashMap<>();

    /**
     * 路径到节点ID的反向映射（仅用于调试和兼容性）
     * key: 节点路径
     * value: 协议ID:节点ID
     * 用于通过路径反向查找节点ID，仅用于兼容旧代码和调试，不参与依赖关系
     */
    private final Map<String, String> reverseIdMap = new HashMap<>();

    /**
     * 用于循环依赖检测的访问标记集合
     * 记录在DFS遍历过程中已访问过的节点
     */
    private final Set<String> visited = new HashSet<>();

    /**
     * 用于循环依赖检测的递归栈集合
     * 记录当前DFS遍历路径上的节点
     */
    private final Set<String> recursionStack = new HashSet<>();

    /**
     * 用于存储检测到的循环依赖路径
     * 当发现循环依赖时，记录构成循环的节点序列
     */
    private final List<String> cyclePath;

    // 记录每个节点的前置依赖节点（被该节点依赖的节点）
    // 以节点ID为key的前置依赖（依赖我的节点ID）
    private final Map<String, Set<String>> prerequisiteNodes = new HashMap<>();
    // 拓扑排序器
    private TopologicalSorter sorter;

    /**
     * 构造函数，初始化依赖图及辅助结构
     */
    public DependencyGraph() {
        this.cyclePath = new ArrayList<>();
        // 拓扑排序器完全基于节点ID（协议ID:节点ID），不依赖path
        this.sorter = new TopologicalSorter(backwardDependencies, inDegree);
    }

    /**
     * 清空依赖图及所有辅助结构
     * 用于重新构建依赖关系时调用，确保所有状态重置
     */
    public void clear() {
        log.debug("[依赖图] 清空依赖图");
        backwardDependencies.clear();
        inDegree.clear();
        nodeMap.clear();
        nodePathMap.clear();
        nodeIdMap.clear();
        reverseIdMap.clear();
        prerequisiteNodes.clear();
        visited.clear();
        recursionStack.clear();
        cyclePath.clear();
        // 拓扑排序器完全基于节点ID（协议ID:节点ID），不依赖path
        this.sorter = new TopologicalSorter(backwardDependencies, inDegree);
    }

    /**
     * 添加节点到依赖图
     *
     * <p><b>核心设计：</b>依赖关系完全基于 协议ID:节点ID，path 仅作为辅助信息用于调试和节点对象属性。</p>
     *
     * @param node       节点对象（字段、结构体等）
     * @param path       节点的路径（仅用于：1.设置节点对象的path属性 2.调试日志输出）
     *                   <b>注意：</b>依赖关系建立不依赖path，完全基于协议ID:节点ID
     * @param protocolId 协议ID，用于生成唯一的节点标识（协议ID:节点ID）
     *                   说明：会自动注册ID映射、路径映射、初始化依赖集合和入度
     * @throws CodecException 如果节点ID为空或存在重复的 scopedNodeId
     */
    public void addNode(INode node, String path, String protocolId) throws CodecException {
        // path 仅用于节点对象的属性设置和调试，不用于依赖关系建立
        node.setPath(path);

        // 节点ID是必需的，不能为空（依赖关系基于协议ID:节点ID）
        String nodeId = node.getId();
        if (nodeId == null || nodeId.isEmpty()) {
            throw new CodecException(String.format(
                    "节点必须有ID才能添加到依赖图。节点路径: %s，节点名称: %s。",
                    path, node.getName()));
        }

        // 生成协议作用域的节点ID：协议ID:节点ID（这是依赖关系的唯一标识）
        String scopedNodeId = protocolId + ":" + nodeId;

        // 检查 nodeMap 中是否已存在相同的 scopedNodeId（全局不允许重复）
        if (nodeMap.containsKey(scopedNodeId)) {
            String existingPath = nodePathMap.get(scopedNodeId);
            throw new CodecException(String.format(
                    "检测到重复的节点标识 '%s'。已存在节点路径: %s，当前节点路径: %s。",
                    scopedNodeId, existingPath, path));
        }

        addNodeIdMapping(scopedNodeId, path);

        // 1) 节点对象采用协议ID:节点ID为key存储（依赖关系的唯一标识）
        nodeMap.put(scopedNodeId, node);
        // 2) 路径映射：协议ID:节点ID -> 路径（仅用于调试和反向查找，不用于依赖关系建立）
        nodePathMap.put(scopedNodeId, path);
        // 3) 依赖关系初始化（以协议ID:节点ID为key，完全基于ID，不依赖path）
        backwardDependencies.putIfAbsent(scopedNodeId, new LinkedHashSet<>());
        forwardDependencies.putIfAbsent(scopedNodeId, new LinkedHashSet<>());
        // 4) 入度初始化（以协议ID:节点ID为key）
        inDegree.putIfAbsent(scopedNodeId, 0);
        // 5) 前置依赖集合初始化（以协议ID:节点ID为key）
        prerequisiteNodes.putIfAbsent(scopedNodeId, new HashSet<>());
    }

    /**
     * 添加节点到依赖图（兼容旧版本，自动使用路径作为协议ID）
     *
     * @deprecated 请使用 addNode(INode, String, String) 并明确指定协议ID
     */
    @Deprecated
    public void addNode(INode node, String path) throws CodecException {
        // 从路径中提取协议ID（假设路径格式为 protocolName.xxx）
        String protocolId = path.split("\\.")[0];
        addNode(node, path, protocolId);
    }

    /**
     * 添加节点ID到路径的映射
     *
     * @param scopedNodeId 协议作用域的节点ID（协议ID:节点ID）
     * @param path         节点路径
     *                     说明：用于表达式依赖、ID查找等场景
     * @throws CodecException 如果存在重复的 scopedNodeId
     */
    public void addNodeIdMapping(String scopedNodeId, String path) throws CodecException {
        // 检查ID是否已存在（全局不允许重复）
        if (nodeIdMap.containsKey(scopedNodeId)) {
            String existingPath = nodeIdMap.get(scopedNodeId);
            throw new CodecException(String.format(
                    "检测到重复的节点标识 '%s'。已存在节点路径: %s，当前节点路径: %s。",
                    scopedNodeId, existingPath, path));
        }

        nodeIdMap.put(scopedNodeId, path);
        reverseIdMap.put(path, scopedNodeId);
    }

    /**
     * 将节点ID转换为协议作用域的节点ID
     * 如果已经是协议ID:节点ID格式，直接返回；否则使用当前协议ID添加前缀
     *
     * @param nodeId            节点ID（可能是简单的节点ID或协议ID:节点ID）
     * @param currentProtocolId 当前协议ID
     * @return 协议作用域的节点ID（协议ID:节点ID）
     */
    public String toScopedNodeId(String nodeId, String currentProtocolId) {
        if (nodeId == null || nodeId.isEmpty()) {
            return nodeId;
        }

        // 如果已经包含冒号，说明已经是协议ID:节点ID格式
        if (nodeId.contains(":")) {
            return nodeId;
        }

        // 否则，添加当前协议ID前缀
        return currentProtocolId + ":" + nodeId;
    }

    /**
     * 检查节点ID是否存在（支持简单的节点ID和协议ID:节点ID格式）
     *
     * @param nodeId            节点ID（可能是简单的节点ID或协议ID:节点ID）
     * @param currentProtocolId 当前协议ID，用于解析简单的节点ID
     * @return 如果节点存在返回true
     */
    public boolean hasNodeId(String nodeId, String currentProtocolId) {
        if (nodeId == null || nodeId.isEmpty()) {
            return false;
        }

        // 如果已经是协议ID:节点ID格式，直接检查
        if (nodeId.contains(":")) {
            return hasNodeId(nodeId);
        }

        // 否则，先转换为协议ID:节点ID格式再检查
        String scopedNodeId = toScopedNodeId(nodeId, currentProtocolId);
        return hasNodeId(scopedNodeId);
    }

    public void addForwardDependency(String fromId, Set<String> toIds) {
        forwardDependencies.put(fromId, new LinkedHashSet<>(toIds));
    }

    /**
     * 根据ID获取节点路径（仅用于调试和日志）
     *
     * <p><b>注意：</b>此方法仅用于调试和日志输出，不参与依赖关系建立。</p>
     *
     * @param nodeId 节点ID（协议ID:节点ID格式）
     * @return 节点路径，若不存在返回null
     */
    public String getNodePathById(String nodeId) {
        String nodePath = nodeIdMap.get(nodeId);
        if (nodePath == null) {
            log.debug("[警告] 找不到ID对应的节点: {}", nodeId);
        } else {
            log.debug("- 通过ID找到节点: {} -> {}", nodeId, nodePath);
        }
        return nodePath;
    }

    /**
     * 根据节点路径获取ID（仅用于兼容性和调试）
     *
     * <p><b>注意：</b>此方法仅用于兼容旧代码和调试，依赖关系建立应直接使用节点ID。</p>
     *
     * @param nodePath 节点路径
     * @return 节点ID（协议ID:节点ID格式），若不存在返回null
     */
    public String getNodeIdByPath(String nodePath) {
        String nodeId = reverseIdMap.get(nodePath);
        if (nodeId == null) {
            log.debug("[警告] 找不到节点对应的ID: {}", nodePath);
        }
        return nodeId;
    }

    /**
     * 根据ID获取节点对象
     *
     * @param nodeId 节点ID
     * @return ProtocolNode对象，若不存在返回null
     */
    public INode getNodeById(String nodeId) {
        INode node = nodeMap.get(nodeId);
        if (node == null) {
            String nodePath = getNodePathById(nodeId);
            log.debug("[警告] 找不到节点对象: {} ({})", nodeId, nodePath);
        }
        return node;
    }

    /**
     * 检查节点ID是否存在
     *
     * @param nodeId 节点ID
     * @return 存在返回true，否则false
     */
    public boolean hasNodeId(String nodeId) {
        return nodeIdMap.containsKey(nodeId);
    }

    /**
     * 获取所有节点ID
     *
     * @return 所有已注册的节点ID集合
     */
    public Set<String> getAllNodeIds() {
        return new HashSet<>(nodeIdMap.keySet());
    }

    /**
     * 添加依赖关系
     *
     * <p>向依赖图中添加一条依赖关系（from 依赖 to）。</p>
     * <p><b>核心设计：</b>依赖关系完全基于 协议ID:节点ID，不依赖path。</p>
     *
     * <p>该方法会自动维护依赖图的邻接表、入度表和前置依赖集合，并进行一系列安全性检查，
     * 防止出现无效依赖或环依赖。to 计算后才能计算from，from 依赖 to。</p>
     *
     * @param fromId 起始节点ID（协议ID:节点ID格式，依赖方）
     * @param toId   目标节点ID（协议ID:节点ID格式，被依赖方）
     *               说明：自动维护入度、前置依赖集合，防止重复和环依赖
     * @throws CodecException 如果节点不存在或添加依赖会导致环依赖，则抛出异常
     */
    public void addDependency(String fromId, String toId) throws CodecException {
        log.debug("[依赖图] 添加依赖关系(ID): {} 依赖 {}", fromId, toId);

        // 1. 检查依赖方节点是否存在（按ID）
        if (!nodeMap.containsKey(fromId)) {
            throw new CodecException("- 起始节点不存在(ID): " + fromId);
        } else if (!nodeMap.containsKey(toId)) {
            throw new CodecException("- 目标节点不存在(ID): " + toId);
        }

        // 2. 检查依赖关系是否已存在
        // backwardDependencies 的key是被依赖节点ID(toId)，value包含依赖方ID(fromId)
        if (backwardDependencies.get(toId) != null && backwardDependencies.get(toId).contains(fromId)) {
            return;
        }

        // 3. 环检测
        if (sorter.findCycle() != null) {
            throw new CodecException(String.format("[警告] 添加依赖将形成循环: %s 依赖 %s", fromId, toId));
        }

        // 4. 添加依赖：被依赖节点toId的邻接表中添加依赖方fromId
        backwardDependencies.get(toId).add(fromId);
        // 同步维护正向依赖：fromId 依赖 toId
        forwardDependencies.computeIfAbsent(fromId, k -> new LinkedHashSet<>()).add(toId);

        // 5. 更新入度
        int oldInDegree = inDegree.getOrDefault(fromId, 0);
        inDegree.put(fromId, oldInDegree + 1);

        // 6. 更新前置依赖集合
        prerequisiteNodes.get(toId).add(fromId);
    }

    /**
     * 获取拓扑排序结果
     *
     * @return 按依赖顺序排序的节点路径列表
     */
    public List<String> getTopologicalOrder() {
        // 直接返回ID序列
        return sorter.getTopologicalOrder();
    }

    /**
     * 获取节点对象
     *
     * @param id 节点ID（协议ID:节点ID格式）
     * @return 节点对象，若不存在返回null
     */
    public INode getNode(String id) {
        INode node = nodeMap.get(id);
        if (node == null) {
            log.debug("[警告] 找不到节点ID: {}", id);
        }
        return node;
    }

    /**
     * 获取节点的所有依赖（该节点依赖的其他节点）
     *
     * @param nodeId 节点ID（协议ID:节点ID格式）
     * @return 依赖节点ID集合（协议ID:节点ID格式）
     */
    public Set<String> getDependencies(String nodeId) {
        // 返回该节点"所依赖"的节点ID集合（正向依赖）
        Set<String> depIds = forwardDependencies.getOrDefault(nodeId, new LinkedHashSet<>());
        log.debug("[依赖图] 获取节点依赖(ID) - 节点: {}，依赖数: {}", nodeId, depIds.size());
        return new LinkedHashSet<>(depIds);
    }

    /**
     * 获取所有依赖该节点的节点（依赖该节点的其他节点）
     *
     * @param nodeId 节点ID（协议ID:节点ID格式）
     * @return 依赖该节点的节点ID集合（协议ID:节点ID格式）
     */
    public Set<String> getDependentNodes(String nodeId) {
        Set<String> dependIds = prerequisiteNodes.getOrDefault(nodeId, new HashSet<>());
        log.debug("[依赖图] 获取依赖该节点的节点(ID) - 节点: {}，数量: {}", nodeId, dependIds.size());
        return new LinkedHashSet<>(dependIds);
    }

    /**
     * 获取节点的完整路径（仅用于调试）
     *
     * <p><b>注意：</b>此方法仅用于调试和日志输出，不参与依赖关系建立。</p>
     *
     * @param nodeId 节点ID（协议ID:节点ID格式）
     * @return 节点完整路径，若不存在返回null
     */
    public String getNodePath(String nodeId) {
        String path = nodePathMap.get(nodeId);
        if (path == null) {
            log.debug("[警告] 找不到节点路径(ID): " + nodeId);
        }
        return path;
    }

    /**
     * 获取所有节点对象映射
     *
     * @return 节点ID（协议ID:节点ID）到节点对象的映射表
     */
    public Map<String, INode> getNodeMap() {
        // 返回 协议ID:节点ID -> 节点对象
        return new LinkedHashMap<>(nodeMap);
    }

    /**
     * 检查两个节点之间是否存在依赖关系
     *
     * @param fromId 起始节点ID（协议ID:节点ID格式）
     * @param toId   目标节点ID（协议ID:节点ID格式）
     * @return 如果from依赖to，则返回true
     */
    public boolean hasDependency(String fromId, String toId) {
        // 使用正向依赖表判断：fromId -> toId
        if (!forwardDependencies.containsKey(fromId)) {
            return false;
        }
        return forwardDependencies.get(fromId).contains(toId);
    }

    /**
     * 获取节点必须在其之前处理的所有节点（直接和间接依赖）
     *
     * @param nodeId 节点ID（协议ID:节点ID格式）
     * @return 必须在该节点之前处理的所有节点ID集合（协议ID:节点ID格式）
     */
    public Set<String> getAllPrerequisites(String nodeId) {
        // 基于正向依赖递归收集所有“直接和间接依赖”
        Set<String> prerequisiteIds = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        // 先加入直接依赖
        for (String dep : forwardDependencies.getOrDefault(nodeId, Collections.emptySet())) {
            if (prerequisiteIds.add(dep)) {
                queue.offer(dep);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String dep : forwardDependencies.getOrDefault(current, Collections.emptySet())) {
                if (prerequisiteIds.add(dep)) {
                    queue.offer(dep);
                }
            }
        }

        return prerequisiteIds;
    }

    /**
     * 检查是否存在循环依赖（基于bwdExpr的#id依赖关系）
     * 若存在则抛出异常
     */
    public void findCycle() throws CodecException {
        List<String> cycle = findCycleByBwdExpr();
        if (cycle != null) {
            StringBuilder sb = new StringBuilder("检测到循环依赖：\n");
            for (int i = 0; i < cycle.size(); i++) {
                sb.append(cycle.get(i));
                if (i < cycle.size() - 1) {
                    sb.append(" -> ");
                }
            }
            throw new CodecException(sb.toString());
        }
    }

    /**
     * 基于bwdExpr的#id依赖关系检测循环依赖
     *
     * <p><b>注意：</b>此方法使用节点ID（协议ID:节点ID格式）作为key进行环检测。</p>
     *
     * @return 如果存在循环依赖，返回环路径（协议ID:节点ID格式），否则返回null
     */
    public List<String> findCycleByBwdExpr() {
        // 1. 构建依赖关系图（仅bwdExpr中的#id，使用协议ID:节点ID作为key）
        Map<String, Set<String>> bwdDependencyGraph = new HashMap<>();
        // 收集所有节点（nodeMap的key已经是协议ID:节点ID格式）
        for (Map.Entry<String, INode> entry : nodeMap.entrySet()) {
            String scopedNodeId = entry.getKey(); // 协议ID:节点ID格式
            INode node = entry.getValue();
            String nodeId = node.getId();
            if (nodeId == null || nodeId.isEmpty()) continue;
            
            Set<String> dependencies = new HashSet<>();
            String fwdExpr = node.getFwdExpr();
            if (fwdExpr != null && !fwdExpr.isEmpty()) {
                // 正则提取#id（支持#节点ID和#协议ID:节点ID格式）
                // 先提取简单格式 #节点ID
                Pattern p = Pattern.compile("#([a-zA-Z0-9_]+(?::[a-zA-Z0-9_]+)?)");
                Matcher m = p.matcher(fwdExpr);
                while (m.find()) {
                    String depRef = m.group(1);
                    // depRef可能是简单的节点ID或协议ID:节点ID格式
                    // 这里先保留原样，后续可以通过dependencyGraph查找对应的协议ID:节点ID
                    dependencies.add(depRef);
                }
            }
            // 使用协议ID:节点ID作为key存储依赖关系
            bwdDependencyGraph.put(scopedNodeId, dependencies);
        }
        // 2. 检查环（DFS，使用协议ID:节点ID格式）
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        List<String> path = new ArrayList<>();
        for (String scopedNodeId : bwdDependencyGraph.keySet()) {
            if (dfsCycle(scopedNodeId, bwdDependencyGraph, visited, stack, path)) {
                // 返回环路径（协议ID:节点ID格式）
                int idx = path.lastIndexOf(scopedNodeId);
                return path.subList(idx, path.size());
            }
        }
        return null;
    }

    /**
     * 辅助DFS检测环
     *
     * <p><b>注意：</b>使用协议ID:节点ID格式进行环检测。</p>
     *
     * @param scopedNodeId 当前节点ID（协议ID:节点ID格式）
     * @param graph        依赖图（key为协议ID:节点ID格式）
     * @param visited      已访问节点集合
     * @param stack        当前递归栈
     * @param path         路径记录
     * @return 是否存在环
     */
    private boolean dfsCycle(String scopedNodeId, Map<String, Set<String>> graph, Set<String> visited, Set<String> stack, List<String> path) {
        if (stack.contains(scopedNodeId)) {
            path.add(scopedNodeId);
            return true;
        }
        if (visited.contains(scopedNodeId)) return false;
        visited.add(scopedNodeId);
        stack.add(scopedNodeId);
        path.add(scopedNodeId);
        
        // 遍历依赖节点（dep可能是简单的节点ID或协议ID:节点ID格式，需要转换为协议ID:节点ID）
        for (String dep : graph.getOrDefault(scopedNodeId, Collections.emptySet())) {
            // dep可能是简单的节点ID，需要转换为协议ID:节点ID格式
            // 如果dep已经是协议ID:节点ID格式，直接使用；否则需要从scopedNodeId中提取协议ID
            String depScopedId = dep;
            if (!dep.contains(":")) {
                // 简单的节点ID，需要添加协议ID前缀
                int colonIdx = scopedNodeId.indexOf(':');
                if (colonIdx > 0) {
                    String protocolId = scopedNodeId.substring(0, colonIdx);
                    depScopedId = protocolId + ":" + dep;
                }
            }
            
            // 如果转换后的依赖ID在图中存在，则进行DFS
            if (graph.containsKey(depScopedId) || nodeMap.containsKey(depScopedId)) {
                if (dfsCycle(depScopedId, graph, visited, stack, path)) {
                    return true;
                }
            }
        }
        stack.remove(scopedNodeId);
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * 获取节点依赖关系映射（只读）
     *
     * @return 节点ID（协议ID:节点ID）到依赖节点ID集合的只读映射
     */
    public Map<String, Set<String>> getNodeDependenciesMap() {
        // 返回 ID -> 我所依赖的ID集合（正向依赖），更符合“依赖关系”的直觉
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : forwardDependencies.entrySet()) {
            copy.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * 获取节点ID到路径的映射（只读）
     *
     * @return 节点ID到路径的只读映射表
     */
    public Map<String, String> getNodeIdMap() {
        return Collections.unmodifiableMap(nodeIdMap);
    }

    // 去除路径->ID转换的对外依赖，所有对外API均以ID为准
} 