# 范围函数依赖关系修复方案

## 问题描述

在原有的表达式解析器中，对于像 `crc16Between(#version,#data_field)` 这样的范围函数，解析器只提取了直接引用的节点ID（`#version` 和 `#data_field`），但实际上这些函数表示从起始节点到结束节点之间的所有节点。

这导致了以下问题：
1. **依赖关系不完整**：只建立了对起始和结束节点的依赖，忽略了中间节点
2. **处理顺序错误**：拓扑排序结果不正确，可能导致节点处理顺序错误
3. **校验计算错误**：CRC等校验值可能基于不完整的数据计算

## 解决方案

### 1. 基于注册机制的范围函数管理

**问题**：原有的硬编码范围函数列表缺乏灵活性和可扩展性。

**解决方案**：创建了完整的注册机制来管理范围函数：

#### 1.1 范围函数注册器 (`RangeFunctionRegistry`)

```java
// 不再硬编码函数列表，而是使用注册器
public class RangeFunctionRegistry {
    private static final ConcurrentHashMap<String, RangeFunctionInfo> REGISTRY = new ConcurrentHashMap<>();
    
    // 支持动态注册和移除函数
    public static void registerRangeFunction(String functionName, String description, 
                                           int minParameters, int maxParameters, boolean isRangeFunction);
    
    // 查询函数信息
    public static boolean isRangeFunction(String functionName);
    public static RangeFunctionInfo getFunctionInfo(String functionName);
}
```

#### 1.2 函数自动注册器 (`FunctionAutoRegistrar`)

```java
// 使用注解自动注册函数
@FunctionAutoRegistrar.RangeFunction(
    name = "crc16Between",
    description = "计算两个节点之间数据的CRC16值",
    minParameters = 2,
    maxParameters = 2
)
public class Crc16BetweenFunction extends AbstractFunction {
    // 函数实现
}
```

#### 1.3 表达式解析器改进

```java
// 使用注册器而不是硬编码列表
public class ExpressionParser {
    // 检查是否为范围函数
    public boolean containsRangeFunction(String expression) {
        // 通过注册器动态检查
        return RangeFunctionRegistry.isRangeFunction(functionName);
    }
}
```

**优势**：
- **灵活性**：支持运行时动态注册和移除函数
- **可扩展性**：新增函数无需修改核心代码
- **维护性**：函数信息集中管理，便于维护
- **类型安全**：通过注解提供编译时检查

### 2. 改进依赖构建器

在 `ProtocolDependencyBuilder` 类中添加了专门的范围函数依赖处理逻辑：

```java
private void addRangeFunctionDependencies(String nodePath, String expression) throws CodecException {
    // 获取范围函数信息
    Set<ExpressionParser.RangeFunctionInfo> rangeFunctions = 
        expressionParser.getRangeFunctionInfo(expression);
    
    for (ExpressionParser.RangeFunctionInfo rangeFunc : rangeFunctions) {
        String startNodeId = rangeFunc.getStartNodeId();
        String endNodeId = rangeFunc.getEndNodeId();
        
        // 获取所有节点，按协议结构顺序排序
        List<String> allNodePaths = dependencyGraph.getAllNodePathsInOrder();
        
        // 找到起始和结束节点在顺序中的位置
        int startIndex = allNodePaths.indexOf(startNodePath);
        int endIndex = allNodePaths.indexOf(endNodePath);
        
        // 为范围内的所有节点建立依赖关系
        for (int i = startIndex; i <= endIndex; i++) {
            String rangeNodePath = allNodePaths.get(i);
            if (!rangeNodePath.equals(nodePath)) { // 避免自依赖
                dependencyGraph.addDependency(nodePath, rangeNodePath);
            }
        }
    }
}
```

### 3. 扩展依赖图

在 `DependencyGraph` 类中添加了 `getAllNodePathsInOrder()` 方法，用于获取按协议结构顺序排序的节点路径列表。

