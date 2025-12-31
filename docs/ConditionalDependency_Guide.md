# 条件依赖功能使用指南

## 概述

条件依赖功能允许协议节点根据其他节点的值来决定自身是否参与编解码过程。这是一个强大的功能，可以实现动态协议结构，根据不同的条件启用或禁用特定的节点。

## 核心概念

### 条件依赖关系
- **条件节点**: 提供条件值的节点
- **目标节点**: 根据条件决定是否启用的节点
- **条件表达式**: 使用AviatorScript语法的布尔表达式
- **动作**: 条件匹配或不匹配时执行的操作

### 支持的动作类型
1. **ENABLE**: 启用节点，节点参与编解码过程
2. **DISABLE**: 禁用节点，节点不参与编解码过程
3. **SET_DEFAULT**: 启用节点并设置默认值
4. **CLEAR_VALUE**: 启用节点但清空其值

## XML配置方式

### 基本语法
```xml
<iNode id="target_node" name="目标节点" length="16" valueType="HEX" value="0x1234">
    <conditionalDependency 
        conditionNode="control_flag" 
        condition="value == 1" 
        action="ENABLE" 
        elseAction="DISABLE" 
        priority="1"
        description="当控制位为1时启用"/>
</iNode>
```

### 完整示例
```xml
<?xml version="1.0" encoding="UTF-8"?>
<protocolDefinition id="CONDITIONAL_001" name="条件依赖示例协议">
    <protocolHeader id="protocolHeader" name="协议头">
        <!-- 控制位节点 -->
        <iNode id="control_flag" name="控制位" length="8" valueType="UINT" value="1">
            <enumRange value="0">禁用模式</enumRange>
            <enumRange value="1">启用模式</enumRange>
        </iNode>
        
        <!-- 模式选择节点 -->
        <iNode id="mode_select" name="模式选择" length="8" valueType="UINT" value="2">
            <enumRange value="1">简单模式</enumRange>
            <enumRange value="2">复杂模式</enumRange>
        </iNode>
    </protocolHeader>
    
    <protocolBody id="protocolBody" name="协议体">
        <!-- 基础数据 - 总是存在 -->
        <iNode id="basic_data" name="基础数据" length="16" valueType="UINT" value="1024"/>
        
        <!-- 可选数据 - 仅当控制位为1时存在 -->
        <iNode id="optional_data" name="可选数据" length="16" valueType="HEX" value="0xAABB">
            <conditionalDependency conditionNode="control_flag" condition="value == 1" 
                                  action="ENABLE" elseAction="DISABLE" priority="1"
                                  description="当控制位为1时启用"/>
        </iNode>
        
        <!-- 复合条件数据 - 当控制位为1且模式为2时存在 -->
        <iNode id="complex_data" name="复合条件数据" length="32" valueType="FLOAT" value="3.14">
            <conditionalDependency conditionNode="control_flag" condition="value == 1" 
                                  action="ENABLE" elseAction="DISABLE" priority="1"
                                  description="控制位必须为1"/>
            <conditionalDependency conditionNode="mode_select" condition="value == 2" 
                                  action="ENABLE" elseAction="DISABLE" priority="2"
                                  description="模式必须为2"/>
        </iNode>
    </protocolBody>
</protocolDefinition>
```

## Java类配置方式

### 基本语法
```java
@ProtocolField(name = "目标节点", length = 16, valueType = ValueType.HEX)
@ConditionalOn(
    conditionNode = "control_flag", 
    condition = "value == 1",
    action = ConditionalAction.ENABLE,
    elseAction = ConditionalAction.DISABLE,
    priority = 1,
    description = "当控制位为1时启用"
)
private String targetNode = "0x1234";
```

### 完整示例
```java
@ProtocolDefinition(name = "条件依赖示例协议")
public class ConditionalProtocol {

    @ProtocolHeader(order = 1)
    public static class Header {
        
        @ProtocolField(id = "control_flag", name = "控制位", length = 8, valueType = ValueType.UINT)
        @ProtocolEnum(values = {"0:禁用模式", "1:启用模式"})
        private int controlFlag = 1;
        
        @ProtocolField(id = "mode_select", name = "模式选择", length = 8, valueType = ValueType.UINT)
        @ProtocolEnum(values = {"1:简单模式", "2:复杂模式"})
        private int modeSelect = 2;
    }

    @ProtocolBody(order = 2)
    public static class Body {
        
        // 基础数据 - 总是存在
        @ProtocolField(name = "基础数据", length = 16, valueType = ValueType.UINT)
        private int basicData = 1024;
        
        // 可选数据 - 仅当控制位为1时存在
        @ProtocolField(name = "可选数据", length = 16, valueType = ValueType.HEX)
        @ConditionalOn(
            conditionNode = "control_flag", 
            condition = "value == 1",
            description = "当控制位为1时启用"
        )
        private String optionalData = "0xAABB";
        
        // 复合条件数据 - 多个条件依赖
        @ProtocolField(name = "复合条件数据", length = 32, valueType = ValueType.FLOAT)
        @ConditionalOn(
            conditionNode = "control_flag", 
            condition = "value == 1",
            priority = 1,
            description = "控制位必须为1"
        )
        @ConditionalOn(
            conditionNode = "mode_select", 
            condition = "value == 2",
            priority = 2,
            description = "模式必须为2"
        )
        private float complexData = 3.14f;
    }
}
```

