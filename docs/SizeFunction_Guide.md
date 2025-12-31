# Size函数使用指南

## 概述

`size()` 函数是协议编解码框架中新增的一个表达式函数，用于动态获取节点组的大小，替代了原来硬编码的 `repeat` 属性配置。

## 问题背景

在之前的版本中，`@ProtocolNodeGroup` 注解需要通过 `repeat` 属性来指定节点组的重复次数：

```java
@ProtocolNodeGroup(
    id = "param-group",
    name = "参数组",
    repeat = 2,  // 硬编码，不够灵活
    elementType = GroupElementType.CUSTOM_OBJECT,
    resolveStrategy = GroupResolveStrategy.FLATTEN
)
private List<ParamGroupItem> group;
```

这种方式存在以下问题：
1. **硬编码问题**：`repeat = 2` 是固定的，无法动态适应实际数据
2. **数据不一致**：如果实际 List 中有 3 个元素，但 `repeat = 2`，会导致数据丢失
3. **维护困难**：每次数据长度变化都需要修改代码

## 解决方案

### 1. 移除硬编码的 repeat 配置

```java
@ProtocolNodeGroup(
    id = "param-group",
    name = "参数组",
    elementType = GroupElementType.CUSTOM_OBJECT,
    resolveStrategy = GroupResolveStrategy.FLATTEN
)
private List<ParamGroupItem> group;  // 必须是集合类型
```

### 2. 使用 size() 函数动态获取大小

```java
@ProtocolNode(id = "body-group-count", name = "组数量", length = 16, 
             fwdExpr = "size(#param-group)", order = 2)
private Integer groupCount;
```

### 3. 确保字段类型正确

**重要**：使用 `@ProtocolNodeGroup` 注解的字段必须是集合类型，否则会抛出异常：

```java
// ✅ 正确 - 集合类型
private List<ParamGroupItem> group;
private Set<String> items;
private Collection<Data> dataList;

// ❌ 错误 - 非集合类型
private String group;        // 会抛出异常
private int count;           // 会抛出异常
private ParamGroupItem item; // 会抛出异常
```

## Size函数用法

### 基本语法

```java
size(#node-group-id)     // 获取指定节点组的大小
size('node-group-id')    // 字符串形式引用
size(collection)         // 直接传入集合对象
```

### 支持的类型

- **集合类型**：`List`, `Set`, `Collection` 等
- **数组类型**：各种基本类型和对象数组
- **字符串**：获取字符串长度
- **节点组引用**：通过 ID 引用获取大小

### 使用场景

#### 1. 动态计算节点组大小

```java
@ProtocolNode(id = "group-size", name = "组大小", length = 16, 
             fwdExpr = "size(#param-group)", order = 1)
private Integer groupSize;
```

#### 2. 条件判断

```java
@ProtocolNode(id = "is-valid", name = "是否有效", length = 8, 
             fwdExpr = "size(#param-group) > 0", order = 2)
private Boolean isValid;
```

#### 3. 计算校验和

```java
@ProtocolNode(id = "checksum", name = "校验和", length = 16, 
             fwdExpr = "crc16(#param-group)", order = 3)
private String checksum;
```

#### 4. 动态长度计算

```java
// 数据组长度会根据实际数据自动确定
@ProtocolNodeGroup(
    id = "data-group",
    name = "数据组",
    elementType = GroupElementType.CUSTOM_OBJECT
)
private List<DataItem> dataGroup;
```

## 优先级说明

系统现在完全依赖自动检测来确定节点组的重复次数：

1. **自动检测**：根据实际数据长度自动确定
2. **异常处理**：如果无法检测，抛出异常

## 完整示例

```java
@ProtocolBody(id = "example-body", name = "示例协议体")
public static class ExampleBody {

    @ProtocolNode(id = "body-type", name = "类型", length = 8, 
                 valueType = ValueType.HEX, order = 1)
    private String type;

    @ProtocolNode(id = "body-group-count", name = "组数量", length = 16, 
                 fwdExpr = "size(#param-group)", order = 2)
    private Integer groupCount;

    // 节点组 - 不设置 repeat，让系统自动检测长度
    @ProtocolNodeGroup(
            id = "param-group",
            name = "参数组",
            resolveStrategy = GroupResolveStrategy.FLATTEN,
            order = 3
    )
    private List<ParamItem> paramGroup;

    // 另一个节点组 - 长度自动检测
    @ProtocolNodeGroup(
            id = "data-group",
            name = "数据组",
            resolveStrategy = GroupResolveStrategy.FLATTEN,
            order = 4
    )
    private List<DataItem> dataGroup;
}
```

## 迁移指南

### 从旧版本迁移

1. **移除硬编码的 repeat 属性**
   ```java
   // 旧版本
   @ProtocolNodeGroup(repeat = 2, ...)
   
   // 新版本
   @ProtocolNodeGroup(...)  // 移除 repeat
   ```

2. **使用 size() 函数替代**
   ```java
   // 旧版本：硬编码长度
   @ProtocolNode(length = 16, ...)
   private Integer groupCount;
   
   // 新版本：动态计算长度
   @ProtocolNode(fwdExpr = "size(#param-group)", ...)
   private Integer groupCount;
   ```

3. **确保字段类型正确**
   ```java
   // ✅ 正确 - 必须是集合类型
   private List<ParamGroupItem> group;
   private Set<String> items;
   
   // ❌ 错误 - 非集合类型会抛出异常
   private String group;
   private int count;
   ```

4. **系统自动检测长度**
   ```java
   // 系统会根据实际数据长度自动确定
   @ProtocolNodeGroup(
       // 无需配置，系统自动检测
       ...
   )
   ```

## 注意事项

1. **性能考虑**：`size()` 函数会在运行时计算，对于大型集合可能有轻微性能影响
2. **依赖关系**：使用 `size()` 函数的节点会依赖于被引用的节点组，确保依赖关系正确
3. **错误处理**：如果节点组不存在或为空，`size()` 函数会返回 0
4. **表达式语法**：支持 Aviator 表达式的所有语法特性
5. **类型要求**：节点组字段必须是集合类型（List、Set等），否则会抛出异常
6. **构建顺序**：系统会先构建所有节点和依赖关系，再验证表达式，确保引用节点存在
7. **节点组完整性**：节点组容器及其所有子节点都会被正确注册到依赖图中，确保协议树形结构完整
8. **自动元素类型检测**：系统能够自动检测节点组内元素类型，无需手动配置 elementType 属性

## 总结

通过引入 `size()` 函数和自动检测机制，我们实现了：

1. **动态长度**：节点组长度不再需要硬编码，可以根据实际数据动态确定
2. **智能检测**：系统能够自动检测实际数据长度，无需手动配置
3. **严格验证**：确保节点组字段类型正确，避免运行时错误
4. **极简配置**：移除了所有重复次数相关的配置，配置更加简洁

这种方式让协议定义更加灵活和智能，能够更好地适应实际的数据结构和业务需求。
