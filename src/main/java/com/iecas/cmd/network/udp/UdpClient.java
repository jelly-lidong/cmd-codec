package com.iecas.cmd.network.udp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP客户端实现（支持单播和组播）
 */
public class UdpClient {
    
    private final NetworkConfig config;
    private final NetworkEventHandler eventHandler;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final boolean isMulticast;
    
    public UdpClient(NetworkConfig config, NetworkEventHandler eventHandler) {
        this.config = config;
        this.eventHandler = eventHandler;
        this.isMulticast = isMulticastAddress(config.getMulticastAddress());
        initBootstrap();
    }
    
    /**
     * 检查是否为组播地址
     */
    private boolean isMulticastAddress(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return inetAddress.isMulticastAddress();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 初始化Bootstrap
     */
    private void initBootstrap() {
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new UdpClientHandler());
                    }
                });
    }
    
    /**
     * 启动客户端
     */
    public CompletableFuture<Void> start() {
        if (started.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            ChannelFuture bindFuture;
            if (isMulticast) {
                // 组播模式：绑定到组播端口
                bindFuture = bootstrap.bind(config.getMulticastPort());
            } else {
                // 单播模式：绑定到任意可用端口
                bindFuture = bootstrap.bind(0);
            }
            
            bindFuture.addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        channel = channelFuture.channel();
                        started.set(true);
                        
                        if (isMulticast) {
                            joinMulticastGroup().thenRun(() -> {
                                InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                                eventHandler.onClientConnected(localAddress, null);
                                future.complete(null);
                            }).exceptionally(throwable -> {
                                future.completeExceptionally(throwable);
                                return null;
                            });
                        } else {
                            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                            InetSocketAddress remoteAddress = new InetSocketAddress(config.getHost(), config.getPort());
                            eventHandler.onClientConnected(localAddress, remoteAddress);
                            future.complete(null);
                        }
                    } else {
                        future.completeExceptionally(channelFuture.cause());
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 加入组播组
     */
    private CompletableFuture<Void> joinMulticastGroup() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            InetAddress multicastAddress = InetAddress.getByName(config.getMulticastAddress());
            NetworkInterface networkInterface = null;
            
            if (config.getNetworkInterface() != null) {
                networkInterface = NetworkInterface.getByName(config.getNetworkInterface());
            }
            
            if (networkInterface != null) {
                // 指定网络接口加入组播组
                if (config.getSourceAddress() != null) {
                    // 指定源的组播（SSM - Source-Specific Multicast）
                    InetAddress sourceAddress = InetAddress.getByName(config.getSourceAddress());
                    ((DatagramChannel) channel).joinGroup(
                            new InetSocketAddress(multicastAddress, config.getMulticastPort()),
                            networkInterface
                    ).addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                                InetSocketAddress sourceAddr = new InetSocketAddress(sourceAddress, 0);
                                eventHandler.onMulticastJoined(multicastAddr, sourceAddr);
                                future.complete(null);
                            } else {
                                future.completeExceptionally(channelFuture.cause());
                            }
                        }
                    });
                } else {
                    // 普通组播
                    ((DatagramChannel) channel).joinGroup(
                            new InetSocketAddress(multicastAddress, config.getMulticastPort()),
                            networkInterface
                    ).addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                                eventHandler.onMulticastJoined(multicastAddr, null);
                                future.complete(null);
                            } else {
                                future.completeExceptionally(channelFuture.cause());
                            }
                        }
                    });
                }
            } else {
                // 使用默认网络接口
                ((DatagramChannel) channel).joinGroup(multicastAddress).addListener(new GenericFutureListener<ChannelFuture>() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                            eventHandler.onMulticastJoined(multicastAddr, null);
                            future.complete(null);
                        } else {
                            future.completeExceptionally(channelFuture.cause());
                        }
                    }
                });
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 离开组播组
     */
    private CompletableFuture<Void> leaveMulticastGroup() {
        if (!isMulticast || channel == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            InetAddress multicastAddress = InetAddress.getByName(config.getMulticastAddress());
            NetworkInterface networkInterface = null;
            
            if (config.getNetworkInterface() != null) {
                networkInterface = NetworkInterface.getByName(config.getNetworkInterface());
            }
            
            if (networkInterface != null) {
                if (config.getSourceAddress() != null) {
                    // 指定源的组播
                    InetAddress sourceAddress = InetAddress.getByName(config.getSourceAddress());
                    ((DatagramChannel) channel).leaveGroup(
                            new InetSocketAddress(multicastAddress, config.getMulticastPort()),
                            networkInterface
                    ).addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                            InetSocketAddress sourceAddr = new InetSocketAddress(sourceAddress, 0);
                            eventHandler.onMulticastLeft(multicastAddr, sourceAddr);
                            future.complete(null);
                        }
                    });
                } else {
                    // 普通组播
                    ((DatagramChannel) channel).leaveGroup(
                            new InetSocketAddress(multicastAddress, config.getMulticastPort()),
                            networkInterface
                    ).addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                            eventHandler.onMulticastLeft(multicastAddr, null);
                            future.complete(null);
                        }
                    });
                }
            } else {
                // 使用默认网络接口
                ((DatagramChannel) channel).leaveGroup(multicastAddress).addListener(new GenericFutureListener<ChannelFuture>() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                        eventHandler.onMulticastLeft(multicastAddr, null);
                        future.complete(null);
                    }
                });
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 停止客户端
     */
    public CompletableFuture<Void> stop() {
        if (!started.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (isMulticast) {
            leaveMulticastGroup().thenRun(() -> {
                closeChannel(future);
            }).exceptionally(throwable -> {
                closeChannel(future);
                return null;
            });
        } else {
            closeChannel(future);
        }
        
        return future;
    }
    
    /**
     * 关闭通道
     */
    private void closeChannel(CompletableFuture<Void> future) {
        if (channel != null && channel.isActive()) {
            channel.close().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    started.set(false);
                    future.complete(null);
                }
            });
        } else {
            started.set(false);
            future.complete(null);
        }
    }
    
    /**
     * 发送消息（单播）
     */
    public CompletableFuture<Void> sendMessage(byte[] data, String targetHost, int targetPort) {
        if (!started.get() || channel == null || !channel.isActive()) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("客户端未启动"));
            return failedFuture;
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
            DatagramPacket packet = new DatagramPacket(Unpooled.wrappedBuffer(data), targetAddress);
            
            NetworkMessage message = new NetworkMessage(data);
            message.setMessageType(NetworkMessage.MessageType.DATA);
            message.setReceiver(targetAddress);
            
            channel.writeAndFlush(packet).addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        eventHandler.onMessageSent(message);
                        future.complete(null);
                    } else {
                        eventHandler.onMessageSendFailed(message, channelFuture.cause());
                        future.completeExceptionally(channelFuture.cause());
                    }
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 发送消息（使用配置的默认目标）
     */
    public CompletableFuture<Void> sendMessage(byte[] data) {
        if (isMulticast) {
            return sendMessage(data, config.getMulticastAddress(), config.getMulticastPort());
        } else {
            return sendMessage(data, config.getHost(), config.getPort());
        }
    }
    
    /**
     * 检查是否已启动
     */
    public boolean isStarted() {
        return started.get() && channel != null && channel.isActive();
    }
    
    /**
     * 检查是否为组播模式
     */
    public boolean isMulticastMode() {
        return isMulticast;
    }
    
    /**
     * 关闭客户端
     */
    public void shutdown() {
        stop().join();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * UDP客户端处理器
     */
    private class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            byte[] data = new byte[packet.content().readableBytes()];
            packet.content().readBytes(data);
            
            InetSocketAddress sender = packet.sender();
            InetSocketAddress receiver = packet.recipient();
            
            NetworkMessage message = new NetworkMessage(data, sender, receiver);
            message.setMessageType(NetworkMessage.MessageType.DATA);
            
            eventHandler.onMessageReceived(message);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            eventHandler.onException(null, cause);
        }
    }
} 