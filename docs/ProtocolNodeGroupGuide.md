# 协议节点组（@ProtocolNodeGroup）功能指南

## 概述

`@ProtocolNodeGroup` 是一个强大的注解，用于支持多组多层协议嵌套。它允许你将任意类型的对象列表作为协议组，支持重复、嵌套和多种解析策略。

## 核心特性

- **多组支持**：支持固定次数或表达式计算的重复
- **多层嵌套**：支持复杂的协议嵌套结构
- **类型灵活**：支持Node、协议对象、自定义对象等任意类型
- **策略可选**：提供扁平化、分组保持、混合等多种解析策略
- **自动区分**：自动为每组元素生成唯一的ID和名称

## 注解参数

### 基本参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `id` | String | "" | 节点组ID，为空时使用字段名 |
| `name` | String | "" | 节点组名称，为空时使用字段名 |
| `description` | String | "" | 节点组描述 |
| `maxSize` | int | 0 | 最大容量，0表示无限制 |
| `minSize` | int | 0 | 最小容量 |
| `optional` | boolean | false | 是否可选 |
| `order` | int | 0 | 字段顺序 |

### 重复控制参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `repeat` | int | 1 | 固定重复次数 |
| `repeatExpr` | String | "" | 重复次数表达式（优先级高于repeat） |
| `idSuffixPattern` | String | "_%d" | ID后缀格式 |
| `nameSuffixPattern` | String | "[%d]" | 名称后缀格式 |

### 解析控制参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `elementType` | GroupElementType | AUTO | 组内元素类型 |
| `recursive` | boolean | true | 是否递归解析 |
| `resolveStrategy` | GroupResolveStrategy | FLATTEN | 解析策略 |

## 元素类型（GroupElementType）

### AUTO（自动检测）
根据字段的泛型类型自动选择最佳解析策略。

### NODE（协议节点）
直接作为Node处理，适用于`List<Node>`类型。

### PROTOCOL_OBJECT（协议对象）
包含@Protocol相关注解的对象，会递归解析其协议结构。

### CUSTOM_OBJECT（自定义对象）
需要特殊解析逻辑的对象，默认按协议对象处理。

## 解析策略（GroupResolveStrategy）

### FLATTEN（扁平化）
将所有解析结果平铺到同一层级，适用于简单节点组。

### GROUP_CONTAINER（分组保持）
保持组的结构，每组作为一个容器，适用于复杂协议组。

### MIXED（混合模式）
根据对象类型自动选择最佳策略。

## 使用示例

### 1. 简单节点组

```java
@ProtocolNodeGroup(
    id = "data_nodes",
    name = "数据节点组",
    repeat = 3,
    elementType = GroupElementType.NODE,
    resolveStrategy = GroupResolveStrategy.FLATTEN
)
private List<Node> dataNodes;
```

**效果**：将`dataNodes`重复3次，所有节点平铺在同一层级，ID和名称自动添加后缀。

### 2. 协议对象组

```java
@ProtocolNodeGroup(
    id = "sensor_groups",
    name = "传感器组",
    repeat = 2,
    elementType = GroupElementType.PROTOCOL_OBJECT,
    resolveStrategy = GroupResolveStrategy.GROUP_CONTAINER,
    idSuffixPattern = "_%02d",
    nameSuffixPattern = "[第%d组]"
)
private List<SensorProtocol> sensorGroups;
```

**效果**：将`SensorProtocol`对象重复2次，每组保持独立结构，ID格式为`sensor_groups_01`、`sensor_groups_02`。

### 3. 表达式控制重复

```java
@ProtocolNodeGroup(
    id = "dynamic_blocks",
    name = "动态数据块",
    repeatExpr = "#block_count + 1",
    elementType = GroupElementType.CUSTOM_OBJECT
)
private List<CustomDataBlock> customBlocks;
```

**效果**：根据表达式`#block_count + 1`动态计算重复次数。

### 4. 嵌套协议组

```java
@ProtocolNodeGroup(
    id = "nested_protocols",
    name = "嵌套协议组",
    repeat = 2,
    elementType = GroupElementType.PROTOCOL_OBJECT,
    resolveStrategy = GroupResolveStrategy.GROUP_CONTAINER
)
private List<NestedProtocol> nestedProtocols;
```

**效果**：支持多层协议嵌套，每个`NestedProtocol`内部可以包含header、body、tail等结构。

## 后缀格式说明

### ID后缀格式
- 默认：`_%d` → `sensor_1`, `sensor_2`
- 自定义：`_%02d` → `sensor_01`, `sensor_02`
- 自定义：`_group%d` → `sensor_group1`, `sensor_group2`

### 名称后缀格式
- 默认：`[%d]` → `传感器[1]`, `传感器[2]`
- 自定义：`(第%d组)` → `传感器(第1组)`, `传感器(第2组)`

## 解析流程

1. **字段扫描**：发现@ProtocolNodeGroup注解
2. **重复计算**：根据repeat/repeatExpr计算重复次数
3. **类型检测**：确定组内元素类型
4. **策略选择**：根据elementType和resolveStrategy选择解析方式
5. **元素解析**：递归解析组内每个对象
6. **后缀应用**：为每组元素应用唯一的ID/名称后缀
7. **结构组装**：根据策略组装最终的协议结构

## 注意事项

1. **类型安全**：确保字段类型与elementType匹配
2. **循环依赖**：避免在协议对象中创建循环引用
3. **性能考虑**：大量重复时注意内存使用
4. **表达式安全**：repeatExpr应避免复杂计算，建议使用简单表达式

## 最佳实践

1. **命名规范**：使用清晰的ID和名称，便于调试和维护
2. **策略选择**：简单节点组使用FLATTEN，复杂协议组使用GROUP_CONTAINER
3. **后缀设计**：设计有意义的后缀格式，便于识别和管理
4. **文档注释**：为复杂的组配置添加详细注释

## 扩展性

该功能设计具有良好的扩展性：
- 可以轻松添加新的元素类型
- 可以扩展新的解析策略
- 支持自定义后缀格式
- 支持表达式计算重复次数

通过合理使用`@ProtocolNodeGroup`，你可以构建复杂而灵活的协议结构，满足各种业务需求。 