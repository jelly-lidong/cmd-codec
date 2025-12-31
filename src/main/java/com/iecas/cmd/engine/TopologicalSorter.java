package com.iecas.cmd.engine;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 拓扑排序器
 * <p>
 * 本类用于处理有向无环图（DAG）中节点的依赖关系排序，确保所有依赖被正确处理。
 * 适用于如协议字段、任务调度、编译依赖等场景。
 * <p>
 * <b>拓扑排序原理：</b>
 * 拓扑排序（Topological Sort）是对有向无环图（DAG）顶点的一种排序，使得对于每一条有向边 (u, v)，顶点 u 都排在顶点 v 之前。
 * 其核心思想是：每次选择入度为0的节点输出，并移除其相关的边，重复直到所有节点都被输出。
 * 若图中存在环，则无法完成拓扑排序。
 * <p>
 * <b>本类实现逻辑：</b>
 * 1. 构造函数接收邻接表（graph）、入度表（inDegree），这些数据由外部依赖图维护。
 * 2. getTopologicalOrder() 方法实现拓扑排序：
 * - 首先将所有入度为0的节点加入队列。
 * - 依次弹出队列节点，检查其所有依赖是否已处理，未处理则重新入队。
 * - 处理节点后，将其所有邻接节点的入度减1，若邻接节点入度为0则入队。
 * - 直到队列为空。
 * - 若排序结果节点数小于图中节点数，说明存在环。
 * 3. findCycle() 方法递归检测图中是否存在环，并返回环路径。
 * 4. getAllPrerequisites() 方法递归获取某节点的所有直接和间接依赖。
 *
 * <b>使用说明：</b>
 * - 本类不负责依赖关系的构建，仅负责排序和环检测。
 * - 适合与依赖图管理类（如 DependencyGraph）配合使用。
 * - 支持详细日志输出，便于调试依赖关系。
 *
 * <b>典型应用：</b>
 * - 协议字段编码顺序
 * - 任务调度
 * - 编译依赖分析
 * - 数据流依赖
 */
@Slf4j
public class TopologicalSorter {


    // 邻接表表示的图（key为协议ID:节点ID）
    private final Map<String, LinkedHashSet<String>> graph;
    // 节点的入度（key为协议ID:节点ID）
    private final Map<String, Integer> inDegree;
    // 循环依赖检测相关
    private final Set<String> visited;
    private final Set<String> recursionStack;
    private List<String> cyclePath;

    /**
     * 构造函数
     *
     * <p><b>注意：</b>拓扑排序完全基于节点ID（协议ID:节点ID），不依赖path。</p>
     *
     * @param graph    邻接表表示的图（key为协议ID:节点ID）
     * @param inDegree 节点的入度（key为协议ID:节点ID）
     */
    public TopologicalSorter(Map<String, LinkedHashSet<String>> graph,
                             Map<String, Integer> inDegree) {
        this.graph = graph;
        this.inDegree = inDegree;
        this.visited = new HashSet<>();
        this.recursionStack = new HashSet<>();
        this.cyclePath = new ArrayList<>();
    }

    /**
     * 拓扑排序原理：
     * 1. 入度：指向该节点的边的数量
     * 2. 算法步骤：
     * - 将所有入度为0的节点加入队列
     * - 从队列取出一个节点，将其加入结果序列
     * - 将该节点指向的所有节点的入度减1
     * - 如果某个节点的入度变为0，则将其加入队列
     * - 重复上述步骤直到队列为空
     * 3. 结果判断：
     * - 如果结果序列长度等于图中节点数，说明排序成功
     * - 如果结果序列长度小于图中节点数，说明图中存在环
     *
     * @return 按依赖顺序排序的节点列表
     * @throws IllegalStateException 如果图中存在循环依赖
     */
    public List<String> getTopologicalOrder() {
        // 打印调试信息：开始排序
        log.debug("[拓扑排序] 开始排序");
        log.debug("[调试] 入度表详情:");
        int index = 1;
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            log.debug("  -{} 节点: {}, 入度: {}", index++, entry.getKey(), entry.getValue());
        }

