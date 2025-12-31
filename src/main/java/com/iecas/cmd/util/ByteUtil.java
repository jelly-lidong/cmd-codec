package com.iecas.cmd.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * 字节操作工具类
 * <p>
 * 提供字节数组与各种数据类型之间的转换功能，包括：
 * <ul>
 * <li>有符号/无符号整数与字节数组的相互转换</li>
 * <li>浮点数（float/double）与字节数组的相互转换</li>
 * <li>二进制字符串、十六进制字符串与字节数组的相互转换</li>
 * <li>BigInteger与字节数组的相互转换</li>
 * <li>字节数组的常用操作（截取、合并、反转、比较等）</li>
 * <li>位级别的精确读写操作（支持跨字节的位范围操作）</li>
 * </ul>
 * </p>
 * <p>
 * 本工具类支持大端序（Big-Endian）和小端序（Little-Endian）两种字节序模式。
 * 所有涉及多字节数据的方法都提供了字节序参数选项。
 * </p>
 *
 * @author IECAS
 * @version 1.0
 */
@Slf4j
public class ByteUtil {
    /**
     * 主方法（用于测试，实际使用时可以移除）
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {

    }

    /**
     * 提取字节数组的子数组
     *
     * @param bytes  原始字节数组
     * @param offset 起始偏移量
     * @param length 长度
     * @return 子数组
     */
    public static byte[] subByte(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException("偏移量或长度参数无效");
        }

