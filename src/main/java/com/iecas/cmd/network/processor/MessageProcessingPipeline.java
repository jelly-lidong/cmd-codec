package com.iecas.cmd.network.processor;

import com.iecas.cmd.network.NetworkEventHandler;
import com.iecas.cmd.network.NetworkMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 消息处理流水线
 * 提供字节码接收 -> 拆段 -> 重新打包的完整处理流程
 */
public class MessageProcessingPipeline {
    
    private final List<MessageProcessor> processors;
    private final MessageSegmenter segmenter;
    private final MessagePacker packer;
    private final Executor executor;
    private final NetworkEventHandler eventHandler;
    private final PipelineConfig config;
    
    /**
     * 流水线配置
     */
    public static class PipelineConfig {
        private boolean asyncProcessing = true;
        private boolean enableMetrics = true;
        private boolean errorRecovery = true;
        private int maxRetries = 3;
        private long timeoutMs = 30000;
        
        public boolean isAsyncProcessing() { return asyncProcessing; }
        public PipelineConfig setAsyncProcessing(boolean asyncProcessing) {
            this.asyncProcessing = asyncProcessing;
            return this;
        }
        
        public boolean isEnableMetrics() { return enableMetrics; }
        public PipelineConfig setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }
        
        public boolean isErrorRecovery() { return errorRecovery; }
        public PipelineConfig setErrorRecovery(boolean errorRecovery) {
            this.errorRecovery = errorRecovery;
            return this;
        }
        
