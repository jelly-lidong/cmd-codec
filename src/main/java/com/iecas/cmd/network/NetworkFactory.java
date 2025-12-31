package com.iecas.cmd.network;

import com.iecas.cmd.network.processor.MessagePacker;
import com.iecas.cmd.network.processor.MessageProcessingPipeline;
import com.iecas.cmd.network.processor.MessageProcessor;
import com.iecas.cmd.network.processor.MessageSegmenter;
import com.iecas.cmd.network.processor.impl.DefaultMessagePacker;
import com.iecas.cmd.network.processor.impl.DefaultMessageSegmenter;
import com.iecas.cmd.network.processor.impl.ValidationProcessor;
import com.iecas.cmd.network.tcp.TcpClient;
import com.iecas.cmd.network.tcp.TcpServer;
import com.iecas.cmd.network.udp.UdpClient;
import com.iecas.cmd.network.udp.UdpServer;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络组件工厂类
 */
public class NetworkFactory {
    
    /**
     * 创建TCP客户端
     * 
     * @param config 网络配置
     * @param eventHandler 事件处理器
     * @return TCP客户端实例
     */
    public static TcpClient createTcpClient(NetworkConfig config, NetworkEventHandler eventHandler) {
        return new TcpClient(config, eventHandler);
    }
    
    /**
     * 创建TCP服务端
     * 
     * @param config 网络配置
     * @param eventHandler 事件处理器
     * @return TCP服务端实例
     */
    public static TcpServer createTcpServer(NetworkConfig config, NetworkEventHandler eventHandler) {
        return new TcpServer(config, eventHandler);
    }
    
    /**
     * 创建UDP客户端（支持单播和组播）
     * 
     * @param config 网络配置
     * @param eventHandler 事件处理器
     * @return UDP客户端实例
     */
    public static UdpClient createUdpClient(NetworkConfig config, NetworkEventHandler eventHandler) {
        return new UdpClient(config, eventHandler);
    }
    
    /**
     * 创建UDP服务端（支持单播和组播）
     * 
     * @param config 网络配置
     * @param eventHandler 事件处理器
     * @return UDP服务端实例
     */
    public static UdpServer createUdpServer(NetworkConfig config, NetworkEventHandler eventHandler) {
        return new UdpServer(config, eventHandler);
    }
    
    /**
     * 创建TCP客户端（简化版）
     * 
     * @param host 服务器地址
     * @param port 服务器端口
     * @param eventHandler 事件处理器
     * @return TCP客户端实例
     */
    public static TcpClient createTcpClient(String host, int port, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.tcp(host, port);
        return new TcpClient(config, eventHandler);
    }
    
    /**
     * 创建TCP服务端（简化版）
     * 
     * @param host 监听地址
     * @param port 监听端口
     * @param eventHandler 事件处理器
     * @return TCP服务端实例
     */
    public static TcpServer createTcpServer(String host, int port, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.tcp(host, port);
        return new TcpServer(config, eventHandler);
    }
    
    /**
     * 创建UDP单播客户端（简化版）
     * 
     * @param host 目标地址
     * @param port 目标端口
     * @param eventHandler 事件处理器
     * @return UDP客户端实例
     */
    public static UdpClient createUdpUnicastClient(String host, int port, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.udp(host, port);
        return new UdpClient(config, eventHandler);
    }
    
    /**
     * 创建UDP单播服务端（简化版）
     * 
     * @param host 监听地址
     * @param port 监听端口
     * @param eventHandler 事件处理器
     * @return UDP服务端实例
     */
    public static UdpServer createUdpUnicastServer(String host, int port, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.udp(host, port);
        return new UdpServer(config, eventHandler);
    }
    
    /**
     * 创建UDP组播客户端（简化版）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param eventHandler 事件处理器
     * @return UDP客户端实例
     */
    public static UdpClient createUdpMulticastClient(String multicastAddress, int multicastPort, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicast(multicastAddress, multicastPort);
        return new UdpClient(config, eventHandler);
    }
    
