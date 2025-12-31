package com.iecas.cmd.util;

/**
 * 位操作工具类
 * 提供各种位级别的操作方法
 */
public class BitUtil {


    /**
     * 将二进制字符串补齐到指定位长度
     * 如果输入字符串长度不足，在左侧补0
     * 如果输入字符串长度超过指定长度，则返回原内容
     *
     * @param bitStr 二进制字符串
     * @param bitLength 目标位长度
     * @return 补齐后的十六进制字符串
     */
    public static String padBitString(String bitStr, int bitLength) {
        if (bitStr == null || bitStr.isEmpty()) {
            throw new IllegalArgumentException("十六进制字符串不能为空");
        }
        if (bitLength <= 0) {
            throw new IllegalArgumentException("字节长度必须大于0");
        }

        // 移除可能存在的0x前缀
        bitStr = bitStr.replaceFirst("^0b", "");


        // 如果输入字符串长度超过目标长度，则返回原内容
        if (bitStr.length() > bitLength) {
            return bitStr;
        }

        // 如果输入字符串长度不足，在左侧补0
        if (bitStr.length() < bitLength) {
            return String.format("%" + bitLength + "s", bitStr).replace(' ', '0');
        }

        return bitStr;
    }

    /**
     * 设置指定位置的位为1
     *
     * @param value    原始值
     * @param position 位置（从0开始，0表示最低位）
     * @return 设置后的值
     */
    public static long setBit(long value, int position) {
        if (position < 0 || position >= 64) {
            throw new IllegalArgumentException("位置必须在0-63之间");
        }
        return value | (1L << position);
    }

    /**
     * 设置指定位置的位为0
     *
     * @param value    原始值
     * @param position 位置（从0开始，0表示最低位）
     * @return 清除后的值
     */
    public static long clearBit(long value, int position) {
        if (position < 0 || position >= 64) {
            throw new IllegalArgumentException("位置必须在0-63之间");
        }
        return value & ~(1L << position);
    }

    /**
     * 切换指定位置的位（0变1，1变0）
     *
     * @param value    原始值
     * @param position 位置（从0开始，0表示最低位）
     * @return 切换后的值
     */
    public static long toggleBit(long value, int position) {
        if (position < 0 || position >= 64) {
            throw new IllegalArgumentException("位置必须在0-63之间");
        }
        return value ^ (1L << position);
    }

    /**
     * 检查指定位置的位是否为1
     *
     * @param value    要检查的值
     * @param position 位置（从0开始，0表示最低位）
     * @return 如果该位为1返回true，否则返回false
     */
    public static boolean testBit(long value, int position) {
        if (position < 0 || position >= 64) {
            throw new IllegalArgumentException("位置必须在0-63之间");
        }
        return (value & (1L << position)) != 0;
    }

    /**
     * 获取指定范围的位
     *
     * @param value    原始值
     * @param startPos 起始位置（包含）
     * @param endPos   结束位置（包含）
     * @return 提取的位值
     */
    public static long getBits(long value, int startPos, int endPos) {
        if (startPos < 0 || endPos < 0 || startPos >= 64 || endPos >= 64 || startPos > endPos) {
            throw new IllegalArgumentException("位置参数无效");
        }
        int length = endPos - startPos + 1;
        long mask = (1L << length) - 1;
        return (value >> startPos) & mask;
    }

    /**
     * 设置指定范围的位
     *
     * @param value    原始值
     * @param startPos 起始位置（包含）
     * @param endPos   结束位置（包含）
     * @param newValue 新的位值
     * @return 设置后的值
     */
    public static long setBits(long value, int startPos, int endPos, long newValue) {
        if (startPos < 0 || endPos < 0 || endPos >= 64 || startPos > endPos) {
            throw new IllegalArgumentException("位置参数无效");
        }
        int length = endPos - startPos + 1;
        long mask = (1L << length) - 1;

        // 验证新值是否超出范围
        if (newValue > mask) {
            throw new IllegalArgumentException("新值超出指定位范围");
        }

        // 清除目标位置的位
        value &= ~(mask << startPos);
        // 设置新值
        value |= (newValue & mask) << startPos;
        return value;
    }

