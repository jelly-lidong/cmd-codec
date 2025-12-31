# 填充节点依赖关系修复

## 问题描述

在原始的编码方法中，构建依赖关系时没有考虑填充节点对其他节点的依赖关系，导致以下问题：

1. **依赖缺失**：填充节点可能依赖其容器的长度或其他节点的长度
2. **拓扑排序错误**：填充节点可能在其依赖节点之前被处理
3. **计算错误**：填充长度计算时依赖的数据可能还未准备好
4. **⚠️ 时机问题**：填充处理在节点编码之前执行，此时context中缺少依赖节点的值

## 具体场景

### 容器填充场景
```xml
<body id="body" length="320">  <!-- 40字节固定长度 -->
    <node id="data1" length="64" value="0x1234567890ABCDEF"/>
    <node id="data2" length="96" value="0x112233445566778899AABBCC"/>
    <node id="padding" length="0" valueType="HEX">
        <paddingConfig paddingType="FILL_CONTAINER" 
                       containerNode="#body" 
                       paddingValue="0xFF"/>
    </node>
</body>
```

**问题**：填充节点需要知道容器总长度和其他子节点长度才能计算填充长度，但原始依赖图中没有建立这些依赖关系。

### 对齐填充场景
```xml
<body>
    <node id="data1" length="24" value="0x123456"/>
    <node id="data2" length="16" value="0xABCD"/>
    <node id="alignment_padding" length="0">
        <paddingConfig paddingType="ALIGNMENT" 
                       targetLength="64" 
                       paddingValue="0x00"/>
    </node>
</body>
```

**问题**：对齐填充需要计算前序节点的累计长度，但在预处理阶段这些节点还未编码。

### 动态填充场景
```xml
<node id="dynamic_padding" length="0">
    <paddingConfig paddingType="DYNAMIC" 
                   lengthExpression="#{data1.length} + #{data2.length} - 32"
                   paddingValue="0xFF"/>
</node>
```

**问题**：表达式引用其他节点的值，但在预处理时这些值还不在context中。

## 解决方案

### 🔧 **第一阶段修复**：建立依赖关系
在构建依赖关系图时，分析填充节点的依赖关系并添加到依赖图中：

```java
// 第三步：分析并添加填充节点的依赖关系
log.debug("[编码] 分析填充节点依赖关系");
analyzePaddingDependencies(protocol);
log.debug("[编码] 填充节点依赖关系分析完成");
```

### 🚀 **第二阶段修复**：动态填充处理
**关键修复**：将填充处理从预处理阶段移到节点编码过程中：

#### 修改前的流程：
```
1. 构建依赖关系图
2. 处理条件依赖
3. 处理填充配置 ❌ (此时context中缺少依赖数据)
4. 拓扑排序
5. 节点编码 (将节点值添加到context)
```

#### 修改后的流程：
```
1. 构建依赖关系图
2. 分析填充节点依赖关系
3. 处理条件依赖
4. 拓扑排序
5. 节点编码 (动态处理填充节点) ✅
```

#### 动态填充处理的核心代码：
```java
// 在节点编码循环中
for (String nodePath : order) {
    ProtocolNode node = dependencyGraph.getNode(nodePath);
    
    // 动态处理填充节点
    if (node.isPaddingNode()) {
        log.debug("- 检测到填充节点，开始动态填充处理");
        processPaddingNodeDynamically(node, context);
        log.debug("- 填充节点处理完成，新长度: {} 位", node.getLength());
    }
    
    // 编码当前节点
    byte[] nodeData = encodeNode(node, context);
    // ...
}
```

## 修复效果

### ✅ **依赖关系完整性**
- 所有填充节点的依赖关系都被正确识别和建立
- 拓扑排序确保填充节点在其依赖节点之后处理

### ✅ **数据可用性保证**
- 填充计算时，所有依赖的节点值都已在context中可用
- 支持引用其他节点的编码结果和计算值

### ✅ **处理时机正确**
- 填充节点在拓扑排序确定的正确时机进行处理
- 避免了预处理阶段数据不可用的问题

### ✅ **支持复杂场景**
- **容器填充**：可以从context中获取容器和兄弟节点的实际编码长度
- **对齐填充**：可以计算前序节点的累计长度
- **动态填充**：表达式可以引用context中已编码节点的值
- **条件填充**：支持基于context数据的条件判断

## 技术细节

### 动态填充处理方法
```java
private void processPaddingNodeDynamically(ProtocolNode paddingNode, Map<String, Object> context) {
    // 1. 检查填充启用条件
    if (!isPaddingEnabledInContext(config, context)) return;
    
    // 2. 根据填充类型计算填充长度
    int paddingLength = calculatePaddingLengthDynamically(config, paddingNode, context);
    
    // 3. 生成填充数据
    byte[] paddingData = generatePaddingDataDynamically(config, paddingLength);
    
    // 4. 更新节点信息
    updatePaddingNodeDynamically(paddingNode, paddingLength, paddingData);
}
```

### 支持的填充类型
1. **FIXED_LENGTH**：固定长度填充
2. **ALIGNMENT**：对齐填充（使用context中的累计长度）
3. **DYNAMIC**：动态填充（表达式可引用context数据）
4. **FILL_CONTAINER**：容器填充（使用context中的实际长度）

### 上下文数据利用
- **已编码节点值**：通过`encodedNodesCache`获取
- **节点实际长度**：使用编码后的真实长度而非配置长度
- **表达式计算**：可引用context中的所有可用数据
- **条件判断**：支持基于运行时数据的条件填充

## 向后兼容性

- 保持了原有的API接口不变
- 解码方法也进行了相应调整，确保编解码一致性
- 现有的填充配置无需修改，自动享受新的处理机制

## 总结

这个修复解决了填充节点依赖处理的根本问题：

1. **依赖关系建立**：确保拓扑排序的正确性
2. **处理时机优化**：从预处理改为动态处理
3. **数据可用性保证**：填充计算时所有依赖数据都可用
4. **功能完整性提升**：支持更复杂的填充场景

通过这个修复，填充节点现在可以正确地依赖其他节点的值和长度，实现了真正意义上的动态填充处理。 