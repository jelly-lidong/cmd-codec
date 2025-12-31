package com.iecas.cmd.util;

import cn.hutool.core.convert.Convert;
import io.netty.buffer.ByteBuf;

import java.util.Locale;

/**
 * 校验和计算工具类
 */
public class VerifyUtils {
    private VerifyUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 校验和,已验证
     */
    public static String checkSum(byte[] bytes) {
        byte sum = 0;
        for (byte aByte : bytes) {
            sum += aByte;
        }
        return Convert.toHex(new byte[]{sum});
    }

    public static String calCRC16(byte[] body) {
        short qx = (short) 0xffff;

        int len = body.length;

        int j = 0;

        while (len != 0) {
            byte t = body[j];

            qx ^= (short) (t << 8);

            for (int i = 0; i < 8; i++) {
                if ((qx & 0x8000) != 0) {
                    qx = (short) ((qx << 1) ^ 0x1021);
                } else {
                    qx = (short) (qx << 1);
                }
            }
            j++;
            len--;
        }
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (qx >> 8);
        bytes[1] = (byte) (qx & 0xff);
        return ByteUtil.bytesToHexString(bytes).toUpperCase(Locale.ROOT);
    }

    public static byte[] calCRC16ToBytes(byte[] body) {
        short qx = (short) 0xffff;

        int len = body.length;

        int j = 0;

        while (len != 0) {
            byte t = body[j];

            qx ^= (short) (t << 8);

            for (int i = 0; i < 8; i++) {
                if ((qx & 0x8000) != 0) {
                    qx = (short) ((qx << 1) ^ 0x1021);
                } else {
                    qx = (short) (qx << 1);
                }
            }
            j++;
            len--;
        }
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (qx >> 8);
        bytes[1] = (byte) (qx & 0xff);
        return  bytes;
    }

    /**
     * 计算 CRC32 校验和
     * @param buffer ByteBuf
     * @param offset 起始位置
     * @param length 长度
     * @return CRC32 校验和
     */
    public static long crc32(ByteBuf buffer, int offset, int length) {
        long crc = 0xFFFFFFFFL;
        for (int i = offset; i < offset + length; i++) {
            crc ^= buffer.getUnsignedByte(i);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x00000001L) != 0) {
                    crc = (crc >> 1) ^ 0xEDB88320L;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc ^ 0xFFFFFFFFL;
    }

    /**
     * 计算累加和
     * @param buffer ByteBuf
     * @param offset 起始位置
     * @param length 长度
     * @return 累加和
     */
    public static int sum(ByteBuf buffer, int offset, int length) {
        int sum = 0;
        for (int i = offset; i < offset + length; i++) {
            sum += buffer.getUnsignedByte(i);
        }
        return sum & 0xFF;
    }

    /**
     * 计算异或校验和
     * @param buffer ByteBuf
     * @param offset 起始位置
     * @param length 长度
     * @return 异或校验和
     */
    public static int xor(ByteBuf buffer, int offset, int length) {
        int xor = 0;
        for (int i = offset; i < offset + length; i++) {
            xor ^= buffer.getUnsignedByte(i);
        }
        return xor;
    }
} 