    /**
     * 左移操作
     *
     * @param value     原始值
     * @param positions 移动位数
     * @return 移动后的值
     */
    public static long leftShift(long value, int positions) {
        if (positions < 0) {
            throw new IllegalArgumentException("移动位数不能为负数");
        }
        return value << positions;
    }

    /**
     * 右移操作（逻辑右移）
     *
     * @param value     原始值
     * @param positions 移动位数
     * @return 移动后的值
     */
    public static long rightShift(long value, int positions) {
        if (positions < 0) {
            throw new IllegalArgumentException("移动位数不能为负数");
        }
        return value >>> positions;
    }

    /**
     * 算术右移操作
     *
     * @param value     原始值
     * @param positions 移动位数
     * @return 移动后的值
     */
    public static long arithmeticRightShift(long value, int positions) {
        if (positions < 0) {
            throw new IllegalArgumentException("移动位数不能为负数");
        }
        return value >> positions;
    }

    /**
     * 循环左移
     *
     * @param value     原始值
     * @param positions 移动位数
     * @param bitWidth  位宽（8, 16, 32, 64）
     * @return 移动后的值
     */
    public static long rotateLeft(long value, int positions, int bitWidth) {
        if (bitWidth != 8 && bitWidth != 16 && bitWidth != 32 && bitWidth != 64) {
            throw new IllegalArgumentException("位宽必须是8、16、32或64");
        }
        if (positions < 0) {
            throw new IllegalArgumentException("移动位数不能为负数");
        }

        positions = positions % bitWidth;
        long mask = (1L << bitWidth) - 1;
        value &= mask;

        return ((value << positions) | (value >>> (bitWidth - positions))) & mask;
    }

    /**
     * 循环右移
     *
     * @param value     原始值
     * @param positions 移动位数
     * @param bitWidth  位宽（8, 16, 32, 64）
     * @return 移动后的值
     */
    public static long rotateRight(long value, int positions, int bitWidth) {
        if (bitWidth != 8 && bitWidth != 16 && bitWidth != 32 && bitWidth != 64) {
            throw new IllegalArgumentException("位宽必须是8、16、32或64");
        }
        if (positions < 0) {
            throw new IllegalArgumentException("移动位数不能为负数");
        }

        positions = positions % bitWidth;
        long mask = (1L << bitWidth) - 1;
        value &= mask;

        return ((value >>> positions) | (value << (bitWidth - positions))) & mask;
    }

    /**
     * 计算值中1的个数（汉明重量）
     *
     * @param value 要计算的值
     * @return 1的个数
     */
    public static int countOnes(long value) {
        return Long.bitCount(value);
    }

    /**
     * 计算值中0的个数
     *
     * @param value    要计算的值
     * @param bitWidth 位宽
     * @return 0的个数
     */
    public static int countZeros(long value, int bitWidth) {
        if (bitWidth <= 0 || bitWidth > 64) {
            throw new IllegalArgumentException("位宽必须在1-64之间");
        }
        long mask = (1L << bitWidth) - 1;
        return bitWidth - Long.bitCount(value & mask);
    }

    /**
     * 反转位顺序
     *
     * @param value    原始值
     * @param bitWidth 位宽
     * @return 反转后的值
     */
    public static long reverseBits(long value, int bitWidth) {
        if (bitWidth <= 0 || bitWidth > 64) {
            throw new IllegalArgumentException("位宽必须在1-64之间");
        }

        long result = 0;
        for (int i = 0; i < bitWidth; i++) {
            if ((value & (1L << i)) != 0) {
                result |= (1L << (bitWidth - 1 - i));
            }
        }
        return result;
    }

