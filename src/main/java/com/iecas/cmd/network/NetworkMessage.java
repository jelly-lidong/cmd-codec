package com.iecas.cmd.network;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;

/**
 * 网络消息类
 */
public class NetworkMessage {
    
    /**
     * 消息内容
     */
    private byte[] data;
    
    /**
     * 发送方地址
     */
    private InetSocketAddress sender;
    
    /**
     * 接收方地址
     */
    private InetSocketAddress receiver;
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 消息长度
     */
    private int length;
    
    /**
     * 是否需要响应
     */
    private boolean needResponse = false;
    
    /**
     * 响应超时时间（毫秒）
     */
    private long responseTimeoutMs = 5000;
    
    public NetworkMessage() {
        this.timestamp = LocalDateTime.now();
        this.messageId = generateMessageId();
    }
    
    public NetworkMessage(byte[] data) {
        this();
        this.data = data;
        this.length = data != null ? data.length : 0;
    }
    
    public NetworkMessage(byte[] data, InetSocketAddress sender, InetSocketAddress receiver) {
        this(data);
        this.sender = sender;
        this.receiver = receiver;
    }
    
    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * 创建响应消息
     */
    public NetworkMessage createResponse(byte[] responseData) {
        NetworkMessage response = new NetworkMessage(responseData);
        response.sender = this.receiver;
        response.receiver = this.sender;
        response.messageType = MessageType.RESPONSE;
        return response;
    }
    
    /**
     * 检查是否为响应消息
     */
    public boolean isResponse() {
        return messageType == MessageType.RESPONSE;
    }
    
    /**
     * 检查是否为请求消息
     */
    public boolean isRequest() {
        return messageType == MessageType.REQUEST;
    }
    
    /**
     * 获取数据的字符串表示
     */
    public String getDataAsString() {
        return data != null ? new String(data) : "";
    }
    
    /**
     * 获取数据的十六进制表示
     */
    public String getDataAsHex() {
        if (data == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    
    // Getters and Setters
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
        this.length = data != null ? data.length : 0;
    }
    
    public InetSocketAddress getSender() {
        return sender;
    }
    
    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }
    
    public InetSocketAddress getReceiver() {
        return receiver;
    }
    
    public void setReceiver(InetSocketAddress receiver) {
        this.receiver = receiver;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public boolean isNeedResponse() {
        return needResponse;
    }
    
    public void setNeedResponse(boolean needResponse) {
        this.needResponse = needResponse;
    }
    
    public long getResponseTimeoutMs() {
        return responseTimeoutMs;
    }
    
    public void setResponseTimeoutMs(long responseTimeoutMs) {
        this.responseTimeoutMs = responseTimeoutMs;
    }
    
    @Override
    public String toString() {
        return String.format("NetworkMessage{id='%s', type=%s, length=%d, sender=%s, receiver=%s, timestamp=%s}", 
                messageId, messageType, length, sender, receiver, timestamp);
    }
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        REQUEST,    // 请求消息
        RESPONSE,   // 响应消息
        NOTIFY,     // 通知消息
        HEARTBEAT,  // 心跳消息
        DATA        // 数据消息
    }
} 