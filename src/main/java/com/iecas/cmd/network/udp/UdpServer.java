package com.iecas.cmd.network.udp;

import com.iecas.cmd.network.NetworkConfig;
import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;
import com.iecas.cmd.util.AddressUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP服务端实现（支持单播和组播）
 */
public class UdpServer {

    private final NetworkConfig config;
    private final NetworkEventHandler eventHandler;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final boolean isMulticast;
    private final ConcurrentHashMap<String, InetSocketAddress> clientAddresses = new ConcurrentHashMap<>();

    public UdpServer(NetworkConfig config, NetworkEventHandler eventHandler) {
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
                        pipeline.addLast(new UdpServerHandler());
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
            ChannelFuture bindFuture;
            if (isMulticast) {
                // 组播模式：绑定到组播端口
                bindFuture = bootstrap.bind(config.getMulticastPort());
            } else {
                // 单播模式：绑定到指定端口
                bindFuture = bootstrap.bind(config.getHost(), config.getPort());
            }

            bindFuture.addListener((GenericFutureListener<ChannelFuture>) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    started.set(true);

                    if (isMulticast) {
                        joinMulticastGroup().thenRun(() -> {
                            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                            eventHandler.onServerStarted(localAddress);
                            future.complete(null);
                        }).exceptionally(throwable -> {
                            future.completeExceptionally(throwable);
                            return null;
                        });
                    } else {
                        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                        eventHandler.onServerStarted(localAddress);
                        future.complete(null);
                    }
                } else {
                    future.completeExceptionally(channelFuture.cause());
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
            InetAddress multicastAddress;
            if (!StringUtils.isEmpty(config.getNetworkInterface())){
                multicastAddress = InetAddress.getByName(config.getMulticastAddress());
            }else{
                multicastAddress = InetAddress.getByName(AddressUtil.getNetCardLocalHost());
            }

            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(multicastAddress);


            if (networkInterface != null) {
                // 指定网络接口加入组播组
                ((DatagramChannel) channel)
                        .joinGroup(new InetSocketAddress(multicastAddress, config.getMulticastPort()), networkInterface)
                        .addListener((GenericFutureListener<ChannelFuture>) channelFuture -> {
                            InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                            InetSocketAddress sourceAddr = config.getSourceAddress() != null ?
                                    new InetSocketAddress(config.getSourceAddress(), 0) : null;
                            if (channelFuture.isSuccess()) {
                                eventHandler.onMulticastJoined(multicastAddr, sourceAddr);
                                future.complete(null);
                            } else {
                                future.completeExceptionally(channelFuture.cause());
                                eventHandler.onException(multicastAddr,channelFuture.cause());
                            }
                        });
            } else {
                // 使用默认网络接口
                ((DatagramChannel) channel).joinGroup(multicastAddress).addListener((GenericFutureListener<ChannelFuture>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                        eventHandler.onMulticastJoined(multicastAddr, null);
                        future.complete(null);
                    } else {
                        future.completeExceptionally(channelFuture.cause());
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
                ((DatagramChannel) channel).leaveGroup(
                        new InetSocketAddress(multicastAddress, config.getMulticastPort()),
                        networkInterface
                ).addListener(new GenericFutureListener<ChannelFuture>() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        InetSocketAddress multicastAddr = new InetSocketAddress(multicastAddress, config.getMulticastPort());
                        InetSocketAddress sourceAddr = config.getSourceAddress() != null ?
                                new InetSocketAddress(config.getSourceAddress(), 0) : null;
                        eventHandler.onMulticastLeft(multicastAddr, sourceAddr);
                        future.complete(null);
                    }
                });
            } else {
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
     * 停止服务器
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
                    InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                    eventHandler.onServerStopped(localAddress);
                    future.complete(null);
                }
            });
        } else {
            started.set(false);
            future.complete(null);
        }
    }

    /**
     * 向指定客户端发送消息
     */
    public CompletableFuture<Void> sendMessage(String clientId, byte[] data) {
        InetSocketAddress clientAddress = clientAddresses.get(clientId);
        if (clientAddress == null) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("客户端地址未知: " + clientId));
            return failedFuture;
        }

        return sendMessage(data, clientAddress);
    }

    /**
     * 向指定地址发送消息
     */
    public CompletableFuture<Void> sendMessage(byte[] data, InetSocketAddress targetAddress) {
        if (!started.get() || channel == null || !channel.isActive()) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("服务器未启动"));
            return failedFuture;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
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
     * 向所有已知客户端广播消息
     */
    public CompletableFuture<Void> broadcastMessage(byte[] data) {
        if (clientAddresses.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void>[] futures = new CompletableFuture[clientAddresses.size()];
        int index = 0;

        for (InetSocketAddress clientAddress : clientAddresses.values()) {
            futures[index++] = sendMessage(data, clientAddress);
        }

        return CompletableFuture.allOf(futures);
    }

    /**
     * 向组播地址发送消息
     */
    public CompletableFuture<Void> sendMulticastMessage(byte[] data) {
        if (!isMulticast) {
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("非组播模式"));
            return failedFuture;
        }

        InetSocketAddress multicastAddress = new InetSocketAddress(config.getMulticastAddress(), config.getMulticastPort());
        return sendMessage(data, multicastAddress);
    }

    /**
     * 获取连接的客户端数量
     */
    public int getClientCount() {
        return clientAddresses.size();
    }

    /**
     * 获取所有客户端ID
     */
    public String[] getClientIds() {
        return clientAddresses.keySet().toArray(new String[0]);
    }

    /**
     * 检查服务器是否已启动
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
     * 关闭服务器
     */
    public void shutdown() {
        stop().join();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 生成客户端ID
     */
    private String generateClientId(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * UDP服务端处理器
     */
    private class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            byte[] data = new byte[packet.content().readableBytes()];
            packet.content().readBytes(data);

            InetSocketAddress sender = packet.sender();
            InetSocketAddress receiver = packet.recipient();

            // 记录客户端地址
            String clientId = generateClientId(sender);
            clientAddresses.put(clientId, sender);

            NetworkMessage message = new NetworkMessage(data, sender, receiver);
            message.setMessageType(NetworkMessage.MessageType.DATA);

            eventHandler.onMessageReceived(message);

            // 如果是心跳消息，自动回复
            if ("HEARTBEAT".equals(message.getDataAsString())) {
                byte[] response = "HEARTBEAT_ACK".getBytes();
                sendMessage(response, sender);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            eventHandler.onException(null, cause);
        }
    }
} 