    /**
     * 检查是否为2的幂
     *
     * @param value 要检查的值
     * @return 如果是2的幂返回true
     */
    public static boolean isPowerOfTwo(long value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /**
     * 找到最高位1的位置
     *
     * @param value 要查找的值
     * @return 最高位1的位置，如果值为0返回-1
     */
    public static int findHighestBit(long value) {
        if (value == 0) {
            return -1;
        }
        return 63 - Long.numberOfLeadingZeros(value);
    }

    /**
     * 找到最低位1的位置
     *
     * @param value 要查找的值
     * @return 最低位1的位置，如果值为0返回-1
     */
    public static int findLowestBit(long value) {
        if (value == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(value);
    }

    /**
     * 将字节数组转换为位字符串
     *
     * @param bytes 字节数组
     * @return 位字符串表示
     */
    public static String bytesToBitString(byte[] bytes) {
        if (bytes == null) {
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
     * 将位字符串转换为字节数组
     *
     * @param bitString 位字符串
     * @return 字节数组
     */
    public static byte[] bitStringToBytes(String bitString) {
        if (bitString == null || bitString.isEmpty()) {
            return new byte[0];
        }

        // 验证位字符串
        if (!bitString.matches("[01]+")) {
            throw new IllegalArgumentException("位字符串只能包含0和1");
        }

        // 补齐到8的倍数
        int padding = 8 - (bitString.length() % 8);
        if (padding != 8) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                sb.append('0');
            }
            sb.append(bitString);
            bitString = sb.toString();
        }

        byte[] result = new byte[bitString.length() / 8];
        for (int i = 0; i < result.length; i++) {
            String byteString = bitString.substring(i * 8, (i + 1) * 8);
            result[i] = (byte) Integer.parseInt(byteString, 2);
        }
        return result;
    }

    /**
     * 获取指定字节中的位
     *
     * @param b        字节值
     * @param position 位置（0-7）
     * @return 该位的值（true表示1，false表示0）
     */
    public static boolean getBitFromByte(byte b, int position) {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("字节位置必须在0-7之间");
        }
        return (b & (1 << position)) != 0;
    }

    /**
     * 设置字节中指定位的值
     *
     * @param b        原始字节
     * @param position 位置（0-7）
     * @param value    要设置的值（true表示1，false表示0）
     * @return 设置后的字节
     */
    public static byte setBitInByte(byte b, int position, boolean value) {
        if (position < 0 || position > 7) {
            throw new IllegalArgumentException("字节位置必须在0-7之间");
        }

        if (value) {
            return (byte) (b | (1 << position));
        } else {
            return (byte) (b & ~(1 << position));
        }
    }

    /**
     * 交换两个位的值
     *
     * @param value 原始值
     * @param pos1  第一个位的位置
     * @param pos2  第二个位的位置
     * @return 交换后的值
     */
    public static long swapBits(long value, int pos1, int pos2) {
        if (pos1 < 0 || pos1 >= 64 || pos2 < 0 || pos2 >= 64) {
            throw new IllegalArgumentException("位置必须在0-63之间");
        }

        if (pos1 == pos2) {
            return value;
        }

        boolean bit1 = testBit(value, pos1);
        boolean bit2 = testBit(value, pos2);

        if (bit1 != bit2) {
            value = toggleBit(value, pos1);
            value = toggleBit(value, pos2);
        }

        return value;
    }

    /**
     * 计算奇偶校验位
     *
     * @param value    要计算的值
     * @param bitWidth 位宽
     * @return 奇偶校验位（true表示奇数个1，false表示偶数个1）
     */
    public static boolean calculateParity(long value, int bitWidth) {
        if (bitWidth <= 0 || bitWidth > 64) {
            throw new IllegalArgumentException("位宽必须在1-64之间");
        }

        long mask = (1L << bitWidth) - 1;
        return (Long.bitCount(value & mask) & 1) == 1;
    }

    /**
     * 将值转换为指定位宽的二进制字符串
     *
     * @param value    要转换的值
     * @param bitWidth 位宽
     * @return 二进制字符串
     */
    public static String toBinaryString(long value, int bitWidth) {
        if (bitWidth <= 0 || bitWidth > 64) {
            throw new IllegalArgumentException("位宽必须在1-64之间");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = bitWidth - 1; i >= 0; i--) {
            sb.append(testBit(value, i) ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * 从二进制字符串解析值
     *
     * @param binaryString 二进制字符串
     * @return 解析的值
     */
    public static long fromBinaryString(String binaryString) {
        if (binaryString == null || binaryString.isEmpty()) {
            throw new IllegalArgumentException("二进制字符串不能为空");
        }

        if (!binaryString.matches("[01]+")) {
            throw new IllegalArgumentException("二进制字符串只能包含0和1");
        }

        if (binaryString.length() > 64) {
            throw new IllegalArgumentException("二进制字符串长度不能超过64位");
        }

        return Long.parseLong(binaryString, 2);
    }

    /**
     * 将位值转换为字节数组
     *
     * @param value     位值
     * @param bitLength 位长度
     * @return 字节数组
     */
    public static byte[] bitsToByteArray(long value, int bitLength) {
        if (bitLength <= 0 || bitLength > 64) {
            throw new IllegalArgumentException("位长度必须在1-64之间");
        }

        int byteLength = (bitLength + 7) / 8;
        byte[] result = new byte[byteLength];

        for (int i = 0; i < byteLength; i++) {
            result[byteLength - 1 - i] = (byte) ((value >> (i * 8)) & 0xFF);
        }

        return result;
    }

    /**
     * 从字节数组转换为位值
     *
     * @param bytes 字节数组
     * @return 位值
     */
    public static long byteArrayToBits(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0L;
        }

        if (bytes.length > 8) {
            throw new IllegalArgumentException("字节数组长度不能超过8字节");
        }

        long result = 0L;
        for (byte aByte : bytes) {
            result = (result << 8) | (aByte & 0xFF);
        }

        return result;
    }

    /**
     * 将位值转换为十六进制字符串
     *
     * @param value     位值
     * @param bitLength 位长度
     * @return 十六进制字符串（带0x前缀）
     */
    public static String bitsToHexString(long value, int bitLength) {
        if (bitLength <= 0 || bitLength > 64) {
            throw new IllegalArgumentException("位长度必须在1-64之间");
        }

        int hexLength = (bitLength + 3) / 4; // 每4位对应一个十六进制字符
        long mask = (1L << bitLength) - 1;
        value &= mask; // 清除高位

        StringBuilder hexStr = new StringBuilder(Long.toHexString(value).toUpperCase());

        // 补齐前导零
        while (hexStr.length() < hexLength) {
            hexStr.insert(0, "0");
        }

        return "0x" + hexStr;
    }

    /**
     * 从十六进制字符串转换为位值
     *
     * @param hexString 十六进制字符串（可以带0x前缀）
     * @return 位值
     */
    public static long hexStringToBits(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            throw new IllegalArgumentException("十六进制字符串不能为空");
        }

        // 移除可能存在的0x前缀
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }

        if (hexString.isEmpty()) {
            throw new IllegalArgumentException("十六进制字符串不能为空");
        }

        // 验证十六进制字符串
        if (!hexString.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException("无效的十六进制字符串: " + hexString);
        }

        if (hexString.length() > 16) {
            throw new IllegalArgumentException("十六进制字符串长度不能超过16个字符");
        }

        return Long.parseUnsignedLong(hexString, 16);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes     字节数组
     * @param uppercase 是否使用大写字母
     * @return 十六进制字符串（带0x前缀）
     */
    public static String byteArrayToHexString(byte[] bytes, boolean uppercase) {
        if (bytes == null || bytes.length == 0) {
            return "0x";
        }

        StringBuilder sb = new StringBuilder("0x");
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
     * 将字节数组转换为十六进制字符串（默认大写）
     *
     * @param bytes 字节数组
     * @return 十六进制字符串（带0x前缀）
     */
    public static String byteArrayToHexString(byte[] bytes) {
        return byteArrayToHexString(bytes, true);
    }

    /**
     * 从十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串（可以带0x前缀）
     * @return 字节数组
     */
    public static byte[] hexStringToByteArray(String hexString) {
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

    /**
     * 将位字符串转换为十六进制字符串
     *
     * @param bitString 位字符串
     * @return 十六进制字符串（带0x前缀）
     */
    public static String bitStringToHexString(String bitString) {
        if (bitString == null || bitString.isEmpty()) {
            return "0x0";
        }

        // 验证位字符串
        if (!bitString.matches("[01]+")) {
            throw new IllegalArgumentException("位字符串只能包含0和1");
        }

        // 补齐到4的倍数（每4位对应一个十六进制字符）
        int padding = 4 - (bitString.length() % 4);
        if (padding != 4) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                sb.append('0');
            }
            sb.append(bitString);
            bitString = sb.toString();
        }

        StringBuilder hexResult = new StringBuilder("0x");
        for (int i = 0; i < bitString.length(); i += 4) {
            String fourBits = bitString.substring(i, i + 4);
            int val = Integer.parseInt(fourBits, 2);
            hexResult.append(Integer.toHexString(val).toUpperCase());
        }

        return hexResult.toString();
    }

    /**
     * 将十六进制字符串转换为位字符串
     *
     * @param hexString 十六进制字符串（可以带0x前缀）
     * @return 位字符串
     */
    public static String hexStringToBitString(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return "0";
        }

        // 移除可能存在的0x前缀
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }

        if (hexString.isEmpty()) {
            return "0";
        }

        // 验证十六进制字符串
        if (!hexString.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException("无效的十六进制字符串: " + hexString);
        }

        StringBuilder bitResult = new StringBuilder();
        for (char hexChar : hexString.toCharArray()) {
            int val = Character.digit(hexChar, 16);
            StringBuilder fourBits = new StringBuilder(Integer.toBinaryString(val));

            // 补齐到4位
            while (fourBits.length() < 4) {
                fourBits.insert(0, "0");
            }

            bitResult.append(fourBits);
        }

        // 移除前导零（但至少保留一个0）
        String result = bitResult.toString();
        int firstOne = result.indexOf('1');
        if (firstOne == -1) {
            return "0";
        }

        return result.substring(firstOne);
    }

