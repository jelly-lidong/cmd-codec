package com.iecas.cmd.util;

import io.netty.buffer.ByteBuf;

/**
 * 位缓冲区接口
 * 提供位级别的操作支持
 */
public interface BitBuf {
    /**
     * 写入一个位
     */
    BitBuf writeBit(boolean bit);

    /**
     * 读取一个位
     */
    boolean readBit();

    /**
     * 获取当前位位置
     */
    int getBitPosition();

    /**
     * 设置当前位位置
     */
    BitBuf setBitPosition(int position);

    /**
     * 获取剩余位数
     */
    int getRemainingBits();

    /**
     * 写入指定位数的值
     */
    BitBuf writeBits(int value, int bits);

    /**
     * 读取指定位数的值
     */
    int readBits(int bits);

    /**
     * 转换为ByteBuf
     */
    ByteBuf toByteBuf();
} 