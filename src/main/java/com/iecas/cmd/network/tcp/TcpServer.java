package com.iecas.cmd.network.tcp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP服务端实现
 */
public class TcpServer {
    
    private final NetworkConfig config;
    private final NetworkEventHandler eventHandler;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, Channel> clientChannels = new ConcurrentHashMap<>();
    
    public TcpServer(NetworkConfig config, NetworkEventHandler eventHandler) {
        this.config = config;
        this.eventHandler = eventHandler;
        initBootstrap();
    }
    
    /**
     * 初始化ServerBootstrap
     */
    private void initBootstrap() {
        bossGroup = new NioEventLoopGroup(config.getBossThreads());
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
                .childOption(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                .childOption(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 移除心跳检测机制
                        // pipeline.addLast(new IdleStateHandler(
                        //         config.getReadTimeoutMs() / 1000, 
                        //         config.getWriteTimeoutMs() / 1000, 
                        //         0, 
                        //         TimeUnit.SECONDS));
                        
                        // 添加长度字段编解码器
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(new LengthFieldPrepender(4));
                        
                        // 添加业务处理器
                        pipeline.addLast(new TcpServerHandler());
                    }
                });
    }
    
    /**
     * 启动服务器
     */
    public CompletableFuture<Void> start() {
        if (started.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            ChannelFuture bindFuture = bootstrap.bind(config.getHost(), config.getPort());
            bindFuture.addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        serverChannel = channelFuture.channel();
                        started.set(true);
                        InetSocketAddress localAddress = (InetSocketAddress) serverChannel.localAddress();
                        eventHandler.onServerStarted(localAddress);
                        future.complete(null);
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
     * 停止服务器
     */
    public CompletableFuture<Void> stop() {
        if (!started.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (serverChannel != null && serverChannel.isActive()) {
            serverChannel.close().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    started.set(false);
                    InetSocketAddress localAddress = (InetSocketAddress) serverChannel.localAddress();
                    eventHandler.onServerStopped(localAddress);
                    future.complete(null);
                }
            });
        } else {
            started.set(false);
            future.complete(null);
        }
        
        return future;
    }
    
    /**
     * 向指定客户端发送消息
     */
    public CompletableFuture<Void> sendMessage(String clientId, byte[] data) {
        Channel clientChannel = clientChannels.get(clientId);
        if (clientChannel == null || !clientChannel.isActive()) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("客户端未连接: " + clientId));
            return failedFuture;
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        NetworkMessage message = new NetworkMessage(data);
        message.setMessageType(NetworkMessage.MessageType.DATA);
        
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        clientChannel.writeAndFlush(buffer).addListener(new GenericFutureListener<ChannelFuture>() {
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
        
        return future;
    }
    
    /**
     * 向所有客户端广播消息
     */
    public CompletableFuture<Void> broadcastMessage(byte[] data) {
        if (clientChannels.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void>[] futures = new CompletableFuture[clientChannels.size()];
        int index = 0;
        
        for (String clientId : clientChannels.keySet()) {
            futures[index++] = sendMessage(clientId, data);
        }
        
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * 获取连接的客户端数量
     */
    public int getClientCount() {
        return clientChannels.size();
    }
    
    /**
     * 获取所有客户端ID
     */
    public String[] getClientIds() {
        return clientChannels.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查服务器是否已启动
     */
    public boolean isStarted() {
        return started.get() && serverChannel != null && serverChannel.isActive();
    }
    
    /**
     * 关闭服务器
     */
    public void shutdown() {
        stop().join();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * 生成客户端ID
     */
    private String generateClientId(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        return remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
    }
    
    /**
     * TCP服务端处理器
     */
    private class TcpServerHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String clientId = generateClientId(ctx.channel());
            clientChannels.put(clientId, ctx.channel());
            
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            eventHandler.onConnected(remoteAddress);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String clientId = generateClientId(ctx.channel());
            clientChannels.remove(clientId);
            
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            eventHandler.onDisconnected(remoteAddress, null);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf) {
                ByteBuf buffer = (ByteBuf) msg;
                try {
                    byte[] data = new byte[buffer.readableBytes()];
                    buffer.readBytes(data);
                    
                    InetSocketAddress sender = (InetSocketAddress) ctx.channel().remoteAddress();
                    InetSocketAddress receiver = (InetSocketAddress) ctx.channel().localAddress();
                    
                    NetworkMessage message = new NetworkMessage(data, sender, receiver);
                    message.setMessageType(NetworkMessage.MessageType.DATA);
                    
                    eventHandler.onMessageReceived(message);
                    
                    // 移除心跳消息自动回复
                    // if ("HEARTBEAT".equals(message.getDataAsString())) {
                    //     byte[] response = "HEARTBEAT_ACK".getBytes();
                    //     ctx.writeAndFlush(Unpooled.wrappedBuffer(response));
                    // }
                } finally {
                    buffer.release();
                }
            }
        }
        
        // 移除心跳事件处理
        // @Override
        // public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //     if (evt instanceof IdleStateEvent) {
        //         IdleStateEvent event = (IdleStateEvent) evt;
        //         switch (event.state()) {
        //             case READER_IDLE:
        //                 // 读超时，可能客户端已断开
        //                 InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        //                 eventHandler.onHeartbeatTimeout(remoteAddress);
        //                 ctx.close();
        //                 break;
        //             case WRITER_IDLE:
        //                 // 写超时，发送心跳
        //                 byte[] heartbeat = "SERVER_HEARTBEAT".getBytes();
        //                 ctx.writeAndFlush(Unpooled.wrappedBuffer(heartbeat));
        //                 break;
        //             case ALL_IDLE:
        //                 // 读写都超时
        //                 InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        //                 eventHandler.onHeartbeatTimeout(address);
        //                 ctx.close();
        //                 break;
        //         }
        //     }
        // }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            eventHandler.onException(remoteAddress, cause);
            ctx.close();
        }
    }
} 