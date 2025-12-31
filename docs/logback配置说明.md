# Logback 彩色日志配置说明

## 📋 配置概述

本项目已成功配置了Logback彩色日志输出，支持代码行数显示和多种颜色区分不同日志级别。

## 🎨 彩色日志特性

### 控制台输出格式
```
%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight([%thread]) %highlight(%-5level) %cyan(%logger{36}:%line) - %msg%n
```

### 颜色配置说明
- **时间戳**: 默认颜色（白色/黑色）
- **线程名**: `%highlight([%thread])` - 高亮显示
- **日志级别**: `%highlight(%-5level)` - 根据级别自动着色
  - `ERROR`: 红色
  - `WARN`: 黄色  
  - `INFO`: 蓝色
  - `DEBUG`: 灰色
- **类名和行号**: `%cyan(%logger{36}:%line)` - 青色显示
- **消息内容**: 默认颜色

### 文件输出格式
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}:%line - %msg%n
```
文件输出不包含颜色代码，确保日志文件的可读性。

## 🔧 配置文件详解

### 控制台输出配置
```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
        <!-- 彩色日志格式，包含代码行数 -->
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight([%thread]) %highlight(%-5level) %cyan(%logger{36}:%line) - %msg%n</pattern>
        <!-- 启用彩色输出 -->
        <charset>UTF-8</charset>
    </encoder>
</appender>
```

### 文件输出配置
```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/cmd-codec.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/cmd-codec.%d{yyyy-MM-dd}.%protocol.log</fileNamePattern>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>10MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <!-- 文件日志格式，包含代码行数但不包含颜色 -->
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}:%line - %msg%n</pattern>
    </encoder>
</appender>
```

## 📊 日志级别配置

### 根日志级别
```xml
<root level="INFO">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
</root>
```

### 包级别配置
```xml
<!-- 项目包设置为DEBUG级别 -->
<logger name="com.iecas.cmd" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
</logger>

<!-- 表达式验证器设置为WARN级别，减少调试信息 -->
<logger name="com.iecas.cmd.util.ExpressionValidator" level="WARN" additivity="false">
    <appender-ref ref="CONSOLE" />
</logger>
```

## 🎯 格式化元素说明

| 元素 | 说明 | 示例 |
|------|------|------|
| `%d{yyyy-MM-dd HH:mm:ss.SSS}` | 时间戳格式 | `2025-06-02 12:54:20.135` |
| `%highlight([%thread])` | 高亮线程名 | `[main]` |
| `%highlight(%-5level)` | 高亮日志级别（左对齐5字符） | `INFO ` |
| `%cyan(%logger{36}:%line)` | 青色类名和行号 | `com.iecas.cmd.ColorLogTest:15` |
| `%msg` | 日志消息内容 | 实际的日志消息 |
| `%n` | 换行符 | 系统换行符 |

## 🚀 使用示例

### 在代码中使用
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger log = LoggerFactory.getLogger(MyClass.class);
    
    public void myMethod() {
        log.debug("调试信息");           // 灰色
        log.debug("一般信息");            // 蓝色
        log.debug("警告信息");            // 黄色
        log.error("错误信息");           // 红色
        
        // 参数化日志
        log.debug("用户 {} 执行了操作 {}", username, operation);
        
        // 异常日志
        try {
            // 业务代码
        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        }
    }
}
```

### 输出效果
```
2025-06-02 12:54:20.135 [main] INFO  com.iecas.cmd.MyClass:15 - 一般信息
2025-06-02 12:54:20.136 [main] WARN  com.iecas.cmd.MyClass:16 - 警告信息
2025-06-02 12:54:20.137 [main] ERROR com.iecas.cmd.MyClass:17 - 错误信息
```

## 📁 文件输出

### 日志文件位置
- **主日志文件**: `logs/cmd-codec.log`
- **归档文件**: `logs/cmd-codec.2025-06-01.0.log`

### 轮转策略
- **按时间轮转**: 每天生成新文件
- **按大小轮转**: 单文件超过10MB时分割
- **保留期限**: 保留30天的历史日志
- **文件命名**: `cmd-codec.yyyy-MM-dd.protocol.log`

## ⚠️ 注意事项

1. **终端支持**: 彩色输出需要终端支持ANSI颜色代码
2. **IDE支持**: 大部分现代IDE都支持彩色日志显示
3. **性能影响**: 彩色输出对性能影响很小，可放心使用
4. **文件输出**: 文件中不包含颜色代码，保持纯文本格式

## 🔧 自定义配置

### 修改颜色
如需修改颜色，可以使用以下颜色代码：
- `%black` - 黑色
- `%red` - 红色
- `%green` - 绿色
- `%yellow` - 黄色
- `%blue` - 蓝色
- `%magenta` - 洋红色
- `%cyan` - 青色
- `%white` - 白色
- `%gray` - 灰色

### 修改格式
可以根据需要调整日志格式，例如：
```xml
<!-- 简化格式 -->
<pattern>%d{HH:mm:ss} %highlight(%-5level) %cyan(%logger{20}:%line) - %msg%n</pattern>

<!-- 详细格式 -->
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{50}:%line) [%method] - %msg%n</pattern>
```

## ✅ 验证测试

运行 `ColorLogTest` 类可以验证彩色日志配置：
```bash
java -cp "classpath" com.iecas.cmd.ColorLogTest
```

预期看到不同颜色的日志输出和正确的行号显示。

---
*配置完成时间: 2024年12月*  
*适用版本: Logback 1.2.3+*  
*项目: CMD-CODEC* 