package com.iecas.cmd.util;

public class HexHelper {
    /**
     * 判断字符串是否为有效的十六进制字符串
     * 支持可选的前缀"0x"或"0X"
     *
     * @param str 待验证的字符串
     * @return 如果是十六进制字符串返回true，否则返回false
     */
    public static boolean isHexString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        String hex = normalizeHexString(str);
        // 必须至少有1位
        if (hex.isEmpty()) {
            return false;
        }
        // 使用正则表达式验证是否为十六进制字符串
        boolean matches = hex.matches("[0-9a-fA-F]+");
        if (!matches) {
            return false;
        }

        return str.startsWith("0x") || str.startsWith("0X") || str.endsWith("H");

    }
    public static String reverse(String hex) {
        return ByteUtil.bytesToHexString(ByteUtil.reverseBytes(ByteUtil.hexStringToBytes(hex)));
    }

    public static String splitSingleByte(String hex) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = hex.toCharArray();
        for (int i = 0; i < charArray.length-1; i+=2) {
            sb.append(charArray[i]);
            sb.append(charArray[i+1]);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    /**
     * 比较两个十六进制字符串是否相等（忽略前缀0x/0X和后缀H/h，不区分大小写，自动补齐前导0）
     * 例如：0x0001 和 1H 视为相等
     *
     * @param hexStr1 第一个十六进制字符串
     * @param hexStr2 第二个十六进制字符串
     * @return 如果相等返回true，否则返回false
     */
    public static boolean compareHexStrings(String hexStr1, String hexStr2) {
        String norm1 = normalizeHexString(hexStr1);
        String norm2 = normalizeHexString(hexStr2);

        // 补齐前导0，使长度一致
        int maxLen = Math.max(norm1.length(), norm2.length());
        norm1 = padLeftWithZero(norm1, maxLen);
        norm2 = padLeftWithZero(norm2, maxLen);

        return norm1.equalsIgnoreCase(norm2);
    }

    /**
     * 规范化十六进制字符串，去除前缀0x/0X和后缀H/h
     *
     * @param hexStr 原始字符串
     * @return 规范化后的字符串（不带前缀和后缀）
     */
    public static String normalizeHexString(String hexStr) {
        if (hexStr == null) return "";
        String hex = hexStr.trim();
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (hex.endsWith("H") || hex.endsWith("h")) {
            hex = hex.substring(0, hex.length() - 1);
        }
        // 去除所有空格
        hex = hex.replaceAll("\\s+", "");
        return hex;
    }

    /**
     * 左侧补0到指定长度
     *
     * @param str    原始字符串
     * @param length 目标长度
     * @return 补齐后的字符串
     */
    public static String padLeftWithZero(String str, int length) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString();
    }


    /**
     * 判断十六进制字符串是否可以表示为浮点数（32位或64位）
     * 支持格式：0x前缀、H/h后缀、纯十六进制字符串
     * 
     * @param hexStr 十六进制字符串
     * @return 如果可以转换为float或double返回true，否则返回false
     */
    public static boolean isHexFloat(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return false;
        }
        String hex = normalizeHexString(hexStr);
        // 长度为8（32位float）或16（64位double）
        if (!(hex.length() == 8 || hex.length() == 16)) {
            return false;
        }
        try {
            long bits = Long.parseUnsignedLong(hex, 16);
            if (hex.length() == 8) {
                // 尝试转换为float
                float f = Float.intBitsToFloat((int) bits);
                // 检查是否为NaN或无穷大也算合法
            } else {
                // 尝试转换为double
                double d = Double.longBitsToDouble(bits);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将十六进制字符串转换为字节数组
     * 支持格式：0x前缀、H/h后缀、纯十六进制字符串
     * 
     * @param hexStr 十六进制字符串
     * @return 字节数组，如果转换失败返回null
     */
    public static byte[] hexStringToBytes(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.isEmpty() || !hex.matches("[0-9a-fA-F]+")) {
            return null;
        }
        
        // 确保长度为偶数
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            try {
                bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return bytes;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @param withPrefix 是否添加0x前缀
     * @param withSuffix 是否添加H后缀
     * @param upperCase 是否使用大写字母
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes, boolean withPrefix, boolean withSuffix, boolean upperCase) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        if (withPrefix) {
            sb.append("0x");
        }
        
        for (byte b : bytes) {
            String hex = String.format("%02x", b & 0xFF);
            if (upperCase) {
                hex = hex.toUpperCase();
            }
            sb.append(hex);
        }
        
        if (withSuffix) {
            sb.append("H");
        }
        
        return sb.toString();
    }

    /**
     * 将字节数组转换为十六进制字符串（默认格式：0x前缀，小写）
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, true, false, false);
    }

    /**
     * 将十六进制字符串转换为整数
     * 支持格式：0x前缀、H/h后缀、纯十六进制字符串
     * 
     * @param hexStr 十六进制字符串
     * @return 整数值，如果转换失败返回null
     */
    public static Integer hexStringToInt(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.isEmpty() || !hex.matches("[0-9a-fA-F]+")) {
            return null;
        }
        
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将十六进制字符串转换为长整数
     * 支持格式：0x前缀、H/h后缀、纯十六进制字符串
     * 
     * @param hexStr 十六进制字符串
     * @return 长整数值，如果转换失败返回null
     */
    public static Long hexStringToLong(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.isEmpty() || !hex.matches("[0-9a-fA-F]+")) {
            return null;
        }
        
        try {
            return Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将整数转换为十六进制字符串
     * 
     * @param value 整数值
     * @param minLength 最小长度（不足时左侧补0）
     * @param withPrefix 是否添加0x前缀
     * @param withSuffix 是否添加H后缀
     * @param upperCase 是否使用大写字母
     * @return 十六进制字符串
     */
    public static String intToHexString(int value, int minLength, boolean withPrefix, boolean withSuffix, boolean upperCase) {
        String hex = Integer.toHexString(value);
        
        // 补齐到最小长度
        if (hex.length() < minLength) {
            hex = padLeftWithZero(hex, minLength);
        }
        
        if (upperCase) {
            hex = hex.toUpperCase();
        }
        
        if (withPrefix) {
            hex = "0x" + hex;
        }
        
        if (withSuffix) {
            hex = hex + "H";
        }
        
        return hex;
    }

    /**
     * 将长整数转换为十六进制字符串
     * 
     * @param value 长整数值
     * @param minLength 最小长度（不足时左侧补0）
     * @param withPrefix 是否添加0x前缀
     * @param withSuffix 是否添加H后缀
     * @param upperCase 是否使用大写字母
     * @return 十六进制字符串
     */
    public static String longToHexString(long value, int minLength, boolean withPrefix, boolean withSuffix, boolean upperCase) {
        String hex = Long.toHexString(value);
        
        // 补齐到最小长度
        if (hex.length() < minLength) {
            hex = padLeftWithZero(hex, minLength);
        }
        
        if (upperCase) {
            hex = hex.toUpperCase();
        }
        
        if (withPrefix) {
            hex = "0x" + hex;
        }
        
        if (withSuffix) {
            hex = hex + "H";
        }
        
        return hex;
    }

    /**
     * 计算十六进制字符串的校验和（简单累加）
     * 
     * @param hexStr 十六进制字符串
     * @return 校验和（十六进制字符串），如果计算失败返回null
     */
    public static String calculateChecksum(String hexStr) {
        byte[] bytes = hexStringToBytes(hexStr);
        if (bytes == null) {
            return null;
        }
        
        int sum = 0;
        for (byte b : bytes) {
            sum += b & 0xFF;
        }
        
        return intToHexString(sum & 0xFF, 2, true, false, false);
    }

    /**
     * 计算十六进制字符串的异或校验
     * 
     * @param hexStr 十六进制字符串
     * @return 异或校验值（十六进制字符串），如果计算失败返回null
     */
    public static String calculateXorChecksum(String hexStr) {
        byte[] bytes = hexStringToBytes(hexStr);
        if (bytes == null) {
            return null;
        }
        
        int xor = 0;
        for (byte b : bytes) {
            xor ^= b & 0xFF;
        }
        
        return intToHexString(xor, 2, true, false, false);
    }

    /**
     * 反转十六进制字符串的字节顺序（大端序/小端序转换）
     * 
     * @param hexStr 十六进制字符串
     * @return 反转后的十六进制字符串，如果转换失败返回null
     */
    public static String reverseByteOrder(String hexStr) {
        byte[] bytes = hexStringToBytes(hexStr);
        if (bytes == null) {
            return null;
        }
        
        // 反转字节数组
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = temp;
        }
        
        return bytesToHexString(bytes);
    }

    /**
     * 提取十六进制字符串的指定位范围
     * 
     * @param hexStr 十六进制字符串
     * @param startBit 起始位（从0开始）
     * @param bitLength 位长度
     * @return 提取的位范围（十六进制字符串），如果提取失败返回null
     */
    public static String extractBits(String hexStr, int startBit, int bitLength) {
        byte[] bytes = hexStringToBytes(hexStr);
        if (bytes == null || startBit < 0 || bitLength <= 0) {
            return null;
        }
        
        int totalBits = bytes.length * 8;
        if (startBit + bitLength > totalBits) {
            return null;
        }
        
        // 将字节数组转换为位数组
        boolean[] bits = new boolean[totalBits];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = ((bytes[i] >> (7 - j)) & 1) == 1;
            }
        }
        
        // 提取指定位范围
        StringBuilder result = new StringBuilder();
        for (int i = startBit; i < startBit + bitLength; i++) {
            result.append(bits[i] ? "1" : "0");
        }
        
        // 将位字符串转换为十六进制
        String bitString = result.toString();
        int hexLength = (bitLength + 3) / 4; // 每4位转换为1个十六进制字符
        
        StringBuilder hexResult = new StringBuilder();
        for (int i = 0; i < bitLength; i += 4) {
            int endIndex = Math.min(i + 4, bitLength);
            String fourBits = bitString.substring(i, endIndex);
            // 左侧补0到4位
            while (fourBits.length() < 4) {
                fourBits = "0" + fourBits;
            }
            int value = Integer.parseInt(fourBits, 2);
            hexResult.append(Integer.toHexString(value));
        }
        
        return hexResult.toString();
    }

    /**
     * 将十六进制字符串转换为浮点数
     * 
     * @param hexStr 十六进制字符串
     * @return 浮点数值，如果转换失败返回null
     */
    public static Float hexStringToFloat(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.length() != 8) {
            return null;
        }
        
        try {
            int bits = Integer.parseInt(hex, 16);
            return Float.intBitsToFloat(bits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将十六进制字符串转换为双精度浮点数
     * 
     * @param hexStr 十六进制字符串
     * @return 双精度浮点数值，如果转换失败返回null
     */
    public static Double hexStringToDouble(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.length() != 16) {
            return null;
        }
        
        try {
            long bits = Long.parseLong(hex, 16);
            return Double.longBitsToDouble(bits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将浮点数转换为十六进制字符串
     * 
     * @param value 浮点数值
     * @param withPrefix 是否添加0x前缀
     * @param upperCase 是否使用大写字母
     * @return 十六进制字符串
     */
    public static String floatToHexString(float value, boolean withPrefix, boolean upperCase) {
        int bits = Float.floatToIntBits(value);
        String hex = Integer.toHexString(bits);
        
        // 补齐到8位
        hex = padLeftWithZero(hex, 8);
        
        if (upperCase) {
            hex = hex.toUpperCase();
        }
        
        if (withPrefix) {
            hex = "0x" + hex;
        }
        
        return hex;
    }

    /**
     * 将双精度浮点数转换为十六进制字符串
     * 
     * @param value 双精度浮点数值
     * @param withPrefix 是否添加0x前缀
     * @param upperCase 是否使用大写字母
     * @return 十六进制字符串
     */
    public static String doubleToHexString(double value, boolean withPrefix, boolean upperCase) {
        long bits = Double.doubleToLongBits(value);
        String hex = Long.toHexString(bits);
        
        // 补齐到16位
        hex = padLeftWithZero(hex, 16);
        
        if (upperCase) {
            hex = hex.toUpperCase();
        }
        
        if (withPrefix) {
            hex = "0x" + hex;
        }
        
        return hex;
    }

    /**
     * 格式化十六进制字符串，每4个字符添加一个空格
     * 
     * @param hexStr 十六进制字符串
     * @return 格式化后的字符串
     */
    public static String formatHexString(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return "";
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 4) {
            if (i > 0) {
                sb.append(" ");
            }
            int endIndex = Math.min(i + 4, hex.length());
            sb.append(hex, i, endIndex);
        }
        
        return sb.toString();
    }

    /**
     * 验证十六进制字符串的长度是否符合预期
     * 
     * @param hexStr 十六进制字符串
     * @param expectedLength 期望的字符长度
     * @return 如果长度符合预期返回true，否则返回false
     */
    public static boolean validateHexLength(String hexStr, int expectedLength) {
        if (hexStr == null || expectedLength <= 0) {
            return false;
        }
        
        String hex = normalizeHexString(hexStr);
        return hex.length() == expectedLength;
    }

    /**
     * 将十六进制字符串转换为二进制字符串
     * 
     * @param hexStr 十六进制字符串
     * @return 二进制字符串，如果转换失败返回null
     */
    public static String hexStringToBinary(String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            return null;
        }
        
        String hex = normalizeHexString(hexStr);
        if (hex.isEmpty() || !hex.matches("[0-9a-fA-F]+")) {
            return null;
        }
        
        StringBuilder binary = new StringBuilder();
        for (char c : hex.toCharArray()) {
            int value = Character.digit(c, 16);
            String binaryStr = Integer.toBinaryString(value);
            // 补齐到4位
            while (binaryStr.length() < 4) {
                binaryStr = "0" + binaryStr;
            }
            binary.append(binaryStr);
        }
        
        return binary.toString();
    }

    /**
     * 将二进制字符串转换为十六进制字符串
     * 
     * @param binaryStr 二进制字符串
     * @return 十六进制字符串，如果转换失败返回null
     */
    public static String binaryToHexString(String binaryStr) {
        if (binaryStr == null || binaryStr.trim().isEmpty()) {
            return null;
        }
        
        String binary = binaryStr.trim();
        if (!binary.matches("[01]+")) {
            return null;
        }
        
        // 补齐到4的倍数
        while (binary.length() % 4 != 0) {
            binary = "0" + binary;
        }
        
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 4) {
            String fourBits = binary.substring(i, i + 4);
            int value = Integer.parseInt(fourBits, 2);
            hex.append(Integer.toHexString(value));
        }
        
        return hex.toString();
    }
}