    /**
     * 创建UDP组播服务端（简化版）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param eventHandler 事件处理器
     * @return UDP服务端实例
     */
    public static UdpServer createUdpMulticastServer(String multicastAddress, int multicastPort, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicast(multicastAddress, multicastPort);
        return new UdpServer(config, eventHandler);
    }
    
    /**
     * 创建UDP组播客户端（指定源）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param sourceAddress 源地址
     * @param eventHandler 事件处理器
     * @return UDP客户端实例
     */
    public static UdpClient createUdpMulticastClientWithSource(String multicastAddress, int multicastPort, 
                                                               String sourceAddress, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicastWithSource(multicastAddress, multicastPort, sourceAddress);
        return new UdpClient(config, eventHandler);
    }
    
    /**
     * 创建UDP组播服务端（指定源）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param sourceAddress 源地址
     * @param eventHandler 事件处理器
     * @return UDP服务端实例
     */
    public static UdpServer createUdpMulticastServerWithSource(String multicastAddress, int multicastPort, 
                                                               String sourceAddress, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicastWithSource(multicastAddress, multicastPort, sourceAddress);
        return new UdpServer(config, eventHandler);
    }
    
    /**
     * 创建UDP组播客户端（指定网络接口）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param networkInterface 网络接口名称
     * @param eventHandler 事件处理器
     * @return UDP客户端实例
     */
    public static UdpClient createUdpMulticastClientWithInterface(String multicastAddress, int multicastPort, 
                                                                  String networkInterface, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicast(multicastAddress, multicastPort)
                .networkInterface(networkInterface);
        return new UdpClient(config, eventHandler);
    }
    
    /**
     * 创建UDP组播服务端（指定网络接口）
     * 
     * @param multicastAddress 组播地址
     * @param multicastPort 组播端口
     * @param networkInterface 网络接口名称
     * @param eventHandler 事件处理器
     * @return UDP服务端实例
     */
    public static UdpServer createUdpMulticastServerWithInterface(String multicastAddress, int multicastPort, 
                                                                  String networkInterface, NetworkEventHandler eventHandler) {
        NetworkConfig config = NetworkConfig.multicast(multicastAddress, multicastPort)
                .networkInterface(networkInterface);
        return new UdpServer(config, eventHandler);
    }
    
    // ==================== 消息处理流水线工厂方法 ====================
    
    /**
     * 创建默认的消息分段器
     * 
     * @return 默认消息分段器实例
     */
    public static MessageSegmenter createDefaultSegmenter() {
        return new DefaultMessageSegmenter();
    }
    
    /**
     * 创建默认的消息打包器
     * 
     * @return 默认消息打包器实例
     */
    public static MessagePacker createDefaultPacker() {
        return new DefaultMessagePacker();
    }
    
    /**
     * 创建数据验证处理器
     * 
     * @return 数据验证处理器实例
     */
    public static MessageProcessor createValidationProcessor() {
        return new ValidationProcessor();
    }
    
    /**
     * 创建数据验证处理器（自定义参数）
     * 
     * @param minDataLength 最小数据长度
     * @param maxDataLength 最大数据长度
     * @param validateChecksum 是否验证校验和
     * @return 数据验证处理器实例
     */
    public static MessageProcessor createValidationProcessor(int minDataLength, int maxDataLength, boolean validateChecksum) {
        return new ValidationProcessor(minDataLength, maxDataLength, validateChecksum);
    }
    
    /**
     * 创建基本的消息处理流水线
     * 
     * @return 基本消息处理流水线
     */
    public static MessageProcessingPipeline createBasicPipeline() {
        return new MessageProcessingPipeline.Builder()
                .setSegmenter(createDefaultSegmenter())
                .setPacker(createDefaultPacker())
                .build();
    }
    