## 使用示例

### 协议定义

```java
@ProtocolDefinition(name = "单层协议")
public class SimpleLayerProtocol {

    @ProtocolHeader
    @ProtocolNode(id = "protocol_id", name = "协议标识", 
                  valueType = ValueType.HEX, length = 16, value = "0x1234")
    private String protocolId;

    @ProtocolNode(id = "checksum", name = "校验和", 
                  valueType = ValueType.HEX, length = 16, 
                  fwdExpr = "crc16Between(#version,#data_field)")
    private String checksum;

    @ProtocolNode(id = "version", name = "协议版本", 
                  valueType = ValueType.UINT, length = 8, value = "1")
    private String version;

    @ProtocolNode(id = "data_length", name = "数据长度", 
                  valueType = ValueType.UINT, length = 16, 
                  fwdExpr = "nodeLength(#data_field)")
    private String dataLength;

    @ProtocolNode(id = "data_field", name = "数据域", 
                  valueType = ValueType.HEX, length = 32, value = "0xDEADBEEF")
    private String dataField;
}
```

### 依赖关系分析

对于表达式 `crc16Between(#version,#data_field)`：

**修复前**：
- 校验和节点只依赖：协议版本、数据域
- 处理顺序：协议标识 → 协议版本 → 数据域 → 校验和 → 数据长度

**修复后**：
- 校验和节点依赖：协议版本、数据域（以及它们之间的所有节点）
- 处理顺序：协议标识 → 协议版本 → 数据域 → 校验和 → 数据长度

## 支持的范围函数

### 预注册函数

系统在启动时自动注册以下常用函数：

| 函数名 | 描述 | 类型 | 示例 |
|--------|------|------|------|
| `crc16Between` | 计算两个节点之间数据的CRC16值 | 范围函数 | `crc16Between(#start,#end)` |
| `crc16Of` | 计算单个节点的CRC16值 | 普通函数 | `crc16Of(#node)` |
| `crc32Between` | 计算两个节点之间数据的CRC32值 | 范围函数 | `crc32Between(#start,#end)` |
| `crc32Of` | 计算单个节点的CRC32值 | 普通函数 | `crc32Of(#node)` |
| `md5Between` | 计算两个节点之间数据的MD5值 | 范围函数 | `md5Between(#start,#end)` |
| `sha1Between` | 计算两个节点之间数据的SHA1值 | 范围函数 | `sha1Between(#start,#end)` |
| `sha256Between` | 计算两个节点之间数据的SHA256值 | 范围函数 | `sha256Between(#start,#end)` |
| `sumBetween` | 计算两个节点之间数据的和 | 范围函数 | `sumBetween(#start,#end)` |
| `lengthBetween` | 计算两个节点之间数据的长度 | 范围函数 | `lengthBetween(#start,#end)` |
| `nodeLengthBetween` | 计算两个节点之间节点的长度 | 范围函数 | `nodeLengthBetween(#start,#end)` |
| `length` | 计算单个节点的长度 | 普通函数 | `length(#node)` |
| `nodeLength` | 计算单个节点的长度 | 普通函数 | `nodeLength(#node)` |
| `sum` | 计算单个节点的值 | 普通函数 | `sum(#node)` |

### 动态注册

支持运行时动态注册新的函数：

```java
// 注册新的范围函数
RangeFunctionRegistry.registerRangeFunction("customRangeFunc", "自定义范围函数", true);

// 注册新的普通函数
RangeFunctionRegistry.registerFunction("customFunc", "自定义普通函数");

// 批量注册
String[][] functions = {
    {"func1", "函数1", "true"},
    {"func2", "函数2", "false"}
};
FunctionAutoRegistrar.registerFunctions(functions);
```

## 测试验证

### 1. 范围函数注册器测试

运行 `RangeFunctionRegistryTest` 的 main 方法可以验证注册机制：

```bash
cd src/main/java/com/iecas/cmd/test
javac RangeFunctionRegistryTest.java
java RangeFunctionRegistryTest
```

