package com.iecas.cmd.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 位缓冲区实现
 * 独立实现，不依赖外部库，提供位级别和字节级别的操作支持
 * 支持大小端、有符号/无符号整数、浮点数等多种数据类型
 */
@Slf4j
public class BitBuffer {
    
    private byte[] buffer;
    private int readBitPosition;   // 读位位置
    private int writeBitPosition;  // 写位位置
    private int readBytePosition;  // 读字节位置
    private int writeBytePosition; // 写字节位置
    private int bitCount;          // 总位数
    private int byteCount;         // 总字节数
    private ByteOrder byteOrder;   // 字节序
    private static final int INITIAL_CAPACITY = 256; // 初始容量（字节）

    public BitBuffer() {
        this(ByteOrder.BIG_ENDIAN);
    }

    public BitBuffer(ByteOrder byteOrder) {
        this.buffer = new byte[INITIAL_CAPACITY];
        this.readBitPosition = 0;
        this.writeBitPosition = 0;
        this.readBytePosition = 0;
        this.writeBytePosition = 0;
        this.bitCount = 0;
        this.byteCount = 0;
        this.byteOrder = byteOrder != null ? byteOrder : ByteOrder.BIG_ENDIAN;
    }

    /**
     * 创建一个新的BitBuffer，并初始化数据
     * @param data 初始数据
     */
    public BitBuffer(byte[] data) {
        this(data, ByteOrder.BIG_ENDIAN);
    }

    /**
     * 创建一个新的BitBuffer，并初始化数据和字节序
     * @param data 初始数据
     * @param byteOrder 字节序
     */
    public BitBuffer(byte[] data, ByteOrder byteOrder) {
        this(byteOrder);
        if (data != null && data.length > 0) {
            // 直接复制数据到缓冲区
            ensureCapacity(data.length);
            System.arraycopy(data, 0, buffer, 0, data.length);
            writeBytePosition = data.length;
            byteCount = data.length;
            writeBitPosition = data.length * 8;
            bitCount = data.length * 8;
            setReadBitPosition(0);
            setReadBytePosition(0);
        }
    }

    // ========== 基本属性获取方法 ==========

    /**
     * 获取当前读位位置
     */
    public int getReadBitPosition() {
        return readBitPosition;
    }

    /**
     * 获取当前写位位置
     */
    public int getWriteBitPosition() {
        return writeBitPosition;
    }

    /**
     * 获取当前读字节位置
     */
    public int getReadBytePosition() {
        return readBytePosition;
    }

    /**
     * 获取当前写字节位置
     */
    public int getWriteBytePosition() {
        return writeBytePosition;
    }

    /**
     * 获取总位数
     */
    public int getBitCount() {
        return bitCount;
    }

    /**
     * 获取总字节数
     */
    public int getByteCount() {
        return byteCount;
    }

    /**
     * 获取缓冲区容量（字节数）
     */
    public int capacity() {
        return buffer.length;
    }

