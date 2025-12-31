package com.iecas.cmd.network;

/**
 * 网络配置类
 */
public class NetworkConfig {
    
    /**
     * 服务器地址
     */
    private String host = "localhost";
    
    /**
     * 服务器端口
     */
    private int port = 8080;
    
    /**
     * 组播地址（用于UDP组播）
     */
    private String multicastAddress = "224.0.0.1";
    
    /**
     * 组播端口
     */
    private int multicastPort = 9999;
    
    /**
     * 源地址（用于指定源的组播）
     */
    private String sourceAddress;
    
    /**
     * 网络接口（用于组播）
     */
    private String networkInterface;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeoutMs = 5000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeoutMs = 30000;
    
    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeoutMs = 30000;
    
    /**
     * 是否启用TCP_NODELAY
     */
    private boolean tcpNoDelay = true;
    
    /**
     * 是否启用SO_KEEPALIVE
     */
    private boolean keepAlive = true;
    
    /**
     * 是否启用SO_REUSEADDR
     */
    private boolean reuseAddress = true;
    
    /**
     * 接收缓冲区大小
     */
    private int receiveBufferSize = 65536;
    
    /**
     * 发送缓冲区大小
     */
    private int sendBufferSize = 65536;
    
    /**
     * 工作线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * Boss线程数（仅用于服务端）
     */
    private int bossThreads = 1;
    
    /**
     * 是否启用SSL/TLS
     */
    private boolean sslEnabled = false;
    
    /**
     * SSL证书路径
     */
    private String sslCertPath;
    
    /**
     * SSL私钥路径
     */
    private String sslKeyPath;
    
    // 构造函数
    public NetworkConfig() {
    }
    
    public NetworkConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    // 静态工厂方法
    public static NetworkConfig tcp(String host, int port) {
        return new NetworkConfig(host, port);
    }
    
    public static NetworkConfig udp(String host, int port) {
        return new NetworkConfig(host, port);
    }
    
    public static NetworkConfig multicast(String multicastAddress, int multicastPort) {
        NetworkConfig config = new NetworkConfig();
        config.multicastAddress = multicastAddress;
        config.multicastPort = multicastPort;
        return config;
    }
    
    public static NetworkConfig multicastWithSource(String multicastAddress, int multicastPort, String sourceAddress) {
        NetworkConfig config = multicast(multicastAddress, multicastPort);
        config.sourceAddress = sourceAddress;
        return config;
    }
    
    // Fluent API
    public NetworkConfig host(String host) {
        this.host = host;
        return this;
    }
    
    public NetworkConfig port(int port) {
        this.port = port;
        return this;
    }
    
    public NetworkConfig multicastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
        return this;
    }
    
    public NetworkConfig multicastPort(int multicastPort) {
        this.multicastPort = multicastPort;
        return this;
    }
    
    public NetworkConfig sourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }
    
    public NetworkConfig networkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
        return this;
    }
    
    public NetworkConfig connectTimeout(int timeoutMs) {
        this.connectTimeoutMs = timeoutMs;
        return this;
    }
    
    public NetworkConfig readTimeout(int timeoutMs) {
        this.readTimeoutMs = timeoutMs;
        return this;
    }
    
    public NetworkConfig writeTimeout(int timeoutMs) {
        this.writeTimeoutMs = timeoutMs;
        return this;
    }
    
    public NetworkConfig tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }
    
    public NetworkConfig keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }
    
    public NetworkConfig reuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
        return this;
    }
    
    public NetworkConfig bufferSize(int receiveSize, int sendSize) {
        this.receiveBufferSize = receiveSize;
        this.sendBufferSize = sendSize;
        return this;
    }
    
    public NetworkConfig workerThreads(int threads) {
        this.workerThreads = threads;
        return this;
    }
    
    public NetworkConfig bossThreads(int threads) {
        this.bossThreads = threads;
        return this;
    }
    
    public NetworkConfig ssl(boolean enabled) {
        this.sslEnabled = enabled;
        return this;
    }
    
    public NetworkConfig ssl(String certPath, String keyPath) {
        this.sslEnabled = true;
        this.sslCertPath = certPath;
        this.sslKeyPath = keyPath;
        return this;
    }
    
    // Getters and Setters
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getMulticastAddress() {
        return multicastAddress;
    }
    
    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }
    
    public int getMulticastPort() {
        return multicastPort;
    }
    
    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }
    
    public String getSourceAddress() {
        return sourceAddress;
    }
    
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    
    public String getNetworkInterface() {
        return networkInterface;
    }
    
    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }
    
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }
    
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
    
    public int getWriteTimeoutMs() {
        return writeTimeoutMs;
    }
    
    public void setWriteTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }
    
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }
    
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }
    
    public boolean isKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public boolean isReuseAddress() {
        return reuseAddress;
    }
    
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }
    
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }
    
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }
    
    public int getSendBufferSize() {
        return sendBufferSize;
    }
    
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }
    
    public int getWorkerThreads() {
        return workerThreads;
    }
    
    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
    
    public int getBossThreads() {
        return bossThreads;
    }
    
    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }
    
    public boolean isSslEnabled() {
        return sslEnabled;
    }
    
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
    
    public String getSslCertPath() {
        return sslCertPath;
    }
    
    public void setSslCertPath(String sslCertPath) {
        this.sslCertPath = sslCertPath;
    }
    
    public String getSslKeyPath() {
        return sslKeyPath;
    }
    
    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }
    
    @Override
    public String toString() {
        return String.format("NetworkConfig{host='%s', port=%d, multicast='%s:%d', source='%s', ssl=%s}", 
                host, port, multicastAddress, multicastPort, sourceAddress, sslEnabled);
    }
} 