        byte[] result = new byte[length];
        System.arraycopy(bytes, offset, result, 0, length);
        return result;
    }
    /**
     * 格式化十六进制字符串，将连续的十六进制字符转换为带空格分隔的格式
     * <p>
     * 例如："010203" -> "01 02 03 "
     * </p>
     *
     * @param hexCode 十六进制字符串（可以带0x前缀）
     * @return 格式化后的十六进制字符串，每个字节用空格分隔，字母大写
     * @throws IllegalArgumentException 如果十六进制字符串格式无效
     */
    public static String formatHex(String hexCode) {
        byte[] data = hexStringToBytes(hexCode);
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 将十六进制字符串补齐到指定字节长度
     * <p>
     * 如果输入字符串长度不足，在左侧补0。
     * 如果输入字符串长度超过指定长度，则返回原内容（不截断）。
     * 自动移除0x前缀（如果存在）。
     * </p>
     * <p>
     * 示例：
     * <ul>
     * <li>padHexString("AB", 2) -> "00AB"</li>
     * <li>padHexString("ABCD", 2) -> "ABCD"</li>
     * <li>padHexString("0xAB", 2) -> "00AB"</li>
     * </ul>
     * </p>
     *
     * @param hex        十六进制字符串（可以带0x前缀）
     * @param byteLength 目标字节长度（必须大于0）
     * @return 补齐后的十六进制字符串（不带前缀）
     * @throws IllegalArgumentException 如果hex为null或空，或byteLength<=0
     */
    public static String padHexString(String hex, int byteLength) {
        if (hex == null || hex.isEmpty()) {
            throw new IllegalArgumentException("十六进制字符串不能为空");
        }
        if (byteLength <= 0) {
            throw new IllegalArgumentException("字节长度必须大于0");
        }

        // 移除可能存在的0x前缀
        hex = hex.replaceFirst("^0x", "");

        // 计算目标十六进制字符串长度（每个字节对应2个十六进制字符）
        int targetLength = byteLength * 2;

        // 如果输入字符串长度超过目标长度，截取右侧部分
        if (hex.length() > targetLength) {
            return hex;
        }

        // 如果输入字符串长度不足，在左侧补0
        if (hex.length() < targetLength) {
            return String.format("%" + targetLength + "s", hex).replace(' ', '0');
        }

        return hex;
    }


    /**
     * 打印字节数组的十六进制表示到标准输出
     * <p>
     * 每行打印10个字节，格式为："XX XX XX ..."
     * 主要用于调试时查看字节数组内容。
     * </p>
     *
     * @param bytes 要打印的字节数组，如果为null或空则打印提示信息
     */
    public static void printBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            System.out.println("字节数组为空");
            return;
        }
        // 打印字节内容
        for (int i = 0; i < bytes.length; i += 10) {
            // 打印当前行的字节
            for (int j = 0; j < 10 && (i + j) < bytes.length; j++) {
                System.out.printf("%02X ", bytes[i + j]);
            }
            System.out.println();
        }
    }

    // ==================== 字节数组与有符号整数转换 ====================

    /**
     * 将有符号整数转换为字节数组（大端序）
     * <p>
     * 大端序（Big-Endian）：最高有效字节存储在最低地址处。
     * 例如：0x12345678 (4字节) -> [0x12, 0x34, 0x56, 0x78]
     * </p>
     * <p>
     * 注意：
     * <ul>
     * <li>如果value超出指定byteLength能表示的范围，不会自动截断，需要调用者确保值在有效范围内</li>
     * <li>负数会按照补码形式存储</li>
     * </ul>
     * </p>
     *
     * @param value      整数值（可以是有符号的负数）
     * @param byteLength 字节长度（1-8），超出此范围会抛出异常
     * @return 转换后的字节数组，长度为byteLength
     * @throws IllegalArgumentException 如果byteLength不在1-8范围内
     */
    public static byte[] signedIntToBytes(long value, int byteLength) {
        if (byteLength < 1 || byteLength > 8) {
            throw new IllegalArgumentException("字节长度必须在1-8之间");
        }

        byte[] result = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            result[byteLength - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
        return result;
    }

    /**
     * 将有符号整数转换为字节数组（指定字节序）
     * <p>
     * 支持大端序和小端序两种模式：
     * <ul>
     * <li>大端序（bigEndian=true）：最高有效字节在最低地址，如 0x1234 -> [0x12, 0x34]</li>
     * <li>小端序（bigEndian=false）：最低有效字节在最低地址，如 0x1234 -> [0x34, 0x12]</li>
     * </ul>
     * </p>
     *
     * @param value      整数值（可以是有符号的负数）
     * @param byteLength 字节长度（1-8），超出此范围会抛出异常
     * @param bigEndian  是否为大端序（true=大端序，false=小端序）
     * @return 转换后的字节数组，长度为byteLength
     * @throws IllegalArgumentException 如果byteLength不在1-8范围内
     */
    public static byte[] signedIntToBytes(long value, int byteLength, boolean bigEndian) {
        if (byteLength < 1 || byteLength > 8) {
            throw new IllegalArgumentException("字节长度必须在1-8之间");
        }

        byte[] result = new byte[byteLength];
        if (bigEndian) {
            for (int i = 0; i < byteLength; i++) {
                result[byteLength - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
            }
        } else {
            for (int i = 0; i < byteLength; i++) {
                result[i] = (byte) ((value >> (i * 8)) & 0xFF);
            }
        }
        return result;
    }

    /**
     * 将字节数组转换为有符号整数（大端序）
     * <p>
     * 按照大端序解析字节数组，并自动处理符号扩展。
     * 对于长度小于8字节的情况，如果最高位为1（负数），会进行符号扩展。
     * </p>
     * <p>
     * 特殊情况：
     * <ul>
     * <li>如果bytes为null或空，返回0L</li>
     * <li>如果bytes长度超过8字节，会抛出异常</li>
     * </ul>
     * </p>
     *
     * @param bytes 字节数组（长度不超过8字节）
     * @return 转换后的有符号整数值
     * @throws IllegalArgumentException 如果bytes长度超过8字节
     */
    public static long bytesToSignedInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0L;
        }
        if (bytes.length > 8) {
            throw new IllegalArgumentException("字节数组长度不能超过8字节");
        }

        long result = 0L;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }

        // 处理符号扩展
        int bitLength = bytes.length * 8;
        if (bitLength < 64 && (bytes[0] & 0x80) != 0) {
            // 负数，进行符号扩展
            long mask = (-1L) << bitLength;
            result |= mask;
        }

        return result;
    }

    /**
     * 将字节数组转换为有符号整数（指定字节序）
     * <p>
     * 支持大端序和小端序两种模式解析字节数组，并自动处理符号扩展。
     * 对于长度小于8字节的情况，根据字节序判断符号位所在位置，进行符号扩展。
     * </p>
     *
     * @param bytes     字节数组（长度不超过8字节）
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 转换后的有符号整数值
     * @throws IllegalArgumentException 如果bytes长度超过8字节
     */
    public static long bytesToSignedInt(byte[] bytes, boolean bigEndian) {
        if (bytes == null || bytes.length == 0) {
            return 0L;
        }
        if (bytes.length > 8) {
            throw new IllegalArgumentException("字节数组长度不能超过8字节");
        }

        long result = 0L;
        if (bigEndian) {
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
            // 处理符号扩展
            int bitLength = bytes.length * 8;
            if (bitLength < 64 && (bytes[0] & 0x80) != 0) {
                long mask = (-1L) << bitLength;
                result |= mask;
            }
        } else {
            for (int i = bytes.length - 1; i >= 0; i--) {
                result = (result << 8) | (bytes[i] & 0xFF);
            }
            // 处理符号扩展
            int bitLength = bytes.length * 8;
            if (bitLength < 64 && (bytes[bytes.length - 1] & 0x80) != 0) {
                long mask = (-1L) << bitLength;
                result |= mask;
            }
        }

        return result;
    }

    // ==================== 字节数组与无符号整数转换 ====================

    /**
     * 将无符号整数转换为字节数组（大端序）
     * <p>
     * 将无符号整数按照大端序转换为字节数组。
     * 会自动验证值是否在指定字节长度能表示的范围内。
     * </p>
     * <p>
     * 范围限制：
     * <ul>
     * <li>1字节：0 到 255 (0xFF)</li>
     * <li>2字节：0 到 65535 (0xFFFF)</li>
     * <li>4字节：0 到 4294967295 (0xFFFFFFFF)</li>
     * <li>8字节：0 到 Long.MAX_VALUE（所有非负数）</li>
     * </ul>
     * </p>
     *
     * @param value      无符号整数值（必须>=0）
     * @param byteLength 字节长度（1-8）
     * @return 转换后的字节数组，长度为byteLength
     * @throws IllegalArgumentException 如果value<0，或value超出byteLength能表示的范围，或byteLength不在1-8范围内
     */
    public static byte[] unsignedIntToBytes(long value, int byteLength) {
        if (byteLength < 1 || byteLength > 8) {
            throw new IllegalArgumentException("字节长度必须在1-8之间");
        }
        if (value < 0) {
            throw new IllegalArgumentException("无符号整数值不能为负数");
        }

        // 检查值是否超出指定字节长度的范围
        // 对于 8 字节，可以表示所有非负数（0 到 0xFFFFFFFFFFFFFFFFL）
        // 对于小于 8 字节的情况，计算最大值
        if (byteLength < 8) {
            long maxValue = (1L << (byteLength * 8)) - 1;
            if (value > maxValue) {
                throw new IllegalArgumentException("值超出" + byteLength + "字节无符号整数范围");
            }
        }
        // byteLength == 8 时，允许所有非负数

        byte[] result = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            result[byteLength - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
        return result;
    }

    /**
     * 将无符号整数转换为字节数组（指定字节序）
     * <p>
     * 支持大端序和小端序两种模式转换无符号整数。
     * 会自动验证值是否在指定字节长度能表示的范围内。
     * </p>
     *
     * @param value      无符号整数值（必须>=0）
     * @param byteLength 字节长度（1-8）
     * @param bigEndian  是否为大端序（true=大端序，false=小端序）
     * @return 转换后的字节数组，长度为byteLength
     * @throws IllegalArgumentException 如果value<0，或value超出byteLength能表示的范围，或byteLength不在1-8范围内
     */
    public static byte[] unsignedIntToBytes(long value, int byteLength, boolean bigEndian) {
        if (byteLength < 1 || byteLength > 8) {
            throw new IllegalArgumentException("字节长度必须在1-8之间");
        }
        if (value < 0) {
            throw new IllegalArgumentException("无符号整数值不能为负数");
        }

        // 检查值是否超出指定字节长度的范围
        // 对于 8 字节，可以表示所有非负数（0 到 0xFFFFFFFFFFFFFFFFL）
        // 对于小于 8 字节的情况，计算最大值
        if (byteLength < 8) {
            long maxValue = (1L << (byteLength * 8)) - 1;
            if (value > maxValue) {
                throw new IllegalArgumentException("值超出" + byteLength + "字节无符号整数范围");
            }
        }
        // byteLength == 8 时，允许所有非负数

        byte[] result = new byte[byteLength];
        if (bigEndian) {
            for (int i = 0; i < byteLength; i++) {
                result[byteLength - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
            }
        } else {
            for (int i = 0; i < byteLength; i++) {
                result[i] = (byte) ((value >> (i * 8)) & 0xFF);
            }
        }
        return result;
    }

    /**
     * 将字节数组转换为无符号整数（大端序）
     * <p>
     * 按照大端序解析字节数组，将所有位都视为数值位（无符号）。
     * 不会进行符号扩展，所有位都参与数值计算。
     * </p>
     * <p>
     * 特殊情况：
     * <ul>
     * <li>如果bytes为null或空，返回0L</li>
     * <li>如果bytes长度超过8字节，会抛出异常</li>
     * </ul>
     * </p>
     *
     * @param bytes 字节数组（长度不超过8字节）
     * @return 转换后的无符号整数值（返回值为long类型，但表示的是无符号值）
     * @throws IllegalArgumentException 如果bytes长度超过8字节
     */
    public static long bytesToUnsignedInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0L;
        }
        if (bytes.length > 8) {
            throw new IllegalArgumentException("字节数组长度不能超过8字节");
        }

        long result = 0L;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    /**
     * 将字节数组转换为无符号整数（指定字节序）
     * <p>
     * 支持大端序和小端序两种模式解析字节数组为无符号整数。
     * 不会进行符号扩展，所有位都参与数值计算。
     * </p>
     *
     * @param bytes     字节数组（长度不超过8字节）
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 转换后的无符号整数值（返回值为long类型，但表示的是无符号值）
     * @throws IllegalArgumentException 如果bytes长度超过8字节
     */
    public static long bytesToUnsignedInt(byte[] bytes, boolean bigEndian) {
        if (bytes == null || bytes.length == 0) {
            return 0L;
        }
        if (bytes.length > 8) {
            throw new IllegalArgumentException("字节数组长度不能超过8字节");
        }

        long result = 0L;
        if (bigEndian) {
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
        } else {
            for (int i = bytes.length - 1; i >= 0; i--) {
                result = (result << 8) | (bytes[i] & 0xFF);
            }
        }
        return result;
    }

    // ==================== 字节数组与二进制字符串转换 ====================

    /**
     * 将字节数组转换为二进制字符串
     * <p>
     * 将每个字节转换为8位二进制表示，按照字节顺序连接。
     * 结果字符串中不包含空格或分隔符。
     * </p>
     * <p>
     * 示例：[0x03] -> "00000011"
     * </p>
     *
     * @param bytes 字节数组，如果为null或空则返回空字符串
     * @return 二进制字符串，每个字节用8位二进制表示
     */
    public static String bytesToBinaryString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                sb.append((b & (1 << i)) != 0 ? '1' : '0');
            }
        }
        return sb.toString();
    }

    /**
     * 将二进制字符串转换为字节数组
     * <p>
     * 将二进制字符串（仅包含0和1）转换为字节数组。
     * 如果字符串长度不是8的倍数，会在左侧补0使其成为8的倍数。
     * </p>
     * <p>
     * 示例：
     * <ul>
     * <li>"00000011" -> [0x03]</li>
     * <li>"11" -> [0x03]（自动补齐为"00000011"）</li>
     * </ul>
     * </p>
     *
     * @param binaryString 二进制字符串（仅包含字符'0'和'1'）
     * @return 转换后的字节数组
     * @throws IllegalArgumentException 如果binaryString包含除'0'和'1'以外的字符
     */
    public static byte[] binaryStringToBytes(String binaryString) {
        if (binaryString == null || binaryString.isEmpty()) {
            return new byte[0];
        }

        // 验证二进制字符串
        if (!binaryString.matches("[01]+")) {
            throw new IllegalArgumentException("二进制字符串只能包含0和1");
        }

        // 补齐到8的倍数
        int padding = 8 - (binaryString.length() % 8);
        if (padding != 8) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                sb.append('0');
            }
            sb.append(binaryString);
            binaryString = sb.toString();
        }

        byte[] result = new byte[binaryString.length() / 8];
        for (int i = 0; i < result.length; i++) {
            String byteString = binaryString.substring(i * 8, (i + 1) * 8);
            result[i] = (byte) Integer.parseInt(byteString, 2);
        }
        return result;
    }

    // ==================== 字节数组与十六进制字符串转换 ====================

    /**
     * 将字节数组转换为十六进制字符串
     * <p>
     * 将每个字节转换为两位十六进制字符表示，按照字节顺序连接。
     * 可以控制大小写和是否添加0x前缀。
     * </p>
     * <p>
     * 示例：
     * <ul>
     * <li>bytesToHexString([0x01, 0x02, 0xFF], true, false) -> "0102FF"</li>
     * <li>bytesToHexString([0x01, 0x02], false, true) -> "0x0102"</li>
     * </ul>
     * </p>
     *
     * @param bytes     字节数组，如果为null或空则返回空字符串或"0x"
     * @param uppercase 是否使用大写字母（true=大写，false=小写）
     * @param prefix    是否添加0x前缀（当前实现中prefix参数暂未生效）
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes, boolean uppercase, boolean prefix) {
        if (bytes == null || bytes.length == 0) {
            return prefix ? "0x" : "";
        }

        StringBuilder sb = new StringBuilder();
//        if (prefix) {
//            sb.append("0x");
//        }

        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(uppercase ? hex.toUpperCase() : hex.toLowerCase());
        }

        return sb.toString();
    }

    /**
     * 将字节数组转换为十六进制字符串（默认大写，带0x前缀）
     * <p>
     * 便捷方法，调用bytesToHexString(bytes, true, true)。
     * 注意：当前实现中0x前缀功能暂未启用。
     * </p>
     *
     * @param bytes 字节数组
     * @return 十六进制字符串（大写，不带前缀）
     */
    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, true, true);
    }

    /**
     * 将十六进制字符串转换为字节数组
     * <p>
     * 将十六进制字符串（可以带0x或0X前缀）转换为字节数组。
     * 如果字符串长度为奇数，会在左侧补0使其成为偶数。
     * </p>
     * <p>
     * 示例：
     * <ul>
     * <li>hexStringToBytes("0102FF") -> [0x01, 0x02, 0xFF]</li>
     * <li>hexStringToBytes("0x0102") -> [0x01, 0x02]</li>
     * <li>hexStringToBytes("ABC") -> [0x0A, 0xBC]（自动补齐）</li>
     * </ul>
     * </p>
     *
     * @param hexString 十六进制字符串（可以带0x或0X前缀，不区分大小写）
     * @return 转换后的字节数组
     * @throws IllegalArgumentException 如果hexString包含无效的十六进制字符
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }

        // 移除可能存在的0x前缀
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }

        if (hexString.isEmpty()) {
            return new byte[0];
        }

        // 验证十六进制字符串
        if (!hexString.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException("无效的十六进制字符串: " + hexString);
        }

        // 确保字符串长度为偶数
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int index = i * 2;
            int val = Integer.parseInt(hexString.substring(index, index + 2), 16);
            result[i] = (byte) val;
        }

        return result;
    }

    // ==================== 字节数组与浮点数转换 ====================

    /**
     * 将float转换为字节数组（IEEE 754格式，大端序）
     * <p>
     * 按照IEEE 754单精度浮点数标准（32位）将float值转换为字节数组。
     * 使用大端序（Big-Endian）排列字节。
     * </p>
     *
     * @param value float值
     * @return 4字节数组，按照IEEE 754格式存储
     */
    public static byte[] floatToBytes(float value) {
        return floatToBytes(value, true);
    }

    /**
     * 将float转换为字节数组（IEEE 754格式，指定字节序）
     * <p>
     * 按照IEEE 754单精度浮点数标准（32位）将float值转换为字节数组。
     * 支持大端序和小端序两种字节序。
     * </p>
     *
     * @param value     float值
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 4字节数组，按照IEEE 754格式存储
     */
    public static byte[] floatToBytes(float value, boolean bigEndian) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        return buffer.array();
    }



    /**
     * 将字节数组转换为float（IEEE 754格式，大端序）
     * <p>
     * 按照IEEE 754单精度浮点数标准（32位）从字节数组解析float值。
     * 使用大端序（Big-Endian）解析字节。
     * </p>
     *
     * @param bytes 4字节数组，必须恰好为4字节
     * @return 解析后的float值
     * @throws IllegalArgumentException 如果bytes为null或长度不等于4字节
     */
    public static float bytesToFloat(byte[] bytes) {
        return bytesToFloat(bytes, true);
    }

    /**
     * 将字节数组转换为float（IEEE 754格式，指定字节序）
     * <p>
     * 按照IEEE 754单精度浮点数标准（32位）从字节数组解析float值。
     * 支持大端序和小端序两种字节序。
     * </p>
     *
     * @param bytes     4字节数组，必须恰好为4字节
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 解析后的float值
     * @throws IllegalArgumentException 如果bytes为null或长度不等于4字节
     */
    public static float bytesToFloat(byte[] bytes, boolean bigEndian) {
        if (bytes == null || bytes.length != 4) {
            throw new IllegalArgumentException("float转换需要4字节数组");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }

    /**
     * 将double转换为字节数组（IEEE 754格式，大端序）
     * <p>
     * 按照IEEE 754双精度浮点数标准（64位）将double值转换为字节数组。
     * 使用大端序（Big-Endian）排列字节。
     * </p>
     *
     * @param value double值
     * @return 8字节数组，按照IEEE 754格式存储
     */
    public static byte[] doubleToBytes(double value) {
        return doubleToBytes(value, true);
    }

    /**
     * 将double转换为字节数组（IEEE 754格式，指定字节序）
     * <p>
     * 按照IEEE 754双精度浮点数标准（64位）将double值转换为字节数组。
     * 支持大端序和小端序两种字节序。
     * </p>
     *
     * @param value     double值
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 8字节数组，按照IEEE 754格式存储
     */
    public static byte[] doubleToBytes(double value, boolean bigEndian) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(value);
        return buffer.array();
    }

    /**
     * 将字节数组转换为double（IEEE 754格式，大端序）
     * <p>
     * 按照IEEE 754双精度浮点数标准（64位）从字节数组解析double值。
     * 使用大端序（Big-Endian）解析字节。
     * </p>
     *
     * @param bytes 8字节数组，必须恰好为8字节
     * @return 解析后的double值
     * @throws IllegalArgumentException 如果bytes为null或长度不等于8字节
     */
    public static double bytesToDouble(byte[] bytes) {
        return bytesToDouble(bytes, true);
    }

    /**
     * 将字节数组转换为double（IEEE 754格式，指定字节序）
     * <p>
     * 按照IEEE 754双精度浮点数标准（64位）从字节数组解析double值。
     * 支持大端序和小端序两种字节序。
     * </p>
     *
     * @param bytes     8字节数组，必须恰好为8字节
     * @param bigEndian 是否为大端序（true=大端序，false=小端序）
     * @return 解析后的double值
     * @throws IllegalArgumentException 如果bytes为null或长度不等于8字节
     */
    public static double bytesToDouble(byte[] bytes, boolean bigEndian) {
        if (bytes == null || bytes.length != 8) {
            throw new IllegalArgumentException("double转换需要8字节数组");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        return buffer.getDouble();
    }

    // ==================== 大整数支持 ====================

    /**
     * 将任意长度的有符号整数转换为字节数组（大端序）
     * <p>
     * 将BigInteger值转换为指定长度的字节数组，支持任意长度的整数。
     * 如果值占用字节数小于目标长度，会进行符号扩展（负数填充0xFF，正数填充0x00）。
     * 如果值占用字节数大于目标长度，会截取低位字节。
     * </p>
     *
     * @param value      BigInteger值（不能为null）
     * @param byteLength 目标字节长度（必须大于0）
     * @return 转换后的字节数组，长度为byteLength
     * @throws IllegalArgumentException 如果value为null或byteLength<=0
     */
    public static byte[] bigIntegerToBytes(BigInteger value, int byteLength) {
        if (value == null) {
            throw new IllegalArgumentException("BigInteger值不能为null");
        }
        if (byteLength < 1) {
            throw new IllegalArgumentException("字节长度必须为正数");
        }

        byte[] valueBytes = value.toByteArray();
        byte[] result = new byte[byteLength];

        if (valueBytes.length <= byteLength) {
            // 复制数据，处理符号扩展
            int offset = byteLength - valueBytes.length;
            System.arraycopy(valueBytes, 0, result, offset, valueBytes.length);

            // 如果是负数，需要填充高位为0xFF
            if (value.signum() < 0) {
                for (int i = 0; i < offset; i++) {
                    result[i] = (byte) 0xFF;
                }
            }
        } else {
            // 截取低位字节
            int offset = valueBytes.length - byteLength;
            System.arraycopy(valueBytes, offset, result, 0, byteLength);
        }

        return result;
    }

    /**
     * 将字节数组转换为BigInteger（大端序）
     * <p>
     * 按照大端序将字节数组转换为BigInteger值。
     * BigInteger可以表示任意长度的整数，包括正数和负数（按补码解释）。
     * </p>
     *
     * @param bytes 字节数组，如果为null或空则返回BigInteger.ZERO
     * @return 转换后的BigInteger值
     */
    public static BigInteger bytesToBigInteger(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(bytes);
    }

    // ==================== 工具方法 ====================

    /**
     * 反转字节数组
     * <p>
     * 将字节数组中的字节顺序反转，返回新的字节数组。
     * 原数组不会被修改。
     * </p>
     * <p>
     * 示例：[0x01, 0x02, 0x03, 0x04] -> [0x04, 0x03, 0x02, 0x01]
     * </p>
     *
     * @param bytes 原始字节数组，如果为null则返回null，如果长度<=1则返回原数组的副本
     * @return 反转后的字节数组（新数组，不修改原数组）
     */
    public static byte[] reverseBytes(byte[] bytes) {
        if (bytes == null || bytes.length <= 1) {
            return bytes == null ? null : bytes.clone();
        }

        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[bytes.length - 1 - i];
        }
        return result;
    }

    /**
     * 连接多个字节数组
     * <p>
     * 将多个字节数组按顺序连接成一个新的字节数组。
     * null数组会被跳过（不参与连接）。
     * </p>
     * <p>
     * 示例：concatBytes([0x01, 0x02], [0x03, 0x04], null) -> [0x01, 0x02, 0x03, 0x04]
     * </p>
     *
     * @param arrays 字节数组列表（可变参数），如果为null或空则返回空数组
     * @return 连接后的字节数组（新数组）
     */
    public static byte[] concatBytes(byte[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return new byte[0];
        }

        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }

        return result;
    }

    /**
     * 提取字节数组的子数组
     * <p>
     * 从指定位置开始提取指定长度的子数组，返回新的字节数组。
     * 此方法与subByte方法功能完全相同。
     * </p>
     *
     * @param bytes  原始字节数组（不能为null）
     * @param offset 起始偏移量（从0开始，必须>=0）
     * @param length 要提取的长度（必须>=0，且offset+length不能超过bytes.length）
     * @return 提取的子数组（新数组），长度为length
     * @throws IllegalArgumentException 如果bytes为null，或offset<0，或length<0，或offset+length>bytes.length
     */
    public static byte[] subBytes(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException("偏移量或长度参数无效");
        }

        byte[] result = new byte[length];
        System.arraycopy(bytes, offset, result, 0, length);
        return result;
    }

    /**
     * 比较两个字节数组是否相等
     * <p>
     * 逐字节比较两个字节数组的内容是否完全相同。
     * 如果两个引用指向同一个数组，直接返回true。
     * </p>
     *
     * @param bytes1 第一个字节数组
     * @param bytes2 第二个字节数组
     * @return 如果两个数组长度相同且所有字节都相等则返回true，否则返回false
     *         如果两个都为null则返回true
     */
    public static boolean equals(byte[] bytes1, byte[] bytes2) {
        if (bytes1 == bytes2) {
            return true;
        }
        if (bytes1 == null || bytes2 == null) {
            return false;
        }
        if (bytes1.length != bytes2.length) {
            return false;
        }

        for (int i = 0; i < bytes1.length; i++) {
            if (bytes1[i] != bytes2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将字节数组转换为可读的字符串表示
     * <p>
     * 将字节数组格式化为类似于 "[0x01, 0x02, 0xFF]" 的字符串形式，
     * 便于调试和日志输出。
     * </p>
     *
     * @param bytes 字节数组
     * @return 字符串表示，格式为 "[0xXX, 0xXX, ...]"
     *         如果bytes为null则返回"null"
     *         如果bytes为空则返回"[]"
     */
    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        if (bytes.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("0x%02X", bytes[i] & 0xFF));
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== 值比较方法 ====================

    /**
     * 比较两个十六进制值是否相等（忽略前缀和前导零）
     * 支持的前缀格式：0x、0X、H后缀
     *
     * @param original 原始值（可以是String、Number或byte[]）
     * @param decoded  解码值（可以是String、Number或byte[]）
     * @return 如果两个值相等返回true，否则返回false
     */
    public static boolean compareHexValues(Object original, Object decoded) {
        if (original == null && decoded == null) {
            return true;
        }
        if (original == null || decoded == null) {
            return false;
        }

        String originalStr = normalizeHexString(convertToHexString(original));
        String decodedStr = normalizeHexString(convertToHexString(decoded));

        return originalStr.equalsIgnoreCase(decodedStr);
    }

    /**
     * 将对象转换为十六进制字符串
     *
     * @param value 要转换的值
     * @return 十六进制字符串
     */
    private static String convertToHexString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return Long.toHexString(((Number) value).longValue()).toUpperCase();
        } else if (value instanceof byte[]) {
            return bytesToHexString((byte[]) value, true, false);
        }
        return value.toString();
    }

    /**
     * 标准化十六进制字符串，移除前缀和前导零
     *
     * @param hexStr 十六进制字符串
     * @return 标准化后的字符串
     */
    private static String normalizeHexString(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) {
            return "0";
        }

        // 移除各种前缀和后缀
        String normalized = hexStr.trim()
                .replace("0x", "")
                .replace("0X", "")
                .replace("H", "")
                .replace("h", "");

        // 移除前导零，但至少保留一个字符
        normalized = normalized.replaceFirst("^0+", "");
        if (normalized.isEmpty()) {
            normalized = "0";
        }

        return normalized;
    }

    /**
     * 合并两个字节数组
     * <p>
     * 将两个字节数组按顺序连接成一个新的字节数组。
     * 此方法与concatBytes(array1, array2)功能相同，但仅支持两个参数。
     * </p>
     *
     * @param array1 第一个字节数组（不能为null）
     * @param array2 第二个字节数组（不能为null）
     * @return 合并后的字节数组（新数组），长度为array1.length + array2.length
     */
    public static byte[] mergeByteArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;

    }

    /**
     * 生成指定长度的随机字节数组
     * <p>
     * 使用Random类生成指定长度的随机字节数组。
     * 每次调用都会生成不同的随机值。
     * </p>
     *
     * @param length 要生成的字节数组长度（必须>0）
     * @return 随机生成的字节数组
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return bytes;
    }

    // ==================== 位范围修改方法 ====================

    /**
     * 设置字节数组中指定位范围的值（有符号整数，大端序）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要设置的位数）
     * @param value     要设置的有符号整数值
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, long value) {
        setBitRange(bytes, startBit, bitLength, value, true, true);
    }

    /**
     * 设置字节数组中指定位范围的值（有符号/无符号整数，指定字节序）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要设置的位数）
     * @param value     要设置的整数值
     * @param signed    是否为有符号整数
     * @param bigEndian 是否为大端序
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, long value, boolean signed, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0 || bitLength <= 0) {
            throw new IllegalArgumentException("起始位必须>=0，位长度必须>0");
        }
        int endBit = startBit + bitLength - 1;
        if (endBit >= bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围");
        }
        if (bitLength > 64) {
            throw new IllegalArgumentException("位范围不能超过64位");
        }

        // 验证值是否在范围内
        if (signed) {
            long maxValue = (1L << (bitLength - 1)) - 1;
            long minValue = -(1L << (bitLength - 1));
            if (value < minValue || value > maxValue) {
                throw new IllegalArgumentException(String.format(
                    "有符号整数值 %d 超出 %d 位范围 [%d, %d]", value, bitLength, minValue, maxValue));
            }
        } else {
            if (value < 0) {
                throw new IllegalArgumentException("无符号整数值不能为负数");
            }
            long maxValue = (1L << bitLength) - 1;
            if (value > maxValue) {
                throw new IllegalArgumentException(String.format(
                    "无符号整数值 %d 超出 %d 位范围 [0, %d]", value, bitLength, maxValue));
            }
        }

        // 将值转换为字节数组
        byte[] valueBytes;
        if (signed) {
            valueBytes = signedIntToBytes(value, (bitLength + 7) / 8, bigEndian);
        } else {
            valueBytes = unsignedIntToBytes(value, (bitLength + 7) / 8, bigEndian);
        }

        // 设置位范围
        setBitRangeFromBytes(bytes, startBit, bitLength, valueBytes, bigEndian);
    }

    /**
     * 设置字节数组中指定位范围的值（二进制字符串）
     *
     * @param bytes        目标字节数组
     * @param startBit     起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength    位长度（要设置的位数）
     * @param binaryString 二进制字符串（可以带0b前缀）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, String binaryString) {
        setBitRange(bytes, startBit, bitLength, binaryString, true);
    }

    /**
     * 设置字节数组中指定位范围的值（二进制字符串，指定字节序）
     *
     * @param bytes        目标字节数组
     * @param startBit     起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength    位长度（要设置的位数）
     * @param binaryString 二进制字符串（可以带0b前缀）
     * @param bigEndian    是否为大端序
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, String binaryString, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (binaryString == null || binaryString.isEmpty()) {
            throw new IllegalArgumentException("二进制字符串不能为空");
        }

        // 移除0b前缀
        String bin = binaryString.trim().replaceFirst("^0[bB]", "");

        // 验证二进制字符串
        if (!bin.matches("[01]+")) {
            throw new IllegalArgumentException("二进制字符串只能包含0和1");
        }

        if (bin.length() > bitLength) {
            // 截取右侧部分（低位）
            bin = bin.substring(bin.length() - bitLength);
        } else if (bin.length() < bitLength) {
            // 左侧补0
            bin = String.format("%" + bitLength + "s", bin).replace(' ', '0');
        }

        // 将二进制字符串转换为字节数组
        byte[] valueBytes = binaryStringToBytes(bin);
        setBitRangeFromBytes(bytes, startBit, bitLength, valueBytes, bigEndian);
    }

    /**
     * 设置字节数组中指定位范围的值（十六进制字符串）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要设置的位数）
     * @param hexString 十六进制字符串（可以带0x前缀）
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, String hexString, boolean isHex, boolean bigEndian) {
        if (!isHex) {
            throw new IllegalArgumentException("此方法仅支持十六进制字符串，请使用其他重载方法");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (hexString == null || hexString.isEmpty()) {
            throw new IllegalArgumentException("十六进制字符串不能为空");
        }
        int requiredBytes = (bitLength + 7) / 8;

        // 将十六进制字符串转换为字节数组
        byte[] valueBytes = hexStringToBytes(hexString);

        // 检查长度
        if (valueBytes.length > requiredBytes) {
            // 如果提供的字节数超过需要的，截取右侧部分
            byte[] trimmed = new byte[requiredBytes];
            System.arraycopy(valueBytes, valueBytes.length - requiredBytes, trimmed, 0, requiredBytes);
            valueBytes = trimmed;
        } else if (valueBytes.length < requiredBytes) {
            // 如果提供的字节数不足，左侧补0
            byte[] padded = new byte[requiredBytes];
            System.arraycopy(valueBytes, 0, padded, requiredBytes - valueBytes.length, valueBytes.length);
            valueBytes = padded;
        }

        setBitRangeFromBytes(bytes, startBit, bitLength, valueBytes, bigEndian);
    }

    /**
     * 设置字节数组中指定位范围的值（单精度浮点数）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（必须为32）
     * @param value     要设置的单精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或位长度不等于32位
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, float value) {
        setBitRange(bytes, startBit, bitLength, value, true);
    }

    /**
     * 设置字节数组中指定位范围的值（单精度浮点数，指定字节序）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（必须为32）
     * @param value     要设置的单精度浮点数值
     * @param bigEndian 是否为大端序
     * @throws IllegalArgumentException 如果参数无效或位长度不等于32位
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, float value, boolean bigEndian) {
        if (bitLength != 32) {
            throw new IllegalArgumentException("单精度浮点数必须占用32位");
        }

        byte[] valueBytes = floatToBytes(value, bigEndian);
        setBitRangeFromBytes(bytes, startBit, bitLength, valueBytes, bigEndian);
    }

    /**
     * 设置字节数组中指定位范围的值（双精度浮点数）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（必须为64）
     * @param value     要设置的双精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或位长度不等于64位
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, double value) {
        setBitRange(bytes, startBit, bitLength, value, true);
    }

    /**
     * 设置字节数组中指定位范围的值（双精度浮点数，指定字节序）
     *
     * @param bytes     目标字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（必须为64）
     * @param value     要设置的双精度浮点数值
     * @param bigEndian 是否为大端序
     * @throws IllegalArgumentException 如果参数无效或位长度不等于64位
     */
    public static void setBitRange(byte[] bytes, int startBit, int bitLength, double value, boolean bigEndian) {
        if (bitLength != 64) {
            throw new IllegalArgumentException("双精度浮点数必须占用64位");
        }

        byte[] valueBytes = doubleToBytes(value, bigEndian);
        setBitRangeFromBytes(bytes, startBit, bitLength, valueBytes, bigEndian);
    }

    /**
     * 核心方法：从字节数组中设置指定位范围的值
     *
     * @param bytes      目标字节数组
     * @param startBit   起始位位置
     * @param bitLength  要设置的位数
     * @param valueBytes 要设置的值的字节数组
     * @param bigEndian  是否为大端序
     */
    private static void setBitRangeFromBytes(byte[] bytes, int startBit, int bitLength, byte[] valueBytes, boolean bigEndian) {
        int endBit = startBit + bitLength - 1;
        // 计算起始和结束字节索引
        int startByte = startBit / 8;
        int endByte = endBit / 8;
        int startBitInByte = startBit % 8;
        int endBitInByte = endBit % 8;

        // 优化：如果从字节边界开始且覆盖完整字节，直接复制字节数组
        if (startBitInByte == 0 && endBitInByte == 7 && valueBytes.length == (endByte - startByte + 1)) {
            // 直接复制字节数组（保持字节序）
            System.arraycopy(valueBytes, 0, bytes, startByte, valueBytes.length);
            return;
        }

        // 如果跨多个字节，需要逐个字节处理
        if (startByte == endByte) {
            // 在同一字节内
            setBitsInByte(bytes, startByte, startBitInByte, endBitInByte, valueBytes, 0, bitLength, bigEndian);
        } else {
            // 跨多个字节
            int bitOffset = 0;
            for (int byteIdx = startByte; byteIdx <= endByte; byteIdx++) {
                int currentStartBit = (byteIdx == startByte) ? startBitInByte : 0;
                int currentEndBit = (byteIdx == endByte) ? endBitInByte : 7;
                int currentBitLength = currentEndBit - currentStartBit + 1;

                setBitsInByte(bytes, byteIdx, currentStartBit, currentEndBit, valueBytes, bitOffset, currentBitLength, bigEndian);

                bitOffset += currentBitLength;
            }
        }
    }

    /**
     * 在单个字节内设置指定位范围的值
     * startBit和endBit是字节内的位位置（0-7，0表示最高位）
     */
    private static void setBitsInByte(byte[] bytes, int byteIdx, int startBit, int endBit, byte[] valueBytes, int bitOffset, int bitLength, boolean bigEndian) {
        // 创建掩码清除目标位范围（startBit到endBit）
        int mask = 0xFF;
        for (int i = startBit; i <= endBit; i++) {
            mask &= ~(1 << (7 - i)); // 清除目标位（位0是最高位）
        }

        // 清除目标位范围
        bytes[byteIdx] = (byte) (bytes[byteIdx] & mask);

        // 从valueBytes中提取要设置的位值
        int valueBits = extractBits(valueBytes, bitOffset, bitLength, bigEndian);

        // 将值对齐到目标位置并设置
        // startBit是最高位位置，endBit是最低位位置
        int shift = 7 - endBit; // 需要右移的位数
        bytes[byteIdx] = (byte) (bytes[byteIdx] | (valueBits << shift));
    }

    /**
     * 从字节数组中提取指定位范围的值
     * 
     * @param valueBytes 源字节数组
     * @param bitOffset  起始位偏移（从0开始）
     * @param bitLength  要提取的位数
     * @param bigEndian  是否为大端序
     * @return 提取的位值
     */
    private static int extractBits(byte[] valueBytes, int bitOffset, int bitLength, boolean bigEndian) {
        if (bitLength > 32) {
            throw new IllegalArgumentException("提取的位数不能超过32位");
        }

        int result = 0;
        for (int i = 0; i < bitLength; i++) {
            int globalBitPos = bitOffset + i;
            int byteIdx = bigEndian ? (globalBitPos / 8) : ((valueBytes.length - 1) - (globalBitPos / 8));
            int bitInByte = globalBitPos % 8;
            
            // 在字节内，bit 0是最高位，bit 7是最低位
            int bitPosInByte = bigEndian ? bitInByte : (7 - bitInByte);
            int bit = (valueBytes[byteIdx] >> (7 - bitPosInByte)) & 1;
            
            result = (result << 1) | bit;
        }
        
        return result;
    }

    // ==================== 位范围读取方法 ====================

    /**
     * 从字节数组中读取指定位范围的值（有符号整数，大端序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @return 读取的有符号整数值
     * @throws IllegalArgumentException 如果参数无效
     */
    public static long getBitRange(byte[] bytes, int startBit, int bitLength) {
        return getBitRange(bytes, startBit, bitLength, true, true);
    }

    /**
     * 从字节数组中读取指定位范围的值（有符号/无符号整数，指定字节序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @param signed    是否为有符号整数
     * @param bigEndian 是否为大端序
     * @return 读取的整数值
     * @throws IllegalArgumentException 如果参数无效
     */
    public static long getBitRange(byte[] bytes, int startBit, int bitLength, boolean signed, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0 || bitLength <= 0) {
            throw new IllegalArgumentException("起始位必须>=0，位长度必须>0");
        }
        int endBit = startBit + bitLength - 1;
        if (endBit >= bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围");
        }
        if (bitLength > 64) {
            throw new IllegalArgumentException("位范围不能超过64位");
        }

        // 提取指定位范围的字节数组
        byte[] extractedBytes = getBitRangeFromBytes(bytes, startBit, bitLength, bigEndian);

        // 转换为整数
        if (signed) {
            return bytesToSignedInt(extractedBytes, bigEndian);
        } else {
            return bytesToUnsignedInt(extractedBytes, bigEndian);
        }
    }

    /**
     * 从字节数组中读取指定位范围的值（二进制字符串）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @return 二进制字符串
     * @throws IllegalArgumentException 如果参数无效
     */
    public static String getBitRangeAsBinaryString(byte[] bytes, int startBit, int bitLength) {
        return getBitRangeAsBinaryString(bytes, startBit, bitLength, true);
    }

    /**
     * 从字节数组中读取指定位范围的值（二进制字符串，指定字节序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @param bigEndian 是否为大端序
     * @return 二进制字符串
     * @throws IllegalArgumentException 如果参数无效
     */
    public static String getBitRangeAsBinaryString(byte[] bytes, int startBit, int bitLength, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0 || bitLength <= 0) {
            throw new IllegalArgumentException("起始位必须>=0，位长度必须>0");
        }
        int endBit = startBit + bitLength - 1;
        if (endBit >= bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitLength; i++) {
            int globalBitPos = startBit + i;
            int byteIdx = globalBitPos / 8;
            int bitInByte = globalBitPos % 8;
            
            // 在字节内，bit 0是最高位，bit 7是最低位
            int bitPosInByte = bigEndian ? bitInByte : (7 - bitInByte);
            int bit = (bytes[byteIdx] >> (7 - bitPosInByte)) & 1;
            sb.append(bit);
        }
        
        return sb.toString();
    }

    /**
     * 从字节数组中读取指定位范围的值（十六进制字符串）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @return 十六进制字符串
     * @throws IllegalArgumentException 如果参数无效
     */
    public static String getBitRangeAsHexString(byte[] bytes, int startBit, int bitLength) {
        return getBitRangeAsHexString(bytes, startBit, bitLength, true);
    }

    /**
     * 从字节数组中读取指定位范围的值（十六进制字符串，指定字节序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     * @param bitLength 位长度（要读取的位数）
     * @param bigEndian 是否为大端序
     * @return 十六进制字符串
     * @throws IllegalArgumentException 如果参数无效
     */
    public static String getBitRangeAsHexString(byte[] bytes, int startBit, int bitLength, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0 || bitLength <= 0) {
            throw new IllegalArgumentException("起始位必须>=0，位长度必须>0");
        }
        int endBit = startBit + bitLength - 1;
        if (endBit >= bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围");
        }

        // 提取指定位范围的字节数组
        byte[] extractedBytes = getBitRangeFromBytes(bytes, startBit, bitLength, bigEndian);
        
        return bytesToHexString(extractedBytes, true, false);
    }

    /**
     * 从字节数组中读取指定位范围的值（单精度浮点数）
     *
     * @param bytes    源字节数组
     * @param startBit 起始位位置（从0开始，0表示数组第一个字节的最高位）
     *                 注意：必须从字节边界开始，读取完整的32位
     * @return 读取的单精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或起始位不在字节边界
     */
    public static float getBitRangeAsFloat(byte[] bytes, int startBit) {
        return getBitRangeAsFloat(bytes, startBit, true);
    }

    /**
     * 从字节数组中读取指定位范围的值（单精度浮点数，指定字节序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     *                  注意：必须从字节边界开始，读取完整的32位
     * @param bigEndian 是否为大端序
     * @return 读取的单精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或起始位不在字节边界
     */
    public static float getBitRangeAsFloat(byte[] bytes, int startBit, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0) {
            throw new IllegalArgumentException("起始位必须>=0");
        }
        if (startBit % 8 != 0) {
            throw new IllegalArgumentException("浮点数读取必须从字节边界开始");
        }
        if (startBit + 32 > bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围，需要32位");
        }

        // 提取32位（4字节）
        byte[] extractedBytes = getBitRangeFromBytes(bytes, startBit, 32, bigEndian);
        return bytesToFloat(extractedBytes, bigEndian);
    }

    /**
     * 从字节数组中读取指定位范围的值（双精度浮点数）
     *
     * @param bytes    源字节数组
     * @param startBit 起始位位置（从0开始，0表示数组第一个字节的最高位）
     *                 注意：必须从字节边界开始，读取完整的64位
     * @return 读取的双精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或起始位不在字节边界
     */
    public static double getBitRangeAsDouble(byte[] bytes, int startBit) {
        return getBitRangeAsDouble(bytes, startBit, true);
    }

    /**
     * 从字节数组中读取指定位范围的值（双精度浮点数，指定字节序）
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置（从0开始，0表示数组第一个字节的最高位）
     *                  注意：必须从字节边界开始，读取完整的64位
     * @param bigEndian 是否为大端序
     * @return 读取的双精度浮点数值
     * @throws IllegalArgumentException 如果参数无效或起始位不在字节边界
     */
    public static double getBitRangeAsDouble(byte[] bytes, int startBit, boolean bigEndian) {
        if (bytes == null) {
            throw new IllegalArgumentException("字节数组不能为null");
        }
        if (startBit < 0) {
            throw new IllegalArgumentException("起始位必须>=0");
        }
        if (startBit % 8 != 0) {
            throw new IllegalArgumentException("浮点数读取必须从字节边界开始");
        }
        if (startBit + 64 > bytes.length * 8) {
            throw new IllegalArgumentException("位范围超出字节数组范围，需要64位");
        }

        // 提取64位（8字节）
        byte[] extractedBytes = getBitRangeFromBytes(bytes, startBit, 64, bigEndian);
        return bytesToDouble(extractedBytes, bigEndian);
    }

    /**
     * 核心方法：从字节数组中提取指定位范围的字节数组
     *
     * @param bytes     源字节数组
     * @param startBit  起始位位置
     * @param bitLength 要读取的位数
     * @param bigEndian 是否为大端序
     * @return 提取的字节数组（根据字节序排列）
     */
    private static byte[] getBitRangeFromBytes(byte[] bytes, int startBit, int bitLength, boolean bigEndian) {
        int endBit = startBit + bitLength - 1;
        int startByte = startBit / 8;
        int endByte = endBit / 8;
        int startBitInByte = startBit % 8;
        int endBitInByte = endBit % 8;

        int requiredBytes = (bitLength + 7) / 8;
        byte[] result = new byte[requiredBytes];

        // 优化：如果从字节边界开始且覆盖完整字节，直接复制字节数组
        if (startBitInByte == 0 && endBitInByte == 7 && requiredBytes == (endByte - startByte + 1)) {
            System.arraycopy(bytes, startByte, result, 0, requiredBytes);
            // 如果需要反转字节序，则反转结果
            if (!bigEndian && requiredBytes > 1) {
                reverseBytesInPlace(result);
            }
            return result;
        }

        // 需要按位提取：先提取到一个临时字节数组（按源顺序）
        byte[] tempBytes = new byte[requiredBytes];
        int bitOffset = 0;
        
        for (int i = 0; i < bitLength; i++) {
            int globalBitPos = startBit + i;
            int byteIdx = globalBitPos / 8;
            int bitInByte = globalBitPos % 8;
            
            // 在字节内，bit 0是最高位，bit 7是最低位
            int bitPosInByte = bitInByte;
            int bit = (bytes[byteIdx] >> (7 - bitPosInByte)) & 1;
            
            // 按照大端序排列到临时数组
            int targetByteIdx = bitOffset / 8;
            int targetBitInByte = bitOffset % 8;
            
            // 设置目标位
            if (bit != 0) {
                tempBytes[targetByteIdx] |= (1 << (7 - targetBitInByte));
            }
            
            bitOffset++;
        }
        
        // 根据字节序处理结果
        if (bigEndian) {
            return tempBytes;
        } else {
            // 小端序：反转字节数组
            reverseBytesInPlace(tempBytes);
            return tempBytes;
        }
    }

    /**
     * 就地反转字节数组
     *
     * @param bytes 要反转的字节数组
     */
    private static void reverseBytesInPlace(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = temp;
        }
    }
} 