    // ==================== 值比较方法 ====================

    /**
     * 比较两个二进制值是否相等（忽略前缀和前导零）
     * 支持的前缀格式：0b、0B
     * 
     * @param original 原始值（可以是String、Number或其他类型）
     * @param decoded 解码值（可以是String、Number或其他类型）
     * @return 如果两个值相等返回true，否则返回false
     */
    public static boolean compareBitValues(Object original, Object decoded) {
        if (original == null && decoded == null) {
            return true;
        }
        if (original == null || decoded == null) {
            return false;
        }

        String originalStr = normalizeBitString(convertToBitString(original));
        String decodedStr = normalizeBitString(convertToBitString(decoded));
        
        return originalStr.equalsIgnoreCase(decodedStr);
    }

    /**
     * 将对象转换为二进制字符串
     * 
     * @param value 要转换的值
     * @return 二进制字符串
     */
    private static String convertToBitString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return Long.toBinaryString(((Number) value).longValue());
        } else if (value instanceof byte[]) {
            return bytesToBitString((byte[]) value);
        }
        return value.toString();
    }

    /**
     * 标准化二进制字符串，移除前缀和前导零
     * 
     * @param bitStr 二进制字符串
     * @return 标准化后的字符串
     */
    private static String normalizeBitString(String bitStr) {
        if (bitStr == null || bitStr.isEmpty()) {
            return "0";
        }

        // 移除各种前缀
        String normalized = bitStr.trim()
                .replace("0b", "")
                .replace("0B", "");

        // 验证二进制字符串格式
        if (!normalized.matches("[01]*")) {
            throw new IllegalArgumentException("无效的二进制字符串: " + bitStr);
        }

        // 移除前导零，但至少保留一个字符
        normalized = normalized.replaceFirst("^0+", "");
        if (normalized.isEmpty()) {
            normalized = "0";
        }

        return normalized;
    }
}
