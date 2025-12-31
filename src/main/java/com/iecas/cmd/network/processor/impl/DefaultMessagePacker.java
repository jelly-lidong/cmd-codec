package com.iecas.cmd.network.processor.impl;

import com.iecas.cmd.network.NetworkMessage;
import com.iecas.cmd.network.processor.MessagePacker;
import com.iecas.cmd.network.processor.ProcessorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * 默认消息打包器实现
 */
public class DefaultMessagePacker implements MessagePacker {

    private static final String NAME = "DefaultMessagePacker";

    @Override
    public PackResult pack(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) throws ProcessorException {

        if (segments == null || segments.isEmpty()) {
            return new PackResult(new ArrayList<>(), "无分段数据需要打包", 0);
        }

        if (rule == null) {
            throw new ProcessorException(NAME, "打包规则不能为空");
        }
        // TODO: 2025/6/24 这里的规则策略需要根据业务决定
        try {
            switch (rule.getStrategy()) {
                case CONCATENATE:       // 简单拼接
                    return packByConcatenate(segments, segmentTypes, rule, originalMessage);
                case LENGTH_PREFIX:     // 添加长度前缀
                    return packByLengthPrefix(segments, segmentTypes, rule, originalMessage);
                case HEADER_PAYLOAD:   // 头部+载荷结构
                    return packByHeaderPayload(segments, segmentTypes, rule, originalMessage);
                case FRAMED:          // 帧结构
                    return packByFramed(segments, segmentTypes, rule, originalMessage);
                case COMPRESSED:     // 压缩打包
                    return packByCompressed(segments, segmentTypes, rule, originalMessage);
                case ENCRYPTED:      // 加密打包
                    return packByEncrypted(segments, segmentTypes, rule, originalMessage);
                case CUSTOM:    // 自定义打包
                    return packByCustom(segments, segmentTypes, rule, originalMessage);
                default:
                    throw new ProcessorException(NAME, "不支持的打包策略: " + rule.getStrategy());
            }
        } catch (Exception e) {
            throw new ProcessorException(NAME, "打包处理失败", e);
        }
    }

