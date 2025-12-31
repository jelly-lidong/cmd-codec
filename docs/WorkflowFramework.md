# 链式执行工作流框架

## 🎯 概述

这是一个专为指令编制设计的链式执行工作流框架，支持树形流程和分支处理。框架允许您根据不同的协议类型或条件，动态选择执行路径，实现复杂的指令编制流程。

## 🏗️ 架构设计

### 核心组件

1. **ExecutionContext** - 执行上下文
   - 在整个工作流中传递数据和状态
   - 支持子上下文创建和结果合并
   - 提供错误处理和状态管理

2. **ExecutionNode** - 执行节点接口
   - 工作流中的基本执行单元
   - 支持链式调用和条件执行
   - 提供前置条件检查和结果输出

3. **BranchNode** - 分支节点接口
   - 支持条件分支和并行分支
   - 动态路径选择
   - 分支结果聚合

4. **WorkflowEngine** - 工作流执行引擎
   - 负责整个工作流的执行
   - 支持并行执行和超时控制
   - 提供完整的执行结果和统计信息

5. **WorkflowBuilder** - 工作流构建器
   - 提供流式API构建工作流
   - 支持复杂的分支逻辑
   - 工作流验证和结构打印

## 🚀 快速开始

### 1. 简单线性工作流

```java
// 创建节点
ExecutionNode step1 = new SimpleTaskNode("step1", "初始化", "设置初始参数");
ExecutionNode step2 = new SimpleTaskNode("step2", "处理", "执行处理逻辑");
ExecutionNode step3 = new SimpleTaskNode("step3", "保存", "保存结果");

// 构建工作流
ExecutionNode workflow = WorkflowEngine.builder()
    .start(step1)
    .then(step2)
    .then(step3)
    .build();

// 执行工作流
ExecutionContext context = new ExecutionContext();
context.setData("inputData", "测试数据");

WorkflowEngine engine = new WorkflowEngine();
WorkflowExecutionResult result = engine.execute(workflow, context);
```

### 2. 条件分支工作流

```java
// 创建分支节点
ExecutionNode validation = new SimpleTaskNode("validation", "验证", "数据验证");
ExecutionNode validPath = new SimpleTaskNode("valid", "正常处理", "处理有效数据");
ExecutionNode invalidPath = new SimpleTaskNode("invalid", "错误处理", "处理无效数据");

// 构建条件分支
WorkflowBuilder builder = WorkflowEngine.builder()
    .start(validation)
    .branch("decision", "数据处理决策");

builder.addDataCondition("valid", "dataValid", true, validPath)
       .addDataCondition("invalid", "dataValid", false, invalidPath);

ExecutionNode workflow = builder.build();
```

### 3. 协议编制分支工作流

```java
// 创建协议编制节点
ExecutionNode protocolSelection = new SimpleTaskNode("select", "协议选择", "选择协议类型");
ExecutionNode protocolA = new ProtocolEncodeNode("encode_a", "协议A编制", codecA);
ExecutionNode protocolB = new ProtocolEncodeNode("encode_b", "协议B编制", codecB);
ExecutionNode protocolC = new ProtocolEncodeNode("encode_c", "协议C编制", codecC);

// 构建协议分支工作流
WorkflowBuilder builder = WorkflowEngine.builder()
    .start(protocolSelection)
    .branch("protocol_branch", "协议分支选择");

builder.addDataCondition("type_a", "protocolType", "A", protocolA)
       .addDataCondition("type_b", "protocolType", "B", protocolB)
       .addDataCondition("type_c", "protocolType", "C", protocolC);

ExecutionNode workflow = builder.build();
```

## 📋 节点类型

### 1. 协议编码节点 (ProtocolEncodeNode)
- 执行协议编码操作
- 支持动态协议选择
- 自动结果验证和缓存

### 2. 条件分支节点 (ConditionalBranchNode)
- 基于条件选择执行路径
- 支持多种条件类型：
  - 数据值比较
  - 数据存在性检查
  - 数值范围比较
  - 自定义条件函数

### 3. 自定义任务节点 (AbstractExecutionNode)
- 实现特定业务逻辑
- 支持前置条件检查
- 提供输出数据管理

## 🔧 高级特性

### 1. 并行执行
```java
WorkflowEngine engine = new WorkflowEngine();
engine.setEnableParallelExecution(true);
engine.setExecutionTimeoutSeconds(300);
```

### 2. 错误处理
```java
engine.setStopOnError(true); // 遇到错误时停止执行

// 在节点中处理错误
@Override
protected Object doExecute(ExecutionContext context, ExecutionResult result) throws Exception {
    try {
        // 执行逻辑
        return processData();
    } catch (Exception e) {
        result.addLog("处理失败: " + e.getMessage());
        throw e;
    }
}
```