        // 用于存储最终排序结果的列表
        List<String> result = new ArrayList<>();
        // 队列用于存放当前入度为0的节点
        Queue<String> queue = new LinkedList<>();
        // 拷贝一份入度表，避免修改原始数据
        Map<String, Integer> currentInDegree = new LinkedHashMap<>(inDegree);

        // 1. 将所有入度为0的节点加入队列（这些节点没有任何依赖，可以最先处理）
        for (Map.Entry<String, Integer> entry : currentInDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        int loopCount = 0; // 死循环保护计数器
        log.debug("[拓扑排序] 开始主循环处理");
        // 2. 主循环：只要队列不为空，就不断处理入度为0的节点
        while (!queue.isEmpty()) {
            loopCount++;
//            log.debug("[拓扑排序] 第 {} 轮处理", loopCount);
            // 取出一个入度为0的节点
            String node = queue.poll();
//            log.debug("- 处理节点: {}", node);
            // 将该节点加入排序结果
            result.add(node);
            //log.debug("  [结果] 当前排序结果: {}", String.join(" -> ", result));

            // 3. 遍历该节点的所有邻接节点（即它指向的节点/它的出边）
            Set<String> neighbors = graph.getOrDefault(node, new LinkedHashSet<>());
            //log.debug("  [依赖] 节点 {} 的邻接节点: {}", node, neighbors);

            for (String neighbor : neighbors) {
                // 该邻接节点的入度减1（因为node已被处理，相当于移除了node->neighbor这条边）
                int oldInDegree = currentInDegree.getOrDefault(neighbor, 0);
                currentInDegree.put(neighbor, oldInDegree - 1);
                //log.debug("  - 更新邻接节点 {} 的入度: {} -> {}", neighbor, oldInDegree, oldInDegree - 1);

                // 如果邻接节点的入度变为0，说明它的所有依赖都已处理，可以入队
                if (currentInDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                    //log.debug("    [入队] 邻接节点 {} 入度降为0，加入处理队列", neighbor);
                }
            }
        }
        //log.debug("[拓扑排序] 主循环处理完成，共处理 {} 轮", loopCount);

        // 4. 检查是否所有节点都已被处理
        if (result.size() != graph.size()) {
            // 如果未全部处理，说明存在环或依赖异常，添加详细诊断信息
//            log.error("[拓扑排序] 排序失败，详细诊断信息:");
//            log.error("- 总节点数: {}", graph.size());
//            log.error("- 已处理节点数: {}", result.size());
//            log.error("- 未处理节点数: {}", graph.size() - result.size());

            // 找出未处理的节点
            Set<String> processedNodes = new HashSet<>(result);
            Set<String> unprocessedNodes = new HashSet<>();
            for (String node : graph.keySet()) {
                if (!processedNodes.contains(node)) {
                    unprocessedNodes.add(node);
                }
            }

//            log.error("- 未处理的节点: {}", String.join(", ", unprocessedNodes));

            // 检查未处理节点的入度
            for (String node : unprocessedNodes) {
                int inDegree = currentInDegree.getOrDefault(node, 0);
//                log.error("- 节点 {} 的入度: {}", node, inDegree);

                // 检查该节点的依赖关系
                Set<String> dependencies = graph.getOrDefault(node, new LinkedHashSet<>());
                if (!dependencies.isEmpty()) {
//                    log.error("  - 依赖的节点: {}", String.join(", ", dependencies));
                }

                // 检查哪些节点依赖这个未处理的节点（反向依赖）
//                log.error("  - 被哪些节点依赖:");
                for (Map.Entry<String, LinkedHashSet<String>> entry : graph.entrySet()) {
                    String fromNode = entry.getKey();
                    LinkedHashSet<String> toNodes = entry.getValue();
                    if (toNodes.contains(node)) {
//                        log.error("    * {} 依赖 {}", fromNode, node);
                    }
                }

//                // 检查该节点的表达式依赖
//                log.error("  - 表达式依赖分析:");
//                if (node.contains("ParamID") || node.contains("ParamValue")) {
//                    log.error("    * 这是一个参数节点，应该被父节点依赖，而不是依赖父节点");
//                    log.error("    * 当前依赖关系方向错误，形成了循环依赖");
//                }
            }

            // 如果未全部处理，说明存在环或依赖异常，抛出异常
            throw new RuntimeException("[错误] 拓扑排序疑似死循环，强制退出。详细诊断信息请查看日志。");
        }
        // 打印排序完成信息
        log.debug("- 拓扑排序完成");
        //log.debug("- 排序结果: " + String.join(" -> ", result));
        return result;
    }