    /**
     * 创建带验证的消息处理流水线
     * 
     * @param eventHandler 事件处理器
     * @return 带验证的消息处理流水线
     */
    public static MessageProcessingPipeline createValidationPipeline(NetworkEventHandler eventHandler) {
        return new MessageProcessingPipeline.Builder()
                .setSegmenter(createDefaultSegmenter())
                .setPacker(createDefaultPacker())
                .addProcessor(createValidationProcessor())
                .setEventHandler(eventHandler)
                .build();
    }
    
    /**
     * 创建自定义消息处理流水线
     * 
     * @param segmenter 分段器
     * @param packer 打包器
     * @param eventHandler 事件处理器
     * @return 自定义消息处理流水线
     */
    public static MessageProcessingPipeline createCustomPipeline(MessageSegmenter segmenter, 
                                                                MessagePacker packer,
                                                                NetworkEventHandler eventHandler) {
        return new MessageProcessingPipeline.Builder()
                .setSegmenter(segmenter)
                .setPacker(packer)
                .setEventHandler(eventHandler)
                .build();
    }
    
    /**
     * 创建固定长度分段规则
     * 
     * @param segmentLength 分段长度
     * @return 分段规则
     */
    public static MessageSegmenter.SegmentRule createFixedLengthRule(int segmentLength) {
        return new MessageSegmenter.SegmentRule(
                "固定长度分段",
                "按固定长度对数据进行分段",
                MessageSegmenter.SegmentStrategy.FIXED_LENGTH,
                segmentLength
        );
    }
    
    /**
     * 创建分隔符分段规则
     * 
     * @param delimiter 分隔符
     * @return 分段规则
     */
    public static MessageSegmenter.SegmentRule createDelimiterRule(String delimiter) {
        return new MessageSegmenter.SegmentRule(
                "分隔符分段",
                "按指定分隔符对数据进行分段",
                MessageSegmenter.SegmentStrategy.DELIMITER,
                delimiter.getBytes()
        );
    }
    
    /**
     * 创建长度前缀分段规则
     * 
     * @param prefixLength 前缀长度（字节数）
     * @return 分段规则
     */
    public static MessageSegmenter.SegmentRule createLengthPrefixRule(int prefixLength) {
        return new MessageSegmenter.SegmentRule(
                "长度前缀分段",
                "按长度前缀对数据进行分段",
                MessageSegmenter.SegmentStrategy.LENGTH_PREFIX,
                prefixLength
        );
    }
    
    /**
     * 创建简单拼接打包规则
     * 
     * @param singleMessage 是否合并为单个消息
     * @return 打包规则
     */
    public static MessagePacker.PackRule createConcatenateRule(boolean singleMessage) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("singleMessage", singleMessage);
        
        return new MessagePacker.PackRule(
                "简单拼接",
                "将分段数据简单拼接",
                MessagePacker.PackStrategy.CONCATENATE,
                parameters
        );
    }
    
    /**
     * 创建长度前缀打包规则
     * 
     * @param prefixLength 前缀长度
     * @param includeCount 是否包含分段数量
     * @return 打包规则
     */
    public static MessagePacker.PackRule createLengthPrefixPackRule(int prefixLength, boolean includeCount) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("prefixLength", prefixLength);
        parameters.put("includeCount", includeCount);
        
        return new MessagePacker.PackRule(
                "长度前缀打包",
                "为每个分段添加长度前缀",
                MessagePacker.PackStrategy.LENGTH_PREFIX,
                parameters
        );
    }
    
    /**
     * 创建压缩打包规则
     * 
     * @return 压缩打包规则
     */
    public static MessagePacker.PackRule createCompressedRule() {
        return new MessagePacker.PackRule(
                "压缩打包",
                "使用GZIP压缩算法对数据进行压缩打包",
                MessagePacker.PackStrategy.COMPRESSED,
                new HashMap<>()
        );
    }
} 