    /**
     * 获取字节序
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * 设置字节序
     */
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder != null ? byteOrder : ByteOrder.BIG_ENDIAN;
    }

    // ========== 容量管理 ==========

    /**
     * 扩容缓冲区
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > buffer.length) {
            int newCapacity = Math.max(buffer.length * 2, minCapacity);
            buffer = Arrays.copyOf(buffer, newCapacity);
        }
    }

    /**
     * 确保有足够的字节容量
     */
    private void ensureByteCapacity(int minByteCapacity) {
        ensureCapacity(minByteCapacity);
    }

    // ========== 位置设置方法 ==========

    /**
     * 设置当前读位位置
     */
    public void setReadBitPosition(int position) {
        if (position < 0 || position > bitCount) {
            throw new IndexOutOfBoundsException("Read bit position out of bounds: " + position);
        }
        this.readBitPosition = position;
    }

    /**
     * 设置当前写位位置
     */
    public void setWriteBitPosition(int position) {
        if (position < 0 || position > bitCount) {
            throw new IndexOutOfBoundsException("Write bit position out of bounds: " + position);
        }
        this.writeBitPosition = position;
    }

    /**
     * 设置当前读字节位置
     */
    public void setReadBytePosition(int position) {
        if (position < 0 || position > byteCount) {
            throw new IndexOutOfBoundsException("Read byte position out of bounds: " + position);
        }
        this.readBytePosition = position;
    }

    /**
     * 设置当前写字节位置
     */
    public void setWriteBytePosition(int position) {
        if (position < 0 || position > byteCount) {
            throw new IndexOutOfBoundsException("Write byte position out of bounds: " + position);
        }
        this.writeBytePosition = position;
    }

    // ========== 可读写数量计算 ==========

    /**
     * 获取可读位数
     */
    public int getReadableBits() {
        return writeBitPosition - readBitPosition;
    }

    /**
     * 获取可写位数
     */
    public int getWritableBits() {
        int maxBits = buffer.length * 8;
        return maxBits - writeBitPosition;
    }

    /**
     * 获取可读字节数
     */
    public int getReadableBytes() {
        return writeBytePosition - readBytePosition;
    }

    /**
     * 获取可写字节数
     */
    public int getWritableBytes() {
        return buffer.length - writeBytePosition;
    }

    // ========== 位操作方法 ==========

    /**
     * 写入一个位
     */
    public void writeBit(boolean bit) {
        int byteIndex = writeBitPosition / 8;
        int bitIndex = 7 - (writeBitPosition % 8);
        
        // 检查是否需要扩容
        ensureCapacity(byteIndex + 1);
        
        if (bit) {
            buffer[byteIndex] |= (byte) (1 << bitIndex);
        } else {
            buffer[byteIndex] &= (byte) ~(1 << bitIndex);
        }
        
        writeBitPosition++;
        if (writeBitPosition > bitCount) {
            bitCount = writeBitPosition;
        }
        
        // 同步字节位置（如果字节对齐）
        if (writeBitPosition % 8 == 0) {
            writeBytePosition = writeBitPosition / 8;
            if (writeBytePosition > byteCount) {
                byteCount = writeBytePosition;
            }
        }
    }

    /**
     * 读取一个位
     */
    public boolean readBit() {
        if (readBitPosition >= bitCount) {
            throw new IndexOutOfBoundsException("No more bits to read");
        }
        
        int byteIndex = readBitPosition / 8;
        int bitIndex = 7 - (readBitPosition % 8);
        
        boolean bit = ((buffer[byteIndex] >> bitIndex) & 1) == 1;
        readBitPosition++;
        
        // 同步字节位置（如果字节对齐）
        if (readBitPosition % 8 == 0) {
            readBytePosition = readBitPosition / 8;
        }
        
        return bit;
    }

    /**
     * 写入指定位数的值
     * 
     * 这里是高位优先写入（big-endian），即先写入高位再写入低位。
     * 
     * 例如：value=3，bits=11
     * 二进制：3的11位表示为 00000000011
     * 实际写入顺序为：先写入第10位（最高位，值为0），再写入第9位，...，最后写入第0位（最低位，值为1）
     * 
     * 也就是说，写入的11个位依次为：0 0 0 0 0 0 0 0 0 1 1
     */
    public void writeBits(int value, int bits) {
        if (bits < 0 || bits > 32) {
            throw new IllegalArgumentException("bits must be between 0 and 32");
        }
        
        for (int i = bits - 1; i >= 0; i--) {
            writeBit(((value >> i) & 1) == 1);
        }

    }

    /**
     * 读取指定位数的值
     */
    public int readBits(int bits) {
        if (bits < 0 || bits > 32) {
            throw new IllegalArgumentException("bits must be between 0 and 32");
        }
        
        if (getReadableBits() < bits) {
            throw new IndexOutOfBoundsException("Not enough bits to read: requested " + bits + ", available " + getReadableBits());
        }
        
        int value = 0;
        for (int i = 0; i < bits; i++) {
            value = (value << 1) | (readBit() ? 1 : 0);
        }
        
        return value;
    }

    // ========== 字节操作方法 ==========

    /**
     * 写入一个字节（使用字节位置）
     */
    public BitBuffer writeByte(int value) {
        // 检查缓冲区是否处于字节对齐状态（没有未完成的位数据）
        if (writeBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot write byte when buffer has incomplete bit data. Write bit position: " + writeBitPosition);
        }
        
        ensureByteCapacity(writeBytePosition + 1);
        buffer[writeBytePosition] = (byte) value;
        writeBytePosition++;
        if (writeBytePosition > byteCount) {
            byteCount = writeBytePosition;
        }
        
        // 同步位位置
        writeBitPosition = writeBytePosition * 8;
        if (writeBitPosition > bitCount) {
            bitCount = writeBitPosition;
        }
        
        return this;
    }

    /**
     * 读取一个字节（使用字节位置）
     */
    public byte readByte() {
        // 检查缓冲区是否处于字节对齐状态（没有未完成的位数据）
        if (writeBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot read byte when buffer has incomplete bit data. Write bit position: " + writeBitPosition);
        }
        
        // 检查读取位置是否字节对齐
        if (readBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot read byte when not byte-aligned. Current bit position: " + readBitPosition);
        }
        
        if (readBytePosition >= byteCount) {
            throw new IndexOutOfBoundsException("Not enough bytes to read");
        }
        
        byte value = buffer[readBytePosition];
        readBytePosition++;
        
        // 同步位位置
        readBitPosition = readBytePosition * 8;
        
        return value;
    }

    /**
     * 写入字节数组（使用字节位置）
     */
    public BitBuffer writeBytes(byte[] src) {
        if (src == null || src.length == 0) {
            return this;
        }
        
        // 检查缓冲区是否处于字节对齐状态（没有未完成的位数据）
        if (writeBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot write bytes when buffer has incomplete bit data. Write bit position: " + writeBitPosition);
        }
        
        ensureByteCapacity(writeBytePosition + src.length);
        System.arraycopy(src, 0, buffer, writeBytePosition, src.length);
        writeBytePosition += src.length;
        if (writeBytePosition > byteCount) {
            byteCount = writeBytePosition;
        }
        
        // 同步位位置
        writeBitPosition = writeBytePosition * 8;
        if (writeBitPosition > bitCount) {
            bitCount = writeBitPosition;
        }
        
        return this;
    }

    /**
     * 读取字节数组（使用字节位置）
     */
    public BitBuffer readBytes(byte[] dst) {
        if (dst == null || dst.length == 0) {
            return this;
        }
        
        // 检查缓冲区是否处于字节对齐状态（没有未完成的位数据）
        if (writeBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot read bytes when buffer has incomplete bit data. Write bit position: " + writeBitPosition);
        }
        
        // 检查读取位置是否字节对齐
        if (readBitPosition % 8 != 0) {
            throw new IllegalStateException("Cannot read bytes when not byte-aligned. Current bit position: " + readBitPosition);
        }
        
        if (readBytePosition + dst.length > byteCount) {
            throw new IndexOutOfBoundsException("Not enough bytes to read");
        }
        
        System.arraycopy(buffer, readBytePosition, dst, 0, dst.length);
        readBytePosition += dst.length;
        
        // 同步位位置
        readBitPosition = readBytePosition * 8;
        
        return this;
    }

    /**
     * 读取指定长度的字节数组
     */
    public byte[] readBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }
        if (length == 0) {
            return new byte[0];
        }
        
        byte[] result = new byte[length];
        readBytes(result);
        return result;
    }

    // ========== 有符号整数操作 ==========

    /**
     * 写入有符号字节
     */
    public BitBuffer writeSignedByte(byte value) {
        return writeByte(value);
    }

    /**
     * 读取有符号字节
     */
    public byte readSignedByte() {
        return readByte();
    }

    /**
     * 写入有符号短整数
     */
    public BitBuffer writeSignedShort(short value) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            writeByte((value >> 8) & 0xFF);
            writeByte(value & 0xFF);
        } else {
            writeByte(value & 0xFF);
            writeByte((value >> 8) & 0xFF);
        }
        return this;
    }

    /**
     * 读取有符号短整数
     */
    public short readSignedShort() {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (short) ((readByte() & 0xFF) << 8 | (readByte() & 0xFF));
        } else {
            return (short) ((readByte() & 0xFF) | (readByte() & 0xFF) << 8);
        }
    }

    /**
     * 写入有符号整数
     */
    public BitBuffer writeSignedInt(int value) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            writeByte((value >> 24) & 0xFF);
            writeByte((value >> 16) & 0xFF);
            writeByte((value >> 8) & 0xFF);
            writeByte(value & 0xFF);
        } else {
            writeByte(value & 0xFF);
            writeByte((value >> 8) & 0xFF);
            writeByte((value >> 16) & 0xFF);
            writeByte((value >> 24) & 0xFF);
        }
        return this;
    }

    /**
     * 读取有符号整数
     */
    public int readSignedInt() {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (readByte() & 0xFF) << 24 | 
                   (readByte() & 0xFF) << 16 | 
                   (readByte() & 0xFF) << 8 | 
                   (readByte() & 0xFF);
        } else {
            return (readByte() & 0xFF) | 
                   (readByte() & 0xFF) << 8 | 
                   (readByte() & 0xFF) << 16 | 
                   (readByte() & 0xFF) << 24;
        }
    }

    /**
     * 写入有符号长整数
     */
    public BitBuffer writeSignedLong(long value) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 7; i >= 0; i--) {
                writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        } else {
            for (int i = 0; i < 8; i++) {
                writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        }
        return this;
    }

    /**
     * 读取有符号长整数
     */
    public long readSignedLong() {
        long value = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (readByte() & 0xFF);
            }
        } else {
            for (int i = 0; i < 8; i++) {
                value |= ((long)(readByte() & 0xFF)) << (i * 8);
            }
        }
        return value;
    }

    // ========== 无符号整数操作 ==========

    /**
     * 写入无符号字节
     */
    public BitBuffer writeUnsignedByte(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Unsigned byte value out of range: " + value);
        }
        return writeByte(value);
    }

    /**
     * 读取无符号字节
     */
    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    /**
     * 写入无符号短整数
     */
    public BitBuffer writeUnsignedShort(int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException("Unsigned short value out of range: " + value);
        }
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            writeByte((value >> 8) & 0xFF);
            writeByte(value & 0xFF);
        } else {
            writeByte(value & 0xFF);
            writeByte((value >> 8) & 0xFF);
        }
        return this;
    }

    /**
     * 读取无符号短整数
     */
    public int readUnsignedShort() {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return (readByte() & 0xFF) << 8 | (readByte() & 0xFF);
        } else {
            return (readByte() & 0xFF) | (readByte() & 0xFF) << 8;
        }
    }

    /**
     * 写入无符号整数
     */
    public BitBuffer writeUnsignedInt(long value) {
        if (value < 0 || value > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Unsigned int value out of range: " + value);
        }
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            writeByte((int)((value >> 24) & 0xFF));
            writeByte((int)((value >> 16) & 0xFF));
            writeByte((int)((value >> 8) & 0xFF));
            writeByte((int)(value & 0xFF));
        } else {
            writeByte((int)(value & 0xFF));
            writeByte((int)((value >> 8) & 0xFF));
            writeByte((int)((value >> 16) & 0xFF));
            writeByte((int)((value >> 24) & 0xFF));
        }
        return this;
    }

    /**
     * 读取无符号整数
     */
    public long readUnsignedInt() {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return ((long)(readByte() & 0xFF)) << 24 | 
                   ((long)(readByte() & 0xFF)) << 16 | 
                   ((long)(readByte() & 0xFF)) << 8 | 
                   ((long)(readByte() & 0xFF));
        } else {
            return ((long)(readByte() & 0xFF)) | 
                   ((long)(readByte() & 0xFF)) << 8 | 
                   ((long)(readByte() & 0xFF)) << 16 | 
                   ((long)(readByte() & 0xFF)) << 24;
        }
    }

    // ========== 任意字节数整数操作 ==========

    /**
     * 写入指定字节数的有符号整数
     */
    public void writeSignedIntBytes(long value, int bytes) {
        if (bytes < 1 || bytes > 8) {
            throw new IllegalArgumentException("bytes must be between 1 and 8");
        }
        
        // 计算最大值和最小值
        long maxValue;
        long minValue;
        if (bytes == 8) {
            maxValue = Long.MAX_VALUE;
            minValue = Long.MIN_VALUE;
        } else {
            maxValue = (1L << (bytes * 8 - 1)) - 1;
            minValue = -(1L << (bytes * 8 - 1));
        }
        
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(
                String.format("Value %d out of range [%d, %d] for %d bytes", 
                    value, minValue, maxValue, bytes));
        }
        
        // 写入指定字节数
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
        for (int i = bytes - 1; i >= 0; i--) {
            writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        }
    }

    /**
     * 读取指定字节数的有符号整数
     */
    public long readSignedIntBytes(int bytes) {
        if (bytes < 1 || bytes > 8) {
            throw new IllegalArgumentException("bytes must be between 1 and 8");
        }
        
        long value = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
        for (int i = 0; i < bytes; i++) {
            value = (value << 8) | (readByte() & 0xFF);
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                value |= ((long)(readByte() & 0xFF)) << (i * 8);
            }
        }
        
        // 处理负数符号扩展
        if (bytes < 8 && (value & (1L << (bytes * 8 - 1))) != 0) {
            value |= -(1L << (bytes * 8));
        }
        
        return value;
    }

    /**
     * 写入指定字节数的无符号整数
     */
    public void writeUnsignedIntBytes(long value, int bytes) {
        if (bytes < 1 || bytes > 8) {
            throw new IllegalArgumentException("bytes must be between 1 and 8");
        }
        
        if (value < 0) {
            throw new IllegalArgumentException("Unsigned value cannot be negative: " + value);
        }
        
        // 计算最大值
        long maxValue = bytes == 8 ? -1L : (1L << (bytes * 8)) - 1;
        if (value > maxValue) {
            throw new IllegalArgumentException(
                String.format("Value %d out of range [0, %d] for %d bytes", 
                    value, maxValue, bytes));
        }
        
        // 写入指定字节数
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = bytes - 1; i >= 0; i--) {
                writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                writeByte((int)((value >> (i * 8)) & 0xFF));
            }
        }
    }

    /**
     * 读取指定字节数的无符号整数
     */
    public long readUnsignedIntBytes(int bytes) {
        if (bytes < 1 || bytes > 8) {
            throw new IllegalArgumentException("bytes must be between 1 and 8");
        }
        
        long value = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < bytes; i++) {
                value = (value << 8) | (readByte() & 0xFF);
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                value |= ((long)(readByte() & 0xFF)) << (i * 8);
            }
        }
        
        return value;
    }

    // ========== 浮点数操作 ==========

    /**
     * 写入单精度浮点数
     */
    public BitBuffer writeFloat(float value) {
        int intBits = Float.floatToIntBits(value);
        return writeSignedInt(intBits);
    }

    /**
     * 读取单精度浮点数
     */
    public float readFloat() {
        int intBits = readSignedInt();
        return Float.intBitsToFloat(intBits);
    }

    /**
     * 写入双精度浮点数
     */
    public BitBuffer writeDouble(double value) {
        long longBits = Double.doubleToLongBits(value);
        return writeSignedLong(longBits);
    }

    /**
     * 读取双精度浮点数
     */
    public double readDouble() {
        long longBits = readSignedLong();
        return Double.longBitsToDouble(longBits);
    }

    // ========== 兼容性方法（保持向后兼容） ==========

    /**
     * 写入整数（兼容性方法）
     */
    public BitBuffer writeInt(int value) {
        return writeSignedInt(value);
    }

    /**
     * 读取整数（兼容性方法）
     */
    public int readInt() {
        return readSignedInt();
    }

    /**
     * 写入长整数（兼容性方法）
     */
    public BitBuffer writeLong(long value) {
        return writeSignedLong(value);
    }

    /**
     * 读取长整数（兼容性方法）
     */
    public long readLong() {
        return readSignedLong();
    }

    /**
     * 写入指定字节数的整数（兼容性方法）
     */
    public void writeIntBytes(long value, int bytes) {
        writeSignedIntBytes(value, bytes);
    }

    /**
     * 读取指定字节数的整数（兼容性方法）
     */
    public long readIntBytes(int bytes) {
        return readSignedIntBytes(bytes);
    }

    // ========== 复制和切片操作 ==========

    /**
     * 复制缓冲区
     */
    public BitBuffer copy() {
        BitBuffer copy = new BitBuffer(byteOrder);
        copy.buffer = Arrays.copyOf(this.buffer, this.buffer.length);
        copy.readBitPosition = this.readBitPosition;
        copy.writeBitPosition = this.writeBitPosition;
        copy.readBytePosition = this.readBytePosition;
        copy.writeBytePosition = this.writeBytePosition;
        copy.bitCount = this.bitCount;
        copy.byteCount = this.byteCount;
        return copy;
    }

    /**
     * 复制缓冲区的一部分
     */
    public BitBuffer copy(int index, int length) {
        BitBuffer copy = new BitBuffer(byteOrder);
        copy.buffer = Arrays.copyOfRange(this.buffer, index, index + length);
        copy.writeBytePosition = length;
        copy.byteCount = length;
        return copy;
    }

    /**
     * 创建切片
     */
    public BitBuffer slice() {
        return copy();
    }

    /**
     * 创建切片
     */
    public BitBuffer slice(int index, int length) {
        return copy(index, length);
    }

    /**
     * 复制
     */
    public BitBuffer duplicate() {
        return copy();
    }

    // ========== 实用方法 ==========

    /**
     * 获取可读字节数（兼容性方法）
     */
    public int readableBytes() {
        return getReadableBytes();
    }

    /**
     * 获取NIO缓冲区数量
     */
    public int nioBufferCount() {
        return 1;
    }

    /**
     * 将缓冲区内容转换为字节数组
     */
    public byte[] toByteArray() {
        // 如果有位数据，确保字节对齐
        if (writeBitPosition % 8 != 0) {
        while (writeBitPosition % 8 != 0) {
            writeBit(false);
            }
        }
        
        // 获取实际使用的字节数
        int actualByteCount = Math.max(byteCount, (writeBitPosition + 7) / 8);
        return Arrays.copyOf(buffer, actualByteCount);
    }

    /**
     * 确保当前写位位置对齐到字节边界
     */
    public void alignToByte() {
        // 如果已经是字节对齐的，不需要处理
        if (writeBitPosition % 8 == 0) {
            return;
        }
        
        // 计算需要补充的位数
        int bitsToAdd = 8 - (writeBitPosition % 8);
        
        // 补充0位
        for (int i = 0; i < bitsToAdd; i++) {
            writeBit(false);
        }
    }

    /**
     * 重置所有位置
     */
    public void reset() {
        readBitPosition = 0;
        writeBitPosition = 0;
        readBytePosition = 0;
        writeBytePosition = 0;
        bitCount = 0;
        byteCount = 0;
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        reset();
        Arrays.fill(buffer, (byte) 0);
    }

    /**
     * 演示BitBuffer的功能
     */
    public static void main(String[] args) {
        log.debug("=== BitBuffer 功能演示 ===");

        // 大端序测试
        log.debug("\n--- 大端序测试 ---");
        BitBuffer bigEndianBuffer = new BitBuffer(ByteOrder.BIG_ENDIAN);
        bigEndianBuffer.writeSignedInt(0x12345678);
        bigEndianBuffer.writeFloat(3.14f);
        bigEndianBuffer.writeDouble(2.718281828);
        bigEndianBuffer.reset();
        log.debug("大端序整数: 0x" + Integer.toHexString(bigEndianBuffer.readSignedInt()));
        log.debug("大端序浮点数: " + bigEndianBuffer.readFloat());
        log.debug("大端序双精度: " + bigEndianBuffer.readDouble());

        // 小端序测试
        log.debug("\n--- 小端序测试 ---");
        BitBuffer littleEndianBuffer = new BitBuffer(ByteOrder.LITTLE_ENDIAN);
        littleEndianBuffer.writeSignedInt(0x12345678);
        littleEndianBuffer.writeFloat(3.14f);
        littleEndianBuffer.writeDouble(2.718281828);
        littleEndianBuffer.reset();
        log.debug("小端序整数: 0x" + Integer.toHexString(littleEndianBuffer.readSignedInt()));
        log.debug("小端序浮点数: " + littleEndianBuffer.readFloat());
        log.debug("小端序双精度: " + littleEndianBuffer.readDouble());

        // 无符号整数测试
        log.debug("\n--- 无符号整数测试 ---");
        BitBuffer unsignedBuffer = new BitBuffer(ByteOrder.BIG_ENDIAN);
        unsignedBuffer.writeUnsignedByte(255);
        unsignedBuffer.writeUnsignedShort(65535);
        unsignedBuffer.writeUnsignedInt(0xFFFFFFFFL);
        unsignedBuffer.reset();
        log.debug("无符号字节: " + unsignedBuffer.readUnsignedByte());
        log.debug("无符号短整数: " + unsignedBuffer.readUnsignedShort());
        log.debug("无符号整数: " + unsignedBuffer.readUnsignedInt());

        // 任意字节数整数测试
        log.debug("\n--- 任意字节数整数测试 ---");
        BitBuffer varBuffer = new BitBuffer(ByteOrder.BIG_ENDIAN);
        varBuffer.writeSignedIntBytes(0x123456, 3);
        varBuffer.writeUnsignedIntBytes(0x789ABC, 3);
        varBuffer.reset();
        log.debug("3字节有符号: 0x" + Long.toHexString(varBuffer.readSignedIntBytes(3)));
        log.debug("3字节无符号: 0x" + Long.toHexString(varBuffer.readUnsignedIntBytes(3)));

        log.debug("\n=== 演示完成 ===");
    }
} 