### 3. 上下文数据管理
```java
// 设置数据
context.setData("protocolType", "A");
context.setData("inputData", rawData);

// 获取数据
String protocolType = context.getData("protocolType");
byte[] inputData = context.getData("inputData", new byte[0]);

// 检查数据存在
if (context.containsKey("encodedData")) {
    // 处理编码数据
}
```

### 4. 执行结果分析
```java
WorkflowExecutionResult result = engine.execute(workflow, context);

System.out.println("执行成功: " + result.isSuccess());
System.out.println("总耗时: " + result.getDuration() + "ms");
System.out.println("成功节点: " + result.getSuccessNodeCount());
System.out.println("失败节点: " + result.getFailureNodeCount());

// 获取失败节点详情
List<ExecutionResult> failures = result.getFailureResults();
for (ExecutionResult failure : failures) {
    System.out.println("失败节点: " + failure.getNodeId());
    System.out.println("错误信息: " + failure.getErrorMessage());
}
```

## 🌟 应用场景

### 1. 指令编制流程
```
指令输入 → 协议选择 → 分支编制 → 验证 → 打包输出
                ├─ 协议A编制
                ├─ 协议B编制
                └─ 协议C编制
```

### 2. 数据处理管道
```
数据接收 → 格式验证 → 分支处理 → 结果聚合 → 存储
                ├─ JSON处理
                ├─ XML处理
                └─ 二进制处理
```

### 3. 多协议通信
```
消息接收 → 协议识别 → 分支解析 → 业务处理 → 响应发送
                ├─ HTTP协议
                ├─ TCP协议
                └─ UDP协议
```

## 📊 性能特性

- **并发执行**: 支持分支并行处理，提高执行效率
- **资源管理**: 自动管理线程池和资源清理
- **超时控制**: 防止长时间运行的任务阻塞工作流
- **内存优化**: 子上下文隔离，避免内存泄漏
- **错误恢复**: 支持错误处理和工作流恢复

## 🔍 调试和监控

### 1. 工作流结构可视化
```java
WorkflowBuilder builder = WorkflowEngine.builder()
    .start(startNode)
    .branch("decision", "决策节点")
    .addDataCondition("path1", "type", "A", nodeA)
    .addDataCondition("path2", "type", "B", nodeB);

builder.printStructure(); // 打印工作流结构
```

### 2. 执行历史追踪
```java
// 获取执行历史
Map<String, ExecutionResult> history = context.getAllExecutionHistory();
for (Map.Entry<String, ExecutionResult> entry : history.entrySet()) {
    String nodeId = entry.getKey();
    ExecutionResult result = entry.getValue();
    System.out.println(nodeId + ": " + result.getStatus());
}
```

### 3. 详细日志记录
```java
// 在节点中添加日志
result.addLog("开始处理数据");
result.addLog("数据大小: " + data.length);
result.addLog("处理完成");

// 获取执行日志
String logs = result.getExecutionLog();
System.out.println(logs);
```

## 🎯 最佳实践

1. **节点设计原则**
   - 单一职责：每个节点只负责一个明确的功能
   - 无状态：节点不应保存状态，所有状态通过上下文传递
   - 可重用：设计通用的节点，支持参数化配置

2. **错误处理策略**
   - 及早失败：在节点开始时验证前置条件
   - 优雅降级：提供默认分支处理异常情况
   - 详细日志：记录足够的信息用于问题诊断

3. **性能优化建议**
   - 合理使用并行执行，避免不必要的串行等待
   - 控制上下文数据大小，避免传递大对象
   - 设置合适的超时时间，防止死锁

4. **工作流设计模式**
   - 管道模式：线性处理流程
   - 分支模式：条件选择执行
   - 聚合模式：多路结果合并
   - 补偿模式：错误回滚处理

## 📝 总结

这个链式执行工作流框架为指令编制提供了强大而灵活的解决方案。通过支持树形流程、条件分支和并行执行，它能够满足复杂的业务需求，同时保持代码的清晰性和可维护性。

框架的核心优势：
- 🔄 **灵活的分支逻辑** - 支持复杂的条件判断和路径选择
- 🚀 **高性能执行** - 并行处理和资源优化
- 🛡️ **健壮的错误处理** - 完善的异常处理和恢复机制
- 📊 **全面的监控** - 详细的执行统计和日志记录
- 🔧 **易于扩展** - 模块化设计，支持自定义节点类型 