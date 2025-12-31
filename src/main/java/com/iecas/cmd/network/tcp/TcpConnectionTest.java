package com.iecas.cmd.network.tcp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP长连接测试类
 * 用于测试TCP客户端和服务端的连接稳定性
 */
public class TcpConnectionTest {
    
    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int TEST_DURATION_SECONDS = 60; // 测试持续时间
    
    private TcpServer server;
    private TcpClient client;
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final CountDownLatch testLatch = new CountDownLatch(1);
    
    public static void main(String[] args) {
        TcpConnectionTest test = new TcpConnectionTest();
        try {
            test.runTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 运行TCP连接测试
     */
    public void runTest() throws Exception {
        System.out.println("🚀 开始TCP长连接测试...");
        System.out.println("测试配置: " + HOST + ":" + PORT);
        System.out.println("测试时长: " + TEST_DURATION_SECONDS + "秒");
        System.out.println("=====================================");
        
        // 创建网络配置
        NetworkConfig config = NetworkConfig.tcp(HOST, PORT)
                .keepAlive(true)
                .tcpNoDelay(true)
                .reuseAddress(true)
                .connectTimeout(5000)
                .bufferSize(8192, 8192);
        
        // 创建事件处理器
        NetworkEventHandler eventHandler = createEventHandler();
        
        // 启动服务端
        startServer(config, eventHandler);
        
        // 等待服务端启动
        Thread.sleep(2000);
        
        // 启动客户端
        startClient(config, eventHandler);
        
        // 等待客户端连接
        Thread.sleep(1000);
        
        // 开始测试
        runConnectionTest();
        
        // 等待测试完成
        testLatch.await();
        
        // 清理资源
        cleanup();
        
        System.out.println("=====================================");
        System.out.println("✅ TCP长连接测试完成");
        System.out.println("总连接次数: " + connectionCount.get());
        System.out.println("总消息数量: " + messageCount.get());
    }
    
    /**
     * 创建事件处理器
     */
    private NetworkEventHandler createEventHandler() {
        return new NetworkEventHandler() {
            @Override
            public void onServerStarted(InetSocketAddress localAddress) {
                System.out.println("🟢 服务端启动成功: " + localAddress);
            }
            
            @Override
            public void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
                System.out.println("🔗 客户端连接成功: " + localAddress + " -> " + remoteAddress);
                connectionCount.incrementAndGet();
            }
            
            @Override
            public void onConnected(InetSocketAddress remoteAddress) {
                System.out.println("🔗 连接建立: " + remoteAddress);
            }
            
            @Override
            public void onDisconnected(InetSocketAddress remoteAddress, Throwable cause) {
                System.out.println("🔴 连接断开: " + remoteAddress + (cause != null ? " 原因: " + cause.getMessage() : ""));
            }
            
            @Override
            public void onMessageReceived(NetworkMessage message) {
                int count = messageCount.incrementAndGet();
                System.out.println("📨 收到消息 #" + count + ": " + message.getDataAsString() + " 来自: " + message.getSender());
                
                // 自动回复消息
                if (server != null && server.isStarted()) {
                    String response = "Server Response #" + count + " at " + System.currentTimeMillis();
                    server.broadcastMessage(response.getBytes());
                }
            }
            
            @Override
            public void onMessageSent(NetworkMessage message) {
                System.out.println("📤 消息发送成功: " + message.getDataAsString());
            }
            
            @Override
            public void onMessageSendFailed(NetworkMessage message, Throwable cause) {
                System.err.println("❌ 消息发送失败: " + message.getDataAsString() + " 原因: " + cause.getMessage());
            }
            
            @Override
            public void onException(InetSocketAddress remoteAddress, Throwable cause) {
                System.err.println("⚠️ 异常发生: " + remoteAddress + " 原因: " + cause.getMessage());
            }
            
            @Override
            public void onHeartbeatTimeout(InetSocketAddress remoteAddress) {
                System.out.println("💓 心跳超时: " + remoteAddress);
            }
        };
    }
    
    /**
     * 启动服务端
     */
    private void startServer(NetworkConfig config, NetworkEventHandler eventHandler) throws Exception {
        server = new TcpServer(config, eventHandler);
        CompletableFuture<Void> startFuture = server.start();
        startFuture.get(10, TimeUnit.SECONDS);
    }
    
    /**
     * 启动客户端
     */
    private void startClient(NetworkConfig config, NetworkEventHandler eventHandler) throws Exception {
        client = new TcpClient(config, eventHandler);
        CompletableFuture<Void> connectFuture = client.connect();
        connectFuture.get(10, TimeUnit.SECONDS);
    }
    
    /**
     * 运行连接测试
     */
    private void runConnectionTest() {
        System.out.println("🔄 开始连接稳定性测试...");
        
        // 启动消息发送线程
        Thread messageThread = new Thread(() -> {
            try {
                for (int i = 0; i < TEST_DURATION_SECONDS; i++) {
                    if (!client.isConnected()) {
                        System.out.println("⚠️ 客户端连接已断开，尝试重连...");
                        try {
                            client.connect().get(5, TimeUnit.SECONDS);
                            System.out.println("✅ 客户端重连成功");
                        } catch (Exception e) {
                            System.err.println("❌ 客户端重连失败: " + e.getMessage());
                        }
                    }
                    
                    // 发送测试消息
                    String message = "Test Message #" + (i + 1) + " at " + System.currentTimeMillis();
                    client.sendMessage(message.getBytes()).get(1, TimeUnit.SECONDS);
                    
                    // 每5秒发送一次消息
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                System.err.println("❌ 消息发送线程异常: " + e.getMessage());
            }
        });
        
        messageThread.start();
        
        // 启动连接状态监控线程
        Thread monitorThread = new Thread(() -> {
            try {
                for (int i = 0; i < TEST_DURATION_SECONDS; i++) {
                    boolean serverRunning = server.isStarted();
                    boolean clientConnected = client.isConnected();
                    
                    System.out.println("📊 状态检查 #" + (i + 1) + ": 服务端=" + 
                                     (serverRunning ? "运行中" : "已停止") + 
                                     ", 客户端=" + (clientConnected ? "已连接" : "已断开"));
                    
                    Thread.sleep(10000); // 每10秒检查一次
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        monitorThread.start();
        
        // 等待测试完成
        try {
            Thread.sleep(TEST_DURATION_SECONDS * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        testLatch.countDown();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        System.out.println("🧹 清理资源...");
        
        try {
            if (client != null) {
                client.disconnect().get(5, TimeUnit.SECONDS);
                client.shutdown();
            }
        } catch (Exception e) {
            System.err.println("❌ 客户端清理失败: " + e.getMessage());
        }
        
        try {
            if (server != null) {
                server.stop().get(5, TimeUnit.SECONDS);
                server.shutdown();
            }
        } catch (Exception e) {
            System.err.println("❌ 服务端清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试连接重连功能
     */
    public void testReconnection() throws Exception {
        System.out.println("🔄 测试连接重连功能...");
        
        NetworkConfig config = NetworkConfig.tcp(HOST, PORT + 1)
                .keepAlive(true)
                .tcpNoDelay(true);
        
        NetworkEventHandler eventHandler = createEventHandler();
        
        // 启动服务端
        TcpServer testServer = new TcpServer(config, eventHandler);
        testServer.start().get(5, TimeUnit.SECONDS);
        
        // 启动客户端
        TcpClient testClient = new TcpClient(config, eventHandler);
        testClient.connect().get(5, TimeUnit.SECONDS);
        
        // 模拟连接断开和重连
        for (int i = 0; i < 3; i++) {
            System.out.println("🔄 第 " + (i + 1) + " 次重连测试...");
            
            // 断开连接
            testClient.disconnect().get(2, TimeUnit.SECONDS);
            Thread.sleep(1000);
            
            // 重连
            testClient.connect().get(5, TimeUnit.SECONDS);
            Thread.sleep(2000);
            
            // 发送测试消息
            String message = "Reconnection Test #" + (i + 1);
            testClient.sendMessage(message.getBytes()).get(2, TimeUnit.SECONDS);
        }
        
        // 清理
        testClient.shutdown();
        testServer.shutdown();
        
        System.out.println("✅ 重连测试完成");
    }
    
    /**
     * 测试并发连接
     */
    public void testConcurrentConnections() throws Exception {
        System.out.println("🔄 测试并发连接...");
        
        NetworkConfig config = NetworkConfig.tcp(HOST, PORT + 2)
                .keepAlive(true)
                .tcpNoDelay(true);
        
        NetworkEventHandler eventHandler = createEventHandler();
        
        // 启动服务端
        TcpServer testServer = new TcpServer(config, eventHandler);
        testServer.start().get(5, TimeUnit.SECONDS);
        
        // 创建多个客户端
        TcpClient[] clients = new TcpClient[5];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new TcpClient(config, eventHandler);
            clients[i].connect().get(5, TimeUnit.SECONDS);
            Thread.sleep(100); // 避免同时连接
        }
        
        System.out.println("✅ 并发连接测试完成，连接数: " + testServer.getClientCount());
        
        // 清理
        for (TcpClient client : clients) {
            client.shutdown();
        }
        testServer.shutdown();
    }
}
