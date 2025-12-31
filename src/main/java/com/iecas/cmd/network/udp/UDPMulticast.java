package com.iecas.cmd.network.udp;


import com.iecas.cmd.util.AddressUtil;
import com.iecas.cmd.util.ByteUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.Enumeration;
import java.util.Locale;

@Slf4j
public class UDPMulticast {
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private NioDatagramChannel ch;
    private final int port;
    private NetworkInterface ni;
    private final String ip;
    private InetSocketAddress groupAddress;
    private final SimpleChannelInboundHandler<DatagramPacket> handler;
    private final String source;


    public static void main(String[] args) {
        UDPMulticast server = new UDPMulticast("229.55.10.1", 8000, null, new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
                ByteBuf buf = datagramPacket.content();
                final int readableSize = buf.readableBytes();
                final byte[] buff = new byte[readableSize];
                buf.readBytes(buff);
                log.debug("收到响应：{}", ByteUtil.bytesToHexString(buff));
            }
        });

        server.init();
        server.connect();
    }

    public UDPMulticast(String ip,
                        int port,
                        String source,
                        SimpleChannelInboundHandler<DatagramPacket> handler) {
        this.port = port;
        this.ip = ip;
        this.handler = handler;
        this.source = source;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public void init() {
        groupAddress = new InetSocketAddress(ip, port);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ni = NetworkInterface.getByInetAddress(InetAddress.getByName(AddressUtil.getNetCardLocalHost()));
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            InetAddress localAddress = null;
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address) {
                    localAddress = address;
                    break;
                }
            }
            bootstrap = new Bootstrap();
            //设置NioDatagramChannel
            bootstrap.group(group)
                    .channelFactory((ChannelFactory<NioDatagramChannel>) () -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
                    .localAddress(localAddress, groupAddress.getPort())
                    //设置Option 组播
                    .option(ChannelOption.IP_MULTICAST_IF, ni)
                    //设置Option 地址
                    .option(ChannelOption.IP_MULTICAST_ADDR, localAddress)
                    .option(ChannelOption.IP_MULTICAST_TTL, 50)
                    //设置地址
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(handler);

                        }
                    });
            log.debug("init successfully");
        } catch (Exception exc) {
            log.error("Unexpected exc: ", exc);
        }
    }

    public boolean connect() {
        try {
            ch = (NioDatagramChannel) bootstrap.bind(port).sync().channel();
            if (isMulticastAddress(ip)) {
                //加入组
                if (source == null) {
                    ch.joinGroup(groupAddress, ni).sync();
                } else {
                    log.debug("join group,ip: {}; port:{}; source: {}", ip, port, source);
                    ch.joinGroup(InetAddress.getByName(ip), ni, InetAddress.getByName(source));
                }
                log.debug("connect successfully -> {}:{},source:{}", ip, port, source);
            } else {
                // 单播模式：绑定到任意可用端口
                ch = (NioDatagramChannel)bootstrap.bind(port).sync().channel();
            }
        } catch (InterruptedException | IllegalArgumentException | UnknownHostException e) {
            log.error("Unexpected error: ", e);
        }
        return ch != null;
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

    public boolean terminate() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
        }
        if (bootstrap != null) {
            bootstrap = null;
        }
        return true;
    }

    public void send(byte[] bytes) {
        if (ch == null) {
            connect();
        }
        try {
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(bytes),
                    new InetSocketAddress(ip, port)
            )).sync();
            log.debug("发送指令 {}:{}  {}", ip, port, ByteUtil.bytesToHexString(bytes).toUpperCase(Locale.ROOT));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }


}
