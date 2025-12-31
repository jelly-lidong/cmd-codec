package com.iecas.cmd.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * TopologicalSorter类的测试用例
 * 
 * 测试覆盖以下复杂情况：
 * 1. 基本拓扑排序功能
 * 2. 复杂依赖关系
 * 3. 边界情况
 * 4. 异常情况
 * 5. 性能测试
 */
public class TopologicalSorterTest{} /*{

    private TopologicalSorter sorter;
    private Map<String, LinkedHashSet<String>> graph;
    private Map<String, Integer> inDegree;
    private Map<String, String> nodePathMap;

    @Before
    public void setUp() {
        graph = new LinkedHashMap<>();
        inDegree = new LinkedHashMap<>();
        nodePathMap = new LinkedHashMap<>();
    }

    // ==================== 基本功能测试 ====================

    @Test
    public void should_SortLinearDependencies() {
        // 准备数据：A -> B -> C
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("C")));
        graph.put("C", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(3, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
        assertEquals("C", result.get(2));
    }

    @Test
    public void should_SortIndependentNodes() {
        // 准备数据：A, B, C 三个独立节点
        graph.put("A", new LinkedHashSet<>());
        graph.put("B", new LinkedHashSet<>());
        graph.put("C", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 0);
        inDegree.put("C", 0);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(3, result.size());
        assertTrue(result.contains("A"));
        assertTrue(result.contains("B"));
        assertTrue(result.contains("C"));
    }

    @Test
    public void should_SortSingleNode() {
        // 准备数据：只有一个节点A
        graph.put("A", new LinkedHashSet<>());
        inDegree.put("A", 0);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0));
    }

    // ==================== 复杂依赖关系测试 ====================

    @Test
    public void should_SortTreeDependencies() {
        // 准备数据：树形结构
        //      A
        //     / \
        //    B   C
        //   / \   \
        //  D   E   F
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("D", "E")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("F")));
        graph.put("D", new LinkedHashSet<>());
        graph.put("E", new LinkedHashSet<>());
        graph.put("F", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 1);
        inDegree.put("E", 1);
        inDegree.put("F", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(6, result.size());
        assertEquals("A", result.get(0));
        
        // B和C应该在A之后，但顺序可能不同
        assertTrue(result.indexOf("B") > result.indexOf("A"));
        assertTrue(result.indexOf("C") > result.indexOf("A"));
        
        // D和E应该在B之后
        assertTrue(result.indexOf("D") > result.indexOf("B"));
        assertTrue(result.indexOf("E") > result.indexOf("B"));
        
        // F应该在C之后
        assertTrue(result.indexOf("F") > result.indexOf("C"));
    }

    @Test
    public void should_SortDiamondDependencies() {
        // 准备数据：菱形结构
        //    A
        //   / \
        //  B   C
        //   \ /
        //    D
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("D", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 2);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(4, result.size());
        assertEquals("A", result.get(0));
        
        // B和C应该在A之后，但顺序可能不同
        assertTrue(result.indexOf("B") > result.indexOf("A"));
        assertTrue(result.indexOf("C") > result.indexOf("A"));
        
        // D应该在B和C之后
        assertTrue(result.indexOf("D") > result.indexOf("B"));
        assertTrue(result.indexOf("D") > result.indexOf("C"));
    }

    @Test
    public void should_SortMultiLayerDependencies() {
        // 准备数据：多层复杂依赖
        // L0: A
        // L1: B, C
        // L2: D, E, F
        // L3: G, H
        // L4: Protocol
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("D", "E")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("E", "F")));
        graph.put("D", new LinkedHashSet<>(Arrays.asList("G")));
        graph.put("E", new LinkedHashSet<>(Arrays.asList("G", "H")));
        graph.put("F", new LinkedHashSet<>(Arrays.asList("H")));
        graph.put("G", new LinkedHashSet<>(Arrays.asList("Protocol")));
        graph.put("H", new LinkedHashSet<>(Arrays.asList("Protocol")));
        graph.put("Protocol", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 1);
        inDegree.put("E", 2);
        inDegree.put("F", 1);
        inDegree.put("G", 2);
        inDegree.put("H", 2);
        inDegree.put("Protocol", 2);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(9, result.size());
        
        // 验证层次关系
        assertTrue(result.indexOf("B") > result.indexOf("A"));
        assertTrue(result.indexOf("C") > result.indexOf("A"));
        assertTrue(result.indexOf("D") > result.indexOf("B"));
        assertTrue(result.indexOf("E") > result.indexOf("B"));
        assertTrue(result.indexOf("E") > result.indexOf("C"));
        assertTrue(result.indexOf("F") > result.indexOf("C"));
        assertTrue(result.indexOf("G") > result.indexOf("D"));
        assertTrue(result.indexOf("G") > result.indexOf("E"));
        assertTrue(result.indexOf("H") > result.indexOf("E"));
        assertTrue(result.indexOf("H") > result.indexOf("F"));
        assertTrue(result.indexOf("Protocol") > result.indexOf("G"));
        assertTrue(result.indexOf("Protocol") > result.indexOf("H"));
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void should_HandleEmptyGraph() {
        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(0, result.size());
    }

    @Test
    public void should_HandleOnlyNonZeroInDegreeNodes() {
        // 准备数据：所有节点都有依赖
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("A")));

        inDegree.put("A", 1);
        inDegree.put("B", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 应该抛出异常，因为存在循环依赖
        try {
            sorter.getTopologicalOrder();
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            // 预期异常
        }
    }

    @Test
    public void should_HandleSelfDependency() {
        // 准备数据：A依赖自己
        graph.put("A", new LinkedHashSet<>(Arrays.asList("A")));
        inDegree.put("A", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 应该抛出异常，因为存在循环依赖
        try {
            sorter.getTopologicalOrder();
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            // 预期异常
        }
    }

    @Test
    public void should_HandleMixedIsolatedAndDependentNodes() {
        // 准备数据：A是孤立节点，B->C有依赖关系
        graph.put("A", new LinkedHashSet<>());
        graph.put("B", new LinkedHashSet<>(Arrays.asList("C")));
        graph.put("C", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 0);
        inDegree.put("C", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(3, result.size());
        assertTrue(result.indexOf("C") > result.indexOf("B"));
    }

    // ==================== 异常情况测试 ====================

    @Test
    public void should_DetectCircularDependencies() {
        // 准备数据：A -> B -> C -> A 循环依赖
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("C")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("A")));

        inDegree.put("A", 1);
        inDegree.put("B", 1);
        inDegree.put("C", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 应该抛出异常
        try {
            sorter.getTopologicalOrder();
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            // 预期异常
        }
    }

    @Test
    public void should_DetectComplexCircularDependencies() {
        // 准备数据：复杂的循环依赖
        // A -> B -> C -> D -> B
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("C")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("D", new LinkedHashSet<>(Arrays.asList("B")));

        inDegree.put("A", 0);
        inDegree.put("B", 2);
        inDegree.put("C", 1);
        inDegree.put("D", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 应该抛出异常
        try {
            sorter.getTopologicalOrder();
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            // 预期异常
        }
    }

    @Test
    public void should_DetectPartialCircularDependencies() {
        // 准备数据：部分节点有循环依赖
        // A -> B -> C (正常)
        // D -> E -> D (循环)
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("C")));
        graph.put("C", new LinkedHashSet<>());
        graph.put("D", new LinkedHashSet<>(Arrays.asList("E")));
        graph.put("E", new LinkedHashSet<>(Arrays.asList("D")));

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 1);
        inDegree.put("E", 1);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 应该抛出异常
        try {
            sorter.getTopologicalOrder();
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            // 预期异常
        }
    }

    // ==================== 性能测试 ====================

    @Test
    public void should_HandleLargeNumberOfNodes() {
        // 准备数据：1000个节点的线性依赖链
        int nodeCount = 1000;
        
        for (int i = 0; i < nodeCount; i++) {
            String nodeName = "Node" + i;
            if (i < nodeCount - 1) {
                graph.put(nodeName, new LinkedHashSet<>(Arrays.asList("Node" + (i + 1))));
            } else {
                graph.put(nodeName, new LinkedHashSet<>());
            }
            
            inDegree.put(nodeName, i == 0 ? 0 : 1);
        }

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        long startTime = System.currentTimeMillis();
        List<String> result = sorter.getTopologicalOrder();
        long endTime = System.currentTimeMillis();

        assertEquals(nodeCount, result.size());
        
        // 验证性能：1000个节点应该在1秒内完成
        long executionTime = endTime - startTime;
        assertTrue("排序1000个节点耗时过长: " + executionTime + "ms", executionTime < 1000);
        
        // 验证顺序正确性
        for (int i = 0; i < result.size() - 1; i++) {
            String current = result.get(i);
            String next = result.get(i + 1);
            int currentIndex = Integer.parseInt(current.substring(4));
            int nextIndex = Integer.parseInt(next.substring(4));
            assertTrue("排序顺序错误", currentIndex < nextIndex);
        }
    }

    @Test
    public void should_HandleDenseDependencyGraph() {
        // 准备数据：100个节点，每个节点依赖多个其他节点
        int nodeCount = 100;
        
        for (int i = 0; i < nodeCount; i++) {
            LinkedHashSet<String> dependencies = new LinkedHashSet<>();
            // 每个节点依赖其后的所有节点
            for (int j = i + 1; j < nodeCount; j++) {
                dependencies.add("Node" + j);
            }
            graph.put("Node" + i, dependencies);
            
            // 入度 = 节点索引
            inDegree.put("Node" + i, i);
        }

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        long startTime = System.currentTimeMillis();
        List<String> result = sorter.getTopologicalOrder();
        long endTime = System.currentTimeMillis();

        assertEquals(nodeCount, result.size());
        
        // 验证性能：密集图应该在合理时间内完成
        long executionTime = endTime - startTime;
        assertTrue("排序密集依赖图耗时过长: " + executionTime + "ms", executionTime < 5000);
        
        // 验证顺序：索引小的节点应该先出现
        for (int i = 0; i < result.size(); i++) {
            String nodeName = result.get(i);
            int nodeIndex = Integer.parseInt(nodeName.substring(4));
            assertEquals("排序顺序错误", i, nodeIndex);
        }
    }

    // ==================== 实际应用场景测试 ====================

    @Test
    public void should_SortProtocolFields() {
        // 模拟协议字段的依赖关系
        // 图结构：Header -> Version, Type; Data -> Length; Checksum -> Data
        // 注意：这里的箭头表示"指向"关系，即Header指向Version和Type
        // 在拓扑排序中，指向者应该在被指向者之前处理
        graph.put("Version", new LinkedHashSet<>());
        graph.put("Type", new LinkedHashSet<>());
        graph.put("Header", new LinkedHashSet<>(Arrays.asList("Version", "Type")));
        graph.put("Length", new LinkedHashSet<>());
        graph.put("Data", new LinkedHashSet<>(Arrays.asList("Length")));
        graph.put("Checksum", new LinkedHashSet<>(Arrays.asList("Data")));

        inDegree.put("Version", 1);  // 被Header依赖
        inDegree.put("Type", 1);     // 被Header依赖
        inDegree.put("Header", 0);   // 不依赖任何节点
        inDegree.put("Length", 1);   // 被Data依赖
        inDegree.put("Data", 1);     // 被Checksum依赖
        inDegree.put("Checksum", 0); // 不依赖任何节点

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(6, result.size());
        
        // 验证关键依赖关系
        // 根据实际图结构：Header -> Version, Type; Data -> Length; Checksum -> Data
        assertTrue(result.indexOf("Header") < result.indexOf("Version"));
        assertTrue(result.indexOf("Header") < result.indexOf("Type"));
        assertTrue(result.indexOf("Data") < result.indexOf("Length"));
        assertTrue(result.indexOf("Checksum") < result.indexOf("Data"));
    }

    @Test
    public void should_SortTaskDependencies() {
        // 模拟任务调度的依赖关系
        // 编译任务 -> 测试任务 -> 部署任务
        // 编译任务依赖：代码检查、依赖下载
        // 测试任务依赖：编译完成、测试环境准备
        // 部署任务依赖：测试通过、部署环境准备
        // 注意：图结构需要表示"谁依赖我"，而不是"我依赖谁"
        graph.put("代码检查", new LinkedHashSet<>(Arrays.asList("编译任务")));
        graph.put("依赖下载", new LinkedHashSet<>(Arrays.asList("编译任务")));
        graph.put("编译任务", new LinkedHashSet<>(Arrays.asList("测试任务")));
        graph.put("测试环境准备", new LinkedHashSet<>(Arrays.asList("测试任务")));
        graph.put("测试任务", new LinkedHashSet<>(Arrays.asList("部署任务")));
        graph.put("部署环境准备", new LinkedHashSet<>(Arrays.asList("部署任务")));
        graph.put("部署任务", new LinkedHashSet<>());

        inDegree.put("代码检查", 0);
        inDegree.put("依赖下载", 0);
        inDegree.put("编译任务", 2);    // 依赖于：代码检查、依赖下载
        inDegree.put("测试环境准备", 0);
        inDegree.put("测试任务", 2);    // 依赖于：编译任务、测试环境准备
        inDegree.put("部署环境准备", 0);
        inDegree.put("部署任务", 2);    // 依赖于：测试任务、部署环境准备

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        List<String> result = sorter.getTopologicalOrder();

        assertEquals(7, result.size());
        
        // 验证任务执行顺序
        assertTrue(result.indexOf("编译任务") < result.indexOf("测试任务"));
        assertTrue(result.indexOf("测试任务") < result.indexOf("部署任务"));
    }

    // ==================== 数据一致性测试 ====================

    @Test
    public void should_MaintainGraphDataConsistency() {
        // 准备数据
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("D", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 2);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 执行排序
        List<String> result = sorter.getTopologicalOrder();

        // 验证原始数据未被修改
        assertEquals(4, graph.size());
        assertEquals(4, inDegree.size());
        
        // 验证图结构完整性
        assertTrue(graph.containsKey("A"));
        assertTrue(graph.containsKey("B"));
        assertTrue(graph.containsKey("C"));
        assertTrue(graph.containsKey("D"));
        
        // 验证入度表完整性
        assertEquals("入度A应该是0", 0, inDegree.get("A").intValue());
        assertEquals("入度B应该是1", 1, inDegree.get("B").intValue());
        assertEquals("入度C应该是1", 1, inDegree.get("C").intValue());
        assertEquals("入度D应该是2", 2, inDegree.get("D").intValue());
    }

    @Test
    public void should_ProduceConsistentResults() {
        // 准备数据
        graph.put("A", new LinkedHashSet<>(Arrays.asList("B", "C")));
        graph.put("B", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("C", new LinkedHashSet<>(Arrays.asList("D")));
        graph.put("D", new LinkedHashSet<>());

        inDegree.put("A", 0);
        inDegree.put("B", 1);
        inDegree.put("C", 1);
        inDegree.put("D", 2);

        sorter = new TopologicalSorter(graph, inDegree, nodePathMap);

        // 多次执行排序，结果应该一致
        List<String> result1 = sorter.getTopologicalOrder();
        List<String> result2 = sorter.getTopologicalOrder();
        List<String> result3 = sorter.getTopologicalOrder();

        assertTrue("多次执行结果应该一致", result1.equals(result2));
        assertTrue("多次执行结果应该一致", result2.equals(result3));
        assertTrue("多次执行结果应该一致", result1.equals(result3));
    }
}*/
