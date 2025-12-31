package com.iecas.cmd.network.processor;

import com.iecas.cmd.network.NetworkMessage;

/**
 * 消息处理器接口
 */
public interface MessageProcessor {
    
    /**
     * 处理消息
     * 
     * @param input 输入消息
     * @return 处理后的消息，如果返回null则表示丢弃该消息
     * @throws ProcessorException 处理异常
     */
    ProcessorResult process(NetworkMessage input) throws ProcessorException;
    
    /**
     * 获取处理器名称
     */
    String getName();
    
    /**
     * 获取处理器描述
     */
    default String getDescription() {
        return getName();
    }
    
    /**
     * 检查是否支持处理该消息
     */
    default boolean supports(NetworkMessage message) {
        return true;
    }
} 