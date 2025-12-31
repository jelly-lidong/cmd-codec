# XML协议配置手册

## 目录

1. [概述](#概述)
2. [基本概念](#基本概念)
3. [协议结构](#协议结构)
4. [节点配置](#节点配置)
5. [数据类型](#数据类型)
6. [表达式系统](#表达式系统)
7. [条件依赖](#条件依赖)
8. [填充功能](#填充功能)
9. [枚举配置](#枚举配置)
10. [最佳实践](#最佳实践)
11. [常见问题](#常见问题)
12. [故障排除](#故障排除)

## 概述

XML协议配置是本框架的核心功能之一，它允许用户通过声明式的XML文件定义复杂的协议结构，而无需编写代码。这种设计的优势在于：

### 设计理念

**为什么选择XML配置？**

1. **声明式编程** - 用户只需描述"是什么"，而不需要关心"怎么做"
2. **可视化结构** - XML的层次结构直观地反映了协议的嵌套关系
3. **工具支持** - 丰富的XML编辑器和验证工具
4. **版本控制友好** - 文本格式便于版本管理和差异比较
5. **跨平台兼容** - 标准化的格式，不依赖特定的开发环境

**解决的问题**

- **协议多样性** - 支持各种复杂的协议格式
- **动态配置** - 运行时加载和解析协议定义
- **维护成本** - 减少硬编码，提高可维护性
- **团队协作** - 非程序员也能参与协议定义

## 基本概念

### 协议层次结构

协议采用树形结构组织，每个层级都有特定的作用：

```
Protocol (协议根节点)
├── Header (协议头)
│   ├── Node (字段节点)
│   └── Node (字段节点)
├── Body (协议体)
│   ├── Node (字段节点)
│   ├── Body (嵌套体)
│   └── Node (字段节点)
└── Check (校验部分)
    ├── Node (字段节点)
    └── Node (字段节点)
```

**为什么这样设计？**

1. **符合协议惯例** - 大多数协议都有头部、数据体、校验的结构
2. **逻辑清晰** - 不同部分承担不同职责
3. **便于处理** - 可以针对不同部分采用不同的处理策略
4. **扩展性好** - 支持嵌套和复杂结构

### 核心元素

#### 1. Protocol（协议）
协议的根元素，定义整个协议的基本属性。

```xml
<protocolDefinition id="example_protocol" name="示例协议" length="800" valueType="HEX">
    <description>协议描述信息</description>
    <!-- 协议内容 -->
</protocolDefinition>
```

**属性说明**：
- `id`: 协议唯一标识符，用于引用和查找
- `name`: 协议显示名称，用于日志和调试
- `length`: 协议总长度（位数），用于验证和内存分配
- `valueType`: 默认值类型，子节点可以继承

#### 2. Header（协议头）
协议头部分，通常包含版本、类型、长度等元信息。

```xml
<protocolHeader id="protocol_header" name="协议头" length="64" valueType="HEX">
    <!-- 头部字段 -->
</protocolHeader>
```

**设计考虑**：
- 头部信息通常是固定格式的
- 包含协议识别和解析所需的关键信息
- 长度相对固定，便于快速解析

#### 3. Body（协议体）
协议的主要数据部分，可以嵌套。

```xml
<protocolBody id="protocol_body" name="协议体" length="640" valueType="HEX">
    <iNode id="command" name="命令字" length="16" valueType="HEX" value="0x1001"/>
    <iNode id="data" name="数据内容" length="624" valueType="HEX" value="0x..."/>
</protocolBody>
```

**支持嵌套的原因**：
- 复杂协议可能有多层数据结构
- 不同层级可能有不同的处理逻辑
- 便于模块化和重用

#### 4. Check（校验部分）
协议的校验和验证部分。

```xml
<protocolTail id="protocol_check" name="校验部分" length="96" valueType="HEX">
    <!-- 校验字段 -->
</protocolTail>
```

#### 5. Node（字段节点）
协议中的具体数据字段。

```xml
<iNode id="field_id" name="字段名称" length="32" valueType="INT" value="12345">
    <description>字段描述</description>
</iNode>
```

## 协议结构

### 基本协议模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<protocolDefinition id="basic_protocol" name="基础协议" length="800" valueType="HEX">
    <description>基础协议模板，展示标准结构</description>
    
    <!-- 协议头部 - 固定64位 -->
    <protocolHeader id="protocolHeader" name="协议头" length="64" valueType="HEX">
        <iNode id="sync_word" name="同步字" length="16" valueType="HEX" value="0xAA55"/>
        <iNode id="version" name="版本号" length="8" valueType="INT" value="1"/>
        <iNode id="type" name="消息类型" length="8" valueType="INT" value="1"/>
        <iNode id="length" name="数据长度" length="16" valueType="INT" value="80"/>
        <iNode id="sequence" name="序列号" length="16" valueType="INT" value="1"/>
    </protocolHeader>
    
    <!-- 协议体 - 可变长度 -->
    <protocolBody id="protocolBody" name="协议体" length="640" valueType="HEX">
        <iNode id="command" name="命令字" length="16" valueType="HEX" value="0x1001"/>
        <iNode id="data" name="数据内容" length="624" valueType="HEX" value="0x..."/>
    </protocolBody>
    
    <!-- 校验部分 - 固定96位 -->
    <protocolTail id="checksum" name="校验和" length="96" valueType="HEX">
        <iNode id="crc16" name="CRC16校验" length="16" valueType="HEX" value="0x1234"/>
        <iNode id="reserved" name="保留字段" length="64" valueType="HEX" value="0x0"/>
        <iNode id="end_flag" name="结束标志" length="16" valueType="HEX" value="0x55AA"/>
    </protocolTail>
</protocolDefinition>
```

### 复杂嵌套结构

```xml
<protocolDefinition id="complex_protocol" name="复杂协议" length="1600" valueType="HEX">
    <description>展示复杂嵌套结构的协议</description>
    
    <protocolHeader id="main_header" name="主头部" length="128" valueType="HEX">
        <!-- 主头部字段 -->
    </protocolHeader>
    
    <protocolBody id="main_body" name="主体" length="1344" valueType="HEX">
        
        <!-- 子协议1 -->
        <protocolBody id="sub_protocol1" name="子协议1" length="640" valueType="HEX">
            <protocolHeader id="sub_header1" name="子头部1" length="64" valueType="HEX">
                <!-- 子头部字段 -->
            </protocolHeader>
            <protocolBody id="sub_body1" name="子体1" length="576" valueType="HEX">
                <!-- 子体字段 -->
            </protocolBody>
        </protocolBody>
        
        <!-- 子协议2 -->
        <protocolBody id="sub_protocol2" name="子协议2" length="704" valueType="HEX">
            <protocolHeader id="sub_header2" name="子头部2" length="96" valueType="HEX">
                <!-- 子头部字段 -->
            </protocolHeader>
            <protocolBody id="sub_body2" name="子体2" length="608" valueType="HEX">
                <!-- 子体字段 -->
            </protocolBody>
        </protocolBody>
        
    </protocolBody>
    
    <protocolTail id="main_check" name="主校验" length="128" valueType="HEX">
        <!-- 主校验字段 -->
    </protocolTail>
</protocolDefinition>
```

**嵌套结构的处理方式**：

1. **递归解析** - 解析器递归处理每个层级
2. **上下文传递** - 父级上下文传递给子级
3. **独立验证** - 每个层级独立进行验证
4. **统一编解码** - 统一的编解码流程处理所有层级

### 长度计算规则

**为什么需要明确长度？**

1. **内存分配** - 预先分配合适的缓冲区
2. **解析验证** - 确保数据完整性
3. **性能优化** - 避免动态扩容
4. **错误检测** - 及早发现长度不匹配问题

**长度计算原则**：

```xml
<!-- 父节点长度 = 所有子节点长度之和 -->
<protocolBody id="container" name="容器" length="96" valueType="HEX">
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>  <!-- 32位 -->
    <iNode id="field2" name="字段2" length="64" valueType="HEX"/>  <!-- 64位 -->
    <!-- 总计：32 + 64 = 96位 ✓ -->
</protocolBody>
```

**常见长度错误**：

```xml
<!-- ❌ 错误：子节点长度之和不等于父节点长度 -->
<protocolBody id="container" name="容器" length="100" valueType="HEX">
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>  <!-- 32位 -->
    <iNode id="field2" name="字段2" length="64" valueType="HEX"/>  <!-- 64位 -->
    <!-- 总计：32 + 64 = 96位 ≠ 100位 ❌ -->
</protocolBody>
```

### 标识符规则

**ID命名规范**：

1. **唯一性** - 在整个协议中必须唯一
2. **可读性** - 使用有意义的名称
3. **一致性** - 遵循统一的命名风格
4. **引用性** - 便于其他地方引用

```xml
<!-- ✅ 推荐的ID命名 -->
<iNode id="sync_word" name="同步字"/>
<iNode id="message_type" name="消息类型"/>
<iNode id="data_length" name="数据长度"/>

<!-- ❌ 不推荐的ID命名 -->
<iNode id="node1" name="同步字"/>        <!-- 无意义 -->
<iNode id="同步字" name="同步字"/>        <!-- 非ASCII -->
<iNode id="sync-word" name="同步字"/>     <!-- 包含特殊字符 -->
```

**引用方式**：

```xml
<!-- 通过ID引用其他节点 -->
<iNode id="data_length" name="数据长度" length="16" valueType="INT" value="100"/>

<!-- 在表达式中引用 -->
<iNode id="variable_data" name="可变数据" 
      length="data_length * 8" valueType="HEX"/>

<!-- 在条件依赖中引用 -->
<conditionalDependency conditionNode="data_length" 
                      condition="value > 0" 
                      action="ENABLE"/>
```

## 节点配置

### 节点基本属性

每个节点都有一组核心属性，这些属性定义了节点的基本特征：

```xml
<iNode id="example_node" 
      name="示例节点" 
      length="32" 
      valueType="INT" 
      value="12345"
      description="这是一个示例节点">
    <description>详细的节点描述信息</description>
</iNode>
```

**核心属性详解**：

| 属性 | 必需 | 说明 | 示例 |
|------|------|------|------|
| `id` | ✅ | 节点唯一标识符 | `sync_word`, `data_length` |
| `name` | ✅ | 节点显示名称 | `同步字`, `数据长度` |
| `length` | ✅ | 节点长度（位数） | `8`, `16`, `32`, `64` |
| `valueType` | ✅ | 数据类型 | `INT`, `HEX`, `STRING`, `FLOAT` |
| `value` | ❌ | 默认值 | `123`, `0xABCD`, `"hello"` |
| `description` | ❌ | 简短描述 | `协议版本号` |

**为什么需要这些属性？**

1. **id** - 用于节点引用和查找，必须在协议范围内唯一
2. **name** - 提供人类可读的标识，用于日志和调试
3. **length** - 确定节点在二进制数据中占用的位数
4. **valueType** - 指定数据的解释方式和编解码规则
5. **value** - 提供默认值，用于编码时的数据填充
6. **description** - 提供详细说明，便于维护和理解

### 节点类型分类

根据用途和特征，节点可以分为以下几类：

#### 1. 固定值节点
用于协议中的常量字段，如同步字、版本号等。

```xml
<!-- 同步字 - 固定为0xAA55 -->
<iNode id="sync_word" name="同步字" length="16" valueType="HEX" value="0xAA55">
    <description>协议同步标识，固定值0xAA55</description>
</iNode>

<!-- 协议版本 - 固定为1 -->
<iNode id="version" name="协议版本" length="8" valueType="INT" value="1">
    <description>协议版本号，当前版本为1</description>
</iNode>
```

**使用场景**：
- 协议标识符
- 版本信息
- 固定的标志位
- 保留字段的默认值

#### 2. 变量节点
用于存储实际的业务数据，值在运行时确定。

```xml
<!-- 数据长度 - 运行时计算 -->
<iNode id="data_length" name="数据长度" length="16" valueType="INT">
    <description>后续数据部分的字节长度</description>
</iNode>

<!-- 用户ID - 业务数据 -->
<iNode id="user_id" name="用户ID" length="32" valueType="INT">
    <description>用户唯一标识符</description>
</iNode>
```

**使用场景**：
- 业务数据字段
- 动态计算的长度字段
- 状态标志位
- 计数器字段

#### 3. 计算节点
值通过表达式计算得出的节点。

```xml
<!-- 总长度 = 头部长度 + 数据长度 -->
<iNode id="total_length" name="总长度" length="16" valueType="INT" 
      value="header_length + data_length">
    <description>协议总长度，由头部长度和数据长度相加得出</description>
</iNode>

<!-- 校验和 - 通过算法计算 -->
<iNode id="checksum" name="校验和" length="16" valueType="HEX" 
      value="crc16(protocolHeader + protocolBody)">
    <description>CRC16校验和</description>
</iNode>
```

**使用场景**：
- 长度字段的自动计算
- 校验和计算
- 复合字段的组合
- 条件值的设置

### 节点长度规范

**长度单位**：所有长度都以**位（bit）**为单位。

**常用长度对照**：

| 数据类型 | 常用长度（位） | 字节数 | 取值范围 |
|----------|----------------|--------|----------|
| 布尔值 | 1 | - | 0, 1 |
| 字节 | 8 | 1 | 0-255 |
| 短整型 | 16 | 2 | -32768 ~ 32767 |
| 整型 | 32 | 4 | -2^31 ~ 2^31-1 |
| 长整型 | 64 | 8 | -2^63 ~ 2^63-1 |

**长度设计原则**：

1. **对齐原则** - 优先使用8的倍数（字节对齐）
2. **够用原则** - 长度应该满足数据范围需求
3. **标准原则** - 遵循行业标准的字段长度
4. **扩展原则** - 为未来扩展预留适当空间

```xml
<!-- ✅ 推荐的长度设置 -->
<iNode id="status" name="状态" length="8" valueType="INT"/>      <!-- 1字节，0-255 -->
<iNode id="count" name="计数" length="16" valueType="INT"/>      <!-- 2字节，0-65535 -->
<iNode id="timestamp" name="时间戳" length="32" valueType="INT"/> <!-- 4字节，Unix时间戳 -->

<!-- ❌ 不推荐的长度设置 -->
<iNode id="status" name="状态" length="7" valueType="INT"/>      <!-- 非字节对齐 -->
<iNode id="count" name="计数" length="12" valueType="INT"/>      <!-- 跨字节边界 -->
```

### 节点值设置

#### 默认值语法

不同数据类型有不同的值表示方法：

```xml
<!-- 整数值 -->
<iNode id="int_field" name="整数字段" length="32" valueType="INT" value="12345"/>

<!-- 十六进制值 -->
<iNode id="hex_field" name="十六进制字段" length="16" valueType="HEX" value="0xABCD"/>
<iNode id="hex_field2" name="十六进制字段2" length="16" valueType="HEX" value="ABCD"/>

<!-- 字符串值 -->
<iNode id="str_field" name="字符串字段" length="64" valueType="STRING" value="Hello"/>

<!-- 浮点数值 -->
<iNode id="float_field" name="浮点数字段" length="32" valueType="FLOAT" value="3.14159"/>

<!-- 布尔值 -->
<iNode id="bool_field" name="布尔字段" length="1" valueType="BOOLEAN" value="true"/>
```

#### 表达式值

值可以通过表达式动态计算：

```xml
<!-- 引用其他节点的值 -->
<iNode id="data_length" name="数据长度" length="16" valueType="INT" value="100"/>
<iNode id="total_length" name="总长度" length="16" valueType="INT" 
      value="data_length + 20"/>

<!-- 使用算术运算 -->
<iNode id="buffer_size" name="缓冲区大小" length="16" valueType="INT" 
      value="data_length * 2 + 64"/>

<!-- 使用条件表达式 -->
<iNode id="flag" name="标志位" length="8" valueType="INT" 
      value="data_length > 100 ? 1 : 0"/>
```

## 数据类型

### 支持的数据类型

框架支持多种数据类型，每种类型都有特定的编解码规则：

#### 1. INT（整数类型）

**特点**：
- 有符号整数
- 支持负数
- 二进制补码表示

```xml
<iNode id="temperature" name="温度" length="16" valueType="INT" value="-25">
    <description>温度值，支持负数，单位：摄氏度</description>
</iNode>

<iNode id="counter" name="计数器" length="32" valueType="INT" value="1000000">
    <description>计数器值，支持大整数</description>
</iNode>
```

**取值范围**：
- 8位：-128 ~ 127
- 16位：-32,768 ~ 32,767
- 32位：-2,147,483,648 ~ 2,147,483,647
- 64位：-9,223,372,036,854,775,808 ~ 9,223,372,036,854,775,807

#### 2. UINT（无符号整数）

**特点**：
- 无符号整数
- 只支持非负数
- 可以表示更大的正数范围

```xml
<iNode id="file_size" name="文件大小" length="32" valueType="UINT" value="4294967295">
    <description>文件大小，字节数，最大4GB</description>
</iNode>

<iNode id="port" name="端口号" length="16" valueType="UINT" value="8080">
    <description>网络端口号，范围0-65535</description>
</iNode>
```

**取值范围**：
- 8位：0 ~ 255
- 16位：0 ~ 65,535
- 32位：0 ~ 4,294,967,295
- 64位：0 ~ 18,446,744,073,709,551,615

#### 3. HEX（十六进制类型）

**特点**：
- 以十六进制形式表示
- 通常用于二进制数据
- 支持0x前缀或直接十六进制

```xml
<iNode id="magic_number" name="魔数" length="32" valueType="HEX" value="0xDEADBEEF">
    <description>协议魔数，用于协议识别</description>
</iNode>

<iNode id="flags" name="标志位" length="8" valueType="HEX" value="A5">
    <description>状态标志位，每位代表不同状态</description>
</iNode>
```

**值表示方法**：
```xml
<!-- 以下三种写法等价 -->
<iNode valueType="HEX" value="0xABCD"/>
<iNode valueType="HEX" value="ABCD"/>
<iNode valueType="HEX" value="abcd"/>  <!-- 大小写不敏感 -->
```

#### 4. STRING（字符串类型）

**特点**：
- UTF-8编码
- 支持中文和特殊字符
- 长度以位为单位（注意转换）

```xml
<iNode id="device_name" name="设备名称" length="128" valueType="STRING" value="传感器01">
    <description>设备名称，UTF-8编码，最大16字节</description>
</iNode>

<iNode id="version_info" name="版本信息" length="256" valueType="STRING" value="v1.0.0-beta">
    <description>版本信息字符串，最大32字节</description>
</iNode>
```

**长度计算**：
- ASCII字符：1字节 = 8位
- 中文字符：3字节 = 24位（UTF-8）
- 特殊字符：1-4字节不等

```xml
<!-- "Hello"需要5字节 = 40位 -->
<iNode id="greeting" name="问候语" length="40" valueType="STRING" value="Hello"/>

<!-- "你好"需要6字节 = 48位 -->
<iNode id="chinese_greeting" name="中文问候" length="48" valueType="STRING" value="你好"/>
```

#### 5. FLOAT（浮点数类型）

**特点**：
- IEEE 754标准
- 支持32位和64位精度
- 科学计数法表示

```xml
<iNode id="latitude" name="纬度" length="32" valueType="FLOAT" value="39.9042">
    <description>GPS纬度，单精度浮点数</description>
</iNode>

<iNode id="precise_value" name="精确值" length="64" valueType="FLOAT" value="3.141592653589793">
    <description>高精度数值，双精度浮点数</description>
</iNode>
```

**精度说明**：
- 32位（单精度）：约7位有效数字
- 64位（双精度）：约15位有效数字

#### 6. BOOLEAN（布尔类型）

**特点**：
- 只占用1位
- true/false或1/0表示

```xml
<iNode id="is_enabled" name="是否启用" length="1" valueType="BOOLEAN" value="true">
    <description>功能启用标志</description>
</iNode>

<iNode id="has_error" name="是否有错误" length="1" valueType="BOOLEAN" value="false">
    <description>错误状态标志</description>
</iNode>
```

**值表示方法**：
```xml
<!-- 以下写法都是有效的 -->
<iNode valueType="BOOLEAN" value="true"/>
<iNode valueType="BOOLEAN" value="false"/>
<iNode valueType="BOOLEAN" value="1"/>
<iNode valueType="BOOLEAN" value="0"/>
```

### 类型转换规则

**自动类型转换**：

框架在某些情况下会自动进行类型转换：

```xml
<!-- INT到HEX的转换 -->
<iNode id="int_value" name="整数值" length="16" valueType="INT" value="255"/>
<!-- 在十六进制上下文中会自动转换为0x00FF -->

<!-- 字符串到数值的转换 -->
<iNode id="str_number" name="字符串数字" length="32" valueType="INT" value="12345"/>
<!-- 字符串"12345"会自动转换为整数12345 -->
```

**显式类型转换**：

在表达式中可以使用转换函数：

```xml
<iNode id="converted_value" name="转换值" length="32" valueType="HEX" 
      value="toHex(int_value)">
    <description>将整数值转换为十六进制</description>
</iNode>
```

### 类型选择指南

**选择原则**：

1. **数据性质** - 根据数据的实际含义选择类型
2. **取值范围** - 确保类型能够容纳所有可能的值
3. **精度要求** - 浮点数需要考虑精度损失
4. **互操作性** - 考虑与其他系统的兼容性

**推荐用法**：

| 数据用途 | 推荐类型 | 说明 |
|----------|----------|------|
| 计数器、ID | UINT | 非负整数，范围大 |
| 温度、坐标 | INT | 可能为负数 |
| 二进制标志 | HEX | 位操作方便 |
| 设备名称 | STRING | 可读性好 |
| GPS坐标 | FLOAT | 需要小数精度 |
| 开关状态 | BOOLEAN | 语义明确 |

## 表达式系统

### 表达式概述

表达式系统是框架的核心功能之一，它允许在XML配置中使用动态计算，而不是硬编码的静态值。这大大增强了协议配置的灵活性和可维护性。

**为什么需要表达式系统？**

1. **动态计算** - 根据其他字段的值动态计算当前字段的值
2. **减少冗余** - 避免在多个地方重复相同的计算逻辑
3. **提高一致性** - 确保相关字段之间的数值关系始终正确
4. **简化维护** - 修改计算逻辑时只需要改一个地方

### 表达式语法

框架使用**AviatorScript**作为表达式引擎，支持丰富的语法特性：

#### 基本运算符

```xml
<!-- 算术运算 -->
<iNode id="sum" name="和" length="16" valueType="INT" value="a + b"/>
<iNode id="difference" name="差" length="16" valueType="INT" value="a - b"/>
<iNode id="product" name="积" length="16" valueType="INT" value="a * b"/>
<iNode id="quotient" name="商" length="16" valueType="INT" value="a / b"/>
<iNode id="remainder" name="余数" length="16" valueType="INT" value="a % b"/>

<!-- 比较运算 -->
<iNode id="is_greater" name="是否更大" length="1" valueType="BOOLEAN" value="a > b"/>
<iNode id="is_equal" name="是否相等" length="1" valueType="BOOLEAN" value="a == b"/>
<iNode id="is_not_equal" name="是否不等" length="1" valueType="BOOLEAN" value="a != b"/>

<!-- 逻辑运算 -->
<iNode id="and_result" name="逻辑与" length="1" valueType="BOOLEAN" value="flag1 && flag2"/>
<iNode id="or_result" name="逻辑或" length="1" valueType="BOOLEAN" value="flag1 || flag2"/>
<iNode id="not_result" name="逻辑非" length="1" valueType="BOOLEAN" value="!flag1"/>
```

#### 条件表达式

```xml
<!-- 三元运算符 -->
<iNode id="status_code" name="状态码" length="8" valueType="INT" 
      value="error_count > 0 ? 1 : 0">
    <description>根据错误计数设置状态码</description>
</iNode>

<!-- 复杂条件 -->
<iNode id="priority" name="优先级" length="8" valueType="INT" 
      value="urgent_flag ? 1 : (important_flag ? 2 : 3)">
    <description>根据紧急和重要标志设置优先级</description>
</iNode>

<!-- 多条件判断 -->
<iNode id="category" name="类别" length="8" valueType="INT" 
      value="size > 1000 ? 1 : (size > 100 ? 2 : 3)">
    <description>根据大小分类</description>
</iNode>
```

#### 函数调用

```xml
<!-- 数学函数 -->
<iNode id="absolute" name="绝对值" length="16" valueType="INT" value="abs(temperature)"/>
<iNode id="maximum" name="最大值" length="16" valueType="INT" value="max(value1, value2)"/>
<iNode id="minimum" name="最小值" length="16" valueType="INT" value="min(value1, value2)"/>

<!-- 字符串函数 -->
<iNode id="str_length" name="字符串长度" length="8" valueType="INT" value="string.length(device_name)"/>
<iNode id="upper_case" name="大写" length="64" valueType="STRING" value="string.upper(device_name)"/>

<!-- 类型转换函数 -->
<iNode id="hex_value" name="十六进制值" length="16" valueType="HEX" value="toHex(int_value)"/>
<iNode id="int_value" name="整数值" length="16" valueType="INT" value="toInt(hex_value)"/>
```

### 节点引用

**直接引用**：

```xml
<iNode id="base_value" name="基础值" length="16" valueType="INT" value="100"/>
<iNode id="derived_value" name="派生值" length="16" valueType="INT" value="base_value * 2"/>
```

**路径引用**：

对于嵌套结构，可以使用路径引用：

```xml
<protocolBody id="protocolHeader" name="头部">
    <iNode id="length" name="长度" length="16" valueType="INT" value="64"/>
</protocolBody>

<protocolBody id="protocolBody" name="主体">
    <!-- 引用头部的长度字段 -->
    <iNode id="total_size" name="总大小" length="16" valueType="INT" 
          value="protocolHeader.length + 1000"/>
</protocolBody>
```

**上下文引用**：

```xml
<!-- 引用父容器的属性 -->
<protocolBody id="container" name="容器" length="128">
    <iNode id="container_length" name="容器长度" length="16" valueType="INT" 
          value="parent.length"/>
</protocolBody>

<!-- 引用兄弟节点 -->
<protocolBody id="data_section" name="数据段">
    <iNode id="data_count" name="数据计数" length="16" valueType="INT" value="10"/>
    <iNode id="data_size" name="数据大小" length="16" valueType="INT" 
          value="data_count * 32"/>
</protocolBody>
```

### 内置函数

#### 数学函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `abs(x)` | 绝对值 | `abs(-5)` → `5` |
| `max(x, y)` | 最大值 | `max(10, 20)` → `20` |
| `min(x, y)` | 最小值 | `min(10, 20)` → `10` |
| `pow(x, y)` | 幂运算 | `pow(2, 3)` → `8` |
| `sqrt(x)` | 平方根 | `sqrt(16)` → `4` |
| `ceil(x)` | 向上取整 | `ceil(3.2)` → `4` |
| `floor(x)` | 向下取整 | `floor(3.8)` → `3` |
| `round(x)` | 四舍五入 | `round(3.6)` → `4` |

#### 字符串函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `string.length(s)` | 字符串长度 | `string.length("hello")` → `5` |
| `string.upper(s)` | 转大写 | `string.upper("hello")` → `"HELLO"` |
| `string.lower(s)` | 转小写 | `string.lower("HELLO")` → `"hello"` |
| `string.substring(s, start, end)` | 子字符串 | `string.substring("hello", 1, 3)` → `"el"` |
| `string.contains(s, sub)` | 包含判断 | `string.contains("hello", "ell")` → `true` |

#### 类型转换函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `toInt(x)` | 转整数 | `toInt("123")` → `123` |
| `toFloat(x)` | 转浮点数 | `toFloat("3.14")` → `3.14` |
| `toHex(x)` | 转十六进制 | `toHex(255)` → `"FF"` |
| `toString(x)` | 转字符串 | `toString(123)` → `"123"` |
| `toBool(x)` | 转布尔值 | `toBool(1)` → `true` |

#### 位操作函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `bitAnd(x, y)` | 按位与 | `bitAnd(0xF0, 0x0F)` → `0x00` |
| `bitOr(x, y)` | 按位或 | `bitOr(0xF0, 0x0F)` → `0xFF` |
| `bitXor(x, y)` | 按位异或 | `bitXor(0xF0, 0x0F)` → `0xFF` |
| `bitNot(x)` | 按位取反 | `bitNot(0x00)` → `0xFF` |
| `leftShift(x, n)` | 左移 | `leftShift(1, 3)` → `8` |
| `rightShift(x, n)` | 右移 | `rightShift(8, 3)` → `1` |

### 表达式最佳实践

#### 1. 保持简洁

```xml
<!-- ✅ 推荐：简洁明了 -->
<iNode id="total" name="总计" length="16" valueType="INT" value="count * size"/>

<!-- ❌ 不推荐：过于复杂 -->
<iNode id="complex" name="复杂计算" length="32" valueType="INT" 
      value="((a + b) * (c - d)) / (e > 0 ? e : 1) + (f ? g * h : protocol / j)"/>
```

#### 2. 使用有意义的变量名

```xml
<!-- ✅ 推荐：变量名有意义 -->
<iNode id="packet_count" name="数据包数量" length="16" valueType="INT" value="10"/>
<iNode id="total_size" name="总大小" length="32" valueType="INT" 
      value="packet_count * packet_size"/>

<!-- ❌ 不推荐：变量名无意义 -->
<iNode id="a" name="值A" length="16" valueType="INT" value="10"/>
<iNode id="b" name="值B" length="32" valueType="INT" value="a * c"/>
```

#### 3. 添加注释说明

```xml
<iNode id="adjusted_length" name="调整后长度" length="16" valueType="INT" 
      value="base_length + (has_extension ? extension_length : 0)">
    <description>基础长度加上可选的扩展长度</description>
</iNode>
```

#### 4. 处理边界情况

```xml
<!-- 避免除零错误 -->
<iNode id="average" name="平均值" length="16" valueType="INT" 
      value="count > 0 ? total / count : 0">
    <description>计算平均值，避免除零错误</description>
</iNode>

<!-- 处理负数情况 -->
<iNode id="positive_value" name="正数值" length="16" valueType="INT" 
      value="max(0, calculated_value)">
    <description>确保值不为负数</description>
</iNode>
```

### 表达式调试

**调试技巧**：

1. **分步计算** - 将复杂表达式分解为多个简单步骤
2. **中间变量** - 使用中间节点存储计算结果
3. **日志输出** - 在表达式中添加调试信息
4. **单元测试** - 为复杂表达式编写测试用例

```xml
<!-- 分步计算示例 -->
<iNode id="step1" name="步骤1" length="16" valueType="INT" value="a + b"/>
<iNode id="step2" name="步骤2" length="16" valueType="INT" value="step1 * c"/>
<iNode id="final_result" name="最终结果" length="16" valueType="INT" value="step2 / d"/>
```

## 条件依赖

### 条件依赖概述

条件依赖是框架的高级功能，它允许某些节点的值控制其他节点的行为。这种机制在处理复杂协议时非常有用，特别是当协议的结构依赖于特定字段的值时。

**为什么需要条件依赖？**

1. **动态协议结构** - 根据标志位决定某些字段是否存在
2. **版本兼容性** - 不同版本的协议有不同的字段组合
3. **可选字段** - 某些字段只在特定条件下才有意义
4. **数据完整性** - 确保相关字段的一致性

**典型应用场景**：

- 协议版本控制：v1.0只有基础字段，v2.0增加扩展字段
- 消息类型控制：不同类型的消息有不同的数据结构
- 功能开关：根据功能标志位启用或禁用相关字段
- 长度控制：根据长度字段决定后续数据的存在

### 条件依赖配置

#### 基本语法

```xml
<conditionalDependency conditionNode="控制节点ID" 
                      condition="条件表达式" 
                      action="执行动作"
                      priority="优先级"
                      description="描述信息">
    <!-- 可选的详细配置 -->
</conditionalDependency>
```

**属性说明**：

| 属性 | 必需 | 说明 | 示例 |
|------|------|------|------|
| `conditionNode` | ✅ | 条件节点ID | `version`, `message_type` |
| `condition` | ✅ | 条件表达式 | `value > 1`, `value == 2` |
| `action` | ✅ | 执行动作 | `ENABLE`, `DISABLE`, `SET_DEFAULT` |
| `priority` | ❌ | 优先级（默认0） | `1`, `2`, `10` |
| `description` | ❌ | 描述信息 | `版本2启用扩展字段` |

#### 支持的动作类型

**1. ENABLE（启用）**
启用当前节点，使其参与编解码过程。

```xml
<iNode id="extended_field" name="扩展字段" length="32" valueType="INT">
    <conditionalDependency conditionNode="version" 
                          condition="value >= 2" 
                          action="ENABLE"
                          description="版本2及以上启用扩展字段"/>
</iNode>
```

**2. DISABLE（禁用）**
禁用当前节点，跳过编解码过程。

```xml
<iNode id="legacy_field" name="遗留字段" length="16" valueType="INT">
    <conditionalDependency conditionNode="version" 
                          condition="value >= 2" 
                          action="DISABLE"
                          description="版本2及以上禁用遗留字段"/>
</iNode>
```

**3. SET_DEFAULT（设置默认值）**
根据条件设置节点的默认值。

```xml
<iNode id="status_flag" name="状态标志" length="8" valueType="INT">
    <conditionalDependency conditionNode="error_count" 
                          condition="value > 0" 
                          action="SET_DEFAULT"
                          defaultValue="1"
                          description="有错误时设置状态为1"/>
    <conditionalDependency conditionNode="error_count" 
                          condition="value == 0" 
                          action="SET_DEFAULT"
                          defaultValue="0"
                          description="无错误时设置状态为0"/>
</iNode>
```

**4. CLEAR_VALUE（清除值）**
清除节点的值，通常用于重置状态。

```xml
<iNode id="temp_data" name="临时数据" length="64" valueType="HEX">
    <conditionalDependency conditionNode="reset_flag" 
                          condition="value == 1" 
                          action="CLEAR_VALUE"
                          description="重置标志为1时清除临时数据"/>
</iNode>
```

### 条件表达式语法

条件表达式使用AviatorScript语法，支持丰富的操作符和函数：

#### 基本比较

```xml
<!-- 数值比较 -->
<conditionalDependency conditionNode="version" condition="value == 1"/>
<conditionalDependency conditionNode="length" condition="value > 100"/>
<conditionalDependency conditionNode="count" condition="value <= 10"/>

<!-- 字符串比较 -->
<conditionalDependency conditionNode="device_type" condition="value == 'sensor'"/>
<conditionalDependency conditionNode="protocol_name" condition="value != 'legacy'"/>

<!-- 布尔值比较 -->
<conditionalDependency conditionNode="is_enabled" condition="value == true"/>
<conditionalDependency conditionNode="has_error" condition="value == false"/>
```

#### 复合条件

```xml
<!-- 逻辑与 -->
<conditionalDependency conditionNode="version" 
                      condition="value >= 2 && value <= 5"/>

<!-- 逻辑或 -->
<conditionalDependency conditionNode="message_type" 
                      condition="value == 1 || value == 3"/>

<!-- 复杂条件 -->
<conditionalDependency conditionNode="status" 
                      condition="(value > 0 && value < 100) || value == 255"/>
```

#### 范围判断

```xml
<!-- 区间判断 -->
<conditionalDependency conditionNode="temperature" 
                      condition="value >= -40 && value <= 85"/>

<!-- 枚举值判断 -->
<conditionalDependency conditionNode="device_type" 
                      condition="value in [1, 2, 3, 5]"/>

<!-- 排除判断 -->
<conditionalDependency conditionNode="error_code" 
                      condition="value not in [0, 255]"/>
```

### 多条件依赖

一个节点可以有多个条件依赖，通过优先级控制执行顺序：

```xml
<iNode id="adaptive_field" name="自适应字段" length="32" valueType="INT">
    <!-- 高优先级：版本控制 -->
    <conditionalDependency conditionNode="version" 
                          condition="value < 2" 
                          action="DISABLE"
                          priority="10"
                          description="版本1禁用此字段"/>
    
    <!-- 中优先级：类型控制 -->
    <conditionalDependency conditionNode="message_type" 
                          condition="value == 0" 
                          action="DISABLE"
                          priority="5"
                          description="控制消息禁用此字段"/>
    
    <!-- 低优先级：默认值设置 -->
    <conditionalDependency conditionNode="data_length" 
                          condition="value > 0" 
                          action="SET_DEFAULT"
                          defaultValue="data_length * 8"
                          priority="1"
                          description="根据数据长度设置默认值"/>
</iNode>
```

**优先级规则**：
- 数值越大，优先级越高
- 相同优先级按配置顺序执行
- 高优先级的动作可能覆盖低优先级的结果

### 条件依赖示例

#### 示例1：协议版本控制

```xml
<protocolDefinition id="versioned_protocol" name="版本化协议" length="800" valueType="HEX">
    <protocolHeader id="protocolHeader" name="协议头" length="64" valueType="HEX">
        <iNode id="sync_word" name="同步字" length="16" valueType="HEX" value="0xAA55"/>
        <iNode id="version" name="版本号" length="8" valueType="INT" value="2"/>
        <iNode id="message_type" name="消息类型" length="8" valueType="INT" value="1"/>
        <iNode id="length" name="数据长度" length="32" valueType="INT" value="90"/>
    </protocolHeader>
    
    <protocolBody id="protocolBody" name="协议体" length="720" valueType="HEX">
        <!-- 基础字段 - 所有版本都有 -->
        <iNode id="device_id" name="设备ID" length="32" valueType="INT"/>
        <iNode id="timestamp" name="时间戳" length="32" valueType="INT"/>
        
        <!-- 版本1的遗留字段 -->
        <iNode id="legacy_status" name="遗留状态" length="16" valueType="INT">
            <conditionalDependency conditionNode="version" 
                                  condition="value == 1" 
                                  action="ENABLE"
                                  description="仅版本1启用遗留状态字段"/>
        </iNode>
        
        <!-- 版本2的新字段 -->
        <iNode id="extended_info" name="扩展信息" length="64" valueType="HEX">
            <conditionalDependency conditionNode="version" 
                                  condition="value >= 2" 
                                  action="ENABLE"
                                  description="版本2及以上启用扩展信息"/>
        </iNode>
        
        <!-- 版本3的高级字段 -->
        <iNode id="advanced_config" name="高级配置" length="128" valueType="HEX">
            <conditionalDependency conditionNode="version" 
                                  condition="value >= 3" 
                                  action="ENABLE"
                                  description="版本3及以上启用高级配置"/>
        </iNode>
        
        <!-- 填充字段 - 保证总长度一致 -->
        <iNode id="padding" name="填充" length="448" valueType="HEX" value="0x0">
            <conditionalDependency conditionNode="version" 
                                  condition="value == 1" 
                                  action="SET_DEFAULT"
                                  defaultValue="0x0"
                                  description="版本1需要更多填充"/>
        </iNode>
    </protocolBody>
    
    <protocolTail id="checksum" name="校验" length="16" valueType="HEX">
        <iNode id="crc16" name="CRC16" length="16" valueType="HEX"/>
    </protocolTail>
</protocolDefinition>
```

#### 示例2：消息类型控制

```xml
<protocolDefinition id="message_protocol" name="消息协议" length="640" valueType="HEX">
    <protocolHeader id="protocolHeader" name="协议头" length="64" valueType="HEX">
        <iNode id="message_type" name="消息类型" length="8" valueType="INT"/>
        <!-- 1=数据消息, 2=控制消息, 3=状态消息 -->
    </protocolHeader>
    
    <protocolBody id="protocolBody" name="协议体" length="576" valueType="HEX">
        <!-- 数据消息字段 -->
        <protocolBody id="data_message" name="数据消息" length="192" valueType="HEX">
            <conditionalDependency conditionNode="message_type" 
                                  condition="value == 1" 
                                  action="ENABLE"
                                  description="消息类型为1时启用数据消息字段"/>
            
            <iNode id="sensor_data" name="传感器数据" length="128" valueType="HEX"/>
            <iNode id="data_quality" name="数据质量" length="8" valueType="INT"/>
            <iNode id="data_timestamp" name="数据时间戳" length="32" valueType="INT"/>
            <iNode id="data_reserved" name="数据保留" length="24" valueType="HEX" value="0x0"/>
        </protocolBody>
        
        <!-- 控制消息字段 -->
        <protocolBody id="control_message" name="控制消息" length="192" valueType="HEX">
            <conditionalDependency conditionNode="message_type" 
                                  condition="value == 2" 
                                  action="ENABLE"
                                  description="消息类型为2时启用控制消息字段"/>
            
            <iNode id="command_code" name="命令码" length="16" valueType="INT"/>
            <iNode id="parameter1" name="参数1" length="32" valueType="INT"/>
            <iNode id="parameter2" name="参数2" length="32" valueType="INT"/>
            <iNode id="control_flags" name="控制标志" length="8" valueType="HEX"/>
            <iNode id="control_reserved" name="控制保留" length="104" valueType="HEX" value="0x0"/>
        </protocolBody>
        
        <!-- 状态消息字段 -->
        <protocolBody id="status_message" name="状态消息" length="192" valueType="HEX">
            <conditionalDependency conditionNode="message_type" 
                                  condition="value == 3" 
                                  action="ENABLE"
                                  description="消息类型为3时启用状态消息字段"/>
            
            <iNode id="device_status" name="设备状态" length="8" valueType="INT"/>
            <iNode id="error_count" name="错误计数" length="16" valueType="INT"/>
            <iNode id="uptime" name="运行时间" length="32" valueType="INT"/>
            <iNode id="temperature" name="温度" length="16" valueType="INT"/>
            <iNode id="voltage" name="电压" length="16" valueType="INT"/>
            <iNode id="status_reserved" name="状态保留" length="104" valueType="HEX" value="0x0"/>
        </protocolBody>
        
        <!-- 通用填充 - 确保未启用的消息类型有填充 -->
        <iNode id="unused_space" name="未使用空间" length="384" valueType="HEX" value="0x0">
            <conditionalDependency conditionNode="message_type" 
                                  condition="value not in [1, 2, 3]" 
                                  action="ENABLE"
                                  description="未知消息类型时填充空间"/>
        </iNode>
    </protocolBody>
</protocolDefinition>
```

### 条件依赖最佳实践

#### 1. 明确的条件逻辑

```xml
<!-- ✅ 推荐：条件清晰明确 -->
<conditionalDependency conditionNode="version" 
                      condition="value >= 2" 
                      action="ENABLE"
                      description="版本2及以上启用"/>

<!-- ❌ 不推荐：条件模糊 -->
<conditionalDependency conditionNode="some_flag" 
                      condition="value != 0" 
                      action="ENABLE"/>
```

#### 2. 合理的优先级设置

```xml
<!-- 按重要性设置优先级 -->
<conditionalDependency priority="100" description="关键安全检查"/>
<conditionalDependency priority="50" description="功能控制"/>
<conditionalDependency priority="10" description="默认值设置"/>
<conditionalDependency priority="1" description="可选优化"/>
```

#### 3. 完整的文档说明

```xml
<conditionalDependency conditionNode="protocol_version" 
                      condition="value >= 3 && value <= 5" 
                      action="ENABLE"
                      priority="10"
                      description="协议版本3-5支持高级加密字段，用于增强安全性">
    <documentation>
        此字段在协议版本3中引入，提供AES-256加密支持。
        版本6开始使用新的加密机制，此字段被废弃。
        兼容性：向前兼容版本1-2（忽略此字段），向后兼容版本6+（使用新字段）
    </documentation>
</conditionalDependency>
```

#### 4. 避免循环依赖

```xml
<!-- ❌ 错误：循环依赖 -->
<iNode id="field_a">
    <conditionalDependency conditionNode="field_b" condition="value > 0" action="ENABLE"/>
</iNode>
<iNode id="field_b">
    <conditionalDependency conditionNode="field_a" condition="value > 0" action="ENABLE"/>
</iNode>

<!-- ✅ 正确：单向依赖 -->
<iNode id="control_field">
    <!-- 控制字段，独立存在 -->
</iNode>
<iNode id="dependent_field">
    <conditionalDependency conditionNode="control_field" condition="value > 0" action="ENABLE"/>
</iNode>
```

## 填充功能

### 填充功能概述

填充功能用于处理协议中的对齐和长度要求。在实际的协议设计中，经常需要将数据填充到特定的长度，以满足硬件要求、网络传输要求或协议规范。

**为什么需要填充功能？**

1. **字节对齐** - 硬件通常要求数据按特定边界对齐
2. **固定长度** - 某些协议要求固定的数据包长度
3. **缓冲区管理** - 预分配固定大小的缓冲区
4. **传输效率** - 网络传输中的MTU对齐

**典型应用场景**：

- 网络协议的MTU对齐
- 硬件接口的字节对齐要求
- 存储系统的块大小对齐
- 加密算法的块长度要求

### 填充类型

框架支持多种填充类型，以适应不同的使用场景：

#### 1. FIXED_LENGTH（固定长度填充）

将数据填充到指定的固定长度。

```xml
<padding type="FIXED_LENGTH" 
         targetLength="1024" 
         paddingValue="0x00"
         description="填充到1024字节">
</padding>
```

**使用场景**：
- 网络数据包的固定长度要求
- 文件系统的块大小对齐
- 硬件缓冲区的固定大小

#### 2. ALIGNMENT（对齐填充）

将数据对齐到指定的边界。

```xml
<padding type="ALIGNMENT" 
         alignmentSize="64" 
         paddingValue="0xFF"
         description="64字节对齐">
</padding>
```

**使用场景**：
- CPU缓存行对齐（通常64字节）
- 内存页对齐（通常4KB）
- DMA传输对齐要求

#### 3. DYNAMIC（动态填充）

根据表达式动态计算填充长度。

```xml
<padding type="DYNAMIC" 
         lengthExpression="(data_length + 7) / 8 * 8" 
         paddingValue="0xAA"
         description="8字节对齐的动态填充">
</padding>
```

**使用场景**：
- 复杂的对齐计算
- 依赖其他字段的填充长度
- 条件性的填充需求

#### 4. FILL_REMAINING（补齐剩余空间）

填充剩余的空间到容器的总长度。

```xml
<padding type="FILL_REMAINING" 
         paddingValue="0x55"
         description="填充剩余空间">
</padding>
```

**使用场景**：
- 容器的剩余空间填充
- 确保数据包的完整性
- 防止信息泄露的安全填充

#### 5. FILL_CONTAINER（容器级别填充）

针对容器级别的填充，确保容器内所有子节点加上填充的总长度等于容器的固定长度。

```xml
<padding type="FILL_CONTAINER" 
         containerFixedLength="512"
         paddingValue="0xCC"
         position="END"
         description="容器级别填充">
</padding>
```

**使用场景**：
- 数据域包含多个节点，需要整体对齐
- 协议段的固定长度要求
- 复合数据结构的长度控制

### 填充配置语法

#### 基本填充配置

```xml
<iNode id="padding_node" name="填充节点" length="计算得出" valueType="HEX">
    <padding type="填充类型" 
             paddingValue="填充值"
             description="填充描述">
        <!-- 类型特定的配置 -->
    </padding>
</iNode>
```

#### 固定长度填充示例

```xml
<protocolBody id="data_section" name="数据段" length="1024" valueType="HEX">
    <!-- 实际数据字段 -->
    <iNode id="header_info" name="头部信息" length="64" valueType="HEX"/>
    <iNode id="payload_data" name="载荷数据" length="variable" valueType="HEX"/>
    
    <!-- 固定长度填充 -->
    <iNode id="fixed_padding" name="固定填充" length="calculated" valueType="HEX">
        <padding type="FIXED_LENGTH" 
                 targetLength="1024"
                 paddingValue="0x00"
                 description="填充到1024字节总长度">
        </padding>
    </iNode>
</protocolBody>
```

#### 对齐填充示例

```xml
<protocolBody id="aligned_data" name="对齐数据" length="calculated" valueType="HEX">
    <iNode id="data_content" name="数据内容" length="variable" valueType="HEX"/>
    
    <!-- 64字节对齐填充 -->
    <iNode id="alignment_padding" name="对齐填充" length="calculated" valueType="HEX">
        <padding type="ALIGNMENT" 
                 alignmentSize="64"
                 paddingValue="0xFF"
                 description="64字节边界对齐">
        </padding>
    </iNode>
</protocolBody>
```

#### 动态填充示例

```xml
<protocolBody id="dynamic_section" name="动态段" length="calculated" valueType="HEX">
    <iNode id="record_count" name="记录数量" length="16" valueType="INT"/>
    <iNode id="record_data" name="记录数据" length="record_count * 32" valueType="HEX"/>
    
    <!-- 动态填充 - 确保8字节对齐 -->
    <iNode id="dynamic_padding" name="动态填充" length="calculated" valueType="HEX">
        <padding type="DYNAMIC" 
                 lengthExpression="8 - ((record_count * 32) % 8)"
                 paddingValue="0xAA"
                 condition="(record_count * 32) % 8 != 0"
                 description="8字节对齐的动态填充">
        </padding>
    </iNode>
</protocolBody>
```

#### 容器级别填充示例

```xml
<protocolBody id="container_section" name="容器段" length="512" valueType="HEX">
    <!-- 多个子节点 -->
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>
    <iNode id="field2" name="字段2" length="64" valueType="HEX"/>
    <iNode id="field3" name="字段3" length="variable" valueType="STRING"/>
    
    <!-- 容器级别填充 -->
    <iNode id="container_padding" name="容器填充" length="calculated" valueType="HEX">
        <padding type="FILL_CONTAINER" 
                 containerFixedLength="512"
                 paddingValue="0xCC"
                 position="END"
                 autoCalculateContainerLength="false"
                 description="确保容器总长度为512字节">
        </padding>
    </iNode>
</protocolBody>
```

### 填充位置控制

填充可以放置在不同的位置：

#### 1. END（末尾填充）

```xml
<padding type="FILL_CONTAINER" 
         position="END"
         description="在容器末尾添加填充">
</padding>
```

#### 2. BEGIN（开头填充）

```xml
<padding type="ALIGNMENT" 
         position="BEGIN"
         description="在容器开头添加填充">
</padding>
```

#### 3. MIDDLE（中间填充）

```xml
<padding type="DYNAMIC" 
         position="MIDDLE"
         insertAfter="field2"
         description="在field2后插入填充">
</padding>
```

### 条件填充

填充可以根据条件动态启用：

```xml
<iNode id="conditional_padding" name="条件填充" length="calculated" valueType="HEX">
    <padding type="FIXED_LENGTH" 
             targetLength="256"
             paddingValue="0x00"
             condition="data_length < 200"
             description="数据长度小于200时进行填充">
    </padding>
    
    <!-- 条件依赖也可以控制填充节点 -->
    <conditionalDependency conditionNode="enable_padding" 
                          condition="value == true" 
                          action="ENABLE"
                          description="根据标志位启用填充"/>
</iNode>
```

### 填充值类型

#### 1. 固定值填充

```xml
<!-- 零填充 -->
<padding paddingValue="0x00"/>

<!-- 特定值填充 -->
<padding paddingValue="0xFF"/>

<!-- 模式填充 -->
<padding paddingValue="0xAA55"/>
```

#### 2. 随机值填充

```xml
<padding paddingValue="random()" 
         description="随机值填充，增强安全性"/>
```

#### 3. 计算值填充

```xml
<padding paddingValue="crc8(data_content)" 
         description="使用CRC8作为填充值"/>
```

#### 4. 重复模式填充

```xml
<padding paddingValue="repeat(0xAA55, padding_length / 2)" 
         description="重复模式填充"/>
```

### 填充功能示例

#### 示例1：网络数据包对齐

```xml
<protocolDefinition id="network_packet" name="网络数据包" length="1500" valueType="HEX">
    <protocolHeader id="packet_header" name="数据包头" length="160" valueType="HEX">
        <iNode id="version" name="版本" length="4" valueType="INT" value="4"/>
        <iNode id="header_length" name="头长度" length="4" valueType="INT" value="20"/>
        <iNode id="total_length" name="总长度" length="16" valueType="INT"/>
        <iNode id="packet_id" name="包ID" length="16" valueType="INT"/>
        <iNode id="flags" name="标志" length="16" valueType="HEX"/>
        <iNode id="ttl" name="生存时间" length="8" valueType="INT" value="64"/>
        <iNode id="protocolDefinition" name="协议" length="8" valueType="INT" value="6"/>
        <iNode id="checksum" name="校验和" length="16" valueType="HEX"/>
        <iNode id="src_ip" name="源IP" length="32" valueType="HEX"/>
        <iNode id="dst_ip" name="目标IP" length="32" valueType="HEX"/>
        
        <!-- 头部选项对齐填充 -->
        <iNode id="header_padding" name="头部填充" length="calculated" valueType="HEX">
            <padding type="ALIGNMENT" 
                     alignmentSize="32"
                     paddingValue="0x00"
                     description="32位边界对齐">
            </padding>
        </iNode>
    </protocolHeader>
    
    <protocolBody id="packet_body" name="数据包体" length="calculated" valueType="HEX">
        <iNode id="payload" name="载荷" length="variable" valueType="HEX"/>
        
        <!-- MTU对齐填充 -->
        <iNode id="mtu_padding" name="MTU填充" length="calculated" valueType="HEX">
            <padding type="FIXED_LENGTH" 
                     targetLength="1500"
                     paddingValue="0x00"
                     condition="payload.length < 1340"
                     description="MTU 1500字节对齐">
            </padding>
        </iNode>
    </protocolBody>
</protocolDefinition>
```

#### 示例2：存储块对齐

```xml
<protocolDefinition id="storage_block" name="存储块" length="4096" valueType="HEX">
    <protocolHeader id="block_header" name="块头" length="128" valueType="HEX">
        <iNode id="magic" name="魔数" length="32" valueType="HEX" value="0xDEADBEEF"/>
        <iNode id="version" name="版本" length="8" valueType="INT" value="1"/>
        <iNode id="block_type" name="块类型" length="8" valueType="INT"/>
        <iNode id="data_size" name="数据大小" length="32" valueType="INT"/>
        <iNode id="timestamp" name="时间戳" length="32" valueType="INT"/>
        <iNode id="reserved" name="保留" length="16" valueType="HEX" value="0x0000"/>
    </protocolHeader>
    
    <protocolBody id="block_data" name="块数据" length="calculated" valueType="HEX">
        <iNode id="user_data" name="用户数据" length="data_size * 8" valueType="HEX"/>
        
        <!-- 块大小填充 - 4KB对齐 -->
        <iNode id="block_padding" name="块填充" length="calculated" valueType="HEX">
            <padding type="FILL_CONTAINER" 
                     containerFixedLength="4096"
                     paddingValue="0x00"
                     position="END"
                     description="4KB块大小对齐">
            </padding>
        </iNode>
    </protocolBody>
</protocolDefinition>
```

### 填充最佳实践

#### 1. 选择合适的填充类型

```xml
<!-- ✅ 网络传输 - 使用固定长度 -->
<padding type="FIXED_LENGTH" targetLength="1500"/>

<!-- ✅ 内存对齐 - 使用对齐填充 -->
<padding type="ALIGNMENT" alignmentSize="64"/>

<!-- ✅ 复杂计算 - 使用动态填充 -->
<padding type="DYNAMIC" lengthExpression="complex_calculation()"/>
```

#### 2. 合理设置填充值

```xml
<!-- ✅ 安全考虑 - 使用随机填充 -->
<padding paddingValue="random()" description="防止信息泄露"/>

<!-- ✅ 调试方便 - 使用特定模式 -->
<padding paddingValue="0xDEADBEEF" description="调试时易于识别"/>

<!-- ✅ 性能考虑 - 使用零填充 -->
<padding paddingValue="0x00" description="零填充性能最佳"/>
```

#### 3. 添加详细说明

```xml
<padding type="FILL_CONTAINER" 
         containerFixedLength="512"
         paddingValue="0x00"
         description="确保数据段总长度为512字节，满足硬件DMA传输要求">
    <documentation>
        此填充确保数据段符合硬件DMA控制器的512字节块传输要求。
        填充值使用0x00以减少功耗和提高传输效率。
        在解码时会自动忽略填充数据。
    </documentation>
</padding>
```

#### 4. 考虑性能影响

```xml
<!-- ✅ 高频使用 - 简单填充 -->
<padding type="FIXED_LENGTH" targetLength="64" paddingValue="0x00"/>

<!-- ❌ 高频使用 - 复杂计算 -->
<padding type="DYNAMIC" lengthExpression="complex_hash_function(data)"/>
```

## 枚举配置

### 枚举概述

枚举配置允许为特定的数值定义有意义的名称，提高协议配置的可读性和可维护性。这在处理状态码、类型标识、错误代码等场景时特别有用。

**为什么需要枚举？**

1. **可读性** - 使用有意义的名称代替数字
2. **维护性** - 集中管理常量定义
3. **一致性** - 确保相同含义的值在不同地方使用相同的名称
4. **文档化** - 自动生成文档和说明

### 枚举定义语法

#### 基本枚举定义

```xml
<enumeration id="message_types" name="消息类型" description="协议支持的消息类型">
    <enum value="1" name="DATA_MESSAGE" description="数据消息"/>
    <enum value="2" name="CONTROL_MESSAGE" description="控制消息"/>
    <enum value="3" name="STATUS_MESSAGE" description="状态消息"/>
    <enum value="4" name="ERROR_MESSAGE" description="错误消息"/>
</enumeration>
```

#### 十六进制枚举

```xml
<enumeration id="device_types" name="设备类型" valueType="HEX">
    <enum value="0x01" name="SENSOR" description="传感器设备"/>
    <enum value="0x02" name="ACTUATOR" description="执行器设备"/>
    <enum value="0x03" name="CONTROLLER" description="控制器设备"/>
    <enum value="0xFF" name="UNKNOWN" description="未知设备类型"/>
</enumeration>
```

#### 字符串枚举

```xml
<enumeration id="protocol_versions" name="协议版本" valueType="STRING">
    <enum value="v1.0" name="VERSION_1_0" description="协议版本1.0"/>
    <enum value="v1.1" name="VERSION_1_1" description="协议版本1.1"/>
    <enum value="v2.0" name="VERSION_2_0" description="协议版本2.0"/>
</enumeration>
```

### 枚举使用

#### 在节点中使用枚举

```xml
<!-- 定义枚举 -->
<enumeration id="status_codes" name="状态码">
    <enum value="0" name="SUCCESS" description="成功"/>
    <enum value="1" name="WARNING" description="警告"/>
    <enum value="2" name="ERROR" description="错误"/>
    <enum value="3" name="CRITICAL" description="严重错误"/>
</enumeration>

<!-- 在节点中使用 -->
<iNode id="status" name="状态" length="8" valueType="INT" 
      enumeration="status_codes" value="SUCCESS">
    <description>设备当前状态</description>
</iNode>
```

#### 在表达式中使用枚举

```xml
<iNode id="response_code" name="响应码" length="8" valueType="INT" 
      enumeration="status_codes"
      value="error_count > 0 ? ERROR : SUCCESS">
    <description>根据错误计数设置响应码</description>
</iNode>
```

#### 在条件依赖中使用枚举

```xml
<iNode id="error_details" name="错误详情" length="64" valueType="STRING">
    <conditionalDependency conditionNode="status" 
                          condition="value == ERROR || value == CRITICAL" 
                          action="ENABLE"
                          description="错误状态时启用错误详情"/>
</iNode>
```

### 复杂枚举示例

#### 示例1：网络协议类型

```xml
<enumeration id="ip_protocols" name="IP协议类型" valueType="INT">
    <enum value="1" name="ICMP" description="Internet Control Message Protocol"/>
    <enum value="2" name="IGMP" description="Internet Group Management Protocol"/>
    <enum value="6" name="TCP" description="Transmission Control Protocol"/>
    <enum value="17" name="UDP" description="User Datagram Protocol"/>
    <enum value="41" name="IPv6" description="IPv6 encapsulation"/>
    <enum value="58" name="ICMPv6" description="ICMP for IPv6"/>
</enumeration>

<protocolDefinition id="ip_packet" name="IP数据包" length="160" valueType="HEX">
    <protocolHeader id="ip_header" name="IP头部" length="160" valueType="HEX">
        <iNode id="version" name="版本" length="4" valueType="INT" value="4"/>
        <iNode id="ihl" name="头部长度" length="4" valueType="INT" value="5"/>
        <iNode id="tos" name="服务类型" length="8" valueType="INT" value="0"/>
        <iNode id="total_length" name="总长度" length="16" valueType="INT"/>
        <iNode id="identification" name="标识" length="16" valueType="INT"/>
        <iNode id="flags" name="标志" length="3" valueType="INT"/>
        <iNode id="fragment_offset" name="片偏移" length="13" valueType="INT"/>
        <iNode id="ttl" name="生存时间" length="8" valueType="INT" value="64"/>
        
        <!-- 使用枚举的协议字段 -->
        <iNode id="protocolDefinition" name="协议" length="8" valueType="INT" 
              enumeration="ip_protocols" value="TCP">
            <description>上层协议类型</description>
        </iNode>
        
        <iNode id="header_checksum" name="头部校验和" length="16" valueType="HEX"/>
        <iNode id="source_ip" name="源IP地址" length="32" valueType="HEX"/>
        <iNode id="destination_ip" name="目标IP地址" length="32" valueType="HEX"/>
    </protocolHeader>
</protocolDefinition>
```

#### 示例2：设备状态管理

```xml
<!-- 设备类型枚举 -->
<enumeration id="device_types" name="设备类型" valueType="HEX">
    <enum value="0x01" name="TEMPERATURE_SENSOR" description="温度传感器"/>
    <enum value="0x02" name="HUMIDITY_SENSOR" description="湿度传感器"/>
    <enum value="0x03" name="PRESSURE_SENSOR" description="压力传感器"/>
    <enum value="0x10" name="RELAY_ACTUATOR" description="继电器执行器"/>
    <enum value="0x11" name="VALVE_ACTUATOR" description="阀门执行器"/>
    <enum value="0x20" name="MAIN_CONTROLLER" description="主控制器"/>
    <enum value="0x21" name="SUB_CONTROLLER" description="子控制器"/>
</enumeration>

<!-- 设备状态枚举 -->
<enumeration id="device_status" name="设备状态" valueType="INT">
    <enum value="0" name="OFFLINE" description="离线"/>
    <enum value="1" name="ONLINE" description="在线"/>
    <enum value="2" name="MAINTENANCE" description="维护中"/>
    <enum value="3" name="ERROR" description="故障"/>
    <enum value="4" name="CALIBRATING" description="校准中"/>
</enumeration>

<!-- 错误代码枚举 -->
<enumeration id="error_codes" name="错误代码" valueType="INT">
    <enum value="0" name="NO_ERROR" description="无错误"/>
    <enum value="1" name="SENSOR_FAULT" description="传感器故障"/>
    <enum value="2" name="COMMUNICATION_ERROR" description="通信错误"/>
    <enum value="3" name="POWER_FAILURE" description="电源故障"/>
    <enum value="4" name="CALIBRATION_ERROR" description="校准错误"/>
    <enum value="5" name="OVERRANGE" description="超量程"/>
    <enum value="6" name="UNDERRANGE" description="欠量程"/>
</enumeration>

<protocolDefinition id="device_status_protocol" name="设备状态协议" length="256" valueType="HEX">
    <protocolHeader id="protocolHeader" name="协议头" length="64" valueType="HEX">
        <iNode id="sync_word" name="同步字" length="16" valueType="HEX" value="0xAA55"/>
        <iNode id="message_type" name="消息类型" length="8" valueType="INT" value="3"/>
        <iNode id="device_count" name="设备数量" length="8" valueType="INT"/>
        <iNode id="timestamp" name="时间戳" length="32" valueType="INT"/>
    </protocolHeader>
    
    <protocolBody id="device_list" name="设备列表" length="192" valueType="HEX">
        <!-- 重复的设备信息结构 -->
        <protocolBody id="device_info" name="设备信息" length="64" valueType="HEX">
            <iNode id="device_id" name="设备ID" length="16" valueType="INT"/>
            
            <!-- 使用设备类型枚举 -->
            <iNode id="device_type" name="设备类型" length="8" valueType="HEX" 
                  enumeration="device_types">
                <description>设备的类型标识</description>
            </iNode>
            
            <!-- 使用设备状态枚举 -->
            <iNode id="status" name="设备状态" length="8" valueType="INT" 
                  enumeration="device_status">
                <description>设备当前运行状态</description>
            </iNode>
            
            <!-- 使用错误代码枚举 -->
            <iNode id="error_code" name="错误代码" length="8" valueType="INT" 
                  enumeration="error_codes" value="NO_ERROR">
                <description>设备错误代码</description>
                
                <!-- 根据状态设置错误代码 -->
                <conditionalDependency conditionNode="status" 
                                      condition="value == ERROR" 
                                      action="SET_DEFAULT"
                                      defaultValue="SENSOR_FAULT"
                                      description="故障状态时设置默认错误代码"/>
            </iNode>
            
            <iNode id="data_value" name="数据值" length="16" valueType="INT"/>
            <iNode id="reserved" name="保留" length="8" valueType="HEX" value="0x00"/>
        </protocolBody>
    </protocolBody>
</protocolDefinition>
```

### 枚举最佳实践

#### 1. 有意义的命名

```xml
<!-- ✅ 推荐：清晰的命名 -->
<enumeration id="message_types" name="消息类型">
    <enum value="1" name="HEARTBEAT" description="心跳消息"/>
    <enum value="2" name="DATA_REPORT" description="数据上报"/>
    <enum value="3" name="COMMAND_REQUEST" description="命令请求"/>
</enumeration>

<!-- ❌ 不推荐：无意义的命名 -->
<enumeration id="types">
    <enum value="1" name="TYPE1"/>
    <enum value="2" name="TYPE2"/>
    <enum value="3" name="TYPE3"/>
</enumeration>
```

#### 2. 预留扩展空间

```xml
<!-- ✅ 推荐：预留扩展值 -->
<enumeration id="device_types" name="设备类型">
    <enum value="1" name="SENSOR" description="传感器"/>
    <enum value="2" name="ACTUATOR" description="执行器"/>
    <!-- 预留3-9给未来的设备类型 -->
    <enum value="10" name="CONTROLLER" description="控制器"/>
    <!-- 预留11-19给未来的控制器类型 -->
    <enum value="255" name="UNKNOWN" description="未知类型"/>
</enumeration>
```

#### 3. 一致的值分配策略

```xml
<!-- ✅ 推荐：按类别分组 -->
<enumeration id="message_codes" name="消息代码">
    <!-- 系统消息：1-99 -->
    <enum value="1" name="SYSTEM_STARTUP" description="系统启动"/>
    <enum value="2" name="SYSTEM_SHUTDOWN" description="系统关闭"/>
    
    <!-- 数据消息：100-199 -->
    <enum value="100" name="SENSOR_DATA" description="传感器数据"/>
    <enum value="101" name="STATUS_DATA" description="状态数据"/>
    
    <!-- 控制消息：200-299 -->
    <enum value="200" name="START_COMMAND" description="启动命令"/>
    <enum value="201" name="STOP_COMMAND" description="停止命令"/>
    
    <!-- 错误消息：300-399 -->
    <enum value="300" name="GENERAL_ERROR" description="一般错误"/>
    <enum value="301" name="CRITICAL_ERROR" description="严重错误"/>
</enumeration>
```

#### 4. 完整的文档说明

```xml
<enumeration id="protocol_states" name="协议状态" description="协议连接的各种状态">
    <enum value="0" name="DISCONNECTED" description="未连接状态，初始状态"/>
    <enum value="1" name="CONNECTING" description="连接中，正在建立连接"/>
    <enum value="2" name="CONNECTED" description="已连接，可以正常通信"/>
    <enum value="3" name="AUTHENTICATING" description="认证中，正在进行身份验证"/>
    <enum value="4" name="AUTHENTICATED" description="已认证，身份验证成功"/>
    <enum value="5" name="DISCONNECTING" description="断开中，正在断开连接"/>
    <enum value="255" name="ERROR" description="错误状态，连接异常"/>
</enumeration>
```

## 最佳实践

### 协议设计原则

#### 1. 模块化设计

将协议分解为逻辑清晰的模块，每个模块负责特定的功能：

```xml
<protocolDefinition id="modular_protocol" name="模块化协议" length="1024" valueType="HEX">
    <!-- 通用头部模块 -->
    <protocolHeader id="common_header" name="通用头部" length="128" valueType="HEX">
        <iNode id="sync_pattern" name="同步模式" length="32" valueType="HEX" value="0xDEADBEEF"/>
        <iNode id="version" name="协议版本" length="8" valueType="INT" value="1"/>
        <iNode id="message_type" name="消息类型" length="8" valueType="INT"/>
        <iNode id="total_length" name="总长度" length="16" valueType="INT"/>
        <iNode id="sequence_number" name="序列号" length="32" valueType="INT"/>
        <iNode id="flags" name="标志位" length="16" valueType="HEX"/>
        <iNode id="reserved" name="保留字段" length="16" valueType="HEX" value="0x0000"/>
    </protocolHeader>
    
    <!-- 安全模块 -->
    <protocolBody id="security_section" name="安全段" length="256" valueType="HEX">
        <iNode id="auth_token" name="认证令牌" length="128" valueType="HEX"/>
        <iNode id="encryption_type" name="加密类型" length="8" valueType="INT"/>
        <iNode id="key_version" name="密钥版本" length="8" valueType="INT"/>
        <iNode id="security_flags" name="安全标志" length="16" valueType="HEX"/>
        <iNode id="security_reserved" name="安全保留" length="96" valueType="HEX" value="0x0"/>
    </protocolBody>
    
    <!-- 数据载荷模块 -->
    <protocolBody id="payload_section" name="载荷段" length="512" valueType="HEX">
        <!-- 根据消息类型动态配置 -->
    </protocolBody>
    
    <!-- 校验模块 -->
    <protocolTail id="integrity_section" name="完整性段" length="128" valueType="HEX">
        <iNode id="crc32" name="CRC32校验" length="32" valueType="HEX"/>
        <iNode id="hash_digest" name="哈希摘要" length="64" valueType="HEX"/>
        <iNode id="signature" name="数字签名" length="32" valueType="HEX"/>
    </protocolTail>
</protocolDefinition>
```

#### 2. 版本兼容性设计

设计时考虑未来的版本升级和向后兼容：

```xml
<protocolDefinition id="versioned_protocol" name="版本化协议" length="variable" valueType="HEX">
    <protocolHeader id="version_header" name="版本头" length="64" valueType="HEX">
        <iNode id="magic_number" name="魔数" length="32" valueType="HEX" value="0x12345678"/>
        <iNode id="major_version" name="主版本号" length="8" valueType="INT" value="1"/>
        <iNode id="minor_version" name="次版本号" length="8" valueType="INT" value="0"/>
        <iNode id="patch_version" name="补丁版本号" length="8" valueType="INT" value="0"/>
        <iNode id="compatibility_flags" name="兼容性标志" length="8" valueType="HEX"/>
    </protocolHeader>
    
    <!-- 基础字段 - 所有版本都支持 -->
    <protocolBody id="base_fields" name="基础字段" length="256" valueType="HEX">
        <iNode id="device_id" name="设备ID" length="32" valueType="INT"/>
        <iNode id="timestamp" name="时间戳" length="32" valueType="INT"/>
        <iNode id="data_type" name="数据类型" length="8" valueType="INT"/>
        <iNode id="data_length" name="数据长度" length="16" valueType="INT"/>
        <iNode id="basic_data" name="基础数据" length="168" valueType="HEX"/>
    </protocolBody>
    
    <!-- 扩展字段 - 版本1.1及以上 -->
    <protocolBody id="extended_fields_v11" name="扩展字段v1.1" length="128" valueType="HEX">
        <conditionalDependency conditionNode="minor_version" 
                              condition="value >= 1" 
                              action="ENABLE"
                              description="版本1.1及以上启用扩展字段"/>
        
        <iNode id="extended_info" name="扩展信息" length="64" valueType="HEX"/>
        <iNode id="quality_metrics" name="质量指标" length="32" valueType="INT"/>
        <iNode id="extended_flags" name="扩展标志" length="16" valueType="HEX"/>
        <iNode id="extended_reserved" name="扩展保留" length="16" valueType="HEX" value="0x0000"/>
    </protocolBody>
    
    <!-- 高级字段 - 版本2.0及以上 -->
    <protocolBody id="advanced_fields_v20" name="高级字段v2.0" length="256" valueType="HEX">
        <conditionalDependency conditionNode="major_version" 
                              condition="value >= 2" 
                              action="ENABLE"
                              description="版本2.0及以上启用高级字段"/>
        
        <iNode id="advanced_config" name="高级配置" length="128" valueType="HEX"/>
        <iNode id="performance_data" name="性能数据" length="64" valueType="HEX"/>
        <iNode id="diagnostic_info" name="诊断信息" length="64" valueType="HEX"/>
    </protocolBody>
</protocolDefinition>
```

#### 3. 错误处理和恢复

设计健壮的错误处理机制：

```xml
<protocolDefinition id="robust_protocol" name="健壮协议" length="512" valueType="HEX">
    <protocolHeader id="protocolHeader" name="协议头" length="128" valueType="HEX">
        <iNode id="sync_word" name="同步字" length="16" valueType="HEX" value="0xAA55"/>
        <iNode id="protocol_id" name="协议ID" length="16" valueType="HEX" value="0x1234"/>
        <iNode id="message_id" name="消息ID" length="32" valueType="INT"/>
        <iNode id="retry_count" name="重试次数" length="8" valueType="INT" value="0"/>
        <iNode id="error_recovery_flags" name="错误恢复标志" length="8" valueType="HEX"/>
        <iNode id="checksum_header" name="头部校验" length="16" valueType="HEX"/>
        <iNode id="header_reserved" name="头部保留" length="32" valueType="HEX" value="0x00000000"/>
    </protocolHeader>
    
    <protocolBody id="data_body" name="数据体" length="320" valueType="HEX">
        <!-- 数据完整性保护 -->
        <iNode id="data_sequence" name="数据序列号" length="16" valueType="INT"/>
        <iNode id="data_fragment_id" name="数据片段ID" length="8" valueType="INT"/>
        <iNode id="total_fragments" name="总片段数" length="8" valueType="INT"/>
        
        <!-- 实际数据 -->
        <iNode id="payload_data" name="载荷数据" length="256" valueType="HEX"/>
        
        <!-- 数据校验 -->
        <iNode id="data_checksum" name="数据校验和" length="32" valueType="HEX"/>
    </protocolBody>
    
    <protocolTail id="error_detection" name="错误检测" length="64" valueType="HEX">
        <iNode id="ecc_code" name="纠错码" length="32" valueType="HEX"/>
        <iNode id="integrity_hash" name="完整性哈希" length="32" valueType="HEX"/>
    </protocolTail>
</protocolDefinition>
```

### 性能优化建议

#### 1. 长度优化

合理设计字段长度，避免浪费：

```xml
<!-- ✅ 推荐：根据实际需求设计长度 -->
<iNode id="device_count" name="设备数量" length="8" valueType="INT"/>  <!-- 最大255个设备 -->
<iNode id="temperature" name="温度" length="16" valueType="INT"/>      <!-- -327.68°C ~ 327.67°C -->
<iNode id="timestamp" name="时间戳" length="32" valueType="INT"/>       <!-- Unix时间戳 -->

<!-- ❌ 不推荐：过度设计 -->
<iNode id="device_count" name="设备数量" length="32" valueType="INT"/>  <!-- 浪费24位 -->
<iNode id="temperature" name="温度" length="64" valueType="INT"/>      <!-- 浪费48位 -->
```

#### 2. 对齐优化

考虑硬件对齐要求：

```xml
<protocolBody id="aligned_structure" name="对齐结构" length="128" valueType="HEX">
    <!-- 32位对齐的字段组 -->
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>
    <iNode id="field2" name="字段2" length="32" valueType="INT"/>
    
    <!-- 16位字段组 -->
    <iNode id="field3" name="字段3" length="16" valueType="INT"/>
    <iNode id="field4" name="字段4" length="16" valueType="INT"/>
    
    <!-- 8位字段组 -->
    <iNode id="field5" name="字段5" length="8" valueType="INT"/>
    <iNode id="field6" name="字段6" length="8" valueType="INT"/>
    <iNode id="field7" name="字段7" length="8" valueType="INT"/>
    <iNode id="field8" name="字段8" length="8" valueType="INT"/>
</protocolBody>
```

#### 3. 表达式优化

避免复杂的表达式计算：

```xml
<!-- ✅ 推荐：简单表达式 -->
<iNode id="total_size" name="总大小" length="16" valueType="INT" 
      value="header_size + data_size"/>

<!-- ❌ 不推荐：复杂表达式 -->
<iNode id="complex_calc" name="复杂计算" length="32" valueType="INT" 
      value="sqrt(pow(x, 2) + pow(y, 2)) * sin(angle) + log(base, value)"/>
```

### 安全性考虑

#### 1. 数据验证

确保所有输入数据都经过验证：

```xml
<iNode id="user_input" name="用户输入" length="64" valueType="STRING">
    <!-- 长度验证 -->
    <validation type="LENGTH" minLength="1" maxLength="8" 
                description="用户输入长度必须在1-8字符之间"/>
    
    <!-- 格式验证 -->
    <validation type="PATTERN" pattern="^[a-zA-Z0-9]+$" 
                description="只允许字母和数字"/>
    
    <!-- 范围验证 -->
    <validation type="RANGE" minValue="0" maxValue="999999" 
                description="数值范围0-999999"/>
</iNode>
```

#### 2. 敏感信息保护

避免在日志中暴露敏感信息：

```xml
<iNode id="password" name="密码" length="128" valueType="STRING" 
      sensitive="true" logLevel="NONE">
    <description>用户密码，不记录到日志</description>
</iNode>

<iNode id="auth_token" name="认证令牌" length="256" valueType="HEX" 
      sensitive="true" logLevel="MASKED">
    <description>认证令牌，日志中显示为***</description>
</iNode>
```

### 调试和维护

#### 1. 详细的文档

为每个重要元素添加详细说明：

```xml
<protocolDefinition id="documented_protocol" name="文档化协议" length="512" valueType="HEX">
    <description>
        这是一个完整文档化的协议示例，展示了如何为协议的各个部分
        添加详细的说明和文档，以便于理解和维护。
    </description>
    
    <protocolHeader id="protocolHeader" name="协议头" length="128" valueType="HEX">
        <description>
            协议头包含协议识别、版本信息、消息类型等元数据。
            头部长度固定为128位，确保快速解析。
        </description>
        
        <iNode id="magic_number" name="魔数" length="32" valueType="HEX" value="0x12345678">
            <description>
                协议魔数，用于快速识别协议类型。
                值0x12345678是本协议的唯一标识。
                如果魔数不匹配，应立即拒绝处理。
            </description>
        </iNode>
    </protocolHeader>
</protocolDefinition>
```

#### 2. 版本信息管理

记录协议的版本变更历史：

```xml
<protocolDefinition id="versioned_protocol" name="版本化协议" 
          version="2.1.0" lastModified="2024-01-15" author="开发团队">
    
    <versionHistory>
        <version number="1.0.0" date="2023-01-01" description="初始版本"/>
        <version number="1.1.0" date="2023-06-01" description="添加扩展字段支持"/>
        <version number="2.0.0" date="2023-12-01" description="重大重构，不向后兼容"/>
        <version number="2.1.0" date="2024-01-15" description="添加新的安全特性"/>
    </versionHistory>
    
    <!-- 协议内容 -->
</protocolDefinition>
```

## 常见问题

### 配置问题

#### Q1: 长度不匹配错误

**问题**：子节点长度之和与父节点长度不匹配

```xml
<!-- ❌ 错误示例 -->
<protocolBody id="container" name="容器" length="100" valueType="HEX">
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>
    <iNode id="field2" name="字段2" length="64" valueType="HEX"/>
    <!-- 32 + 64 = 96 ≠ 100 -->
</protocolBody>
```

**解决方案**：
1. 重新计算长度确保匹配
2. 添加填充节点补齐差异
3. 使用动态长度计算

```xml
<!-- ✅ 正确示例 -->
<protocolBody id="container" name="容器" length="100" valueType="HEX">
    <iNode id="field1" name="字段1" length="32" valueType="INT"/>
    <iNode id="field2" name="字段2" length="64" valueType="HEX"/>
    <iNode id="padding" name="填充" length="4" valueType="HEX" value="0x0"/>
    <!-- 32 + 64 + 4 = 100 ✓ -->
</protocolBody>
```

#### Q2: 节点ID重复

**问题**：在同一协议中使用了重复的节点ID

```xml
<!-- ❌ 错误示例 -->
<protocolDefinition id="test_protocol">
    <protocolHeader id="protocolHeader">
        <iNode id="version" name="版本"/>
    </protocolHeader>
    <protocolBody id="protocolBody">
        <iNode id="version" name="数据版本"/>  <!-- ID重复 -->
    </protocolBody>
</protocolDefinition>
```

**解决方案**：使用唯一的、有意义的ID

```xml
<!-- ✅ 正确示例 -->
<protocolDefinition id="test_protocol">
    <protocolHeader id="protocolHeader">
        <iNode id="protocol_version" name="协议版本"/>
    </protocolHeader>
    <protocolBody id="protocolBody">
        <iNode id="data_version" name="数据版本"/>
    </protocolBody>
</protocolDefinition>
```

#### Q3: 表达式引用错误

**问题**：表达式中引用了不存在的节点

```xml
<!-- ❌ 错误示例 -->
<iNode id="total" name="总计" length="16" valueType="INT" 
      value="count * size"/>  <!-- count和size节点不存在 -->
```

**解决方案**：确保引用的节点存在且在正确的作用域内

```xml
<!-- ✅ 正确示例 -->
<protocolBody id="data_section">
    <iNode id="count" name="计数" length="8" valueType="INT" value="10"/>
    <iNode id="size" name="大小" length="8" valueType="INT" value="32"/>
    <iNode id="total" name="总计" length="16" valueType="INT" 
          value="count * size"/>
</protocolBody>
```

### 性能问题

#### Q4: 复杂表达式导致性能下降

**问题**：使用了过于复杂的表达式计算

**解决方案**：
1. 简化表达式逻辑
2. 使用中间变量分步计算
3. 预计算常量值

```xml
<!-- ❌ 复杂表达式 -->
<iNode id="result" name="结果" length="32" valueType="INT" 
      value="sqrt(pow(a, 2) + pow(b, 2)) * sin(angle * PI / 180)"/>

<!-- ✅ 分步计算 -->
<iNode id="a_squared" name="A平方" length="32" valueType="INT" value="pow(a, 2)"/>
<iNode id="b_squared" name="B平方" length="32" valueType="INT" value="pow(b, 2)"/>
<iNode id="distance" name="距离" length="32" valueType="INT" value="sqrt(a_squared + b_squared)"/>
<iNode id="angle_rad" name="角度弧度" length="32" valueType="FLOAT" value="angle * 0.017453"/>
<iNode id="result" name="结果" length="32" valueType="INT" value="distance * sin(angle_rad)"/>
```

### 兼容性问题

#### Q5: 版本兼容性处理

**问题**：如何处理不同版本协议的兼容性

**解决方案**：使用条件依赖和版本控制

```xml
<protocolDefinition id="compatible_protocol">
    <protocolHeader id="protocolHeader">
        <iNode id="version" name="版本" length="8" valueType="INT"/>
    </protocolHeader>
    
    <protocolBody id="protocolBody">
        <!-- 所有版本都有的基础字段 -->
        <iNode id="basic_data" name="基础数据" length="64" valueType="HEX"/>
        
        <!-- 版本2及以上的字段 -->
        <iNode id="extended_data" name="扩展数据" length="32" valueType="HEX">
            <conditionalDependency conditionNode="version" 
                                  condition="value >= 2" 
                                  action="ENABLE"/>
        </iNode>
        
        <!-- 版本1的填充 -->
        <iNode id="v1_padding" name="版本1填充" length="32" valueType="HEX" value="0x0">
            <conditionalDependency conditionNode="version" 
                                  condition="value == 1" 
                                  action="ENABLE"/>
        </iNode>
    </protocolBody>
</protocolDefinition>
```

## 故障排除

### 调试工具和技巧

#### 1. 启用详细日志

在开发和调试阶段启用详细的日志输出：

```xml
<protocolDefinition id="debug_protocol" name="调试协议" debugLevel="VERBOSE">
    <iNode id="debug_field" name="调试字段" length="32" valueType="INT" 
          logLevel="DEBUG" value="calculated_value">
        <description>调试时会输出详细的计算过程</description>
    </iNode>
</protocolDefinition>
```

#### 2. 使用测试数据

创建专门的测试配置来验证协议：

```xml
<protocolDefinition id="test_protocol" name="测试协议" testMode="true">
    <testData>
        <testCase name="正常情况" input="AA55010064..." expectedOutput="..."/>
        <testCase name="边界情况" input="AA55FF00FF..." expectedOutput="..."/>
        <testCase name="错误情况" input="FFFF..." expectError="true"/>
    </testData>
    
    <!-- 协议定义 -->
</protocolDefinition>
```

#### 3. 分步验证

将复杂的协议分解为小的部分进行验证：

```xml
<!-- 先验证头部 -->
<protocolDefinition id="header_test" name="头部测试" length="64">
    <protocolHeader id="protocolHeader" name="协议头" length="64">
        <!-- 只包含头部字段 -->
    </protocolHeader>
</protocolDefinition>

<!-- 再验证完整协议 -->
<protocolDefinition id="full_test" name="完整测试" length="512">
    <!-- 包含所有部分 -->
</protocolDefinition>
```

### 常见错误诊断

#### 1. 解析失败

**症状**：协议解析时抛出异常或返回错误

**诊断步骤**：
1. 检查输入数据的长度是否匹配协议定义
2. 验证数据格式是否正确（十六进制、编码等）
3. 检查必需字段是否都有值
4. 验证表达式引用是否正确

#### 2. 长度计算错误

**症状**：实际数据长度与期望长度不匹配

**诊断步骤**：
1. 逐层检查每个容器的长度定义
2. 验证动态长度表达式的计算结果
3. 检查条件依赖是否影响了长度计算
4. 确认填充配置是否正确

#### 3. 条件依赖不生效

**症状**：条件依赖没有按预期工作

**诊断步骤**：
1. 检查条件节点是否存在且有值
2. 验证条件表达式语法是否正确
3. 确认优先级设置是否合理
4. 检查是否存在循环依赖

### 性能调优

#### 1. 减少表达式复杂度

```xml
<!-- ❌ 复杂表达式 -->
<iNode value="complex_function(a, b, c, d, e)"/>

<!-- ✅ 简化表达式 -->
<iNode id="temp1" value="simple_function(a, b)"/>
<iNode id="temp2" value="simple_function(c, d)"/>
<iNode value="combine(temp1, temp2, e)"/>
```

#### 2. 优化条件依赖

```xml
<!-- ❌ 频繁的条件检查 -->
<iNode id="field1">
    <conditionalDependency conditionNode="flag" condition="complex_condition()"/>
</iNode>
<iNode id="field2">
    <conditionalDependency conditionNode="flag" condition="complex_condition()"/>
</iNode>

<!-- ✅ 预计算条件结果 -->
<iNode id="condition_result" value="complex_condition()"/>
<iNode id="field1">
    <conditionalDependency conditionNode="condition_result" condition="value == true"/>
</iNode>
<iNode id="field2">
    <conditionalDependency conditionNode="condition_result" condition="value == true"/>
</iNode>
```

#### 3. 合理使用缓存

对于重复计算的值，考虑使用缓存机制：

```xml
<protocolDefinition cacheEnabled="true" cacheSize="1000">
    <!-- 启用缓存的协议配置 -->
</protocolDefinition>
```

---

## 总结

XML协议配置是一个功能强大且灵活的协议定义系统。通过合理使用本手册中介绍的各种特性，您可以：

1. **快速定义复杂协议** - 使用声明式的XML配置
2. **实现动态行为** - 通过条件依赖和表达式系统
3. **确保数据完整性** - 使用填充和校验机制
4. **提高可维护性** - 通过模块化设计和详细文档
5. **保证性能** - 遵循最佳实践和优化建议

记住，好的协议设计不仅要满足功能需求，还要考虑性能、安全性、可维护性和扩展性。希望本手册能够帮助您设计出高质量的协议配置。