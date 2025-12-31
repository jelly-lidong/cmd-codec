# 网络通信模块使用指南

## 📖 概述

本模块基于Netty实现了完整的网络通信功能，支持：

- ✅ **TCP长连接**：可靠的双向通信，支持心跳检测
- ✅ **UDP单播**：快速的点对点通信
- ✅ **UDP组播**：一对多的广播通信
- ✅ **UDP组播（指定源）**：支持SSM（Source-Specific Multicast）

## 🏗️ 架构设计

### 核心组件

1. **NetworkConfig** - 网络配置类，支持流式API配置
2. **NetworkMessage** - 网络消息封装类
3. **NetworkEventHandler** - 事件处理接口
4. **NetworkFactory** - 工厂类，提供统一创建接口

### 客户端和服务端

- **TcpClient/TcpServer** - TCP长连接实现
- **UdpClient/UdpServer** - UDP通信实现（支持单播和组播）

## 🚀 快速开始

### 1. TCP长连接示例

#### 服务端
```java
// 创建事件处理器
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onServerStarted(InetSocketAddress localAddress) {
        System.out.println("服务器启动: " + localAddress);
    }
    
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("收到消息: " + message.getDataAsString());
    }
};

// 创建TCP服务端
TcpServer server = NetworkFactory.createTcpServer("localhost", 8080, eventHandler);

// 启动服务器
server.start().get();

// 向客户端发送消息
server.sendMessage("clientId", "Hello Client!".getBytes());

// 关闭服务器
server.shutdown();
```

#### 客户端
```java
// 创建事件处理器
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onClientConnected(InetSocketAddress local, InetSocketAddress remote) {
        System.out.println("连接成功: " + local + " -> " + remote);
    }
    
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("收到消息: " + message.getDataAsString());
    }
};

// 创建TCP客户端
TcpClient client = NetworkFactory.createTcpClient("localhost", 8080, eventHandler);

// 连接服务器
client.connect().get();

// 发送消息
client.sendMessage("Hello Server!".getBytes()).get();

// 发送消息并等待响应
NetworkMessage response = client.sendMessageAndWaitResponse("Request".getBytes(), 5000).get();

// 关闭客户端
client.shutdown();
```

### 2. UDP单播示例

#### 服务端
```java
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("收到UDP消息: " + message.getDataAsString());
        // 回复消息
        server.sendMessage("Reply".getBytes(), message.getSender());
    }
};

UdpServer server = NetworkFactory.createUdpUnicastServer("localhost", 9090, eventHandler);
server.start().get();
```

#### 客户端
```java
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("收到回复: " + message.getDataAsString());
    }
};

UdpClient client = NetworkFactory.createUdpUnicastClient("localhost", 9090, eventHandler);
client.start().get();
client.sendMessage("Hello UDP Server!".getBytes()).get();
```

### 3. UDP组播示例

#### 服务端（发送方）
```java
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onMulticastJoined(InetSocketAddress multicastAddress, InetSocketAddress sourceAddress) {
        System.out.println("加入组播组: " + multicastAddress);
    }
};

UdpServer server = NetworkFactory.createUdpMulticastServer("224.0.0.1", 9999, eventHandler);
server.start().get();

// 发送组播消息
server.sendMulticastMessage("Hello Multicast Group!".getBytes()).get();
```

#### 客户端（接收方）
```java
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onMessageReceived(NetworkMessage message) {
        System.out.println("收到组播消息: " + message.getDataAsString());
    }
};

UdpClient client = NetworkFactory.createUdpMulticastClient("224.0.0.1", 9999, eventHandler);
client.start().get();
```

### 4. UDP组播（指定源）示例

```java
// 创建指定源的组播客户端
UdpClient client = NetworkFactory.createUdpMulticastClientWithSource(
    "224.0.0.1",    // 组播地址
    9999,           // 组播端口
    "192.168.1.100", // 源地址
    eventHandler
);

// 创建指定网络接口的组播客户端
UdpClient client2 = NetworkFactory.createUdpMulticastClientWithInterface(
    "224.0.0.1",    // 组播地址
    9999,           // 组播端口
    "eth0",         // 网络接口名称
    eventHandler
);
```

## ⚙️ 高级配置

### 使用NetworkConfig进行详细配置

```java
NetworkConfig config = NetworkConfig.tcp("localhost", 8080)
    .connectTimeout(10000)          // 连接超时10秒
    .readTimeout(30000)             // 读超时30秒
    .writeTimeout(30000)            // 写超时30秒
    .tcpNoDelay(true)               // 启用TCP_NODELAY
    .keepAlive(true)                // 启用SO_KEEPALIVE
    .bufferSize(65536, 65536)       // 设置缓冲区大小
    .workerThreads(8)               // 工作线程数
    .ssl(true);                     // 启用SSL（需要配置证书）

TcpServer server = NetworkFactory.createTcpServer(config, eventHandler);
```

### 组播配置示例

```java
NetworkConfig config = NetworkConfig.multicast("224.0.0.1", 9999)
    .sourceAddress("192.168.1.100")    // 指定源地址
    .networkInterface("eth0")           // 指定网络接口
    .bufferSize(8192, 8192);           // 设置缓冲区大小

UdpClient client = NetworkFactory.createUdpClient(config, eventHandler);
```

