# Java类配置协议手册

## 目录

1. [概述](#概述)
2. [基本概念](#基本概念)
3. [注解系统](#注解系统)
4. [协议类设计](#协议类设计)
5. [字段配置](#字段配置)
6. [数据类型映射](#数据类型映射)
7. [条件依赖注解](#条件依赖注解)
8. [填充注解](#填充注解)
9. [枚举和常量](#枚举和常量)
10. [继承和组合](#继承和组合)
11. [最佳实践](#最佳实践)
12. [常见问题](#常见问题)
13. [故障排除](#故障排除)

## 概述

Java类配置协议是框架提供的另一种协议定义方式，它允许开发者使用Java类和注解来定义协议结构。这种方式特别适合Java开发者，提供了类型安全、IDE支持和编译时检查等优势。

### 设计理念

**为什么选择Java类配置？**

1. **类型安全** - 编译时检查，减少运行时错误
2. **IDE支持** - 完整的代码补全、重构和导航支持
3. **面向对象** - 利用Java的继承、封装、多态特性
4. **工具集成** - 与现有的Java工具链无缝集成
5. **代码复用** - 通过继承和组合实现代码复用

**解决的问题**

- **开发效率** - 减少配置文件的维护成本
- **类型错误** - 编译时发现类型不匹配问题
- **重构支持** - IDE可以安全地重构协议定义
- **团队协作** - 利用Java的模块化和包管理

**与XML配置的对比**

| 特性 | Java类配置 | XML配置 |
|------|------------|---------|
| 类型安全 | ✅ 编译时检查 | ❌ 运行时检查 |
| IDE支持 | ✅ 完整支持 | ⚠️ 有限支持 |
| 学习曲线 | ⚠️ 需要Java知识 | ✅ 相对简单 |
| 动态性 | ❌ 编译时确定 | ✅ 运行时加载 |
| 代码复用 | ✅ 继承和组合 | ⚠️ 有限复用 |
| 工具支持 | ✅ 丰富的工具 | ⚠️ 专用工具 |

## 基本概念

### 协议类层次结构

Java类配置采用面向对象的方式组织协议结构：

```java
@Protocol
public class MyProtocol {
    @Header
    private HeaderSection protocolHeader;
    
    @Body  
    private BodySection protocolBody;
    
    @Check
    private CheckSection protocolTail;
}
```

**为什么这样设计？**

1. **自然映射** - Java类的结构直接映射到协议结构
2. **类型安全** - 每个字段都有明确的类型
3. **封装性** - 可以控制字段的访问权限
4. **扩展性** - 通过继承轻松扩展协议

### 核心注解

#### 1. @Protocol（协议注解）
标识一个类为协议定义类。

```java
@Protocol(
    id = "example_protocol",
    name = "示例协议", 
    length = 800,
    valueType = ValueType.HEX,
    description = "这是一个示例协议"
)
public class ExampleProtocol {
    // 协议字段
}
```

**属性说明**：
- `id`: 协议唯一标识符
- `name`: 协议显示名称
- `length`: 协议总长度（位数）
- `valueType`: 默认值类型
- `description`: 协议描述

#### 2. @Header（协议头注解）
标识协议头部分。

```java
@Header(
    id = "protocol_header",
    name = "协议头",
    length = 64,
    valueType = ValueType.HEX
)
public class ProtocolHeader {
    // 头部字段
}
```

#### 3. @Body（协议体注解）
标识协议体部分，支持嵌套。

```java
@Body(
    id = "protocol_body",
    name = "协议体", 
    length = 640,
    valueType = ValueType.HEX
)
public class ProtocolBody {
    // 体部字段
    
    @Body(id = "nested_section", name = "嵌套段", length = 320)
    private NestedSection nestedSection;
}
```

#### 4. @Check（校验注解）
标识协议校验部分。

```java
@Check(
    id = "protocol_check",
    name = "校验部分",
    length = 96,
    valueType = ValueType.HEX
)
public class ProtocolCheck {
    // 校验字段
}
```

#### 5. @Node（字段注解）
标识协议中的具体数据字段。

```java
@Node(
    id = "field_id",
    name = "字段名称",
    length = 32,
    valueType = ValueType.INT,
    value = "12345",
    description = "字段描述"
)
private int fieldValue;
```

### 基本协议示例

```java
@Protocol(
    id = "basic_protocol",
    name = "基础协议",
    length = 800,
    valueType = ValueType.HEX,
    description = "展示Java类配置的基础协议"
)
public class BasicProtocol {
    
    @Header(id = "protocolHeader", name = "协议头", length = 64)
    private ProtocolHeader protocolHeader;
    
    @Body(id = "protocolBody", name = "协议体", length = 640)
    private ProtocolBody protocolBody;
    
    @Check(id = "checksum", name = "校验", length = 96)
    private ProtocolCheck checksum;
    
    // 构造函数
    public BasicProtocol() {
        this.protocolHeader = new ProtocolHeader();
        this.protocolBody = new ProtocolBody();
        this.checksum = new ProtocolCheck();
    }
    
    // Getter和Setter方法
    public ProtocolHeader getHeader() { return protocolHeader; }
    public void setHeader(ProtocolHeader protocolHeader) { this.protocolHeader = protocolHeader; }
    
    public ProtocolBody getBody() { return protocolBody; }
    public void setBody(ProtocolBody protocolBody) { this.protocolBody = protocolBody; }
    
    public ProtocolCheck getChecksum() { return checksum; }
    public void setChecksum(ProtocolCheck checksum) { this.checksum = checksum; }
}

@Header(id = "protocolHeader", name = "协议头", length = 64, valueType = ValueType.HEX)
public class ProtocolHeader {
    
    @Node(id = "sync_word", name = "同步字", length = 16, 
          valueType = ValueType.HEX, value = "0xAA55")
    private String syncWord;
    
    @Node(id = "version", name = "版本号", length = 8, 
          valueType = ValueType.INT, value = "1")
    private int version;
    
    @Node(id = "type", name = "消息类型", length = 8, 
          valueType = ValueType.INT, value = "1")
    private int messageType;
    
    @Node(id = "length", name = "数据长度", length = 16, 
          valueType = ValueType.INT, value = "80")
    private int dataLength;
    
    @Node(id = "sequence", name = "序列号", length = 16, 
          valueType = ValueType.INT, value = "1")
    private int sequenceNumber;
    
    // 构造函数、Getter和Setter方法
    public ProtocolHeader() {
        this.syncWord = "0xAA55";
        this.version = 1;
        this.messageType = 1;
        this.dataLength = 80;
        this.sequenceNumber = 1;
    }
    
    // ... getter/setter方法
}

@Body(id = "protocolBody", name = "协议体", length = 640, valueType = ValueType.HEX)
public class ProtocolBody {
    
    @Node(id = "command", name = "命令字", length = 16, 
          valueType = ValueType.HEX, value = "0x1001")
    private String command;
    
    @Node(id = "data", name = "数据内容", length = 624, 
          valueType = ValueType.HEX)
    private String data;
    
    // 构造函数、Getter和Setter方法
    public ProtocolBody() {
        this.command = "0x1001";
    }
    
    // ... getter/setter方法
}

@Check(id = "checksum", name = "校验", length = 96, valueType = ValueType.HEX)
public class ProtocolCheck {
    
    @Node(id = "crc16", name = "CRC16校验", length = 16, 
          valueType = ValueType.HEX, value = "0x1234")
    private String crc16;
    
    @Node(id = "reserved", name = "保留字段", length = 64, 
          valueType = ValueType.HEX, value = "0x0")
    private String reserved;
    
    @Node(id = "end_flag", name = "结束标志", length = 16, 
          valueType = ValueType.HEX, value = "0x55AA")
    private String endFlag;
    
    // 构造函数、Getter和Setter方法
    public ProtocolCheck() {
        this.crc16 = "0x1234";
        this.reserved = "0x0";
        this.endFlag = "0x55AA";
    }
    
    // ... getter/setter方法
}
```

## 注解系统

### 注解设计原理

框架的注解系统基于Java的标准注解机制，通过反射在运行时解析注解信息。这种设计的优势：

1. **标准化** - 使用Java标准的注解语法
2. **工具支持** - IDE可以提供完整的支持
3. **类型安全** - 注解参数在编译时检查
4. **扩展性** - 可以轻松添加新的注解

### 核心注解详解

#### @Protocol注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Protocol {
    /**
     * 协议唯一标识符
     */
    String id();
    
    /**
     * 协议显示名称
     */
    String name() default "";
    
    /**
     * 协议总长度（位数）
     */
    int length() default -1;
    
    /**
     * 默认值类型
     */
    ValueType valueType() default ValueType.HEX;
    
    /**
     * 协议描述
     */
    String description() default "";
    
    /**
     * 协议版本
     */
    String version() default "1.0.0";
    
    /**
     * 作者信息
     */
    String author() default "";
    
    /**
     * 最后修改时间
     */
    String lastModified() default "";
}
```

**使用示例**：

```java
@Protocol(
    id = "sensor_data_protocol",
    name = "传感器数据协议",
    length = 512,
    valueType = ValueType.HEX,
    description = "用于传输传感器数据的协议",
    version = "2.1.0",
    author = "开发团队",
    lastModified = "2024-01-15"
)
public class SensorDataProtocol {
    // 协议内容
}
```

#### @Node注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Node {
    /**
     * 节点唯一标识符
     */
    String id();
    
    /**
     * 节点显示名称
     */
    String name() default "";
    
    /**
     * 节点长度（位数）
     */
    int length();
    
    /**
     * 数据类型
     */
    ValueType valueType() default ValueType.HEX;
    
    /**
     * 默认值
     */
    String value() default "";
    
    /**
     * 节点描述
     */
    String description() default "";
    
    /**
     * 是否必需
     */
    boolean required() default true;
    
    /**
     * 枚举类型（如果适用）
     */
    Class<? extends Enum<?>> enumType() default NullEnum.class;
    
    /**
     * 验证规则
     */
    String validation() default "";
    
    /**
     * 是否敏感信息
     */
    boolean sensitive() default false;
}
```

**使用示例**：

```java
public class DeviceInfo {
    
    @Node(
        id = "device_id",
        name = "设备ID", 
        length = 32,
        valueType = ValueType.INT,
        description = "设备的唯一标识符",
        required = true,
        validation = "range(1, 999999)"
    )
    private int deviceId;
    
    @Node(
        id = "device_type",
        name = "设备类型",
        length = 8,
        valueType = ValueType.INT,
        enumType = DeviceType.class,
        description = "设备类型枚举值"
    )
    private DeviceType channelType;
    
    @Node(
        id = "device_name",
        name = "设备名称",
        length = 128,
        valueType = ValueType.STRING,
        description = "设备的显示名称",
        validation = "length(1, 16)"
    )
    private String deviceName;
    
    @Node(
        id = "auth_token",
        name = "认证令牌",
        length = 256,
        valueType = ValueType.HEX,
        description = "设备认证令牌",
        sensitive = true
    )
    private String authToken;
}
```

### 容器注解

#### @Header注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Header {
    String id();
    String name() default "";
    int length() default -1;
    ValueType valueType() default ValueType.HEX;
    String description() default "";
}
```

#### @Body注解

```java
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {
    String id();
    String name() default "";
    int length() default -1;
    ValueType valueType() default ValueType.HEX;
    String description() default "";
    
    /**
     * 是否支持嵌套
     */
    boolean nested() default false;
    
    /**
     * 重复次数（用于数组结构）
     */
    int repeat() default 1;
    
    /**
     * 重复次数表达式（动态数组）
     */
    String repeatExpression() default "";
}
```

**嵌套Body示例**：

```java
@Body(id = "main_body", name = "主体", length = 1024)
public class MainBody {
    
    @Node(id = "section_count", name = "段数量", length = 8, valueType = ValueType.INT)
    private int sectionCount;
    
    @Body(
        id = "data_section", 
        name = "数据段", 
        length = 256,
        nested = true,
        repeat = 4  // 固定4个段
    )
    private DataSection[] dataSections;
    
    @Body(
        id = "dynamic_section",
        name = "动态段",
        length = 128,
        nested = true,
        repeatExpression = "sectionCount"  // 根据sectionCount动态重复
    )
    private DynamicSection[] dynamicSections;
}

@Body(id = "data_section", name = "数据段", length = 256)
public class DataSection {
    
    @Node(id = "section_id", name = "段ID", length = 16, valueType = ValueType.INT)
    private int sectionId;
    
    @Node(id = "section_data", name = "段数据", length = 240, valueType = ValueType.HEX)
    private String sectionData;
}
```

#### @Check注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Check {
    String id();
    String name() default "";
    int length() default -1;
    ValueType valueType() default ValueType.HEX;
    String description() default "";
    
    /**
     * 校验算法类型
     */
    ChecksumType checksumType() default ChecksumType.NONE;
    
    /**
     * 校验范围
     */
    String checksumRange() default "ALL";
}
```

**校验示例**：

```java
@Check(
    id = "protocol_check",
    name = "协议校验",
    length = 64,
    checksumType = ChecksumType.CRC16,
    checksumRange = "protocolHeader,protocolBody"
)
public class ProtocolCheck {
    
    @Node(id = "crc16", name = "CRC16校验", length = 16, valueType = ValueType.HEX)
    private String crc16;
    
    @Node(id = "timestamp", name = "时间戳", length = 32, valueType = ValueType.INT)
    private long timestamp;
    
    @Node(id = "reserved", name = "保留", length = 16, valueType = ValueType.HEX, value = "0x0000")
    private String reserved;
}
```

### 注解处理器

框架提供了注解处理器来解析和验证注解配置：

```java
@Component
public class ProtocolAnnotationProcessor {
    
    /**
     * 处理协议类注解
     */
    public ProtocolDefinition processProtocol(Class<?> protocolClass) {
        // 验证类是否有@Protocol注解
        if (!protocolClass.isAnnotationPresent(Protocol.class)) {
            throw new ProtocolConfigurationException(
                "Class " + protocolClass.getName() + " is not annotated with @ProtocolDefinition");
        }
        
        Protocol protocolAnnotation = protocolClass.getAnnotation(Protocol.class);
        ProtocolDefinition definition = new ProtocolDefinition();
        
        // 设置协议基本信息
        definition.setId(protocolAnnotation.id());
        definition.setName(protocolAnnotation.name());
        definition.setLength(protocolAnnotation.length());
        definition.setValueType(protocolAnnotation.valueType());
        definition.setDescription(protocolAnnotation.description());
        
        // 处理字段
        processFields(protocolClass, definition);
        
        // 验证配置
        validateConfiguration(definition);
        
        return definition;
    }
    
    /**
     * 处理字段注解
     */
    private void processFields(Class<?> clazz, ProtocolDefinition definition) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Node.class)) {
                processNodeField(field, definition);
            } else if (field.isAnnotationPresent(Header.class)) {
                processHeaderField(field, definition);
            } else if (field.isAnnotationPresent(Body.class)) {
                processBodyField(field, definition);
            } else if (field.isAnnotationPresent(Check.class)) {
                processCheckField(field, definition);
            }
        }
    }
    
    /**
     * 验证配置的一致性
     */
    private void validateConfiguration(ProtocolDefinition definition) {
        // 验证长度一致性
        validateLengthConsistency(definition);
        
        // 验证ID唯一性
        validateIdUniqueness(definition);
        
        // 验证引用完整性
        validateReferenceIntegrity(definition);
    }
}
```

## 协议类设计

### 设计原则

#### 1. 单一职责原则

每个协议类应该只负责一个特定的功能或数据结构：

```java
// ✅ 推荐：职责单一的协议头
@Header(id = "device_header", name = "设备协议头", length = 128)
public class DeviceProtocolHeader {
    @Node(id = "device_id", name = "设备ID", length = 32, valueType = ValueType.INT)
    private int deviceId;
    
    @Node(id = "protocol_version", name = "协议版本", length = 8, valueType = ValueType.INT)
    private int protocolVersion;
    
    @Node(id = "message_type", name = "消息类型", length = 8, valueType = ValueType.INT)
    private MessageType messageType;
    
    @Node(id = "timestamp", name = "时间戳", length = 32, valueType = ValueType.INT)
    private long timestamp;
    
    @Node(id = "flags", name = "标志位", length = 16, valueType = ValueType.HEX)
    private String flags;
    
    @Node(id = "reserved", name = "保留", length = 32, valueType = ValueType.HEX, value = "0x00000000")
    private String reserved;
}

// ❌ 不推荐：职责混乱的类
@Protocol(id = "mixed_protocol", name = "混合协议", length = 1024)
public class MixedProtocol {
    // 头部字段
    @Node(id = "sync_word", name = "同步字", length = 16, valueType = ValueType.HEX)
    private String syncWord;
    
    // 数据字段
    @Node(id = "sensor_data", name = "传感器数据", length = 256, valueType = ValueType.HEX)
    private String sensorData;
    
    // 校验字段
    @Node(id = "checksum", name = "校验和", length = 16, valueType = ValueType.HEX)
    private String checksum;
    
    // 业务逻辑方法（不应该在协议类中）
    public void processSensorData() { /* ... */ }
}
```

#### 2. 开闭原则

协议类应该对扩展开放，对修改关闭：

```java
// 基础协议类
@Protocol(id = "base_protocol", name = "基础协议", length = 256)
public abstract class BaseProtocol {
    
    @Header(id = "common_header", name = "通用头部", length = 64)
    protected CommonHeader protocolHeader;
    
    @Check(id = "common_check", name = "通用校验", length = 32)
    protected CommonCheck protocolTail;
    
    // 构造函数
    public BaseProtocol() {
        this.protocolHeader = new CommonHeader();
        this.protocolTail = new CommonCheck();
    }
    
    // 抽象方法，由子类实现
    public abstract void processData();
    
    // 通用方法
    public boolean validateChecksum() {
        // 校验逻辑
        return true;
    }
}

// 扩展协议类 - 传感器协议
@Protocol(id = "sensor_protocol", name = "传感器协议", length = 512)
public class SensorProtocol extends BaseProtocol {
    
    @Body(id = "sensor_body", name = "传感器数据体", length = 416)
    private SensorBody sensorBody;
    
    public SensorProtocol() {
        super();
        this.sensorBody = new SensorBody();
    }
    
    @Override
    public void processData() {
        // 传感器特定的数据处理逻辑
        processSensorData();
    }
    
    private void processSensorData() {
        // 具体实现
    }
}

// 扩展协议类 - 控制协议
@Protocol(id = "control_protocol", name = "控制协议", length = 384)
public class ControlProtocol extends BaseProtocol {
    
    @Body(id = "control_body", name = "控制数据体", length = 288)
    private ControlBody controlBody;
    
    public ControlProtocol() {
        super();
        this.controlBody = new ControlBody();
    }
    
    @Override
    public void processData() {
        // 控制特定的数据处理逻辑
        processControlData();
    }
    
    private void processControlData() {
        // 具体实现
    }
}
```

#### 3. 里氏替换原则

子类必须能够替换其父类：

```java
// 基础消息类
@Body(id = "base_message", name = "基础消息", length = 256)
public abstract class BaseMessage {
    
    @Node(id = "message_id", name = "消息ID", length = 32, valueType = ValueType.INT)
    protected int messageId;
    
    @Node(id = "timestamp", name = "时间戳", length = 32, valueType = ValueType.INT)
    protected long timestamp;
    
    // 基础验证方法
    public boolean validate() {
        return messageId > 0 && timestamp > 0;
    }
    
    // 抽象方法
    public abstract String getMessageType();
}

// 数据消息 - 完全兼容父类
@Body(id = "data_message", name = "数据消息", length = 256)
public class DataMessage extends BaseMessage {
    
    @Node(id = "data_content", name = "数据内容", length = 192, valueType = ValueType.HEX)
    private String dataContent;
    
    @Override
    public String getMessageType() {
        return "DATA";
    }
    
    // 重写验证方法，增强但不改变契约
    @Override
    public boolean validate() {
        return super.validate() && dataContent != null && !dataContent.isEmpty();
    }
}

// 控制消息 - 完全兼容父类
@Body(id = "control_message", name = "控制消息", length = 256)
public class ControlMessage extends BaseMessage {
    
    @Node(id = "command_code", name = "命令码", length = 16, valueType = ValueType.INT)
    private int commandCode;
    
    @Node(id = "parameters", name = "参数", length = 176, valueType = ValueType.HEX)
    private String parameters;
    
    @Override
    public String getMessageType() {
        return "CONTROL";
    }
    
    // 重写验证方法，增强但不改变契约
    @Override
    public boolean validate() {
        return super.validate() && commandCode > 0;
    }
}

// 使用示例 - 里氏替换原则
public class MessageProcessor {
    
    public void processMessage(BaseMessage message) {
        // 可以接受任何BaseMessage的子类
        if (message.validate()) {
            System.out.println("Processing " + message.getMessageType() + " message");
            // 处理逻辑
        }
    }
    
    public static void main(String[] args) {
        MessageProcessor processor = new MessageProcessor();
        
        // 可以传入任何子类实例
        processor.processMessage(new DataMessage());
        processor.processMessage(new ControlMessage());
    }
}
```

### 类结构设计模式

#### 1. 组合模式

使用组合来构建复杂的协议结构：

```java
@Protocol(id = "composite_protocol", name = "组合协议", length = 1024)
public class CompositeProtocol {
    
    @Header(id = "protocolHeader", name = "协议头", length = 128)
    private ProtocolHeader protocolHeader;
    
    @Body(id = "protocolBody", name = "协议体", length = 768)
    private ProtocolBody protocolBody;
    
    @Check(id = "protocolTail", name = "校验", length = 128)
    private ProtocolCheck protocolTail;
    
    // 组合构造
    public CompositeProtocol() {
        this.protocolHeader = new ProtocolHeader();
        this.protocolBody = new ProtocolBody();
        this.protocolTail = new ProtocolCheck();
    }
}

@Body(id = "protocol_body", name = "协议体", length = 768)
public class ProtocolBody {
    
    // 组合多个子组件
    @Body(id = "device_info", name = "设备信息", length = 256, nested = true)
    private DeviceInfo deviceInfo;
    
    @Body(id = "sensor_data", name = "传感器数据", length = 256, nested = true)
    private SensorData sensorData;
    
    @Body(id = "status_info", name = "状态信息", length = 256, nested = true)
    private StatusInfo statusInfo;
    
    public ProtocolBody() {
        this.deviceInfo = new DeviceInfo();
        this.sensorData = new SensorData();
        this.statusInfo = new StatusInfo();
    }
}
```

#### 2. 工厂模式

使用工厂模式创建协议实例：

```java
// 协议工厂接口
public interface ProtocolFactory {
    <T> T createProtocol(Class<T> protocolClass);
    <T> T createProtocol(String protocolId);
}

// 协议工厂实现
@Component
public class DefaultProtocolFactory implements ProtocolFactory {
    
    private final Map<String, Class<?>> protocolRegistry = new HashMap<>();
    
    public DefaultProtocolFactory() {
        // 注册协议类
        registerProtocol("sensor_protocol", SensorProtocol.class);
        registerProtocol("control_protocol", ControlProtocol.class);
        registerProtocol("status_protocol", StatusProtocol.class);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProtocol(Class<T> protocolClass) {
        try {
            // 使用反射创建实例
            Constructor<T> constructor = protocolClass.getDeclaredConstructor();
            T instance = constructor.newInstance();
            
            // 初始化默认值
            initializeDefaultValues(instance);
            
            return instance;
        } catch (Exception e) {
            throw new ProtocolCreationException("Failed to create protocolDefinition instance", e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProtocol(String protocolId) {
        Class<?> protocolClass = protocolRegistry.get(protocolId);
        if (protocolClass == null) {
            throw new ProtocolNotFoundException("ProtocolDefinition not found: " + protocolId);
        }
        
        return (T) createProtocol(protocolClass);
    }
    
    private void registerProtocol(String id, Class<?> protocolClass) {
        protocolRegistry.put(id, protocolClass);
    }
    
    private void initializeDefaultValues(Object instance) {
        // 通过反射设置注解中定义的默认值
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Node.class)) {
                Node nodeAnnotation = field.getAnnotation(Node.class);
                String defaultValue = nodeAnnotation.value();
                
                if (!defaultValue.isEmpty()) {
                    try {
                        field.setAccessible(true);
                        Object convertedValue = convertValue(defaultValue, field.getType(), nodeAnnotation.valueType());
                        field.set(instance, convertedValue);
                    } catch (Exception e) {
                        // 记录警告但不中断创建过程
                        System.err.println("Failed to set default value for field: " + field.getName());
                    }
                }
            }
        }
    }
    
    private Object convertValue(String value, Class<?> channelType, ValueType valueType) {
        // 值转换逻辑
        if (channelType == int.class || channelType == Integer.class) {
            return Integer.parseInt(value);
        } else if (channelType == long.class || channelType == Long.class) {
            return Long.parseLong(value);
        } else if (channelType == String.class) {
            return value;
        }
        // 更多类型转换...
        
        return value;
    }
}

// 使用示例
@Service
public class ProtocolService {
    
    @Autowired
    private ProtocolFactory protocolFactory;
    
    public void processProtocol(String protocolId, byte[] data) {
        // 根据ID创建协议实例
        Object protocolDefinition = protocolFactory.createProtocol(protocolId);
        
        // 处理协议数据
        // ...
    }
    
    public SensorProtocol createSensorProtocol() {
        // 根据类型创建协议实例
        return protocolFactory.createProtocol(SensorProtocol.class);
    }
}
```

## 字段配置

### 字段类型映射

Java类配置支持将Java字段类型自动映射到协议数据类型：

#### 1. 基本类型映射

```java
public class TypeMappingExample {
    
    // 整数类型
    @Node(id = "byte_field", name = "字节字段", length = 8, valueType = ValueType.INT)
    private byte byteValue;
    
    @Node(id = "short_field", name = "短整型字段", length = 16, valueType = ValueType.INT)
    private short shortValue;
    
    @Node(id = "int_field", name = "整型字段", length = 32, valueType = ValueType.INT)
    private int intValue;
    
    @Node(id = "long_field", name = "长整型字段", length = 64, valueType = ValueType.INT)
    private long longValue;
    
    // 浮点类型
    @Node(id = "float_field", name = "单精度浮点", length = 32, valueType = ValueType.FLOAT)
    private float floatValue;
    
    @Node(id = "double_field", name = "双精度浮点", length = 64, valueType = ValueType.FLOAT)
    private double doubleValue;
    
    // 布尔类型
    @Node(id = "boolean_field", name = "布尔字段", length = 1, valueType = ValueType.BOOLEAN)
    private boolean booleanValue;
    
    // 字符串类型
    @Node(id = "string_field", name = "字符串字段", length = 128, valueType = ValueType.STRING)
    private String stringValue;
    
    // 十六进制字符串
    @Node(id = "hex_field", name = "十六进制字段", length = 64, valueType = ValueType.HEX)
    private String hexValue;
}
```

#### 2. 包装类型映射

```java
public class WrapperTypeExample {
    
    // 包装类型 - 支持null值
    @Node(id = "integer_wrapper", name = "整型包装", length = 32, valueType = ValueType.INT, required = false)
    private Integer integerValue;
    
    @Node(id = "long_wrapper", name = "长整型包装", length = 64, valueType = ValueType.INT, required = false)
    private Long longValue;
    
    @Node(id = "float_wrapper", name = "浮点包装", length = 32, valueType = ValueType.FLOAT, required = false)
    private Float floatValue;
    
    @Node(id = "double_wrapper", name = "双精度包装", length = 64, valueType = ValueType.FLOAT, required = false)
    private Double doubleValue;
    
    @Node(id = "boolean_wrapper", name = "布尔包装", length = 1, valueType = ValueType.BOOLEAN, required = false)
    private Boolean booleanValue;
}
```

#### 3. 枚举类型映射

```java
// 定义枚举
public enum DeviceType {
    SENSOR(1, "传感器"),
    ACTUATOR(2, "执行器"),
    CONTROLLER(3, "控制器"),
    GATEWAY(4, "网关");
    
    private final int code;
    private final String description;
    
    DeviceType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() { return code; }
    public String getDescription() { return description; }
    
    public static DeviceType fromCode(int code) {
        for (DeviceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type code: " + code);
    }
}

// 使用枚举字段
public class DeviceMessage {
    
    @Node(
        id = "device_type",
        name = "设备类型",
        length = 8,
        valueType = ValueType.INT,
        enumType = DeviceType.class,
        description = "设备类型枚举"
    )
    private DeviceType channelType;
    
    @Node(
        id = "device_status",
        name = "设备状态",
        length = 8,
        valueType = ValueType.INT,
        enumType = DeviceStatus.class
    )
    private DeviceStatus status;
}
```

#### 4. 数组类型映射

```java
public class ArrayTypeExample {
    
    // 固定长度数组
    @Node(
        id = "sensor_readings",
        name = "传感器读数",
        length = 128,  // 4个32位整数
        valueType = ValueType.INT
    )
    private int[] sensorReadings = new int[4];
    
    // 字节数组
    @Node(
        id = "raw_data",
        name = "原始数据",
        length = 256,  // 32字节
        valueType = ValueType.HEX
    )
    private byte[] rawData = new byte[32];
    
    // 动态数组（需要配合长度字段）
    @Node(id = "data_count", name = "数据数量", length = 8, valueType = ValueType.INT)
    private int dataCount;
    
    @Node(
        id = "dynamic_data",
        name = "动态数据",
        length = 0,  // 动态计算：dataCount * 16
        valueType = ValueType.INT
    )
    private int[] dynamicData;
    
    // 字符串数组
    @Node(
        id = "device_names",
        name = "设备名称列表",
        length = 512,  // 4个128位字符串
        valueType = ValueType.STRING
    )
    private String[] deviceNames = new String[4];
}
```

### 字段验证

#### 1. 基本验证注解

```java
public class ValidationExample {
    
    @Node(
        id = "device_id",
        name = "设备ID",
        length = 32,
        valueType = ValueType.INT,
        validation = "range(1, 999999)",
        description = "设备ID必须在1-999999范围内"
    )
    private int deviceId;
    
    @Node(
        id = "temperature",
        name = "温度",
        length = 16,
        valueType = ValueType.INT,
        validation = "range(-40, 85)",
        description = "温度范围-40°C到85°C"
    )
    private int temperature;
    
    @Node(
        id = "device_name",
        name = "设备名称",
        length = 128,
        valueType = ValueType.STRING,
        validation = "length(1, 16) && pattern('^[a-zA-Z0-9_]+$')",
        description = "设备名称1-16字符，只允许字母数字下划线"
    )
    private String deviceName;
    
    @Node(
        id = "ip_address",
        name = "IP地址",
        length = 128,
        valueType = ValueType.STRING,
        validation = "pattern('^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$')",
        description = "有效的IPv4地址格式"
    )
    private String ipAddress;
    
    @Node(
        id = "percentage",
        name = "百分比",
        length = 8,
        valueType = ValueType.INT,
        validation = "range(0, 100)",
        description = "百分比值0-100"
    )
    private int percentage;
}
```

#### 2. 自定义验证器

```java
// 自定义验证注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomValidation {
    Class<? extends FieldValidator> validator();
    String message() default "Validation failed";
    String[] parameters() default {};
}

// 验证器接口
public interface FieldValidator {
    boolean validate(Object value, String[] parameters);
    String getErrorMessage(Object value, String[] parameters);
}

// MAC地址验证器
public class MacAddressValidator implements FieldValidator {
    
    private static final Pattern MAC_PATTERN = 
        Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    
    @Override
    public boolean validate(Object value, String[] parameters) {
        if (value == null) return false;
        return MAC_PATTERN.matcher(value.toString()).matches();
    }
    
    @Override
    public String getErrorMessage(Object value, String[] parameters) {
        return "Invalid MAC address format: " + value;
    }
}

// 使用自定义验证器
public class NetworkDevice {
    
    @Node(
        id = "mac_address",
        name = "MAC地址",
        length = 128,
        valueType = ValueType.STRING,
        description = "设备MAC地址"
    )
    @CustomValidation(
        validator = MacAddressValidator.class,
        message = "MAC地址格式不正确"
    )
    private String macAddress;
    
    @Node(
        id = "serial_number",
        name = "序列号",
        length = 256,
        valueType = ValueType.STRING,
        description = "设备序列号"
    )
    @CustomValidation(
        validator = SerialNumberValidator.class,
        parameters = {"prefix=DEV", "length=12"},
        message = "序列号格式不正确"
    )
    private String serialNumber;
}
```

### 字段计算和表达式

#### 1. 计算字段

```java
public class CalculatedFieldExample {
    
    @Node(id = "data_count", name = "数据数量", length = 8, valueType = ValueType.INT)
    private int dataCount;
    
    @Node(id = "item_size", name = "项目大小", length = 8, valueType = ValueType.INT)
    private int itemSize;
    
    // 计算字段 - 总大小
    @Node(
        id = "total_size",
        name = "总大小",
        length = 16,
        valueType = ValueType.INT,
        value = "dataCount * itemSize",
        description = "数据总大小，由数量乘以单项大小计算得出"
    )
    private int totalSize;
    
    // 计算字段 - 校验和
    @Node(
        id = "checksum",
        name = "校验和",
        length = 16,
        valueType = ValueType.HEX,
        value = "crc16(protocolHeader + protocolBody)",
        description = "CRC16校验和"
    )
    private String checksum;
    
    // 条件计算字段
    @Node(
        id = "status_flag",
        name = "状态标志",
        length = 8,
        valueType = ValueType.INT,
        value = "dataCount > 0 ? 1 : 0",
        description = "根据数据数量设置状态标志"
    )
    private int statusFlag;
}
```

#### 2. 表达式引用

```java
@Protocol(id = "expression_protocol", name = "表达式协议", length = 512)
public class ExpressionProtocol {
    
    @Header(id = "protocolHeader", name = "协议头", length = 128)
    private ExpressionHeader protocolHeader;
    
    @Body(id = "protocolBody", name = "协议体", length = 320)
    private ExpressionBody protocolBody;
    
    @Check(id = "protocolTail", name = "校验", length = 64)
    private ExpressionCheck protocolTail;
}

@Header(id = "expression_header", name = "表达式头", length = 128)
public class ExpressionHeader {
    
    @Node(id = "packet_length", name = "数据包长度", length = 16, valueType = ValueType.INT)
    private int packetLength;
    
    @Node(id = "header_length", name = "头部长度", length = 8, valueType = ValueType.INT, value = "16")
    private int headerLength;
    
    @Node(id = "data_length", name = "数据长度", length = 16, valueType = ValueType.INT, 
          value = "packetLength - headerLength - 8")  // 减去头部和校验长度
    private int dataLength;
    
    @Node(id = "reserved", name = "保留", length = 88, valueType = ValueType.HEX, value = "0x0")
    private String reserved;
}

@Body(id = "expression_body", name = "表达式体", length = 320)
public class ExpressionBody {
    
    // 引用头部字段
    @Node(
        id = "variable_data",
        name = "可变数据",
        length = 0,  // 动态长度
        valueType = ValueType.HEX,
        description = "长度由头部的dataLength字段决定"
    )
    private String variableData;
    
    // 计算剩余长度
    @Node(
        id = "padding",
        name = "填充",
        length = 0,  // 动态计算
        valueType = ValueType.HEX,
        value = "320 - protocolHeader.dataLength * 8",  // 320位减去实际数据长度
        description = "填充到固定长度"
    )
    private String padding;
}
```

## 数据类型映射

### Java类型到协议类型的映射

框架提供了完整的Java类型到协议数据类型的映射机制：

#### 1. 自动类型推断

```java
public class AutoTypeInference {
    
    // 自动推断为ValueType.INT
    @Node(id = "int_field", name = "整数字段", length = 32)
    private int intValue;
    
    // 自动推断为ValueType.INT
    @Node(id = "long_field", name = "长整数字段", length = 64)
    private long longValue;
    
    // 自动推断为ValueType.FLOAT
    @Node(id = "float_field", name = "浮点字段", length = 32)
    private float floatValue;
    
    // 自动推断为ValueType.FLOAT
    @Node(id = "double_field", name = "双精度字段", length = 64)
    private double doubleValue;
    
    // 自动推断为ValueType.BOOLEAN
    @Node(id = "bool_field", name = "布尔字段", length = 1)
    private boolean boolValue;
    
    // 自动推断为ValueType.STRING
    @Node(id = "str_field", name = "字符串字段", length = 128)
    private String stringValue;
    
    // 需要显式指定为HEX类型
    @Node(id = "hex_field", name = "十六进制字段", length = 64, valueType = ValueType.HEX)
    private String hexValue;
}
```

#### 2. 类型转换器

```java
// 类型转换器接口
public interface TypeConverter<J, P> {
    P javaToProtocol(J javaValue);
    J protocolToJava(P protocolValue);
    Class<J> getJavaType();
    ValueType getProtocolType();
}

// 日期时间转换器
@Component
public class DateTimeConverter implements TypeConverter<LocalDateTime, Long> {
    
    @Override
    public Long javaToProtocol(LocalDateTime javaValue) {
        return javaValue.toEpochSecond(ZoneOffset.UTC);
    }
    
    @Override
    public LocalDateTime protocolToJava(Long protocolValue) {
        return LocalDateTime.ofEpochSecond(protocolValue, 0, ZoneOffset.UTC);
    }
    
    @Override
    public Class<LocalDateTime> getJavaType() {
        return LocalDateTime.class;
    }
    
    @Override
    public ValueType getProtocolType() {
        return ValueType.INT;
    }
}

// 使用自定义转换器
public class TimestampExample {
    
    @Node(
        id = "created_time",
        name = "创建时间",
        length = 32,
        valueType = ValueType.INT,
        description = "创建时间戳"
    )
    @TypeConverter(DateTimeConverter.class)
    private LocalDateTime createdTime;
    
    @Node(
        id = "modified_time",
        name = "修改时间",
        length = 32,
        valueType = ValueType.INT,
        description = "修改时间戳"
    )
    @TypeConverter(DateTimeConverter.class)
    private LocalDateTime modifiedTime;
}
```

#### 3. 复杂类型映射

```java
// 坐标点类
public class Point {
    private double x;
    private double y;
    
    // 构造函数、getter、setter
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // ... getter/setter方法
}

// 坐标点转换器
@Component
public class PointConverter implements TypeConverter<Point, String> {
    
    @Override
    public String javaToProtocol(Point point) {
        return String.format("%.6f,%.6f", point.getX(), point.getY());
    }
    
    @Override
    public Point protocolToJava(String protocolValue) {
        String[] parts = protocolValue.split(",");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        return new Point(x, y);
    }
    
    @Override
    public Class<Point> getJavaType() {
        return Point.class;
    }
    
    @Override
    public ValueType getProtocolType() {
        return ValueType.STRING;
    }
}

// 使用复杂类型
public class LocationMessage {
    
    @Node(
        id = "gps_location",
        name = "GPS位置",
        length = 256,
        valueType = ValueType.STRING,
        description = "GPS坐标点"
    )
    @TypeConverter(PointConverter.class)
    private Point gpsLocation;
    
    @Node(
        id = "target_location",
        name = "目标位置",
        length = 256,
        valueType = ValueType.STRING,
        description = "目标坐标点"
    )
    @TypeConverter(PointConverter.class)
    private Point targetLocation;
}
```

## 条件依赖注解

### @ConditionalOn注解

条件依赖注解允许根据其他字段的值来控制当前字段的行为：

#### 1. 基本用法

```java
// 条件依赖注解定义
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ConditionalOn.List.class)
public @interface ConditionalOn {
    /**
     * 条件节点ID
     */
    String nodeId();
    
    /**
     * 条件表达式
     */
    String condition();
    
    /**
     * 条件满足时的动作
     */
    ConditionalAction action() default ConditionalAction.ENABLE;
    
    /**
     * 动作参数
     */
    String actionValue() default "";
    
    /**
     * 优先级
     */
    int priority() default 0;
    
    /**
     * 描述
     */
    String description() default "";
    
    // 容器注解
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ConditionalOn[] value();
    }
}

// 条件动作枚举
public enum ConditionalAction {
    ENABLE,      // 启用字段
    DISABLE,     // 禁用字段
    SET_DEFAULT, // 设置默认值
    CLEAR_VALUE  // 清空值
}
```

#### 2. 简单条件依赖

```java
public class SimpleConditionalExample {
    
    @Node(id = "device_type", name = "设备类型", length = 8, valueType = ValueType.INT)
    private int channelType;
    
    // 当设备类型为1时启用传感器配置
    @Node(
        id = "sensor_config",
        name = "传感器配置",
        length = 128,
        valueType = ValueType.HEX,
        description = "传感器特定配置"
    )
    @ConditionalOn(
        nodeId = "device_type",
        condition = "value == 1",
        action = ConditionalAction.ENABLE,
        description = "仅当设备类型为传感器时启用"
    )
    private String sensorConfig;
    
    // 当设备类型为2时启用执行器配置
    @Node(
        id = "actuator_config",
        name = "执行器配置",
        length = 128,
        valueType = ValueType.HEX,
        description = "执行器特定配置"
    )
    @ConditionalOn(
        nodeId = "device_type",
        condition = "value == 2",
        action = ConditionalAction.ENABLE,
        description = "仅当设备类型为执行器时启用"
    )
    private String actuatorConfig;
    
    // 当设备类型不为3时禁用高级功能
    @Node(
        id = "advanced_features",
        name = "高级功能",
        length = 64,
        valueType = ValueType.HEX,
        description = "高级功能配置"
    )
    @ConditionalOn(
        nodeId = "device_type",
        condition = "value != 3",
        action = ConditionalAction.DISABLE,
        description = "非控制器设备禁用高级功能"
    )
    private String advancedFeatures;
}
```

#### 3. 复杂条件依赖

```java
public class ComplexConditionalExample {
    
    @Node(id = "protocol_version", name = "协议版本", length = 8, valueType = ValueType.INT)
    private int protocolVersion;
    
    @Node(id = "device_capabilities", name = "设备能力", length = 16, valueType = ValueType.INT)
    private int deviceCapabilities;
    
    @Node(id = "power_mode", name = "电源模式", length = 8, valueType = ValueType.INT)
    private int powerMode;
    
    // 多条件依赖 - 需要协议版本>=2且设备支持加密
    @Node(
        id = "encryption_config",
        name = "加密配置",
        length = 256,
        valueType = ValueType.HEX,
        description = "数据加密配置"
    )
    @ConditionalOn.List({
        @ConditionalOn(
            nodeId = "protocol_version",
            condition = "value >= 2",
            action = ConditionalAction.ENABLE,
            priority = 1,
            description = "协议版本2.0以上支持加密"
        ),
        @ConditionalOn(
            nodeId = "device_capabilities",
            condition = "(value & 0x04) != 0",  // 检查加密能力位
            action = ConditionalAction.ENABLE,
            priority = 2,
            description = "设备必须支持加密功能"
        )
    })
    private String encryptionConfig;
    
    // 条件设置默认值
    @Node(
        id = "sampling_rate",
        name = "采样率",
        length = 16,
        valueType = ValueType.INT,
        description = "数据采样率"
    )
    @ConditionalOn(
        nodeId = "power_mode",
        condition = "value == 1",  // 低功耗模式
        action = ConditionalAction.SET_DEFAULT,
        actionValue = "100",  // 设置为100Hz
        description = "低功耗模式下设置默认采样率"
    )
    private int samplingRate;
    
    // 条件清空值
    @Node(
        id = "debug_info",
        name = "调试信息",
        length = 512,
        valueType = ValueType.HEX,
        description = "调试信息"
    )
    @ConditionalOn(
        nodeId = "power_mode",
        condition = "value == 1",  // 低功耗模式
        action = ConditionalAction.CLEAR_VALUE,
        description = "低功耗模式下清空调试信息"
    )
    private String debugInfo;
}
```

#### 4. 表达式语法

条件依赖支持丰富的表达式语法：

```java
public class ExpressionSyntaxExample {
    
    @Node(id = "temperature", name = "温度", length = 16, valueType = ValueType.INT)
    private int temperature;
    
    @Node(id = "humidity", name = "湿度", length = 16, valueType = ValueType.INT)
    private int humidity;
    
    @Node(id = "pressure", name = "压力", length = 16, valueType = ValueType.INT)
    private int pressure;
    
    // 数值比较
    @Node(id = "high_temp_warning", name = "高温警告", length = 8, valueType = ValueType.INT)
    @ConditionalOn(
        nodeId = "temperature",
        condition = "value > 50",
        action = ConditionalAction.SET_DEFAULT,
        actionValue = "1"
    )
    private int highTempWarning;
    
    // 范围检查
    @Node(id = "normal_operation", name = "正常运行", length = 8, valueType = ValueType.INT)
    @ConditionalOn(
        nodeId = "temperature",
        condition = "value >= 10 && value <= 40",
        action = ConditionalAction.SET_DEFAULT,
        actionValue = "1"
    )
    private int normalOperation;
    
    // 复合条件
    @Node(id = "comfort_index", name = "舒适度指数", length = 8, valueType = ValueType.INT)
    @ConditionalOn.List({
        @ConditionalOn(
            nodeId = "temperature",
            condition = "value >= 20 && value <= 26",
            action = ConditionalAction.ENABLE,
            priority = 1
        ),
        @ConditionalOn(
            nodeId = "humidity",
            condition = "value >= 40 && value <= 60",
            action = ConditionalAction.ENABLE,
            priority = 2
        )
    })
    private int comfortIndex;
    
    // 函数调用
    @Node(id = "data_valid", name = "数据有效", length = 8, valueType = ValueType.INT)
    @ConditionalOn(
        nodeId = "temperature",
        condition = "abs(value) < 100",  // 使用abs函数
        action = ConditionalAction.SET_DEFAULT,
        actionValue = "1"
    )
    private int dataValid;
    
    // 字符串匹配
    @Node(id = "device_name", name = "设备名称", length = 128, valueType = ValueType.STRING)
    private String deviceName;
    
    @Node(id = "vendor_specific", name = "厂商特定", length = 256, valueType = ValueType.HEX)
    @ConditionalOn(
        nodeId = "device_name",
        condition = "value.startsWith('ACME')",
        action = ConditionalAction.ENABLE
    )
    private String vendorSpecific;
}
```

### 条件依赖处理器

框架提供了条件依赖处理器来执行条件逻辑：

```java
@Component
public class ConditionalDependencyProcessor {
    
    private final AviatorEvaluator evaluator;
    
    public ConditionalDependencyProcessor() {
        this.evaluator = AviatorEvaluator.getInstance();
        // 注册自定义函数
        registerCustomFunctions();
    }
    
    /**
     * 处理字段的条件依赖
     */
    public void processConditionalDependencies(Object protocolInstance, Field field) {
        ConditionalOn[] conditions = getConditionalAnnotations(field);
        if (conditions.length == 0) {
            return;
        }
        
        // 按优先级排序
        Arrays.sort(conditions, Comparator.comparingInt(ConditionalOn::priority));
        
        for (ConditionalOn condition : conditions) {
            processSingleCondition(protocolInstance, field, condition);
        }
    }
    
    /**
     * 处理单个条件
     */
    private void processSingleCondition(Object protocolInstance, Field field, ConditionalOn condition) {
        try {
            // 获取条件节点的值
            Object conditionValue = getNodeValue(protocolInstance, condition.nodeId());
            
            // 构建表达式上下文
            Map<String, Object> context = buildExpressionContext(protocolInstance, conditionValue);
            
            // 评估条件表达式
            boolean conditionMet = evaluateCondition(condition.condition(), context);
            
            if (conditionMet) {
                executeAction(protocolInstance, field, condition);
            }
            
        } catch (Exception e) {
            throw new ConditionalDependencyException(
                "Failed to process conditional dependency for field: " + field.getName(), e);
        }
    }
    
    /**
     * 执行条件动作
     */
    private void executeAction(Object protocolInstance, Field field, ConditionalOn condition) {
        switch (condition.action()) {
            case ENABLE:
                enableField(protocolInstance, field);
                break;
            case DISABLE:
                disableField(protocolInstance, field);
                break;
            case SET_DEFAULT:
                setDefaultValue(protocolInstance, field, condition.actionValue());
                break;
            case CLEAR_VALUE:
                clearFieldValue(protocolInstance, field);
                break;
        }
    }
    
    /**
     * 构建表达式上下文
     */
    private Map<String, Object> buildExpressionContext(Object protocolInstance, Object conditionValue) {
        Map<String, Object> context = new HashMap<>();
        context.put("value", conditionValue);
        
        // 添加所有字段值到上下文
        Class<?> clazz = protocolInstance.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Node.class)) {
                Node nodeAnnotation = field.getAnnotation(Node.class);
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(protocolInstance);
                    context.put(nodeAnnotation.id(), fieldValue);
                } catch (Exception e) {
                    // 忽略无法访问的字段
                }
            }
        }
        
        return context;
    }
    
    /**
     * 注册自定义函数
     */
    private void registerCustomFunctions() {
        // 注册abs函数
        evaluator.addFunction(new AbstractFunction("abs", "abs(value)") {
            @Override
            public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
                Number num = (Number) arg1.getValue(env);
                return AviatorNumber.valueOf(Math.abs(num.doubleValue()));
            }
        });
        
        // 注册字符串函数
        evaluator.addFunction(new AbstractFunction("startsWith", "startsWith(str, prefix)") {
            @Override
            public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
                String str = (String) arg1.getValue(env);
                String prefix = (String) arg2.getValue(env);
                return AviatorBoolean.valueOf(str.startsWith(prefix));
            }
        });
    }
}
```

## 填充注解

### @Padding注解

填充注解用于在协议中添加填充数据，确保数据对齐或达到固定长度：

#### 1. 注解定义

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Padding {
    /**
     * 填充类型
     */
    PaddingType type() default PaddingType.FIXED_LENGTH;
    
    /**
     * 填充长度（位数）
     */
    int length() default 0;
    
    /**
     * 填充值
     */
    String value() default "0x00";
    
    /**
     * 填充位置
     */
    PaddingPosition position() default PaddingPosition.AFTER;
    
    /**
     * 对齐大小（用于对齐填充）
     */
    int alignment() default 8;
    
    /**
     * 容器固定长度（用于容器填充）
     */
    int containerFixedLength() default 0;
    
    /**
     * 是否自动计算容器长度
     */
    boolean autoCalculateContainerLength() default false;
    
    /**
     * 条件表达式
     */
    String condition() default "";
    
    /**
     * 描述
     */
    String description() default "";
}

// 填充类型枚举
public enum PaddingType {
    FIXED_LENGTH,      // 固定长度填充
    ALIGNMENT,         // 对齐填充
    DYNAMIC,           // 动态填充
    FILL_REMAINING,    // 填充剩余空间
    FILL_CONTAINER     // 容器级别填充
}

// 填充位置枚举
public enum PaddingPosition {
    BEFORE,   // 在字段前填充
    AFTER,    // 在字段后填充
    REPLACE   // 替换字段内容
}
```

#### 2. 固定长度填充

```java
public class FixedLengthPaddingExample {
    
    @Node(id = "message_type", name = "消息类型", length = 8, valueType = ValueType.INT)
    private int messageType;
    
    @Node(id = "data_length", name = "数据长度", length = 16, valueType = ValueType.INT)
    private int dataLength;
    
    // 固定长度填充 - 在字段后添加8位填充
    @Node(id = "header_padding", name = "头部填充", length = 8, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.FIXED_LENGTH,
        length = 8,
        value = "0x00",
        position = PaddingPosition.AFTER,
        description = "头部固定填充8位"
    )
    private String headerPadding;
    
    @Node(id = "payload", name = "载荷数据", length = 256, valueType = ValueType.HEX)
    private String payload;
    
    // 固定长度填充 - 确保总长度为512位
    @Node(id = "end_padding", name = "结束填充", length = 232, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.FIXED_LENGTH,
        length = 232,  // 512 - 8 - 16 - 8 - 256 = 232
        value = "0xFF",
        position = PaddingPosition.AFTER,
        description = "填充到512位总长度"
    )
    private String endPadding;
}
```

#### 3. 对齐填充

```java
public class AlignmentPaddingExample {
    
    @Node(id = "device_id", name = "设备ID", length = 12, valueType = ValueType.INT)
    private int deviceId;
    
    // 对齐填充 - 对齐到16位边界
    @Node(id = "alignment_padding1", name = "对齐填充1", length = 4, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.ALIGNMENT,
        alignment = 16,
        value = "0x0",
        description = "对齐到16位边界"
    )
    private String alignmentPadding1;
    
    @Node(id = "sensor_data", name = "传感器数据", length = 20, valueType = ValueType.HEX)
    private String sensorData;
    
    // 对齐填充 - 对齐到32位边界
    @Node(id = "alignment_padding2", name = "对齐填充2", length = 12, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.ALIGNMENT,
        alignment = 32,
        value = "0x0",
        description = "对齐到32位边界"
    )
    private String alignmentPadding2;
    
    @Node(id = "status_flags", name = "状态标志", length = 32, valueType = ValueType.HEX)
    private String statusFlags;
}
```

#### 4. 动态填充

```java
public class DynamicPaddingExample {
    
    @Node(id = "data_count", name = "数据数量", length = 8, valueType = ValueType.INT)
    private int dataCount;
    
    @Node(id = "variable_data", name = "可变数据", length = 0, valueType = ValueType.HEX)
    private String variableData;  // 长度由dataCount决定
    
    // 动态填充 - 根据实际数据长度计算填充
    @Node(id = "dynamic_padding", name = "动态填充", length = 0, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.DYNAMIC,
        value = "0xAA",
        condition = "dataCount < 32",  // 只有当数据少于32个时才填充
        description = "根据数据量动态填充"
    )
    private String dynamicPadding;
    
    @Node(id = "checksum", name = "校验和", length = 16, valueType = ValueType.HEX)
    private String checksum;
}
```

#### 5. 容器级别填充

```java
@Body(id = "container_body", name = "容器体", length = 512)
public class ContainerPaddingExample {
    
    @Node(id = "item_count", name = "项目数量", length = 8, valueType = ValueType.INT)
    private int itemCount;
    
    @Node(id = "item1", name = "项目1", length = 64, valueType = ValueType.HEX)
    private String item1;
    
    @Node(id = "item2", name = "项目2", length = 64, valueType = ValueType.HEX)
    private String item2;
    
    @Node(id = "item3", name = "项目3", length = 64, valueType = ValueType.HEX)
    private String item3;
    
    // 容器级别填充 - 填充到容器固定长度
    @Node(id = "container_padding", name = "容器填充", length = 0, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.FILL_CONTAINER,
        containerFixedLength = 512,  // 容器总长度512位
        value = "0x00",
        description = "填充到容器固定长度512位"
    )
    private String containerPadding;
    
    // 自动计算容器长度的填充
    @Node(id = "auto_padding", name = "自动填充", length = 0, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.FILL_CONTAINER,
        autoCalculateContainerLength = true,
        value = "0xFF",
        description = "自动计算并填充到容器长度"
    )
    private String autoPadding;
}
```

#### 6. 条件填充

```java
public class ConditionalPaddingExample {
    
    @Node(id = "protocol_version", name = "协议版本", length = 8, valueType = ValueType.INT)
    private int protocolVersion;
    
    @Node(id = "message_data", name = "消息数据", length = 256, valueType = ValueType.HEX)
    private String messageData;
    
    // 条件填充 - 只有协议版本1.0才需要填充
    @Node(id = "legacy_padding", name = "遗留填充", length = 0, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.FIXED_LENGTH,
        length = 128,
        value = "0x00",
        condition = "protocolVersion == 1",
        description = "协议版本1.0的兼容性填充"
    )
    private String legacyPadding;
    
    @Node(id = "power_mode", name = "电源模式", length = 8, valueType = ValueType.INT)
    private int powerMode;
    
    // 条件填充 - 低功耗模式下减少填充
    @Node(id = "power_aware_padding", name = "功耗感知填充", length = 0, valueType = ValueType.HEX)
    @Padding(
        type = PaddingType.DYNAMIC,
        value = "0x00",
        condition = "powerMode != 1",  // 非低功耗模式
        description = "非低功耗模式下的填充"
    )
    private String powerAwarePadding;
}
```

### 填充处理器

```java
@Component
public class PaddingProcessor {
    
    private final AviatorEvaluator evaluator;
    
    public PaddingProcessor() {
        this.evaluator = AviatorEvaluator.getInstance();
    }
    
    /**
     * 处理字段的填充配置
     */
    public void processPadding(Object protocolInstance, Field field) {
        if (!field.isAnnotationPresent(Padding.class)) {
            return;
        }
        
        Padding paddingAnnotation = field.getAnnotation(Padding.class);
        
        // 检查条件
        if (!evaluatePaddingCondition(protocolInstance, paddingAnnotation)) {
            return;
        }
        
        // 根据填充类型处理
        switch (paddingAnnotation.type()) {
            case FIXED_LENGTH:
                processFixedLengthPadding(protocolInstance, field, paddingAnnotation);
                break;
            case ALIGNMENT:
                processAlignmentPadding(protocolInstance, field, paddingAnnotation);
                break;
            case DYNAMIC:
                processDynamicPadding(protocolInstance, field, paddingAnnotation);
                break;
            case FILL_REMAINING:
                processFillRemainingPadding(protocolInstance, field, paddingAnnotation);
                break;
            case FILL_CONTAINER:
                processFillContainerPadding(protocolInstance, field, paddingAnnotation);
                break;
        }
    }
    
    /**
     * 处理固定长度填充
     */
    private void processFixedLengthPadding(Object protocolInstance, Field field, Padding padding) {
        try {
            String paddingValue = generatePaddingValue(padding.value(), padding.length());
            field.setAccessible(true);
            field.set(protocolInstance, paddingValue);
        } catch (Exception e) {
            throw new PaddingProcessException("Failed to process fixed length padding", e);
        }
    }
    
    /**
     * 处理容器级别填充
     */
    private void processFillContainerPadding(Object protocolInstance, Field field, Padding padding) {
        try {
            int containerLength = padding.autoCalculateContainerLength() 
                ? calculateContainerLength(protocolInstance)
                : padding.containerFixedLength();
                
            int usedLength = calculateUsedLength(protocolInstance);
            int paddingLength = containerLength - usedLength;
            
            if (paddingLength > 0) {
                String paddingValue = generatePaddingValue(padding.value(), paddingLength);
                field.setAccessible(true);
                field.set(protocolInstance, paddingValue);
            }
        } catch (Exception e) {
            throw new PaddingProcessException("Failed to process container padding", e);
        }
    }
    
    /**
     * 生成填充值
     */
    private String generatePaddingValue(String pattern, int length) {
        if (pattern.startsWith("0x")) {
            // 十六进制模式
            String hexValue = pattern.substring(2);
            StringBuilder result = new StringBuilder();
            int patternLength = hexValue.length() * 4; // 每个十六进制字符4位
            
            for (int protocol = 0; protocol < length; protocol += patternLength) {
                int remainingLength = Math.min(patternLength, length - protocol);
                if (remainingLength == patternLength) {
                    result.append(hexValue);
                } else {
                    // 截取部分模式
                    int hexChars = (remainingLength + 3) / 4; // 向上取整
                    result.append(hexValue.substring(0, hexChars));
                }
            }
            
            return "0x" + result.toString();
        } else {
            // 其他模式
            return pattern.repeat(length / pattern.length() + 1).substring(0, length);
        }
    }
}
```

## 枚举和常量

### 枚举定义和使用

#### 1. 基本枚举定义

```java
// 设备类型枚举
public enum DeviceType {
    UNKNOWN(0, "未知设备"),
    SENSOR(1, "传感器"),
    ACTUATOR(2, "执行器"),
    CONTROLLER(3, "控制器"),
    GATEWAY(4, "网关"),
    REPEATER(5, "中继器");
    
    private final int code;
    private final String description;
    
    DeviceType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() { return code; }
    public String getDescription() { return description; }
    
    // 从代码值获取枚举
    public static DeviceType fromCode(int code) {
        for (DeviceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    // 验证代码值是否有效
    public static boolean isValidCode(int code) {
        return fromCode(code) != UNKNOWN || code == 0;
    }
}

// 消息类型枚举
public enum MessageType {
    HEARTBEAT(0x01, "心跳消息"),
    DATA_REPORT(0x02, "数据上报"),
    COMMAND_REQUEST(0x03, "命令请求"),
    COMMAND_RESPONSE(0x04, "命令响应"),
    CONFIG_UPDATE(0x05, "配置更新"),
    ALARM_NOTIFICATION(0x06, "告警通知"),
    STATUS_QUERY(0x07, "状态查询"),
    STATUS_RESPONSE(0x08, "状态响应");
    
    private final int code;
    private final String description;
    
    MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() { return code; }
    public String getDescription() { return description; }
    
    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}
```

#### 2. 在协议中使用枚举

```java
public class EnumUsageExample {
    
    // 使用枚举字段
    @Node(
        id = "device_type",
        name = "设备类型",
        length = 8,
        valueType = ValueType.INT,
        enumType = DeviceType.class,
        description = "设备类型枚举值"
    )
    private DeviceType channelType;
    
    @Node(
        id = "message_type",
        name = "消息类型",
        length = 8,
        valueType = ValueType.INT,
        enumType = MessageType.class,
        description = "消息类型枚举值"
    )
    private MessageType messageType;
    
    // 枚举数组
    @Node(
        id = "supported_types",
        name = "支持的类型",
        length = 64,  // 8个类型，每个8位
        valueType = ValueType.INT,
        enumType = DeviceType.class,
        description = "设备支持的类型列表"
    )
    private DeviceType[] supportedTypes = new DeviceType[8];
    
    // 条件依赖中使用枚举
    @Node(
        id = "sensor_config",
        name = "传感器配置",
        length = 128,
        valueType = ValueType.HEX,
        description = "传感器特定配置"
    )
    @ConditionalOn(
        nodeId = "device_type",
        condition = "value == DeviceType.SENSOR",
        action = ConditionalAction.ENABLE
    )
    private String sensorConfig;
}
```

#### 3. 复杂枚举示例

```java
// 状态枚举 - 支持位掩码
public enum DeviceStatus {
    OFFLINE(0x00, "离线"),
    ONLINE(0x01, "在线"),
    BUSY(0x02, "忙碌"),
    ERROR(0x04, "错误"),
    MAINTENANCE(0x08, "维护中"),
    LOW_BATTERY(0x10, "低电量"),
    HIGH_TEMPERATURE(0x20, "高温"),
    COMMUNICATION_ERROR(0x40, "通信错误");
    
    private final int mask;
    private final String description;
    
    DeviceStatus(int mask, String description) {
        this.mask = mask;
        this.description = description;
    }
    
    public int getMask() { return mask; }
    public String getDescription() { return description; }
    
    // 检查状态是否包含指定标志
    public static boolean hasStatus(int statusValue, DeviceStatus status) {
        return (statusValue & status.mask) != 0;
    }
    
    // 设置状态标志
    public static int setStatus(int statusValue, DeviceStatus status) {
        return statusValue | status.mask;
    }
    
    // 清除状态标志
    public static int clearStatus(int statusValue, DeviceStatus status) {
        return statusValue & ~status.mask;
    }
    
    // 获取所有激活的状态
    public static Set<DeviceStatus> getActiveStatuses(int statusValue) {
        Set<DeviceStatus> activeStatuses = EnumSet.noneOf(DeviceStatus.class);
        for (DeviceStatus status : values()) {
            if (hasStatus(statusValue, status)) {
                activeStatuses.add(status);
            }
        }
        return activeStatuses;
    }
}

// 使用位掩码枚举
public class StatusExample {
    
    @Node(
        id = "device_status",
        name = "设备状态",
        length = 8,
        valueType = ValueType.INT,
        enumType = DeviceStatus.class,
        description = "设备状态位掩码"
    )
    private int deviceStatus;
    
    // 业务方法
    public boolean isOnline() {
        return DeviceStatus.hasStatus(deviceStatus, DeviceStatus.ONLINE);
    }
    
    public void setOnline() {
        deviceStatus = DeviceStatus.setStatus(deviceStatus, DeviceStatus.ONLINE);
        deviceStatus = DeviceStatus.clearStatus(deviceStatus, DeviceStatus.OFFLINE);
    }
    
    public Set<DeviceStatus> getActiveStatuses() {
        return DeviceStatus.getActiveStatuses(deviceStatus);
    }
}
```

### 常量定义

#### 1. 协议常量类

```java
// 协议常量定义
public final class ProtocolConstants {
    
    // 防止实例化
    private ProtocolConstants() {}
    
    // 同步字
    public static final String SYNC_WORD = "0xAA55";
    public static final String ALT_SYNC_WORD = "0x55AA";
    
    // 协议版本
    public static final int PROTOCOL_VERSION_1_0 = 1;
    public static final int PROTOCOL_VERSION_2_0 = 2;
    public static final int CURRENT_PROTOCOL_VERSION = PROTOCOL_VERSION_2_0;
    
    // 长度常量
    public static final int HEADER_LENGTH = 64;
    public static final int CHECKSUM_LENGTH = 16;
    public static final int MAX_PAYLOAD_LENGTH = 1024;
    public static final int MIN_PACKET_LENGTH = HEADER_LENGTH + CHECKSUM_LENGTH;
    
    // 默认值
    public static final String DEFAULT_PADDING = "0x00";
    public static final int DEFAULT_TIMEOUT = 5000; // 毫秒
    public static final int DEFAULT_RETRY_COUNT = 3;
    
    // 错误码
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_INVALID_PARAMETER = 1;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_CHECKSUM_FAILED = 3;
    public static final int ERROR_UNSUPPORTED_VERSION = 4;
    
    // 设备能力位
    public static final int CAPABILITY_ENCRYPTION = 0x01;
    public static final int CAPABILITY_COMPRESSION = 0x02;
    public static final int CAPABILITY_HEARTBEAT = 0x04;
    public static final int CAPABILITY_REMOTE_CONFIG = 0x08;
    public static final int CAPABILITY_OTA_UPDATE = 0x10;
}

// 使用常量
public class ConstantUsageExample {
    
    @Node(
        id = "sync_word",
        name = "同步字",
        length = 16,
        valueType = ValueType.HEX,
        value = ProtocolConstants.SYNC_WORD,
        description = "协议同步字"
    )
    private String syncWord;
    
    @Node(
        id = "protocol_version",
        name = "协议版本",
        length = 8,
        valueType = ValueType.INT,
        value = String.valueOf(ProtocolConstants.CURRENT_PROTOCOL_VERSION),
        description = "当前协议版本"
    )
    private int protocolVersion;
    
    @Node(
        id = "capabilities",
        name = "设备能力",
        length = 16,
        valueType = ValueType.INT,
        description = "设备支持的功能位掩码"
    )
    private int capabilities;
    
    // 业务方法
    public boolean supportsEncryption() {
        return (capabilities & ProtocolConstants.CAPABILITY_ENCRYPTION) != 0;
    }
    
    public void enableEncryption() {
        capabilities |= ProtocolConstants.CAPABILITY_ENCRYPTION;
    }
}
```

#### 2. 配置常量

```java
// 配置常量接口
public interface ConfigConstants {
    
    // 网络配置
    interface Network {
        int DEFAULT_PORT = 8080;
        int MAX_CONNECTIONS = 100;
        int CONNECTION_TIMEOUT = 30000;
        String DEFAULT_HOST = "localhost";
    }
    
    // 数据配置
    interface Data {
        int MAX_BUFFER_SIZE = 8192;
        int DEFAULT_BATCH_SIZE = 100;
        String DEFAULT_ENCODING = "UTF-8";
    }
    
    // 安全配置
    interface Security {
        int MIN_PASSWORD_LENGTH = 8;
        int MAX_LOGIN_ATTEMPTS = 3;
        int SESSION_TIMEOUT = 1800000; // 30分钟
        String DEFAULT_ALGORITHM = "AES";
    }
}

// 使用配置常量
@Protocol(id = "config_protocol", name = "配置协议", length = 512)
public class ConfigProtocol {
    
    @Node(
        id = "server_port",
        name = "服务器端口",
        length = 16,
        valueType = ValueType.INT,
        value = String.valueOf(ConfigConstants.Network.DEFAULT_PORT),
        description = "服务器监听端口"
    )
    private int serverPort;
    
    @Node(
        id = "max_connections",
        name = "最大连接数",
        length = 16,
        valueType = ValueType.INT,
        value = String.valueOf(ConfigConstants.Network.MAX_CONNECTIONS),
        description = "最大并发连接数"
    )
    private int maxConnections;
    
    @Node(
        id = "buffer_size",
        name = "缓冲区大小",
        length = 16,
        valueType = ValueType.INT,
        value = String.valueOf(ConfigConstants.Data.MAX_BUFFER_SIZE),
        description = "数据缓冲区大小"
    )
    private int bufferSize;
}
```

## 继承和组合

### 协议继承

#### 1. 基础协议类

```java
// 抽象基础协议
@Protocol(id = "base_protocol", name = "基础协议", length = 256)
public abstract class BaseProtocol {
    
    @Node(id = "sync_word", name = "同步字", length = 16, 
          valueType = ValueType.HEX, value = "0xAA55")
    protected String syncWord;
    
    @Node(id = "protocol_version", name = "协议版本", length = 8, 
          valueType = ValueType.INT, value = "1")
    protected int protocolVersion;
    
    @Node(id = "message_length", name = "消息长度", length = 16, 
          valueType = ValueType.INT)
    protected int messageLength;
    
    @Node(id = "message_type", name = "消息类型", length = 8, 
          valueType = ValueType.INT)
    protected int messageType;
    
    @Node(id = "sequence_number", name = "序列号", length = 16, 
          valueType = ValueType.INT)
    protected int sequenceNumber;
    
    // 构造函数
    public BaseProtocol() {
        this.syncWord = "0xAA55";
        this.protocolVersion = 1;
    }
    
    // 抽象方法
    public abstract void processMessage();
    public abstract boolean validateMessage();
    
    // 通用方法
    public boolean isValidSyncWord() {
        return "0xAA55".equals(syncWord) || "0x55AA".equals(syncWord);
    }
    
    // Getter和Setter方法
    public String getSyncWord() { return syncWord; }
    public void setSyncWord(String syncWord) { this.syncWord = syncWord; }
    
    public int getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; }
    
    // ... 其他getter/setter方法
}
```

#### 2. 具体协议实现

```java
// 传感器数据协议
@Protocol(id = "sensor_data_protocol", name = "传感器数据协议", length = 512)
public class SensorDataProtocol extends BaseProtocol {
    
    @Body(id = "sensor_body", name = "传感器数据体", length = 192)
    private SensorDataBody sensorBody;
    
    @Check(id = "checksum", name = "校验", length = 64)
    private ProtocolChecksum checksum;
    
    public SensorDataProtocol() {
        super();
        this.messageType = MessageType.DATA_REPORT.getCode();
        this.sensorBody = new SensorDataBody();
        this.checksum = new ProtocolChecksum();
    }
    
    @Override
    public void processMessage() {
        // 传感器数据特定处理逻辑
        if (validateMessage()) {
            processSensorData();
            updateTimestamp();
        }
    }
    
    @Override
    public boolean validateMessage() {
        return super.isValidSyncWord() && 
               sensorBody != null && 
               checksum.validate();
    }
    
    private void processSensorData() {
        // 处理传感器数据
        System.out.println("Processing sensor data: " + sensorBody.getSensorValue());
    }
    
    private void updateTimestamp() {
        sensorBody.setTimestamp(System.currentTimeMillis());
    }
    
    // Getter和Setter
    public SensorDataBody getSensorBody() { return sensorBody; }
    public void setSensorBody(SensorDataBody sensorBody) { this.sensorBody = sensorBody; }
    
    public ProtocolChecksum getChecksum() { return checksum; }
    public void setChecksum(ProtocolChecksum checksum) { this.checksum = checksum; }
}

// 控制命令协议
@Protocol(id = "control_command_protocol", name = "控制命令协议", length = 384)
public class ControlCommandProtocol extends BaseProtocol {
    
    @Body(id = "command_body", name = "命令数据体", length = 128)
    private CommandDataBody commandBody;
    
    @Check(id = "checksum", name = "校验", length = 32)
    private ProtocolChecksum checksum;
    
    public ControlCommandProtocol() {
        super();
        this.messageType = MessageType.COMMAND_REQUEST.getCode();
        this.commandBody = new CommandDataBody();
        this.checksum = new ProtocolChecksum();
    }
    
    @Override
    public void processMessage() {
        // 控制命令特定处理逻辑
        if (validateMessage()) {
            executeCommand();
            sendResponse();
        }
    }
    
    @Override
    public boolean validateMessage() {
        return super.isValidSyncWord() && 
               commandBody != null && 
               commandBody.isValidCommand() &&
               checksum.validate();
    }
    
    private void executeCommand() {
        // 执行控制命令
        System.out.println("Executing command: " + commandBody.getCommandCode());
    }
    
    private void sendResponse() {
        // 发送响应
        System.out.println("Command executed successfully");
    }
    
    // Getter和Setter
    public CommandDataBody getCommandBody() { return commandBody; }
    public void setCommandBody(CommandDataBody commandBody) { this.commandBody = commandBody; }
}
```

### 协议组合

#### 1. 复杂协议组合

```java
// 复合协议 - 包含多种数据类型
@Protocol(id = "composite_protocol", name = "复合协议", length = 1024)
public class CompositeProtocol {
    
    @Header(id = "protocolHeader", name = "协议头", length = 128)
    private CompositeHeader protocolHeader;
    
    @Body(id = "device_info", name = "设备信息", length = 256)
    private DeviceInfoSection deviceInfo;
    
    @Body(id = "sensor_data", name = "传感器数据", length = 320)
    private SensorDataSection sensorData;
    
    @Body(id = "status_info", name = "状态信息", length = 192)
    private StatusInfoSection statusInfo;
    
    @Check(id = "checksum", name = "校验", length = 128)
    private CompositeChecksum checksum;
    
    public CompositeProtocol() {
        this.protocolHeader = new CompositeHeader();
        this.deviceInfo = new DeviceInfoSection();
        this.sensorData = new SensorDataSection();
        this.statusInfo = new StatusInfoSection();
        this.checksum = new CompositeChecksum();
    }
    
    // 组合操作方法
    public void updateAllSections() {
        updateDeviceInfo();
        updateSensorData();
        updateStatusInfo();
        updateChecksum();
    }
    
    public boolean validateAllSections() {
        return protocolHeader.validate() &&
               deviceInfo.validate() &&
               sensorData.validate() &&
               statusInfo.validate() &&
               checksum.validate();
    }
    
    private void updateDeviceInfo() {
        deviceInfo.setLastUpdateTime(System.currentTimeMillis());
    }
    
    private void updateSensorData() {
        sensorData.refreshData();
    }
    
    private void updateStatusInfo() {
        statusInfo.updateStatus();
    }
    
    private void updateChecksum() {
        checksum.calculate(protocolHeader, deviceInfo, sensorData, statusInfo);
    }
    
    // Getter和Setter方法
    // ... 省略getter/setter方法
}
```

#### 2. 可配置协议组合

```java
// 可配置的协议组合器
public class ConfigurableProtocolComposer {
    
    private final List<ProtocolSection> sections;
    private final ProtocolConfiguration configuration;
    
    public ConfigurableProtocolComposer(ProtocolConfiguration configuration) {
        this.configuration = configuration;
        this.sections = new ArrayList<>();
        initializeSections();
    }
    
    private void initializeSections() {
        // 根据配置初始化协议段
        if (configuration.includeHeader()) {
            sections.add(new HeaderSection());
        }
        
        if (configuration.includeDeviceInfo()) {
            sections.add(new DeviceInfoSection());
        }
        
        if (configuration.includeSensorData()) {
            sections.add(new SensorDataSection());
        }
        
        if (configuration.includeStatusInfo()) {
            sections.add(new StatusInfoSection());
        }
        
        if (configuration.includeChecksum()) {
            sections.add(new ChecksumSection());
        }
    }
    
    public ComposedProtocol compose() {
        ComposedProtocol protocolDefinition = new ComposedProtocol();
        
        for (ProtocolSection section : sections) {
            protocolDefinition.addSection(section);
        }
        
        return protocolDefinition;
    }
    
    public void addCustomSection(ProtocolSection section) {
        sections.add(section);
    }
    
    public void removeSection(Class<? extends ProtocolSection> sectionType) {
        sections.removeIf(section -> sectionType.isInstance(section));
    }
}

// 协议配置类
public class ProtocolConfiguration {
    private boolean includeHeader = true;
    private boolean includeDeviceInfo = true;
    private boolean includeSensorData = false;
    private boolean includeStatusInfo = false;
    private boolean includeChecksum = true;
    
    // Builder模式
    public static class Builder {
        private ProtocolConfiguration config = new ProtocolConfiguration();
        
        public Builder withHeader(boolean include) {
            config.includeHeader = include;
            return this;
        }
        
        public Builder withDeviceInfo(boolean include) {
            config.includeDeviceInfo = include;
            return this;
        }
        
        public Builder withSensorData(boolean include) {
            config.includeSensorData = include;
            return this;
        }
        
        public Builder withStatusInfo(boolean include) {
            config.includeStatusInfo = include;
            return this;
        }
        
        public Builder withChecksum(boolean include) {
            config.includeChecksum = include;
            return this;
        }
        
        public ProtocolConfiguration build() {
            return config;
        }
    }
    
    // Getter方法
    public boolean includeHeader() { return includeHeader; }
    public boolean includeDeviceInfo() { return includeDeviceInfo; }
    public boolean includeSensorData() { return includeSensorData; }
    public boolean includeStatusInfo() { return includeStatusInfo; }
    public boolean includeChecksum() { return includeChecksum; }
}
```

## 最佳实践

### 1. 协议设计原则

#### 单一职责原则
```java
// ✅ 推荐：职责单一
@Header(id = "device_header", name = "设备头部", length = 64)
public class DeviceHeader {
    @Node(id = "device_id", name = "设备ID", length = 32, valueType = ValueType.INT)
    private int deviceId;
    
    @Node(id = "timestamp", name = "时间戳", length = 32, valueType = ValueType.INT)
    private long timestamp;
}

// ❌ 不推荐：职责混乱
@Protocol(id = "mixed_protocol", name = "混合协议", length = 512)
public class MixedProtocol {
    // 头部信息
    @Node(id = "device_id", name = "设备ID", length = 32, valueType = ValueType.INT)
    private int deviceId;
    
    // 数据信息
    @Node(id = "sensor_value", name = "传感器值", length = 32, valueType = ValueType.FLOAT)
    private float sensorValue;
    
    // 校验信息
    @Node(id = "checksum", name = "校验和", length = 16, valueType = ValueType.HEX)
    private String checksum;
    
    // 业务逻辑（不应该在协议类中）
    public void processData() { /* ... */ }
}
```

#### 开闭原则
```java
// 基础协议 - 对扩展开放，对修改关闭
public abstract class ExtensibleProtocol {
    @Node(id = "version", name = "版本", length = 8, valueType = ValueType.INT)
    protected int version;
    
    @Node(id = "type", name = "类型", length = 8, valueType = ValueType.INT)
    protected int type;
    
    // 扩展点
    public abstract void processSpecificData();
    
    // 稳定的通用逻辑
    public final void process() {
        validateCommonFields();
        processSpecificData();
        updateTimestamp();
    }
    
    protected void validateCommonFields() { /* ... */ }
    protected void updateTimestamp() { /* ... */ }
}

// 具体实现 - 扩展而不修改基类
public class SensorProtocol extends ExtensibleProtocol {
    @Node(id = "sensor_data", name = "传感器数据", length = 64, valueType = ValueType.HEX)
    private String sensorData;
    
    @Override
    public void processSpecificData() {
        // 传感器特定处理逻辑
        processSensorData();
    }
    
    private void processSensorData() { /* ... */ }
}
```

### 2. 命名规范

#### 类命名
```java
// ✅ 推荐的命名
@Protocol(id = "device_status_protocol", name = "设备状态协议")
public class DeviceStatusProtocol { }

@Header(id = "message_header", name = "消息头")
public class MessageHeader { }

@Body(id = "sensor_data_body", name = "传感器数据体")
public class SensorDataBody { }

@Check(id = "crc_checksum", name = "CRC校验")
public class CrcChecksum { }

// ❌ 不推荐的命名
public class DSP { }  // 缩写不清晰
public class Data { }  // 过于通用
public class ProtocolClass { }  // 冗余后缀
```

#### 字段命名
```java
public class NamingExample {
    // ✅ 推荐的字段命名
    @Node(id = "device_id", name = "设备ID", length = 32, valueType = ValueType.INT)
    private int deviceId;
    
    @Node(id = "sensor_temperature", name = "传感器温度", length = 16, valueType = ValueType.FLOAT)
    private float sensorTemperature;
    
    @Node(id = "last_update_timestamp", name = "最后更新时间戳", length = 64, valueType = ValueType.INT)
    private long lastUpdateTimestamp;
    
    // ❌ 不推荐的字段命名
    @Node(id = "d", name = "数据", length = 32, valueType = ValueType.INT)
    private int d;  // 名称过短
    
    @Node(id = "temp", name = "温度", length = 16, valueType = ValueType.FLOAT)
    private float temp;  // 缩写不清晰
    
    @Node(id = "deviceIdentificationNumber", name = "设备标识号", length = 32, valueType = ValueType.INT)
    private int deviceIdentificationNumber;  // 名称过长
}
```

### 3. 性能优化

#### 对象创建优化
```java
// ✅ 推荐：使用对象池
@Component
public class ProtocolObjectPool {
    private final Queue<SensorProtocol> sensorProtocolPool = new ConcurrentLinkedQueue<>();
    private final Queue<ControlProtocol> controlProtocolPool = new ConcurrentLinkedQueue<>();
    
    public SensorProtocol borrowSensorProtocol() {
        SensorProtocol protocolDefinition = sensorProtocolPool.poll();
        if (protocolDefinition == null) {
            protocolDefinition = new SensorProtocol();
        }
        return protocolDefinition;
    }
    
    public void returnSensorProtocol(SensorProtocol protocolDefinition) {
        // 重置协议状态
        protocolDefinition.reset();
        sensorProtocolPool.offer(protocolDefinition);
    }
}

// ✅ 推荐：延迟初始化
public class LazyInitializationExample {
    private SensorDataBody sensorBody;
    
    public SensorDataBody getSensorBody() {
        if (sensorBody == null) {
            sensorBody = new SensorDataBody();
        }
        return sensorBody;
    }
}

// ❌ 不推荐：频繁创建对象
public class BadPerformanceExample {
    public void processMessages(List<byte[]> messages) {
        for (byte[] message : messages) {
            // 每次都创建新对象
            SensorProtocol protocolDefinition = new SensorProtocol();
            protocolDefinition.decode(message);
            protocolDefinition.process();
        }
    }
}
```

#### 内存使用优化
```java
// ✅ 推荐：合理使用基本类型
public class MemoryOptimizedExample {
    @Node(id = "device_id", name = "设备ID", length = 32, valueType = ValueType.INT)
    private int deviceId;  // 使用基本类型
    
    @Node(id = "is_active", name = "是否激活", length = 1, valueType = ValueType.BOOLEAN)
    private boolean isActive;  // 使用基本类型
    
    // 只有在需要null值时才使用包装类型
    @Node(id = "optional_value", name = "可选值", length = 32, valueType = ValueType.INT, required = false)
    private Integer optionalValue;  // 可以为null
}

// ✅ 推荐：字符串常量池
public class StringOptimizationExample {
    private static final String DEFAULT_SYNC_WORD = "0xAA55";
    private static final String DEFAULT_PADDING = "0x00";
    
    @Node(id = "sync_word", name = "同步字", length = 16, 
          valueType = ValueType.HEX, value = DEFAULT_SYNC_WORD)
    private String syncWord = DEFAULT_SYNC_WORD;  // 使用常量
}
```

### 4. 错误处理

#### 异常处理策略
```java
// 自定义异常类
public class ProtocolException extends Exception {
    private final String protocolId;
    private final String fieldId;
    
    public ProtocolException(String message, String protocolId, String fieldId) {
        super(message);
        this.protocolId = protocolId;
        this.fieldId = fieldId;
    }
    
    public ProtocolException(String message, String protocolId, String fieldId, Throwable cause) {
        super(message, cause);
        this.protocolId = protocolId;
        this.fieldId = fieldId;
    }
    
    // Getter方法
    public String getProtocolId() { return protocolId; }
    public String getFieldId() { return fieldId; }
}

// 协议验证
public class ProtocolValidator {
    
    public void validateProtocol(Object protocolDefinition) throws ProtocolException {
        Class<?> protocolClass = protocolDefinition.getClass();
        
        if (!protocolClass.isAnnotationPresent(Protocol.class)) {
            throw new ProtocolException(
                "Class is not annotated with @ProtocolDefinition", 
                protocolClass.getSimpleName(), 
                null
            );
        }
        
        Field[] fields = protocolClass.getDeclaredFields();
        for (Field field : fields) {
            validateField(protocolDefinition, field);
        }
    }
    
    private void validateField(Object protocolDefinition, Field field) throws ProtocolException {
        if (!field.isAnnotationPresent(Node.class)) {
            return;
        }
        
        Node nodeAnnotation = field.getAnnotation(Node.class);
        
        try {
            field.setAccessible(true);
            Object value = field.get(protocolDefinition);
            
            // 必需字段检查
            if (nodeAnnotation.required() && value == null) {
                throw new ProtocolException(
                    "Required field is null",
                    protocolDefinition.getClass().getSimpleName(),
                    nodeAnnotation.id()
                );
            }
            
            // 类型检查
            validateFieldType(value, field.getType(), nodeAnnotation);
            
            // 值验证
            validateFieldValue(value, nodeAnnotation);
            
        } catch (IllegalAccessException e) {
            throw new ProtocolException(
                "Cannot access field: " + field.getName(),
                protocolDefinition.getClass().getSimpleName(),
                nodeAnnotation.id(),
                e
            );
        }
    }
    
    private void validateFieldType(Object value, Class<?> fieldType, Node nodeAnnotation) 
            throws ProtocolException {
        if (value == null) return;
        
        // 类型兼容性检查
        if (!fieldType.isAssignableFrom(value.getClass())) {
            throw new ProtocolException(
                String.format("Type mismatch: expected %s, got %s", 
                    fieldType.getSimpleName(), 
                    value.getClass().getSimpleName()),
                null,
                nodeAnnotation.id()
            );
        }
    }
    
    private void validateFieldValue(Object value, Node nodeAnnotation) throws ProtocolException {
        if (value == null) return;
        
        String validation = nodeAnnotation.validation();
        if (!validation.isEmpty()) {
            // 执行验证规则
            if (!executeValidationRule(value, validation)) {
                throw new ProtocolException(
                    "Field value validation failed: " + validation,
                    null,
                    nodeAnnotation.id()
                );
            }
        }
    }
    
    private boolean executeValidationRule(Object value, String rule) {
        // 实现验证规则执行逻辑
        // 这里简化处理
        return true;
    }
}
```

### 5. 测试策略

#### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class SensorProtocolTest {
    
    @Mock
    private ProtocolCodec protocolCodec;
    
    @InjectMocks
    private SensorProtocol sensorProtocol;
    
    @Test
    void should_CreateValidSensorProtocol_When_DefaultConstructor() {
        // Given & When
        SensorProtocol protocolDefinition = new SensorProtocol();
        
        // Then
        assertThat(protocolDefinition.getSyncWord()).isEqualTo("0xAA55");
        assertThat(protocolDefinition.getProtocolVersion()).isEqualTo(1);
        assertThat(protocolDefinition.getMessageType()).isEqualTo(MessageType.DATA_REPORT.getCode());
    }
    
    @Test
    void should_ValidateSuccessfully_When_AllFieldsValid() {
        // Given
        SensorProtocol protocolDefinition = new SensorProtocol();
        protocolDefinition.getSensorBody().setSensorValue(25.5f);
        protocolDefinition.getSensorBody().setTimestamp(System.currentTimeMillis());
        
        // When
        boolean isValid = protocolDefinition.validateMessage();
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void should_ThrowException_When_RequiredFieldMissing() {
        // Given
        SensorProtocol protocolDefinition = new SensorProtocol();
        protocolDefinition.setSensorBody(null);
        
        // When & Then
        assertThatThrownBy(() -> protocolDefinition.validateMessage())
            .isInstanceOf(ProtocolException.class)
            .hasMessageContaining("Required field is null");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"0xAA55", "0x55AA"})
    void should_AcceptValidSyncWords(String syncWord) {
        // Given
        SensorProtocol protocolDefinition = new SensorProtocol();
        protocolDefinition.setSyncWord(syncWord);
        
        // When
        boolean isValid = protocolDefinition.isValidSyncWord();
        
        // Then
        assertThat(isValid).isTrue();
    }
}
```

#### 集成测试
```java
@SpringBootTest
@TestPropertySource(properties = {
    "protocolDefinition.validation.enabled=true",
    "protocolDefinition.logging.level=DEBUG"
})
class ProtocolIntegrationTest {
    
    @Autowired
    private ProtocolCodec protocolCodec;
    
    @Autowired
    private ProtocolFactory protocolFactory;
    
    @Test
    void should_EncodeAndDecodeSuccessfully_When_ValidProtocol() {
        // Given
        SensorProtocol originalProtocol = protocolFactory.createProtocol(SensorProtocol.class);
        originalProtocol.getSensorBody().setSensorValue(23.5f);
        originalProtocol.getSensorBody().setTimestamp(1640995200000L);
        
        // When
        byte[] encodedData = protocolCodec.encode(originalProtocol);
        SensorProtocol decodedProtocol = protocolCodec.decode(encodedData, SensorProtocol.class);
        
        // Then
        assertThat(decodedProtocol.getSensorBody().getSensorValue())
            .isEqualTo(originalProtocol.getSensorBody().getSensorValue());
        assertThat(decodedProtocol.getSensorBody().getTimestamp())
            .isEqualTo(originalProtocol.getSensorBody().getTimestamp());
    }
}
```

## 常见问题

### 1. 注解配置问题

#### 问题：注解参数配置错误
```java
// ❌ 常见错误
@Node(id = "temperature", name = "温度", length = 16, valueType = ValueType.STRING)
private int temperature;  // 类型不匹配

// ✅ 正确配置
@Node(id = "temperature", name = "温度", length = 16, valueType = ValueType.INT)
private int temperature;
```

#### 问题：ID重复
```java
// ❌ 常见错误
public class DuplicateIdExample {
    @Node(id = "data", name = "数据1", length = 32, valueType = ValueType.INT)
    private int data1;
    
    @Node(id = "data", name = "数据2", length = 32, valueType = ValueType.INT)  // ID重复
    private int data2;
}

// ✅ 正确配置
public class UniqueIdExample {
    @Node(id = "data1", name = "数据1", length = 32, valueType = ValueType.INT)
    private int data1;
    
    @Node(id = "data2", name = "数据2", length = 32, valueType = ValueType.INT)
    private int data2;
}
```

### 2. 类型转换问题

#### 问题：自动类型推断失败
```java
// ❌ 可能出现问题的情况
@Node(id = "hex_data", name = "十六进制数据", length = 64)
private String hexData;  // 可能被推断为STRING类型

// ✅ 明确指定类型
@Node(id = "hex_data", name = "十六进制数据", length = 64, valueType = ValueType.HEX)
private String hexData;
```

### 3. 性能问题

#### 问题：频繁的反射调用
```java
// ❌ 性能问题
public class PerformanceProblem {
    public void processMany(List<SensorProtocol> protocols) {
        for (SensorProtocol protocolDefinition : protocols) {
            // 每次都进行反射解析
            processProtocolWithReflection(protocolDefinition);
        }
    }
}

// ✅ 性能优化
@Component
public class PerformanceOptimized {
    private final Map<Class<?>, ProtocolMetadata> metadataCache = new ConcurrentHashMap<>();
    
    public void processMany(List<SensorProtocol> protocols) {
        ProtocolMetadata metadata = getOrCreateMetadata(SensorProtocol.class);
        
        for (SensorProtocol protocolDefinition : protocols) {
            // 使用缓存的元数据
            processProtocolWithCache(protocolDefinition, metadata);
        }
    }
    
    private ProtocolMetadata getOrCreateMetadata(Class<?> protocolClass) {
        return metadataCache.computeIfAbsent(protocolClass, this::createMetadata);
    }
}
```

## 故障排除

### 1. 编译时问题

#### 注解处理器未运行
```bash
# 检查Maven配置
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>11</source>
        <target>11</target>
        <annotationProcessorPaths>
            <path>
                <groupId>com.iecas.cmd</groupId>
                <artifactId>protocolDefinition-annotation-processor</artifactId>
                <version>1.0.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

#### IDE配置问题
```text
IntelliJ IDEA:
1. File -> Settings -> Build -> Compiler -> Annotation Processors
2. 勾选 "Enable annotation processing"
3. 设置 "Processor path" 指向注解处理器JAR

Eclipse:
1. Project Properties -> Java Build Path -> Libraries
2. 添加注解处理器JAR到Classpath
3. Project Properties -> Java Compiler -> Annotation Processing
4. 勾选 "Enable project specific settings"
```

### 2. 运行时问题

#### 反射访问权限问题
```java
// 问题：Java 9+模块系统限制
// 解决方案：添加JVM参数
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED

// 或者在代码中处理
public class ReflectionUtils {
    public static void makeAccessible(Field field) {
        try {
            field.setAccessible(true);
        } catch (SecurityException e) {
            // 记录警告但不中断程序
            logger.warn("Cannot make field accessible: " + field.getName(), e);
        }
    }
}
```

#### 类加载问题
```java
// 问题：在不同类加载器环境中使用
// 解决方案：使用线程上下文类加载器
public class ClassLoaderSafeProtocolFactory {
    
    public <T> T createProtocol(Class<T> protocolClass) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(protocolClass.getClassLoader());
            return doCreateProtocol(protocolClass);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private <T> T doCreateProtocol(Class<T> protocolClass) {
        // 实际创建逻辑
        return null;
    }
}
```

### 3. 调试技巧

#### 启用详细日志
```properties
# application.properties
logging.level.com.iecas.cmd.protocolDefinition=DEBUG
protocolDefinition.debug.enabled=true
protocolDefinition.validation.strict=true
```

#### 使用调试工具
```java
@Component
public class ProtocolDebugger {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDebugger.class);
    
    public void debugProtocol(Object protocolDefinition) {
        Class<?> protocolClass = protocolDefinition.getClass();
        logger.debug("Debugging protocolDefinition: {}", protocolClass.getSimpleName());
        
        // 输出协议基本信息
        if (protocolClass.isAnnotationPresent(Protocol.class)) {
            Protocol protocolAnnotation = protocolClass.getAnnotation(Protocol.class);
            logger.debug("ProtocolDefinition ID: {}, Name: {}, Length: {}", 
                protocolAnnotation.id(), 
                protocolAnnotation.name(), 
                protocolAnnotation.length());
        }
        
        // 输出字段信息
        Field[] fields = protocolClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Node.class)) {
                debugField(protocolDefinition, field);
            }
        }
    }
    
    private void debugField(Object protocolDefinition, Field field) {
        Node nodeAnnotation = field.getAnnotation(Node.class);
        
        try {
            field.setAccessible(true);
            Object value = field.get(protocolDefinition);
            
            logger.debug("Field - ID: {}, Name: {}, Type: {}, Value: {}", 
                nodeAnnotation.id(),
                nodeAnnotation.name(),
                nodeAnnotation.valueType(),
                value);
                
        } catch (IllegalAccessException e) {
            logger.error("Cannot access field: " + field.getName(), e);
        }
    }
}
```

---

## 总结

Java类配置协议提供了一种类型安全、IDE友好的协议定义方式。通过合理使用注解、继承、组合等面向对象特性，可以构建出既灵活又可维护的协议系统。

**关键要点：**

1. **类型安全** - 编译时检查，减少运行时错误
2. **面向对象** - 充分利用Java的继承、封装、多态特性
3. **注解驱动** - 使用注解简化配置，提高开发效率
4. **性能优化** - 合理使用缓存、对象池等技术
5. **错误处理** - 完善的异常处理和验证机制
6. **测试友好** - 支持单元测试和集成测试

通过遵循本手册中的最佳实践和设计原则，可以开发出高质量、高性能的协议处理系统。