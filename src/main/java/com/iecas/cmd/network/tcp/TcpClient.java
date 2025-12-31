package com.iecas.cmd.network.tcp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
 * TCP客户端实现
 */
public class TcpClient {
    
    private final NetworkConfig config;
    private final NetworkEventHandler eventHandler;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, CompletableFuture<NetworkMessage>> pendingResponses = new ConcurrentHashMap<>();
    
    public TcpClient(NetworkConfig config, NetworkEventHandler eventHandler) {
        this.config = config;
        this.eventHandler = eventHandler;
        initBootstrap();
    }
    
    /**
     * 初始化Bootstrap
     */
    private void initBootstrap() {
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .option(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMs())
                .handler(new ChannelInitializer<SocketChannel>() {
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
                        pipeline.addLast(new TcpClientHandler());
                    }
                });
    }
    
    /**
     * 连接到服务器
     */
    public CompletableFuture<Void> connect() {
        if (connected.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (!connecting.compareAndSet(false, true)) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("正在连接中"));
            return failedFuture;
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            ChannelFuture connectFuture = bootstrap.connect(config.getHost(), config.getPort());
            connectFuture.addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    connecting.set(false);
                    if (channelFuture.isSuccess()) {
                        channel = channelFuture.channel();
                        connected.set(true);
                        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
                        eventHandler.onClientConnected(localAddress, remoteAddress);
                        future.complete(null);
                    } else {
                        InetSocketAddress remoteAddress = new InetSocketAddress(config.getHost(), config.getPort());
                        eventHandler.onClientConnectFailed(remoteAddress, channelFuture.cause());
                        future.completeExceptionally(channelFuture.cause());
                    }
                }
            });
        } catch (Exception e) {
            connecting.set(false);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 断开连接
     */
    public CompletableFuture<Void> disconnect() {
        if (!connected.get()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (channel != null && channel.isActive()) {
            channel.close().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    connected.set(false);
                    future.complete(null);
                }
            });
        } else {
            connected.set(false);
            future.complete(null);
        }
        
        return future;
    }
    
    /**
     * 发送消息
     */
    public CompletableFuture<Void> sendMessage(byte[] data) {
        if (!connected.get() || channel == null || !channel.isActive()) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("未连接到服务器"));
            return failedFuture;
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        NetworkMessage message = new NetworkMessage(data);
        message.setMessageType(NetworkMessage.MessageType.DATA);
        
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        channel.writeAndFlush(buffer).addListener(new GenericFutureListener<ChannelFuture>() {
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
     * 发送消息并等待响应
     */
    public CompletableFuture<NetworkMessage> sendMessageAndWaitResponse(byte[] data, long timeoutMs) {
        if (!connected.get() || channel == null || !channel.isActive()) {
            CompletableFuture<NetworkMessage> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("未连接到服务器"));
            return failedFuture;
        }
        
        NetworkMessage message = new NetworkMessage(data);
        message.setMessageType(NetworkMessage.MessageType.REQUEST);
        message.setNeedResponse(true);
        message.setResponseTimeoutMs(timeoutMs);
        
        CompletableFuture<NetworkMessage> responseFuture = new CompletableFuture<>();
        pendingResponses.put(message.getMessageId(), responseFuture);
        
        // 设置超时
        workerGroup.schedule(() -> {
            CompletableFuture<NetworkMessage> future = pendingResponses.remove(message.getMessageId());
            if (future != null && !future.isDone()) {
                future.completeExceptionally(new RuntimeException("响应超时"));
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        channel.writeAndFlush(buffer).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    eventHandler.onMessageSent(message);
                } else {
                    CompletableFuture<NetworkMessage> future = pendingResponses.remove(message.getMessageId());
                    if (future != null) {
                        eventHandler.onMessageSendFailed(message, channelFuture.cause());
                        future.completeExceptionally(channelFuture.cause());
                    }
                }
            }
        });
        
        return responseFuture;
    }
    
    /**
     * 发送心跳
     */
    public CompletableFuture<Void> sendHeartbeat() {
        byte[] heartbeatData = "HEARTBEAT".getBytes();
        return sendMessage(heartbeatData);
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return connected.get() && channel != null && channel.isActive();
    }
    
    /**
     * 关闭客户端
     */
    public void shutdown() {
        disconnect().join();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * TCP客户端处理器
     */
    private class TcpClientHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            eventHandler.onConnected(remoteAddress);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            connected.set(false);
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
        //                 // 读超时，可能需要发送心跳
        //                 sendHeartbeat();
        //                 break;
        //             case WRITER_IDLE:
        //                 // 写超时，发送心跳
        //                 sendHeartbeat();
        //                 break;
        //             case ALL_IDLE:
        //                 // 读写都超时
        //                 InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        //                 eventHandler.onHeartbeatTimeout(remoteAddress);
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