## 📋 事件处理

### NetworkEventHandler接口方法

```java
public interface NetworkEventHandler {
    // 连接事件
    void onConnected(InetSocketAddress remoteAddress);
    void onDisconnected(InetSocketAddress remoteAddress, Throwable cause);
    
    // 消息事件
    void onMessageReceived(NetworkMessage message);
    void onMessageSent(NetworkMessage message);
    void onMessageSendFailed(NetworkMessage message, Throwable cause);
    
    // 服务器事件
    void onServerStarted(InetSocketAddress localAddress);
    void onServerStopped(InetSocketAddress localAddress);
    
    // 客户端事件
    void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress);
    void onClientConnectFailed(InetSocketAddress remoteAddress, Throwable cause);
    
    // 心跳事件
    void onHeartbeatTimeout(InetSocketAddress remoteAddress);
    
    // 组播事件
    void onMulticastJoined(InetSocketAddress multicastAddress, InetSocketAddress sourceAddress);
    void onMulticastLeft(InetSocketAddress multicastAddress, InetSocketAddress sourceAddress);
    
    // 异常事件
    void onException(InetSocketAddress remoteAddress, Throwable cause);
}
```

## 🔧 消息处理

### NetworkMessage类

```java
NetworkMessage message = new NetworkMessage(data);

// 获取消息内容
byte[] data = message.getData();
String text = message.getDataAsString();
String hex = message.getDataAsHex();

// 获取地址信息
InetSocketAddress sender = message.getSender();
InetSocketAddress receiver = message.getReceiver();

// 消息类型
MessageType type = message.getMessageType(); // REQUEST, RESPONSE, NOTIFY, HEARTBEAT, DATA

// 创建响应消息
NetworkMessage response = message.createResponse("Response Data".getBytes());
```

## 🎯 最佳实践

### 1. 资源管理
```java
// 始终在finally块或try-with-resources中关闭资源
try {
    TcpClient client = NetworkFactory.createTcpClient("localhost", 8080, eventHandler);
    client.connect().get();
    // 使用客户端...
} finally {
    if (client != null) {
        client.shutdown();
    }
}
```

### 2. 异常处理
```java
NetworkEventHandler eventHandler = new NetworkEventHandler() {
    @Override
    public void onException(InetSocketAddress remoteAddress, Throwable cause) {
        logger.error("网络异常: " + remoteAddress, cause);
        // 实现重连逻辑或其他恢复策略
    }
    
    @Override
    public void onMessageSendFailed(NetworkMessage message, Throwable cause) {
        logger.warn("消息发送失败: " + message.getMessageId(), cause);
        // 实现重试逻辑
    }
};
```

### 3. 心跳检测
```java
// TCP客户端自动发送心跳
client.sendHeartbeat().get();

// 处理心跳超时
@Override
public void onHeartbeatTimeout(InetSocketAddress remoteAddress) {
    logger.warn("心跳超时: " + remoteAddress);
    // 实现重连或清理逻辑
}
```

### 4. 线程安全
```java
// 所有网络操作都是线程安全的，可以在多线程环境中使用
ExecutorService executor = Executors.newFixedThreadPool(10);

for (int protocol = 0; protocol < 10; protocol++) {
    executor.submit(() -> {
        client.sendMessage("Message from thread".getBytes());
    });
}
```

## 🔍 故障排除

### 常见问题

1. **连接超时**
   - 检查网络连通性
   - 调整connectTimeout配置
   - 确认服务端已启动

2. **组播不工作**
   - 检查组播地址范围（224.0.0.0-239.255.255.255）
   - 确认网络接口支持组播
   - 检查防火墙设置

3. **内存泄漏**
   - 确保调用shutdown()方法
   - 检查事件处理器中是否有循环引用

### 调试技巧

```java
// 启用详细日志
NetworkConfig config = NetworkConfig.tcp("localhost", 8080)
    .workerThreads(1); // 减少线程数便于调试

// 在事件处理器中添加详细日志
@Override
public void onMessageReceived(NetworkMessage message) {
    logger.debug("收到消息: {} 字节, 来自: {}", 
        message.getLength(), message.getSender());
}
```

## 📊 性能优化

### 1. 缓冲区调优
```java
NetworkConfig config = NetworkConfig.tcp("localhost", 8080)
    .bufferSize(128 * 1024, 128 * 1024); // 128KB缓冲区
```

### 2. 线程池调优
```java
NetworkConfig config = NetworkConfig.tcp("localhost", 8080)
    .workerThreads(Runtime.getRuntime().availableProcessors() * 2)
    .bossThreads(1);
```

### 3. TCP优化
```java
NetworkConfig config = NetworkConfig.tcp("localhost", 8080)
    .tcpNoDelay(true)    // 禁用Nagle算法，降低延迟
    .keepAlive(true);    // 启用TCP保活
```

## 🧪 测试

运行演示程序：
```java
// 运行完整演示
NetworkDemo.main(new String[]{});
```

## �� 许可证

本模块遵循项目整体许可证。 