    /**
     * 简单拼接打包
     */
    private PackResult packByConcatenate(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        boolean singleMessage = rule.getParameter("singleMessage", Boolean.class, true);

        if (singleMessage) {
            // 所有分段拼接成一个消息
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte[] segment : segments) {
                try {
                    baos.write(segment);
                } catch (IOException e) {
                    // ByteArrayOutputStream写入内存，不会发生IO异常
                }
            }

            NetworkMessage packedMessage = createMessage(baos.toByteArray(), originalMessage);
            List<NetworkMessage> messages = new ArrayList<>();
            messages.add(packedMessage);

            return new PackResult(messages, String.format("拼接打包完成，%d个分段合并为1个消息", segments.size()), baos.size());
        } else {
            // 每个分段一个消息
            List<NetworkMessage> messages = new ArrayList<>();
            long totalSize = 0;

            for (byte[] segment : segments) {
                NetworkMessage message = createMessage(segment, originalMessage);
                messages.add(message);
                totalSize += segment.length;
            }

            return new PackResult(messages, String.format("分段打包完成，%d个分段对应%d个消息", segments.size(), messages.size()), totalSize);
        }
    }

    /**
     * 长度前缀打包
     */
    private PackResult packByLengthPrefix(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        int prefixLength = rule.getParameter("prefixLength", Integer.class, 4);
        boolean includeCount = rule.getParameter("includeCount", Boolean.class, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 写入分段数量（可选）
            if (includeCount) {
                writeLength(baos, segments.size(), prefixLength);
            }

            // 写入每个分段的长度和数据
            for (byte[] segment : segments) {
                writeLength(baos, segment.length, prefixLength);
                baos.write(segment);
            }

            NetworkMessage packedMessage = createMessage(baos.toByteArray(), originalMessage);
            List<NetworkMessage> messages = new ArrayList<>();
            messages.add(packedMessage);

            return new PackResult(messages, String.format("长度前缀打包完成，前缀长度=%d字节，分段数=%d", prefixLength, segments.size()), baos.size());

        } catch (IOException e) {
            throw new RuntimeException("打包过程中IO异常", e);
        }
    }

    /**
     * 头部+载荷结构打包
     */
    private PackResult packByHeaderPayload(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        boolean includeTypes = rule.getParameter("includeTypes", Boolean.class, true);
        String headerFormat = rule.getParameter("headerFormat", String.class, "BINARY");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 构建头部
            byte[] header = buildHeader(segments, segmentTypes, includeTypes, headerFormat);
            baos.write(header);

            // 写入载荷
            for (byte[] segment : segments) {
                baos.write(segment);
            }

            NetworkMessage packedMessage = createMessage(baos.toByteArray(), originalMessage);
            List<NetworkMessage> messages = new ArrayList<>();
            messages.add(packedMessage);

            return new PackResult(messages, String.format("头部载荷打包完成，头部长度=%d字节，载荷分段数=%d", header.length, segments.size()), baos.size());

        } catch (IOException e) {
            throw new RuntimeException("打包过程中IO异常", e);
        }
    }

    /**
     * 帧结构打包
     */
    private PackResult packByFramed(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        byte[] frameStart = rule.getParameter("frameStart", String.class, "<<").getBytes();
        byte[] frameEnd = rule.getParameter("frameEnd", String.class, ">>").getBytes();
        boolean escapeData = rule.getParameter("escapeData", Boolean.class, true);

        List<NetworkMessage> messages = new ArrayList<>();
        long totalSize = 0;

        for (int i = 0; i < segments.size(); i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                // 帧开始标记
                baos.write(frameStart);

                // 分段数据（可能需要转义）
                byte[] segmentData = segments.get(i);
                if (escapeData) {
                    segmentData = escapeFrameData(segmentData, frameStart, frameEnd);
                }
                baos.write(segmentData);

                // 帧结束标记
                baos.write(frameEnd);

                NetworkMessage message = createMessage(baos.toByteArray(), originalMessage);
                messages.add(message);
                totalSize += baos.size();

            } catch (IOException e) {
                throw new RuntimeException("帧打包过程中IO异常", e);
            }
        }

        return new PackResult(messages, String.format("帧结构打包完成，分段数=%d，总帧数=%d", segments.size(), messages.size()), totalSize);
    }

    /**
     * 压缩打包
     */
    private PackResult packByCompressed(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        // 先简单拼接所有分段
        ByteArrayOutputStream rawBaos = new ByteArrayOutputStream();
        for (byte[] segment : segments) {
            try {
                rawBaos.write(segment);
            } catch (IOException e) {
                // ByteArrayOutputStream写入内存，不会发生IO异常
            }
        }

        // 使用GZIP压缩
        ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(compressedBaos)) {
            gzipOut.write(rawBaos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("压缩过程中IO异常", e);
        }

        NetworkMessage packedMessage = createMessage(compressedBaos.toByteArray(), originalMessage);
        List<NetworkMessage> messages = new ArrayList<>();
        messages.add(packedMessage);

        return new PackResult(messages, String.format("压缩打包完成，原始大小=%d字节，压缩后大小=%d字节，压缩率=%.2f%%", rawBaos.size(), compressedBaos.size(), (1.0 - (double) compressedBaos.size() / rawBaos.size()) * 100), compressedBaos.size());
    }

    /**
     * 加密打包（简单示例，实际使用需要真正的加密算法）
     */
    private PackResult packByEncrypted(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        String algorithm = rule.getParameter("algorithm", String.class, "XOR");
        byte key = rule.getParameter("key", Integer.class, 0x5A).byteValue();

        List<NetworkMessage> messages = new ArrayList<>();
        long totalSize = 0;

        for (byte[] segment : segments) {
            byte[] encryptedData = simpleEncrypt(segment, algorithm, key);
            NetworkMessage message = createMessage(encryptedData, originalMessage);
            messages.add(message);
            totalSize += encryptedData.length;
        }

        return new PackResult(messages, String.format("加密打包完成，算法=%s，分段数=%d", algorithm, segments.size()), totalSize);
    }

    /**
     * 自定义打包
     */
    private PackResult packByCustom(List<byte[]> segments, List<Integer> segmentTypes, PackRule rule, NetworkMessage originalMessage) {

        // 示例：添加序列号和校验和
        List<NetworkMessage> messages = new ArrayList<>();
        long totalSize = 0;

        for (int i = 0; i < segments.size(); i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                // 序列号（2字节）
                baos.write((i >> 8) & 0xFF);
                baos.write(i & 0xFF);

                // 分段类型（1字节）
                int segmentType = segmentTypes != null && i < segmentTypes.size() ? segmentTypes.get(i) : 0;
                baos.write(segmentType);

                // 数据长度（2字节）
                byte[] segment = segments.get(i);
                baos.write((segment.length >> 8) & 0xFF);
                baos.write(segment.length & 0xFF);

                // 数据
                baos.write(segment);

                // 校验和（1字节）
                int checksum = calculateChecksum(segment);
                baos.write(checksum & 0xFF);

                NetworkMessage message = createMessage(baos.toByteArray(), originalMessage);
                messages.add(message);
                totalSize += baos.size();

            } catch (IOException e) {
                throw new RuntimeException("自定义打包过程中IO异常", e);
            }
        }

        return new PackResult(messages, String.format("自定义打包完成，分段数=%d，每个分段添加6字节头部和校验", segments.size()), totalSize);
    }

    /**
     * 写入长度值到输出流
     */
    private void writeLength(ByteArrayOutputStream baos, int length, int prefixLength) throws IOException {
        for (int i = prefixLength - 1; i >= 0; i--) {
            baos.write((length >> (i * 8)) & 0xFF);
        }
    }

    /**
     * 构建头部数据
     */
    private byte[] buildHeader(List<byte[]> segments, List<Integer> segmentTypes, boolean includeTypes, String headerFormat) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 魔数（4字节）
            baos.write("HEAD".getBytes());

            // 分段数量（4字节）
            writeLength(baos, segments.size(), 4);

            // 总数据长度（4字节）
            int totalLength = segments.stream().mapToInt(seg -> seg.length).sum();
            writeLength(baos, totalLength, 4);

            // 分段信息
            for (int i = 0; i < segments.size(); i++) {
                // 分段长度（4字节）
                writeLength(baos, segments.get(i).length, 4);

                // 分段类型（可选，1字节）
                if (includeTypes && segmentTypes != null && i < segmentTypes.size()) {
                    baos.write(segmentTypes.get(i) & 0xFF);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("构建头部过程中IO异常", e);
        }

        return baos.toByteArray();
    }

    /**
     * 对帧数据进行转义
     */
    private byte[] escapeFrameData(byte[] data, byte[] frameStart, byte[] frameEnd) {
        // 简单实现：将帧标记替换为转义序列
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (int i = 0; i < data.length; i++) {
            if (matchesAt(data, i, frameStart) || matchesAt(data, i, frameEnd)) {
                // 添加转义字符
                baos.write(0x1B); // ESC
                baos.write(data[i]);
            } else {
                baos.write(data[i]);
            }
        }

        return baos.toByteArray();
    }

    /**
     * 检查在指定位置是否匹配模式
     */
    private boolean matchesAt(byte[] data, int pos, byte[] pattern) {
        if (pos + pattern.length > data.length) {
            return false;
        }

        for (int i = 0; i < pattern.length; i++) {
            if (data[pos + i] != pattern[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * 简单加密（示例实现）
     */
    private byte[] simpleEncrypt(byte[] data, String algorithm, byte key) {
        byte[] result = new byte[data.length];

        switch (algorithm.toUpperCase()) {
            case "XOR":
                for (int i = 0; i < data.length; i++) {
                    result[i] = (byte) (data[i] ^ key);
                }
                break;
            case "SHIFT":
                for (int i = 0; i < data.length; i++) {
                    result[i] = (byte) ((data[i] + key) & 0xFF);
                }
                break;
            default:
                System.arraycopy(data, 0, result, 0, data.length);
                break;
        }

        return result;
    }

    /**
     * 计算校验和
     */
    private int calculateChecksum(byte[] data) {
        int sum = 0;
        for (byte b : data) {
            sum += b & 0xFF;
        }
        return sum & 0xFF;
    }

    /**
     * 创建新的网络消息
     */
    private NetworkMessage createMessage(byte[] data, NetworkMessage originalMessage) {
        NetworkMessage message = new NetworkMessage(data);

        // 复制原始消息的元数据
        if (originalMessage != null) {
            message.setSender(originalMessage.getSender());
            message.setReceiver(originalMessage.getReceiver());
            message.setMessageType(originalMessage.getMessageType());
        }

        return message;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supports(PackStrategy strategy) {
        return strategy != null; // 支持所有策略
    }

    @Override
    public List<PackStrategy> getSupportedStrategies() {
        return Arrays.asList(PackStrategy.values());
    }
} 