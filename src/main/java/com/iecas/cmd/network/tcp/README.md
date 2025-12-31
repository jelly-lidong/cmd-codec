# TCP连接测试类使用说明

## 📁 文件说明

### 1. TcpConnectionTest.java
**完整功能测试类**
- 长时间连接稳定性测试（默认60秒）
- 连接重连功能测试
- 并发连接测试
- 详细的事件日志和状态监控

### 2. SimpleTcpTest.java
**简单快速测试类**
- 基本连接测试
- 消息收发测试
- 压力测试（1000条消息）
- 适合快速验证功能

### 3. run-tcp-test.sh
**测试运行脚本**
- 自动编译项目
- 运行测试类
- 提供交互式选择

## 🚀 快速开始

### 方法1: 使用脚本运行
```bash
cd cmd-back/cmd-codec
./src/main/java/com/iecas/cmd/network/tcp/run-tcp-test.sh
```

### 方法2: 直接运行Java类
```bash
# 编译项目
mvn compile

# 运行简单测试
java -cp target/classes:target/dependency/* com.iecas.cmd.network.tcp.SimpleTcpTest

# 运行完整测试
java -cp target/classes:target/dependency/* com.iecas.cmd.network.tcp.TcpConnectionTest
```

### 方法3: 在IDE中运行
1. 打开 `SimpleTcpTest.java` 或 `TcpConnectionTest.java`
2. 右键选择 "Run" 或 "Debug"

## 📊 测试内容

### SimpleTcpTest 测试项目
1. **基本连接测试**
   - 服务端启动
   - 客户端连接
   - 连接状态检查

2. **消息收发测试**
   - 发送多种类型消息
   - 验证消息接收
   - 连接稳定性测试

3. **压力测试**
   - 发送1000条消息
   - 性能统计
   - 连接稳定性验证

### TcpConnectionTest 测试项目
1. **长时间连接测试**
   - 60秒持续连接
   - 定期消息发送
   - 连接状态监控

2. **重连功能测试**
   - 模拟连接断开
   - 自动重连验证
   - 重连后消息发送

3. **并发连接测试**
   - 多客户端同时连接
   - 连接数统计
   - 并发消息处理

## 🔧 配置说明

### 端口配置
- 简单测试: 9999, 9998, 9997
- 完整测试: 8888, 8889, 8890

### 网络配置
```java
NetworkConfig config = NetworkConfig.tcp("localhost", PORT)
    .keepAlive(true)      // 启用TCP Keep-Alive
    .tcpNoDelay(true)     // 禁用Nagle算法
    .reuseAddress(true)   // 允许地址重用
    .connectTimeout(5000) // 连接超时5秒
    .bufferSize(8192, 8192); // 缓冲区大小
```

## 📈 测试结果解读

### 成功指标
- ✅ 服务端成功启动
- ✅ 客户端成功连接
- ✅ 消息正常收发
- ✅ 连接保持稳定
- ✅ 无异常错误

### 失败排查
- ❌ 端口被占用 → 更换端口或停止占用进程
- ❌ 连接超时 → 检查网络配置
- ❌ 消息发送失败 → 检查连接状态
- ❌ 连接频繁断开 → 检查心跳机制

## 🐛 常见问题

### 1. 端口被占用
```bash
# 查看端口占用
lsof -i :9999

# 杀死占用进程
kill -9 <PID>
```

### 2. 连接超时
- 检查防火墙设置
- 确认端口未被占用
- 调整连接超时时间

### 3. 消息发送失败
- 确认连接状态
- 检查消息内容
- 查看异常日志

## 📝 自定义测试

### 修改测试参数
```java
// 修改测试端口
private static final int PORT = 8888;

// 修改测试时长
private static final int TEST_DURATION_SECONDS = 120;

// 修改消息发送间隔
Thread.sleep(5000); // 5秒发送一次
```

### 添加自定义测试
```java
public void customTest() throws Exception {
    // 你的自定义测试逻辑
    NetworkConfig config = NetworkConfig.tcp("localhost", 9999);
    NetworkEventHandler handler = new NetworkEventHandler() {
        // 自定义事件处理
    };
    
    TcpServer server = new TcpServer(config, handler);
    TcpClient client = new TcpClient(config, handler);
    
    // 测试逻辑...
}
```

## 📋 测试报告模板

```
=== TCP连接测试报告 ===
测试时间: 2024-01-01 12:00:00
测试环境: localhost
测试端口: 9999

测试结果:
✅ 服务端启动: 成功
✅ 客户端连接: 成功
✅ 消息收发: 成功
✅ 连接稳定性: 良好
✅ 重连功能: 正常

性能统计:
- 总消息数: 1000
- 测试时长: 60秒
- 平均速度: 16.7 消息/秒
- 连接断开次数: 0

结论: 测试通过 ✅
```

## 🔗 相关文件

- `TcpServer.java` - TCP服务端实现
- `TcpClient.java` - TCP客户端实现
- `NetworkConfig.java` - 网络配置类
- `NetworkEventHandler.java` - 事件处理接口
- `NetworkMessage.java` - 网络消息类