预期输出：
```
=== 范围函数注册器测试 ===

1. 初始状态:
总函数数: 15, 范围函数: 9, 普通函数: 6

2. 函数查询测试:
范围函数:
  - crc16Between(2) - 计算两个节点之间数据的CRC16值
  - crc32Between(2) - 计算两个节点之间数据的CRC32值
  ...

3. 动态注册测试:
注册自定义范围函数: customRangeFunc
注册自定义普通函数: customFunc
验证 customRangeFunc 是否为范围函数: true
验证 customFunc 是否为范围函数: false

4. 自动注册测试:
自动注册完成

5. 最终状态:
总函数数: 17, 范围函数: 10, 普通函数: 7
```

### 2. 协议依赖关系测试

运行 `SimpleLayerProtocol` 的 main 方法可以验证修复效果：

```bash
cd src/main/java/com/iecas/cmd/test
javac SimpleLayerProtocol.java
java SimpleLayerProtocol
```

预期输出：
```
协议编码成功！
编码结果长度: 11 字节
编码结果: 12 34 7F 0A 01 00 04 DE AD BE EF

=== 依赖关系验证 ===
校验和节点应该依赖: 协议版本, 数据域
数据长度节点应该依赖: 数据域
处理顺序应该是: 协议标识 -> 协议版本 -> 数据域 -> 校验和 -> 数据长度
```

## 注意事项

1. **节点顺序**：范围函数依赖的建立依赖于节点在协议中的自然顺序
2. **自依赖检查**：避免节点依赖自身，防止循环依赖
3. **索引边界**：确保起始和结束索引在有效范围内
4. **性能考虑**：范围函数会增加依赖关系的复杂度，但确保了计算的正确性

## 使用说明

### 1. 注册新的范围函数

#### 方式一：使用注解（推荐）

```java
@FunctionAutoRegistrar.RangeFunction(
    name = "myRangeFunc",
    description = "我的自定义范围函数"
)
public class MyRangeFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "myRangeFunc";
    }
    
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject start, AviatorObject end) {
        // 函数实现
        return new AviatorString("result");
    }
}
```

#### 方式二：手动注册

```java
// 在应用启动时注册
RangeFunctionRegistry.registerRangeFunction("myRangeFunc", "我的自定义范围函数", true);
```

### 2. 注册新的普通函数

```java
@FunctionAutoRegistrar.NormalFunction(
    name = "myFunc",
    description = "我的自定义函数"
)
public class MyFunction extends AbstractFunction {
    // 函数实现
}
```

### 3. 查询函数信息

```java
// 检查函数是否为范围函数
boolean isRange = RangeFunctionRegistry.isRangeFunction("crc16Between");

// 获取函数详细信息
RangeFunctionRegistry.RangeFunctionInfo info = RangeFunctionRegistry.getFunctionInfo("crc16Between");
System.out.println("函数名: " + info.getFunctionName());
System.out.println("描述: " + info.getDescription());
System.out.println("是否范围函数: " + info.isRangeFunction());
```

### 4. 动态管理函数

```java
// 移除函数
boolean removed = RangeFunctionRegistry.unregisterFunction("myFunc");

// 清空所有函数
RangeFunctionRegistry.clear();

// 获取统计信息
String stats = RangeFunctionRegistry.getStatistics();
System.out.println(stats);
```

## 未来扩展

可以考虑添加以下功能：

1. **动态范围**：支持基于表达式的动态范围计算
2. **条件范围**：支持条件性的范围依赖
3. **范围验证**：验证范围函数的参数有效性
4. **性能优化**：优化大量范围函数的依赖构建性能
5. **函数版本管理**：支持函数的版本控制和升级
6. **函数依赖分析**：分析函数之间的依赖关系
7. **函数性能监控**：监控函数的执行性能
8. **函数热更新**：支持运行时动态更新函数实现
