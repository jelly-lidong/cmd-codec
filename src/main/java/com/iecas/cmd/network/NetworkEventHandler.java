package com.iecas.cmd.network;

import java.net.InetSocketAddress;

/**
 * 网络事件处理接口
 */
public interface NetworkEventHandler {
    
    /**
     * 连接建立事件
     * 
     * @param remoteAddress 远程地址
     */
    default void onConnected(InetSocketAddress remoteAddress) {
        // 默认实现为空
    }
    
    /**
     * 连接断开事件
     * 
     * @param remoteAddress 远程地址
     * @param cause 断开原因
     */
    default void onDisconnected(InetSocketAddress remoteAddress, Throwable cause) {
        // 默认实现为空
    }
    
    /**
     * 消息接收事件
     * 
     * @param message 接收到的消息
     */
    void onMessageReceived(NetworkMessage message);
    
    /**
     * 消息发送成功事件
     * 
     * @param message 发送的消息
     */
    default void onMessageSent(NetworkMessage message) {
        // 默认实现为空
    }
    
    /**
     * 消息发送失败事件
     * 
     * @param message 发送失败的消息
     * @param cause 失败原因
     */
    default void onMessageSendFailed(NetworkMessage message, Throwable cause) {
        // 默认实现为空
    }
    
    /**
     * 异常事件
     * 
     * @param remoteAddress 远程地址
     * @param cause 异常原因
     */
    default void onException(InetSocketAddress remoteAddress, Throwable cause) {
        // 默认实现为空
    }
    
    /**
     * 服务器启动事件
     * 
     * @param localAddress 本地监听地址
     */
    default void onServerStarted(InetSocketAddress localAddress) {
        // 默认实现为空
    }
    
    /**
     * 服务器停止事件
     * 
     * @param localAddress 本地监听地址
     */
    default void onServerStopped(InetSocketAddress localAddress) {
        // 默认实现为空
    }
    
    /**
     * 客户端连接成功事件
     * 
     * @param localAddress 本地地址
     * @param remoteAddress 远程地址
     */
    default void onClientConnected(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        // 默认实现为空
    }
    
    /**
     * 客户端连接失败事件
     * 
     * @param remoteAddress 远程地址
     * @param cause 失败原因
     */
    default void onClientConnectFailed(InetSocketAddress remoteAddress, Throwable cause) {
        // 默认实现为空
    }
    
    /**
     * 心跳超时事件
     * 
     * @param remoteAddress 远程地址
     */
    default void onHeartbeatTimeout(InetSocketAddress remoteAddress) {
        // 默认实现为空
    }
    
    /**
     * 组播加入事件
     * 
     * @param multicastAddress 组播地址
     * @param sourceAddress 源地址（可能为null）
     */
    default void onMulticastJoined(InetSocketAddress multicastAddress, InetSocketAddress sourceAddress) {
        // 默认实现为空
    }
    
    /**
     * 组播离开事件
     * 
     * @param multicastAddress 组播地址
     * @param sourceAddress 源地址（可能为null）
     */
    default void onMulticastLeft(InetSocketAddress multicastAddress, InetSocketAddress sourceAddress) {
        // 默认实现为空
    }
} 