        public int getMaxRetries() { return maxRetries; }
        public PipelineConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public long getTimeoutMs() { return timeoutMs; }
        public PipelineConfig setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
    }
    
    /**
     * 处理上下文
     */
    public static class ProcessingContext {
        private final NetworkMessage originalMessage;
        private final long startTime;
        private final String sessionId;
        private MessageSegmenter.SegmentResult segmentResult;
        private MessagePacker.PackResult packResult;
        private final List<ProcessorResult> processorResults;
        
        public ProcessingContext(NetworkMessage originalMessage, String sessionId) {
            this.originalMessage = originalMessage;
            this.sessionId = sessionId;
            this.startTime = System.currentTimeMillis();
            this.processorResults = new ArrayList<>();
        }
        
        public NetworkMessage getOriginalMessage() { return originalMessage; }
        public long getStartTime() { return startTime; }
        public String getSessionId() { return sessionId; }
        public MessageSegmenter.SegmentResult getSegmentResult() { return segmentResult; }
        public MessagePacker.PackResult getPackResult() { return packResult; }
        public List<ProcessorResult> getProcessorResults() { return processorResults; }
        
        public void setSegmentResult(MessageSegmenter.SegmentResult segmentResult) {
            this.segmentResult = segmentResult;
        }
        
        public void setPackResult(MessagePacker.PackResult packResult) {
            this.packResult = packResult;
        }
        
        public void addProcessorResult(ProcessorResult result) {
            this.processorResults.add(result);
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    private MessageProcessingPipeline(Builder builder) {
        this.processors = new ArrayList<>(builder.processors);
        this.segmenter = builder.segmenter;
        this.packer = builder.packer;
        this.executor = builder.executor != null ? builder.executor : ForkJoinPool.commonPool();
        this.eventHandler = builder.eventHandler;
        this.config = builder.config != null ? builder.config : new PipelineConfig();
    }
    
    /**
     * 处理消息的主入口
     * 
     * @param message 输入消息
     * @param segmentRule 分段规则
     * @param packRule 打包规则
     * @return 处理结果的Future
     */
    public CompletableFuture<ProcessingContext> processMessage(NetworkMessage message,
                                                              MessageSegmenter.SegmentRule segmentRule,
                                                              MessagePacker.PackRule packRule) {
        
        final String sessionId = generateSessionId();
        final ProcessingContext context = new ProcessingContext(message, sessionId);
        
        if (config.isAsyncProcessing()) { //异步处理
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return doProcessMessage(context, segmentRule, packRule);
                } catch (Exception e) {
                    handleProcessingError(context, e);
                    throw new RuntimeException("消息处理失败", e);
                }
            }, executor);
        } else { // 同步处理
            try {
                return CompletableFuture.completedFuture(doProcessMessage(context, segmentRule, packRule));
            } catch (Exception e) {
                final CompletableFuture<ProcessingContext> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(e);
                return failedFuture;
            }
        }
    }
    
    /**
     * 执行消息处理
     */
    private ProcessingContext doProcessMessage(ProcessingContext context,
                                             MessageSegmenter.SegmentRule segmentRule,
                                             MessagePacker.PackRule packRule) throws ProcessorException {
        
        fireProcessingStarted(context);
        
        try {
            // 1. 预处理阶段
            NetworkMessage preprocessedMessage = executePreProcessors(context);
            
            // 2. 分段阶段
            MessageSegmenter.SegmentResult segmentResult = segmenter.segment(preprocessedMessage.getData(), segmentRule);
            context.setSegmentResult(segmentResult);
            fireSegmentationCompleted(context, segmentResult);
            
            // 3. 打包阶段
            MessagePacker.PackResult packResult = packer.pack(
                    segmentResult.getSegments(),
                    segmentResult.getSegmentTypes(),
                    packRule,
                    preprocessedMessage);
            context.setPackResult(packResult);

            // 回调通知 :打包结束
            firePackingCompleted(context, packResult);
            
            // 4. 后处理阶段
            executePostProcessors(context);

            // 回调通知 :处理结束
            fireProcessingCompleted(context);

            return context;
            
        } catch (ProcessorException e) {
            fireProcessingFailed(context, e);
            throw e;
        }
    }
    
    /**
     * 执行预处理器
     */
    private NetworkMessage executePreProcessors(ProcessingContext context) throws ProcessorException {
        NetworkMessage currentMessage = context.getOriginalMessage();
        
        for (MessageProcessor processor : processors) {
            if (processor.supports(currentMessage)) {
                try {
                    ProcessorResult result = processor.process(currentMessage);
                    context.addProcessorResult(result);
                    
                    if (result.isSuccess() && result.hasMessages()) {
                        currentMessage = result.getMessage();
                    } else if (result.isFailed()) {
                        throw new ProcessorException(processor.getName(), "预处理失败: " + result.getResultMessage(), result.getCause());
                    }
                } catch (ProcessorException e) {
                    if (config.isErrorRecovery()) {
                        // 记录错误但继续处理
                        fireProcessorError(context, processor.getName(), e);
                    } else {
                        throw e;
                    }
                }
            }
        }
        
        return currentMessage;
    }
    
    /**
     * 执行后处理器
     */
    private void executePostProcessors(ProcessingContext context) throws ProcessorException {
        if (context.getPackResult() == null || context.getPackResult().isEmpty()) {
            return;
        }
        
        // 对打包后的每个消息执行后处理
        for (NetworkMessage message : context.getPackResult().getMessages()) {
            for (MessageProcessor processor : processors) {
                if (processor.supports(message)) {
                    try {
                        ProcessorResult result = processor.process(message);
                        context.addProcessorResult(result);
                        
                        if (result.isFailed()) {
                            throw new ProcessorException(processor.getName(), 
                                    "后处理失败: " + result.getResultMessage(), result.getCause());
                        }
                    } catch (ProcessorException e) {
                        if (config.isErrorRecovery()) {
                            fireProcessorError(context, processor.getName(), e);
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * 处理处理错误
     */
    private void handleProcessingError(ProcessingContext context, Exception e) {
        if (eventHandler != null) {
            eventHandler.onException(context.getOriginalMessage().getSender(), e);
        }
    }
    
    // 事件触发方法
    private void fireProcessingStarted(ProcessingContext context) {
        if (eventHandler != null) {
            // 创建一个带有处理信息的新消息
            NetworkMessage infoMessage = new NetworkMessage(("处理开始: " + context.getSessionId()).getBytes());
            infoMessage.setMessageType(NetworkMessage.MessageType.NOTIFY);
            infoMessage.setSender(context.getOriginalMessage().getSender());
            infoMessage.setReceiver(context.getOriginalMessage().getReceiver());
            eventHandler.onMessageReceived(infoMessage);
        }
    }
    
    private void fireSegmentationCompleted(ProcessingContext context, MessageSegmenter.SegmentResult result) {
        if (eventHandler != null) {
            NetworkMessage infoMessage = new NetworkMessage(String.format("分段完成: %s, 分段数: %d", 
                    context.getSessionId(), result.getSegmentCount()).getBytes());
            infoMessage.setMessageType(NetworkMessage.MessageType.NOTIFY);
            infoMessage.setSender(context.getOriginalMessage().getSender());
            infoMessage.setReceiver(context.getOriginalMessage().getReceiver());
            eventHandler.onMessageReceived(infoMessage);
        }
    }
    
    private void firePackingCompleted(ProcessingContext context, MessagePacker.PackResult result) {
        if (eventHandler != null) {
            NetworkMessage infoMessage = new NetworkMessage(String.format("打包完成: %s, 消息数: %d", 
                    context.getSessionId(), result.getMessageCount()).getBytes());
            infoMessage.setMessageType(NetworkMessage.MessageType.NOTIFY);
            infoMessage.setSender(context.getOriginalMessage().getSender());
            infoMessage.setReceiver(context.getOriginalMessage().getReceiver());
            eventHandler.onMessageReceived(infoMessage);
        }
    }
    
    private void fireProcessingCompleted(ProcessingContext context) {
        if (eventHandler != null) {
            NetworkMessage infoMessage = new NetworkMessage(String.format("处理完成: %s, 耗时: %dms", 
                    context.getSessionId(), context.getElapsedTime()).getBytes());
            infoMessage.setMessageType(NetworkMessage.MessageType.NOTIFY);
            infoMessage.setSender(context.getOriginalMessage().getSender());
            infoMessage.setReceiver(context.getOriginalMessage().getReceiver());
            eventHandler.onMessageReceived(infoMessage);
        }
    }
    
    private void fireProcessingFailed(ProcessingContext context, Exception e) {
        if (eventHandler != null) {
            eventHandler.onException(context.getOriginalMessage().getSender(), e);
        }
    }
    
    private void fireProcessorError(ProcessingContext context, String processorName, Exception e) {
        if (eventHandler != null) {
            eventHandler.onException(context.getOriginalMessage().getSender(), e);
        }
    }
    
    /**
     * 流水线构建器
     */
    public static class Builder {
        private final List<MessageProcessor> processors = new ArrayList<>();
        private MessageSegmenter segmenter;
        private MessagePacker packer;
        private Executor executor;
        private NetworkEventHandler eventHandler;
        private PipelineConfig config;
        
        public Builder addProcessor(MessageProcessor processor) {
            if (processor != null) {
                this.processors.add(processor);
            }
            return this;
        }
        
        public Builder addProcessors(List<MessageProcessor> processors) {
            if (processors != null) {
                this.processors.addAll(processors);
            }
            return this;
        }
        
        public Builder setSegmenter(MessageSegmenter segmenter) {
            this.segmenter = segmenter;
            return this;
        }
        
        public Builder setPacker(MessagePacker packer) {
            this.packer = packer;
            return this;
        }
        
        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }
        
        public Builder setEventHandler(NetworkEventHandler eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }
        
        public Builder setConfig(PipelineConfig config) {
            this.config = config;
            return this;
        }
        
        public MessageProcessingPipeline build() {
            if (segmenter == null) {
                throw new IllegalStateException("MessageSegmenter 不能为空");
            }
            if (packer == null) {
                throw new IllegalStateException("MessagePacker 不能为空");
            }
            
            return new MessageProcessingPipeline(this);
        }
    }
    
    // Getters
    public List<MessageProcessor> getProcessors() {
        return Collections.unmodifiableList(processors);
    }
    
    public MessageSegmenter getSegmenter() {
        return segmenter;
    }
    
    public MessagePacker getPacker() {
        return packer;
    }
    
    public PipelineConfig getConfig() {
        return config;
    }
} 