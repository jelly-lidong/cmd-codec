package com.iecas.cmd.network.tcp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的TCP连接测试类
 * 用于快速验证TCP连接功能
 */
public class SimpleTcpTest {
    
    public static void main(String[] args) {
        try {
            // 测试1: 基本连接测试
            testBasicConnection();
            
            Thread.sleep(2000);
            
            // 测试2: 消息收发测试
            testMessageExchange();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 基本连接测试
     */
    public static void testBasicConnection() throws Exception {
        System.out.println("=== 基本连接测试 ===");
        
        NetworkConfig config = NetworkConfig.tcp("localhost", 9999)
                .keepAlive(true)
                .tcpNoDelay(true);
        
        // 创建简单的事件处理器
        NetworkEventHandler eventHandler = new NetworkEventHandler() {
            @Override
            public void onServerStarted(InetSocketAddress localAddress) {
                System.out.println("✅ 服务端启动: " + localAddress);
            }
            
            @Override
            public void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
                System.out.println("✅ 客户端连接: " + localAddress + " -> " + remoteAddress);
            }
            
            @Override
            public void onConnected(InetSocketAddress remoteAddress) {
                System.out.println("✅ 连接建立: " + remoteAddress);
            }
            
            @Override
            public void onDisconnected(InetSocketAddress remoteAddress, Throwable cause) {
                System.out.println("❌ 连接断开: " + remoteAddress + (cause != null ? " 原因: " + cause.getMessage() : ""));
            }
            
            @Override
            public void onMessageReceived(NetworkMessage message) {
                System.out.println("📨 收到消息: " + message.getDataAsString());
            }
            
            @Override
            public void onMessageSent(NetworkMessage message) {
                System.out.println("📤 发送消息: " + message.getDataAsString());
            }
            
            @Override
            public void onException(InetSocketAddress remoteAddress, Throwable cause) {
                System.err.println("⚠️ 异常: " + remoteAddress + " - " + cause.getMessage());
            }
        };
        
        // 启动服务端
        TcpServer server = new TcpServer(config, eventHandler);
        CompletableFuture<Void> serverStart = server.start();
        serverStart.get(5, TimeUnit.SECONDS);
        
        // 等待服务端启动
        Thread.sleep(1000);
        
        // 启动客户端
        TcpClient client = new TcpClient(config, eventHandler);
        CompletableFuture<Void> clientConnect = client.connect();
        clientConnect.get(5, TimeUnit.SECONDS);
        
        // 等待连接建立
        Thread.sleep(1000);
        
        // 检查连接状态
        System.out.println("服务端状态: " + (server.isStarted() ? "运行中" : "已停止"));
        System.out.println("客户端状态: " + (client.isConnected() ? "已连接" : "已断开"));
        System.out.println("客户端数量: " + server.getClientCount());
        
        // 清理
        client.shutdown();
        server.shutdown();
        
        System.out.println("✅ 基本连接测试完成\n");
    }
    
    /**
     * 消息收发测试
     */
    public static void testMessageExchange() throws Exception {
        System.out.println("=== 消息收发测试 ===");
        
        NetworkConfig config = NetworkConfig.tcp("localhost", 9998)
                .keepAlive(true)
                .tcpNoDelay(true);
        
        final StringBuilder receivedMessages = new StringBuilder();
        
        NetworkEventHandler eventHandler = new NetworkEventHandler() {
            @Override
            public void onServerStarted(InetSocketAddress localAddress) {
                System.out.println("✅ 服务端启动: " + localAddress);
            }
            
            @Override
            public void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
                System.out.println("✅ 客户端连接: " + localAddress + " -> " + remoteAddress);
            }
            
            @Override
            public void onMessageReceived(NetworkMessage message) {
                String msg = message.getDataAsString();
                receivedMessages.append(msg).append("; ");
                System.out.println("📨 服务端收到: " + msg);
                
                // 自动回复
                if (message.getSender() != null) {
                    String response = "Echo: " + msg;
                    // 这里需要通过服务端发送回复
                    System.out.println("📤 服务端回复: " + response);
                }
            }
            
            @Override
            public void onMessageSent(NetworkMessage message) {
                System.out.println("📤 消息发送: " + message.getDataAsString());
            }
            
            @Override
            public void onException(InetSocketAddress remoteAddress, Throwable cause) {
                System.err.println("⚠️ 异常: " + cause.getMessage());
            }
        };
        
        // 启动服务端
        TcpServer server = new TcpServer(config, eventHandler);
        server.start().get(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        
        // 启动客户端
        TcpClient client = new TcpClient(config, eventHandler);
        client.connect().get(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        
        // 发送测试消息
        String[] testMessages = {
            "Hello Server!",
            "这是中文测试消息",
            "Test Message #1",
            "Test Message #2",
            "Final Test Message"
        };
        
        for (String message : testMessages) {
            System.out.println("📤 客户端发送: " + message);
            client.sendMessage(message.getBytes()).get(2, TimeUnit.SECONDS);
            Thread.sleep(500); // 避免消息发送过快
        }
        
        // 等待消息处理
        Thread.sleep(2000);
        
        System.out.println("收到的所有消息: " + receivedMessages.toString());
        
        // 测试连接稳定性
        System.out.println("🔄 测试连接稳定性...");
        for (int i = 0; i < 10; i++) {
            String stabilityMsg = "Stability Test #" + (i + 1);
            client.sendMessage(stabilityMsg.getBytes()).get(1, TimeUnit.SECONDS);
            Thread.sleep(1000);
        }
        
        // 清理
        client.shutdown();
        server.shutdown();
        
        System.out.println("✅ 消息收发测试完成\n");
    }
    
    /**
     * 压力测试
     */
    public static void testStress() throws Exception {
        System.out.println("=== 压力测试 ===");
        
        NetworkConfig config = NetworkConfig.tcp("localhost", 9997)
                .keepAlive(true)
                .tcpNoDelay(true);
        
        final AtomicInteger messageCount = new AtomicInteger(0);
        
        NetworkEventHandler eventHandler = new NetworkEventHandler() {
            @Override
            public void onServerStarted(InetSocketAddress localAddress) {
                System.out.println("✅ 服务端启动: " + localAddress);
            }
            
            @Override
            public void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
                System.out.println("✅ 客户端连接: " + localAddress + " -> " + remoteAddress);
            }
            
            @Override
            public void onMessageReceived(NetworkMessage message) {
                int count = messageCount.incrementAndGet();
                if (count % 100 == 0) {
                    System.out.println("📨 已处理消息数: " + count);
                }
            }
            
            @Override
            public void onException(InetSocketAddress remoteAddress, Throwable cause) {
                System.err.println("⚠️ 异常: " + cause.getMessage());
            }
        };
        
        // 启动服务端
        TcpServer server = new TcpServer(config, eventHandler);
        server.start().get(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        
        // 启动客户端
        TcpClient client = new TcpClient(config, eventHandler);
        client.connect().get(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        
        // 发送大量消息
        System.out.println("🚀 开始发送大量消息...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            String message = "Stress Test Message #" + i;
            client.sendMessage(message.getBytes()).get(1, TimeUnit.SECONDS);
            
            if (i % 100 == 0) {
                System.out.println("📤 已发送消息数: " + i);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("✅ 压力测试完成");
        System.out.println("发送消息数: 1000");
        System.out.println("接收消息数: " + messageCount.get());
        System.out.println("耗时: " + duration + "ms");
        System.out.println("平均速度: " + (1000.0 / duration * 1000) + " 消息/秒");
        
        // 清理
        client.shutdown();
        server.shutdown();
        
        System.out.println("✅ 压力测试完成\n");
    }
}
