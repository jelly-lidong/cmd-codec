package com.iecas.cmd.network.processor;

import com.iecas.cmd.network.NetworkMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息处理结果
 */
public class ProcessorResult {
    
    /**
     * 处理状态
     */
    public enum Status {
        SUCCESS,    // 处理成功
        SKIPPED,    // 跳过处理
        FAILED,     // 处理失败
        DROPPED     // 丢弃消息
    }
    
    private final Status status;
    private final List<NetworkMessage> messages;
    private final String message;
    private final Throwable cause;
    
    private ProcessorResult(Status status, List<NetworkMessage> messages, String message, Throwable cause) {
        this.status = status;
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        this.message = message;
        this.cause = cause;
    }
    
    /**
     * 创建成功结果（单个消息）
     */
    public static ProcessorResult success(NetworkMessage message) {
        return new ProcessorResult(Status.SUCCESS, Collections.singletonList(message), null, null);
    }
    
    /**
     * 创建成功结果（多个消息）
     */
    public static ProcessorResult success(List<NetworkMessage> messages) {
        return new ProcessorResult(Status.SUCCESS, messages, null, null);
    }
    
    /**
     * 创建跳过结果
     */
    public static ProcessorResult skipped(String reason) {
        return new ProcessorResult(Status.SKIPPED, null, reason, null);
    }
    
    /**
     * 创建失败结果
     */
    public static ProcessorResult failed(String reason, Throwable cause) {
        return new ProcessorResult(Status.FAILED, null, reason, cause);
    }
    
    /**
     * 创建丢弃结果
     */
    public static ProcessorResult dropped(String reason) {
        return new ProcessorResult(Status.DROPPED, null, reason, null);
    }
    
    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    /**
     * 检查是否跳过
     */
    public boolean isSkipped() {
        return status == Status.SKIPPED;
    }
    
    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }
    
    /**
     * 检查是否丢弃
     */
    public boolean isDropped() {
        return status == Status.DROPPED;
    }
    
    /**
     * 获取处理后的消息列表
     */
    public List<NetworkMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    /**
     * 获取单个消息（如果只有一个）
     */
    public NetworkMessage getMessage() {
        return messages.isEmpty() ? null : messages.get(0);
    }
    
    /**
     * 获取消息数量
     */
    public int getMessageCount() {
        return messages.size();
    }
    
    /**
     * 检查是否有消息
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }
    
    // Getters
    public Status getStatus() {
        return status;
    }
    
    public String getResultMessage() {
        return message;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    @Override
    public String toString() {
        return String.format("ProcessorResult{status=%s, messageCount=%d, message='%s'}", 
                status, messages.size(), message);
    }
} 