    /**
     * 计算每个节点的被依赖深度
     * 被依赖深度越大，说明越多节点依赖它，应该越先处理
     *
     * @return 节点名到被依赖深度的映射
     */
    private Map<String, Integer> calculateNodeDepths() {
        Map<String, Integer> depths = new HashMap<>();

        // 构建反向图：谁依赖我
        Map<String, Set<String>> reverseGraph = new HashMap<>();
        for (Map.Entry<String, ? extends Set<String>> entry : graph.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                reverseGraph.computeIfAbsent(to, k -> new HashSet<>()).add(from);
            }
        }

        // 计算每个节点的被依赖深度
        for (String node : graph.keySet()) {
            calculateDependentDepth(node, reverseGraph, depths, new HashSet<>());
        }

        return depths;
    }

    private int calculateDependentDepth(String node, Map<String, Set<String>> reverseGraph,
                                        Map<String, Integer> depths, Set<String> visited) {
        // 已经计算过的节点直接返回
        if (depths.containsKey(node)) {
            return depths.get(node);
        }

        // 避免循环依赖
        if (visited.contains(node)) {
            return 0;
        }
        visited.add(node);

        // 获取所有依赖当前节点的节点
        Set<String> dependents = reverseGraph.getOrDefault(node, new HashSet<>());

        // 没有节点依赖我，深度为1
        if (dependents.isEmpty()) {
            depths.put(node, 1);
            return 1;
        }

        // 被依赖深度是所有依赖我的节点的深度最大值+1
        int maxDepth = 0;
        for (String dependent : dependents) {
            maxDepth = Math.max(maxDepth, calculateDependentDepth(dependent, reverseGraph, depths, new HashSet<>(visited)));
        }

        int depth = maxDepth + 1;
        depths.put(node, depth);
        return depth;
    }

    /**
     * 获取节点的所有前置依赖（直接和间接依赖）
     *
     * @param node 节点名称
     * @return 必须在该节点之前处理的所有节点集合
     */
    private Set<String> getAllPrerequisites(String node) {
        Set<String> prerequisites = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(node);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> deps = graph.getOrDefault(current, new LinkedHashSet<>());

            for (String dep : deps) {
                if (prerequisites.add(dep)) {
                    // 如果是新添加的依赖，则继续查找其依赖
                    queue.offer(dep);
                }
            }
        }

        return prerequisites;
    }

    /**
     * 检查是否存在循环依赖
     *
     * @return 如果存在循环依赖，返回循环路径；否则返回null
     */
    public List<String> findCycle() {
        visited.clear();
        recursionStack.clear();
        cyclePath.clear();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                if (isCyclicUtil(node)) {
                    log.debug("- 发现循环依赖");
                    log.debug("- 循环路径: " + String.join(" -> ", cyclePath));
                    return new ArrayList<>(cyclePath);
                }
            }
        }

        return null;
    }

    /**
     * 递归检查节点是否形成循环
     *
     * @param node 当前检查的节点
     * @return 如果形成循环返回true，否则返回false
     */
    private boolean isCyclicUtil(String node) {
        if (recursionStack.contains(node)) {
            // 找到循环，记录循环路径
            int startIndex = cyclePath.indexOf(node);
            cyclePath = cyclePath.subList(startIndex, cyclePath.size());
            cyclePath.add(node);
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);
        cyclePath.add(node);

        for (String neighbor : graph.get(node)) {
            if (isCyclicUtil(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(node);
        cyclePath.remove(cyclePath.size() - 1);
        return false;
    }
} 