## 条件表达式语法

### 基本比较
```javascript
value == 1          // 等于
value != 0          // 不等于
value > 10          // 大于
value >= 5          // 大于等于
value < 100         // 小于
value <= 50         // 小于等于
```

### 字符串比较
```javascript
value == "0x01"     // 字符串相等
value != "disabled" // 字符串不等
```

### 范围检查
```javascript
value in [1, 2, 3]          // 值在列表中
value >= 1 && value <= 10   // 值在范围内
```

### 复合条件
```javascript
value == 1 && iNode.length > 0           // 逻辑与
value == 0 || value == 255              // 逻辑或
!(value == 0)                           // 逻辑非
value != null && value > 0              // 空值检查
```

### 高级表达式
```javascript
value % 2 == 0                          // 偶数检查
string.contains(value, "test")          // 字符串包含
math.abs(value) > 10                    // 绝对值
```

## 节点引用方式

### 支持的引用格式
1. **字段名称**: `"controlFlag"` - 引用同级字段
2. **节点ID**: `"#control_flag"` - 引用指定ID的节点
3. **路径引用**: `"protocolHeader.controlFlag"` - 引用特定路径的节点

### 引用示例
```java
// 引用同级字段
@ConditionalOn(conditionNode = "controlFlag", condition = "value == 1")

// 引用指定ID的节点
@ConditionalOn(conditionNode = "#control_flag", condition = "value == 1")

// 引用父级字段（如果支持）
@ConditionalOn(conditionNode = "protocolHeader.controlFlag", condition = "value == 1")
```

## 优先级和执行顺序

### 优先级规则
- 数字越小，优先级越高
- 相同优先级按定义顺序执行
- 一旦节点被禁用，后续条件不再执行

### 示例
```java
@ConditionalOn(conditionNode = "flag1", condition = "value == 1", priority = 1)  // 先执行
@ConditionalOn(conditionNode = "flag2", condition = "value == 1", priority = 2)  // 后执行
```

## 最佳实践

### 1. 命名规范
- 使用有意义的节点ID和名称
- 条件描述要清晰明确
- 避免循环依赖

### 2. 性能考虑
- 简单条件优先于复杂条件
- 避免过深的依赖链
- 合理设置优先级

### 3. 调试技巧
- 使用描述字段记录条件逻辑
- 启用详细日志查看条件评估过程
- 测试各种条件组合

### 4. 错误处理
- 确保条件节点存在
- 验证表达式语法正确性
- 处理空值和异常情况

## 使用场景

### 1. 协议版本兼容
```java
@ConditionalOn(conditionNode = "version", condition = "value >= 2")
private String newFeatureData;
```

### 2. 可选功能模块
```java
@ConditionalOn(conditionNode = "featureFlag", condition = "value == 1")
private String optionalModule;
```

### 3. 数据格式切换
```java
@ConditionalOn(conditionNode = "dataFormat", condition = "value == 'binary'")
private byte[] binaryData;

@ConditionalOn(conditionNode = "dataFormat", condition = "value == 'text'")
private String textData;
```

### 4. 错误处理模式
```java
@ConditionalOn(conditionNode = "errorMode", condition = "value == 'detailed'")
private String detailedErrorInfo;
```

## 注意事项

1. **避免循环依赖**: 确保条件依赖不形成循环
2. **性能影响**: 复杂条件表达式可能影响编解码性能
3. **调试复杂性**: 多层条件依赖增加调试难度
4. **向后兼容**: 添加条件依赖时考虑现有协议的兼容性
5. **文档维护**: 及时更新协议文档，说明条件依赖逻辑

## 故障排除

### 常见问题
1. **条件节点未找到**: 检查节点ID和引用路径
2. **表达式语法错误**: 验证AviatorScript语法
3. **条件永远不满足**: 检查条件逻辑和节点值
4. **性能问题**: 简化复杂表达式，优化条件顺序

### 调试方法
1. 启用详细日志
2. 单步测试条件表达式
3. 验证节点值和类型
4. 检查依赖关系图 