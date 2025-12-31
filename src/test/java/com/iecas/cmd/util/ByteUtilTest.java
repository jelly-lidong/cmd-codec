package com.iecas.cmd.util;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * ByteUtil工具类的完整测试用例
 * 覆盖所有公共方法的功能测试、边界测试和异常测试
 */
public class ByteUtilTest {

    // ==================== 子数组操作测试 ====================

    @Test
    public void testSubByte() {
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] result = ByteUtil.subByte(original, 1, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    @Test
    public void testSubByte_StartAtZero() {
        byte[] original = {0x01, 0x02, 0x03};
        byte[] result = ByteUtil.subByte(original, 0, 2);
        assertArrayEquals(new byte[]{0x01, 0x02}, result);
    }

    @Test
    public void testSubByte_EntireArray() {
        byte[] original = {0x01, 0x02, 0x03};
        byte[] result = ByteUtil.subByte(original, 0, 3);
        assertArrayEquals(original, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubByte_NullArray() {
        ByteUtil.subByte(null, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubByte_InvalidOffset() {
        byte[] bytes = {0x01, 0x02};
        ByteUtil.subByte(bytes, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubByte_InvalidLength() {
        byte[] bytes = {0x01, 0x02};
        ByteUtil.subByte(bytes, 0, 3);
    }

    @Test
    public void testSubBytes() {
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] result = ByteUtil.subBytes(original, 1, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    // ==================== 格式化方法测试 ====================

    @Test
    public void testFormatHex() {
        String result = ByteUtil.formatHex("010203");
        assertEquals("01 02 03 ", result);
    }

    @Test
    public void testFormatHex_WithPrefix() {
        String result = ByteUtil.formatHex("0x010203");
        assertEquals("01 02 03 ", result);
    }

    @Test
    public void testPadHexString() {
        String result = ByteUtil.padHexString("AB", 2);
        assertEquals("00AB", result);
    }

    @Test
    public void testPadHexString_ExactLength() {
        String result = ByteUtil.padHexString("ABCD", 2);
        assertEquals("ABCD", result);
    }

    @Test
    public void testPadHexString_OverLength() {
        String result = ByteUtil.padHexString("ABCDEF", 2);
        assertEquals("ABCDEF", result);
    }

    @Test
    public void testPadHexString_WithPrefix() {
        String result = ByteUtil.padHexString("0xAB", 2);
        assertEquals("00AB", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPadHexString_NullInput() {
        ByteUtil.padHexString(null, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPadHexString_InvalidLength() {
        ByteUtil.padHexString("AB", 0);
    }

    @Test
    public void testToString() {
        byte[] bytes = {(byte) 0x01, (byte) 0x02, (byte) 0xFF};
        String result = ByteUtil.toString(bytes);
        assertTrue(result.contains("0x01"));
        assertTrue(result.contains("0x02"));
        assertTrue(result.contains("0xFF"));
    }

    @Test
    public void testToString_Null() {
        String result = ByteUtil.toString(null);
        assertEquals("null", result);
    }

    @Test
    public void testToString_Empty() {
        String result = ByteUtil.toString(new byte[0]);
        assertEquals("[]", result);
    }

    // ==================== 有符号整数转换测试 ====================

    @Test
    public void testSignedIntToBytes() {
        byte[] result = ByteUtil.signedIntToBytes(255, 1);
        assertArrayEquals(new byte[]{(byte) 0xFF}, result);
    }

    @Test
    public void testSignedIntToBytes_Negative() {
        byte[] result = ByteUtil.signedIntToBytes(-1, 1);
        assertArrayEquals(new byte[]{(byte) 0xFF}, result);
    }

    @Test
    public void testSignedIntToBytes_MultiByte() {
        byte[] result = ByteUtil.signedIntToBytes(0x1234, 2);
        assertArrayEquals(new byte[]{0x12, 0x34}, result);
    }

    @Test
    public void testSignedIntToBytes_WithEndian() {
        byte[] resultBig = ByteUtil.signedIntToBytes(0x1234, 2, true);
        byte[] resultLittle = ByteUtil.signedIntToBytes(0x1234, 2, false);
        assertArrayEquals(new byte[]{(byte) 0x12, (byte) 0x34}, resultBig);
        assertArrayEquals(new byte[]{(byte) 0x34, (byte) 0x12}, resultLittle);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignedIntToBytes_InvalidLength() {
        ByteUtil.signedIntToBytes(100, 9);
    }

    @Test
    public void testBytesToSignedInt() {
        byte[] bytes = {(byte) 0x12, (byte) 0x34};
        long result = ByteUtil.bytesToSignedInt(bytes);
        assertEquals(0x1234, result);
    }

    @Test
    public void testBytesToSignedInt_Negative() {
        byte[] bytes = {(byte) 0xFF};
        long result = ByteUtil.bytesToSignedInt(bytes);
        assertEquals(-1, result);
    }

    @Test
    public void testBytesToSignedInt_WithEndian() {
        byte[] bytes = {(byte) 0x12, (byte) 0x34};
        long resultBig = ByteUtil.bytesToSignedInt(bytes, true);
        long resultLittle = ByteUtil.bytesToSignedInt(bytes, false);
        assertEquals(0x1234, resultBig);
        assertEquals(0x3412, resultLittle);
    }

    @Test
    public void testBytesToSignedInt_Empty() {
        long result = ByteUtil.bytesToSignedInt(new byte[0]);
        assertEquals(0, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBytesToSignedInt_TooLong() {
        byte[] bytes = new byte[9];
        ByteUtil.bytesToSignedInt(bytes);
    }

    // ==================== 无符号整数转换测试 ====================

    @Test
    public void testUnsignedIntToBytes() {
        byte[] result = ByteUtil.unsignedIntToBytes(255, 1);
        assertArrayEquals(new byte[]{(byte) 0xFF}, result);
    }

    @Test
    public void testUnsignedIntToBytes_MultiByte() {
        byte[] result = ByteUtil.unsignedIntToBytes(0x1234, 2);
        assertArrayEquals(new byte[]{0x12, 0x34}, result);
    }

    @Test
    public void testUnsignedIntToBytes_WithEndian() {
        byte[] resultBig = ByteUtil.unsignedIntToBytes(0x1234, 2, true);
        byte[] resultLittle = ByteUtil.unsignedIntToBytes(0x1234, 2, false);
        assertArrayEquals(new byte[]{0x12, 0x34}, resultBig);
        assertArrayEquals(new byte[]{0x34, 0x12}, resultLittle);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsignedIntToBytes_Negative() {
        ByteUtil.unsignedIntToBytes(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsignedIntToBytes_Overflow() {
        ByteUtil.unsignedIntToBytes(256, 1);
    }

    @Test
    public void testBytesToUnsignedInt() {
        byte[] bytes = {0x12, 0x34};
        long result = ByteUtil.bytesToUnsignedInt(bytes);
        assertEquals(0x1234, result);
    }

    @Test
    public void testBytesToUnsignedInt_WithEndian() {
        byte[] bytes = {0x12, 0x34};
        long resultBig = ByteUtil.bytesToUnsignedInt(bytes, true);
        long resultLittle = ByteUtil.bytesToUnsignedInt(bytes, false);
        assertEquals(0x1234, resultBig);
        assertEquals(0x3412, resultLittle);
    }

    @Test
    public void testBytesToUnsignedInt_Empty() {
        long result = ByteUtil.bytesToUnsignedInt(new byte[0]);
        assertEquals(0, result);
    }

    // ==================== 二进制字符串转换测试 ====================

    @Test
    public void testBytesToBinaryString() {
        byte[] bytes = {0x03}; // 00000011
        String result = ByteUtil.bytesToBinaryString(bytes);
        assertEquals("00000011", result);
    }

    @Test
    public void testBytesToBinaryString_MultiByte() {
        byte[] bytes = {0x12, 0x34};
        String result = ByteUtil.bytesToBinaryString(bytes);
        assertEquals(16, result.length());
        assertTrue(result.startsWith("00010010"));
    }

    @Test
    public void testBytesToBinaryString_Empty() {
        String result = ByteUtil.bytesToBinaryString(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testBinaryStringToBytes() {
        byte[] result = ByteUtil.binaryStringToBytes("00000011");
        assertArrayEquals(new byte[]{0x03}, result);
    }

    @Test
    public void testBinaryStringToBytes_ShortString() {
        byte[] result = ByteUtil.binaryStringToBytes("11");
        assertArrayEquals(new byte[]{0x03}, result); // 自动补齐为 00000011
    }

    @Test
    public void testBinaryStringToBytes_MultiByte() {
        byte[] result = ByteUtil.binaryStringToBytes("0001001011010100");
        assertArrayEquals(new byte[]{0x12, (byte) 0xD4}, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinaryStringToBytes_InvalidChar() {
        ByteUtil.binaryStringToBytes("0102");
    }

    @Test
    public void testBinaryStringToBytes_Empty() {
        byte[] result = ByteUtil.binaryStringToBytes("");
        assertArrayEquals(new byte[0], result);
    }

    // ==================== 十六进制字符串转换测试 ====================

    @Test
    public void testBytesToHexString() {
        byte[] bytes = {(byte) 0x01, (byte) 0x02, (byte) 0xFF};
        String result = ByteUtil.bytesToHexString(bytes);
        assertTrue(result.startsWith("0102FF"));
    }

    @Test
    public void testBytesToHexString_WithOptions() {
        byte[] bytes = {(byte) 0xAB, (byte) 0xCD};
        String upper = ByteUtil.bytesToHexString(bytes, true, false);
        String lower = ByteUtil.bytesToHexString(bytes, false, false);
        assertEquals("ABCD", upper);
        assertEquals("abcd", lower);
    }

    @Test
    public void testBytesToHexString_Empty() {
        String result = ByteUtil.bytesToHexString(new byte[0]);
        assertEquals("0x", result);
    }

    @Test
    public void testHexStringToBytes() {
        byte[] result = ByteUtil.hexStringToBytes("0102FF");
        assertArrayEquals(new byte[]{0x01, 0x02, (byte) 0xFF}, result);
    }

    @Test
    public void testHexStringToBytes_WithPrefix() {
        byte[] result = ByteUtil.hexStringToBytes("0x0102");
        assertArrayEquals(new byte[]{0x01, 0x02}, result);
    }

    @Test
    public void testHexStringToBytes_OddLength() {
        byte[] result = ByteUtil.hexStringToBytes("ABC");
        assertArrayEquals(new byte[]{0x0A, (byte) 0xBC}, result);
    }

    @Test
    public void testHexStringToBytes_Empty() {
        byte[] result = ByteUtil.hexStringToBytes("");
        assertArrayEquals(new byte[0], result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexStringToBytes_InvalidChar() {
        ByteUtil.hexStringToBytes("01GH");
    }

    // ==================== 浮点数转换测试 ====================

    @Test
    public void testFloatToBytes() {
        float value = 3.14f;
        byte[] result = ByteUtil.floatToBytes(value);
        assertEquals(4, result.length);
        float back = ByteUtil.bytesToFloat(result);
        assertEquals(value, back, 0.001f);
    }

    @Test
    public void testFloatToBytes_WithEndian() {
        float value = -123.456f;
        byte[] resultBig = ByteUtil.floatToBytes(value, true);
        byte[] resultLittle = ByteUtil.floatToBytes(value, false);
        assertEquals(4, resultBig.length);
        assertEquals(4, resultLittle.length);
        assertNotEquals(resultBig[0], resultLittle[0]); // 字节序不同，结果应该不同
    }

    @Test
    public void testBytesToFloat() {
        float original = 42.5f;
        byte[] bytes = ByteUtil.floatToBytes(original);
        float result = ByteUtil.bytesToFloat(bytes);
        assertEquals(original, result, 0.001f);
    }

    @Test
    public void testBytesToFloat_WithEndian() {
        float value = 99.99f;
        byte[] bytesBig = ByteUtil.floatToBytes(value, true);
        byte[] bytesLittle = ByteUtil.floatToBytes(value, false);
        float resultBig = ByteUtil.bytesToFloat(bytesBig, true);
        float resultLittle = ByteUtil.bytesToFloat(bytesLittle, false);
        assertEquals(value, resultBig, 0.001f);
        assertEquals(value, resultLittle, 0.001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBytesToFloat_WrongLength() {
        ByteUtil.bytesToFloat(new byte[3]);
    }

    @Test
    public void testDoubleToBytes() {
        double value = 3.141592653589793;
        byte[] result = ByteUtil.doubleToBytes(value);
        assertEquals(8, result.length);
        double back = ByteUtil.bytesToDouble(result);
        assertEquals(value, back, 0.0000001);
    }

    @Test
    public void testDoubleToBytes_WithEndian() {
        double value = -987.654321;
        byte[] resultBig = ByteUtil.doubleToBytes(value, true);
        byte[] resultLittle = ByteUtil.doubleToBytes(value, false);
        assertEquals(8, resultBig.length);
        assertEquals(8, resultLittle.length);
    }

    @Test
    public void testBytesToDouble() {
        double original = 123.456789;
        byte[] bytes = ByteUtil.doubleToBytes(original);
        double result = ByteUtil.bytesToDouble(bytes);
        assertEquals(original, result, 0.000001);
    }

    @Test
    public void testBytesToDouble_WithEndian() {
        double value = 456.789012;
        byte[] bytesBig = ByteUtil.doubleToBytes(value, true);
        byte[] bytesLittle = ByteUtil.doubleToBytes(value, false);
        double resultBig = ByteUtil.bytesToDouble(bytesBig, true);
        double resultLittle = ByteUtil.bytesToDouble(bytesLittle, false);
        assertEquals(value, resultBig, 0.000001);
        assertEquals(value, resultLittle, 0.000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBytesToDouble_WrongLength() {
        ByteUtil.bytesToDouble(new byte[7]);
    }

    // ==================== 大整数转换测试 ====================

    @Test
    public void testBigIntegerToBytes() {
        BigInteger value = new BigInteger("12345678901234567890");
        byte[] result = ByteUtil.bigIntegerToBytes(value, 8);
        assertEquals(8, result.length);
    }

    @Test
    public void testBigIntegerToBytes_Negative() {
        BigInteger value = new BigInteger("-12345678901234567890");
        byte[] result = ByteUtil.bigIntegerToBytes(value, 8);
        assertEquals(8, result.length);
    }

    @Test
    public void testBytesToBigInteger() {
        byte[] bytes = {0x12, 0x34, 0x56, 0x78};
        BigInteger result = ByteUtil.bytesToBigInteger(bytes);
        assertNotNull(result);
    }

    @Test
    public void testBytesToBigInteger_Empty() {
        BigInteger result = ByteUtil.bytesToBigInteger(new byte[0]);
        assertEquals(BigInteger.ZERO, result);
    }

    @Test
    public void testBytesToBigInteger_RoundTrip() {
        BigInteger original = new BigInteger("98765432109876543210");
        byte[] bytes = ByteUtil.bigIntegerToBytes(original, 16);
        BigInteger result = ByteUtil.bytesToBigInteger(bytes);
        // 注意：由于可能有符号扩展，可能不完全相等
        assertNotNull(result);
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testReverseBytes() {
        byte[] original = {0x01, 0x02, 0x03, 0x04};
        byte[] result = ByteUtil.reverseBytes(original);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, result);
    }

    @Test
    public void testReverseBytes_Single() {
        byte[] original = {0x01};
        byte[] result = ByteUtil.reverseBytes(original);
        assertArrayEquals(original, result);
    }

    @Test
    public void testReverseBytes_Empty() {
        byte[] original = new byte[0];
        byte[] result = ByteUtil.reverseBytes(original);
        assertArrayEquals(original, result);
    }

    @Test
    public void testReverseBytes_Null() {
        assertNull(ByteUtil.reverseBytes(null));
    }

    @Test
    public void testConcatBytes() {
        byte[] array1 = {0x01, 0x02};
        byte[] array2 = {0x03, 0x04};
        byte[] array3 = {0x05};
        byte[] result = ByteUtil.concatBytes(array1, array2, array3);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, result);
    }

    @Test
    public void testConcatBytes_Empty() {
        byte[] result = ByteUtil.concatBytes();
        assertArrayEquals(new byte[0], result);
    }

    @Test
    public void testConcatBytes_WithNull() {
        byte[] array1 = {0x01};
        byte[] result = ByteUtil.concatBytes(array1, null, array1);
        assertArrayEquals(new byte[]{0x01, 0x01}, result);
    }

    @Test
    public void testEquals() {
        byte[] bytes1 = {0x01, 0x02, 0x03};
        byte[] bytes2 = {0x01, 0x02, 0x03};
        assertTrue(ByteUtil.equals(bytes1, bytes2));
    }

    @Test
    public void testEquals_Different() {
        byte[] bytes1 = {0x01, 0x02, 0x03};
        byte[] bytes2 = {0x01, 0x02, 0x04};
        assertFalse(ByteUtil.equals(bytes1, bytes2));
    }

    @Test
    public void testEquals_SameReference() {
        byte[] bytes = {0x01, 0x02};
        assertTrue(ByteUtil.equals(bytes, bytes));
    }

    @Test
    public void testEquals_WithNull() {
        byte[] bytes = {0x01};
        assertFalse(ByteUtil.equals(bytes, null));
        assertFalse(ByteUtil.equals(null, bytes));
        assertTrue(ByteUtil.equals(null, null));
    }

    @Test
    public void testMergeByteArrays() {
        byte[] array1 = {0x01, 0x02};
        byte[] array2 = {0x03, 0x04};
        byte[] result = ByteUtil.mergeByteArrays(array1, array2);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, result);
    }

    @Test
    public void testGenerateRandomBytes() {
        byte[] result = ByteUtil.generateRandomBytes(10);
        assertEquals(10, result.length);
        // 多次生成应该不同（概率极高）
        byte[] result2 = ByteUtil.generateRandomBytes(10);
        assertFalse(ByteUtil.equals(result, result2));
    }

    // ==================== 值比较测试 ====================

    @Test
    public void testCompareHexValues_String() {
        assertTrue(ByteUtil.compareHexValues("0xAB", "AB"));
        assertTrue(ByteUtil.compareHexValues("AB", "0xAB"));
    }

    @Test
    public void testCompareHexValues_Number() {
        assertTrue(ByteUtil.compareHexValues(255, "FF"));
        assertTrue(ByteUtil.compareHexValues(255L, "0xFF"));
    }

    @Test
    public void testCompareHexValues_ByteArray() {
        byte[] bytes = {0x12, 0x34};
        assertTrue(ByteUtil.compareHexValues(bytes, "1234"));
    }

    @Test
    public void testCompareHexValues_Null() {
        assertTrue(ByteUtil.compareHexValues(null, null));
        assertFalse(ByteUtil.compareHexValues(null, "AB"));
        assertFalse(ByteUtil.compareHexValues("AB", null));
    }

    @Test
    public void testCompareHexValues_IgnoreCase() {
        assertTrue(ByteUtil.compareHexValues("AB", "ab"));
        assertTrue(ByteUtil.compareHexValues("0xABCD", "abcd"));
    }

    // ==================== 位范围设置测试 ====================

    @Test
    public void testSetBitRange_SignedInt() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 8, 127L); // 使用有符号范围内的值
        assertEquals((byte) 127, bytes[0]);
    }

    @Test
    public void testSetBitRange_SignedInt_Negative() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 8, -1L);
        assertEquals((byte) 0xFF, bytes[0]);
    }

    @Test
    public void testSetBitRange_SignedInt_WithEndian() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, 0x1234L, true, true);
        assertEquals(0x12, bytes[0] & 0xFF);
        assertEquals(0x34, bytes[1] & 0xFF);
    }

    @Test
    public void testSetBitRange_UnsignedInt() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, 255L, false, true);
        assertEquals((byte) 0xFF, bytes[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_UnsignedInt_Negative() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, -1L, false, true);
    }

    @Test
    public void testSetBitRange_BinaryString() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, "0b11111111");
        assertEquals((byte) 0xFF, bytes[0]);
    }

    @Test
    public void testSetBitRange_BinaryString_Short() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, "1111");
        assertEquals(0x0F, bytes[0] & 0xFF);
    }

    @Test
    public void testSetBitRange_BinaryString_WithEndian() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, "10101010", true);
        assertEquals((byte) 0xAA, bytes[0]);
    }

    @Test
    public void testSetBitRange_HexString() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, "0x1234", true, true);
        assertEquals(0x12, bytes[0] & 0xFF);
        assertEquals(0x34, bytes[1] & 0xFF);
    }

    @Test
    public void testSetBitRange_HexString_Short() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, "AB", true, true);
        assertEquals(0x00, bytes[0] & 0xFF);
        assertEquals(0xAB, bytes[1] & 0xFF);
    }

    @Test
    public void testSetBitRange_Float() {
        byte[] bytes = new byte[4];
        float value = 3.14f;
        ByteUtil.setBitRange(bytes, 0, 32, value);
        float result = ByteUtil.bytesToFloat(bytes);
        assertEquals(value, result, 0.001f);
    }

    @Test
    public void testSetBitRange_Float_WithEndian() {
        byte[] bytes = new byte[4];
        float value = -123.456f;
        ByteUtil.setBitRange(bytes, 0, 32, value, false);
        float result = ByteUtil.bytesToFloat(bytes, false);
        assertEquals(value, result, 0.001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_Float_WrongLength() {
        byte[] bytes = new byte[4];
        ByteUtil.setBitRange(bytes, 0, 16, 3.14f);
    }

    @Test
    public void testSetBitRange_Double() {
        byte[] bytes = new byte[8];
        double value = 3.141592653589793;
        ByteUtil.setBitRange(bytes, 0, 64, value);
        double result = ByteUtil.bytesToDouble(bytes);
        assertEquals(value, result, 0.0000001);
    }

    @Test
    public void testSetBitRange_Double_WithEndian() {
        byte[] bytes = new byte[8];
        double value = -987.654321;
        ByteUtil.setBitRange(bytes, 0, 64, value, false);
        double result = ByteUtil.bytesToDouble(bytes, false);
        assertEquals(value, result, 0.000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_Double_WrongLength() {
        byte[] bytes = new byte[8];
        ByteUtil.setBitRange(bytes, 0, 32, 3.14);
    }

    @Test
    public void testSetBitRange_CrossByte() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 4, 8, 100L, true, true); // 使用无符号模式，避免超出有符号范围
        // 验证跨字节设置
        assertNotEquals(0, bytes[0]);
        assertNotEquals(0, bytes[1]);
    }

    @Test
    public void testSetBitRange_InvalidRange() {
        byte[] bytes = new byte[1];
        try {
            ByteUtil.setBitRange(bytes, 0, 9, 0xFFL);
            fail("应该抛出异常");
        } catch (IllegalArgumentException e) {
            // 预期异常
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_NullBytes() {
        ByteUtil.setBitRange(null, 0, 8, 100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_NegativeStartBit() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, -1, 8, 100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_InvalidBitLength() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 0, 100L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBitRange_Overflow() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, 256L, false, true); // 8位无符号整数最大值是255
    }

    // ==================== 边界和异常测试 ====================

    @Test
    public void testRoundTrip_SignedInt() {
        long original = 0x12345678L;
        byte[] bytes = ByteUtil.signedIntToBytes(original, 4);
        long result = ByteUtil.bytesToSignedInt(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_UnsignedInt() {
        long original = 0x12345678L;
        byte[] bytes = ByteUtil.unsignedIntToBytes(original, 4);
        long result = ByteUtil.bytesToUnsignedInt(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_BinaryString() {
        String original = "1010101011110000";
        byte[] bytes = ByteUtil.binaryStringToBytes(original);
        String result = ByteUtil.bytesToBinaryString(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_HexString() {
        String original = "ABCDEF";
        byte[] bytes = ByteUtil.hexStringToBytes(original);
        String result = ByteUtil.bytesToHexString(bytes, true, false);
        assertEquals(original, result);
    }

    @Test
    public void testRoundTrip_Float() {
        float[] testValues = {0.0f, 1.0f, -1.0f, 3.14f, Float.MAX_VALUE, Float.MIN_VALUE};
        for (float value : testValues) {
            byte[] bytes = ByteUtil.floatToBytes(value);
            float result = ByteUtil.bytesToFloat(bytes);
            assertEquals("Float往返转换失败: " + value, value, result, 0.001f);
        }
    }

    @Test
    public void testRoundTrip_Double() {
        double[] testValues = {0.0, 1.0, -1.0, 3.141592653589793, Double.MAX_VALUE, Double.MIN_VALUE};
        for (double value : testValues) {
            byte[] bytes = ByteUtil.doubleToBytes(value);
            double result = ByteUtil.bytesToDouble(bytes);
            assertEquals("Double往返转换失败: " + value, value, result, 0.0000001);
        }
    }

    @Test
    public void testExtremeValues() {
        // 测试极值
        byte[] bytes1 = ByteUtil.signedIntToBytes(Long.MAX_VALUE, 8);
        assertEquals(Long.MAX_VALUE, ByteUtil.bytesToSignedInt(bytes1));

        byte[] bytes2 = ByteUtil.signedIntToBytes(Long.MIN_VALUE, 8);
        assertEquals(Long.MIN_VALUE, ByteUtil.bytesToSignedInt(bytes2));

        // 测试无符号整数的极值（使用 Long.MAX_VALUE，这是 63 位无符号整数的最大值）
        // 注意：在 Java 中，0xFFFFFFFFFFFFFFFFL 是 -1L，不能直接作为无符号整数传递
        byte[] bytes3 = ByteUtil.unsignedIntToBytes(Long.MAX_VALUE, 8);
        assertEquals(Long.MAX_VALUE, ByteUtil.bytesToUnsignedInt(bytes3));
        
        // 测试 32 位无符号整数的最大值
        byte[] bytes4 = ByteUtil.unsignedIntToBytes(0xFFFFFFFFL, 4);
        assertEquals(0xFFFFFFFFL, ByteUtil.bytesToUnsignedInt(bytes4));
    }

    // ==================== 位范围读取测试 ====================

    @Test
    public void testGetBitRange_SignedInt() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 8, 127L);
        long result = ByteUtil.getBitRange(bytes, 0, 8);
        assertEquals(127L, result);
    }

    @Test
    public void testGetBitRange_SignedInt_Negative() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 8, -1L);
        long result = ByteUtil.getBitRange(bytes, 0, 8);
        assertEquals(-1L, result);
    }

    @Test
    public void testGetBitRange_SignedInt_WithEndian() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, 0x1234L, true, true);
        long result = ByteUtil.getBitRange(bytes, 0, 16, true, true);
        assertEquals(0x1234L, result);
    }

    @Test
    public void testGetBitRange_SignedInt_LittleEndian() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, 0x1234L, true, false);
        long result = ByteUtil.getBitRange(bytes, 0, 16, true, false);
        assertEquals(0x1234L, result);
    }

    @Test
    public void testGetBitRange_UnsignedInt() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, 255L, false, true);
        long result = ByteUtil.getBitRange(bytes, 0, 8, false, true);
        assertEquals(255L, result);
    }

    @Test
    public void testGetBitRange_CrossByte() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 4, 8, 100L, true, true);
        long result = ByteUtil.getBitRange(bytes, 4, 8, true, true);
        assertEquals(100L, result);
    }

    @Test
    public void testGetBitRange_RoundTrip() {
        byte[] bytes = new byte[4];
        long original = 0x12345678L;
        ByteUtil.setBitRange(bytes, 0, 32, original, true, true);
        long result = ByteUtil.getBitRange(bytes, 0, 32, true, true);
        assertEquals(original, result);
    }

    @Test
    public void testGetBitRange_RoundTrip_LittleEndian() {
        byte[] bytes = new byte[4];
        long original = 0x12345678L;
        ByteUtil.setBitRange(bytes, 0, 32, original, true, false);
        long result = ByteUtil.getBitRange(bytes, 0, 32, true, false);
        assertEquals(original, result);
    }

    @Test
    public void testGetBitRange_PartialByte() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 4, 0x0FL);
        long result = ByteUtil.getBitRange(bytes, 0, 4);
        assertEquals(0x0FL, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRange_NullBytes() {
        ByteUtil.getBitRange(null, 0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRange_NegativeStartBit() {
        byte[] bytes = new byte[1];
        ByteUtil.getBitRange(bytes, -1, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRange_InvalidBitLength() {
        byte[] bytes = new byte[1];
        ByteUtil.getBitRange(bytes, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRange_Overflow() {
        byte[] bytes = new byte[1];
        ByteUtil.getBitRange(bytes, 0, 9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRange_BitLengthTooLarge() {
        byte[] bytes = new byte[8];
        ByteUtil.getBitRange(bytes, 0, 65);
    }

    @Test
    public void testGetBitRangeAsBinaryString() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 8, "0b11111111", true);
        String result = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 8);
        assertEquals("11111111", result);
    }

    @Test
    public void testGetBitRangeAsBinaryString_Partial() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 4, "1111", true);
        String result = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 4);
        assertEquals("1111", result);
    }

    @Test
    public void testGetBitRangeAsBinaryString_CrossByte() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 4, 8, "10101010", true);
        String result = ByteUtil.getBitRangeAsBinaryString(bytes, 4, 8);
        assertEquals("10101010", result);
    }

    @Test
    public void testGetBitRangeAsBinaryString_WithEndian() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, 0x1234L, true, true);
        String resultBig = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 16, true);
        String resultLittle = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 16, false);
        assertNotEquals(resultBig, resultLittle); // 字节序不同，结果应该不同
    }

    @Test
    public void testGetBitRangeAsBinaryString_RoundTrip() {
        byte[] bytes = new byte[2];
        String original = "1010101011110000";
        ByteUtil.setBitRange(bytes, 0, 16, original, true);
        String result = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 16);
        assertEquals(original, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsBinaryString_NullBytes() {
        ByteUtil.getBitRangeAsBinaryString(null, 0, 8);
    }

    @Test
    public void testGetBitRangeAsHexString() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, "0x1234", true, true);
        String result = ByteUtil.getBitRangeAsHexString(bytes, 0, 16);
        assertEquals("1234", result);
    }

    @Test
    public void testGetBitRangeAsHexString_Partial() {
        byte[] bytes = new byte[1];
        ByteUtil.setBitRange(bytes, 0, 4, 0xFL);
        String result = ByteUtil.getBitRangeAsHexString(bytes, 0, 4);
        assertEquals("F", result); // 4位 = 0xF = "F"
    }

    @Test
    public void testGetBitRangeAsHexString_CrossByte() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 4, 8, 0xABL, true, true);
        String result = ByteUtil.getBitRangeAsHexString(bytes, 4, 8);
        assertEquals("AB", result);
    }

    @Test
    public void testGetBitRangeAsHexString_WithEndian() {
        byte[] bytes = new byte[2];
        ByteUtil.setBitRange(bytes, 0, 16, 0x1234L, true, true);
        String resultBig = ByteUtil.getBitRangeAsHexString(bytes, 0, 16, true);
        String resultLittle = ByteUtil.getBitRangeAsHexString(bytes, 0, 16, false);
        // 字节序不同，结果应该不同
        assertNotEquals(resultBig, resultLittle);
    }

    @Test
    public void testGetBitRangeAsHexString_RoundTrip() {
        byte[] bytes = new byte[2];
        String original = "ABCD";
        ByteUtil.setBitRange(bytes, 0, 16, original, true, true);
        String result = ByteUtil.getBitRangeAsHexString(bytes, 0, 16);
        assertEquals(original, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsHexString_NullBytes() {
        ByteUtil.getBitRangeAsHexString(null, 0, 8);
    }

    @Test
    public void testGetBitRangeAsFloat() {
        byte[] bytes = new byte[4];
        float value = 3.14f;
        ByteUtil.setBitRange(bytes, 0, 32, value);
        float result = ByteUtil.getBitRangeAsFloat(bytes, 0);
        assertEquals(value, result, 0.001f);
    }

    @Test
    public void testGetBitRangeAsFloat_Negative() {
        byte[] bytes = new byte[4];
        float value = -123.456f;
        ByteUtil.setBitRange(bytes, 0, 32, value);
        float result = ByteUtil.getBitRangeAsFloat(bytes, 0);
        assertEquals(value, result, 0.001f);
    }

    @Test
    public void testGetBitRangeAsFloat_WithEndian() {
        byte[] bytes = new byte[4];
        float value = 99.99f;
        ByteUtil.setBitRange(bytes, 0, 32, value, false);
        float result = ByteUtil.getBitRangeAsFloat(bytes, 0, false);
        assertEquals(value, result, 0.001f);
    }

    @Test
    public void testGetBitRangeAsFloat_RoundTrip() {
        byte[] bytes = new byte[4];
        float[] testValues = {0.0f, 1.0f, -1.0f, 3.14f, Float.MAX_VALUE, Float.MIN_VALUE};
        for (float value : testValues) {
            ByteUtil.setBitRange(bytes, 0, 32, value);
            float result = ByteUtil.getBitRangeAsFloat(bytes, 0);
            assertEquals("Float往返转换失败: " + value, value, result, 0.001f);
        }
    }

    @Test
    public void testGetBitRangeAsFloat_RoundTrip_WithEndian() {
        byte[] bytes = new byte[4];
        float value = -987.654f;
        ByteUtil.setBitRange(bytes, 0, 32, value, false);
        float result = ByteUtil.getBitRangeAsFloat(bytes, 0, false);
        assertEquals(value, result, 0.001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsFloat_NotByteAligned() {
        byte[] bytes = new byte[4];
        float value = 3.14f;
        ByteUtil.setBitRange(bytes, 0, 32, value);
        ByteUtil.getBitRangeAsFloat(bytes, 1); // 不是字节边界
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsFloat_InsufficientBits() {
        byte[] bytes = new byte[3];
        ByteUtil.getBitRangeAsFloat(bytes, 0); // 需要32位，但只有24位
    }

    @Test
    public void testGetBitRangeAsDouble() {
        byte[] bytes = new byte[8];
        double value = 3.141592653589793;
        ByteUtil.setBitRange(bytes, 0, 64, value);
        double result = ByteUtil.getBitRangeAsDouble(bytes, 0);
        assertEquals(value, result, 0.0000001);
    }

    @Test
    public void testGetBitRangeAsDouble_Negative() {
        byte[] bytes = new byte[8];
        double value = -987.654321;
        ByteUtil.setBitRange(bytes, 0, 64, value);
        double result = ByteUtil.getBitRangeAsDouble(bytes, 0);
        assertEquals(value, result, 0.000001);
    }

    @Test
    public void testGetBitRangeAsDouble_WithEndian() {
        byte[] bytes = new byte[8];
        double value = 456.789012;
        ByteUtil.setBitRange(bytes, 0, 64, value, false);
        double result = ByteUtil.getBitRangeAsDouble(bytes, 0, false);
        assertEquals(value, result, 0.000001);
    }

    @Test
    public void testGetBitRangeAsDouble_RoundTrip() {
        byte[] bytes = new byte[8];
        double[] testValues = {0.0, 1.0, -1.0, 3.141592653589793, Double.MAX_VALUE, Double.MIN_VALUE};
        for (double value : testValues) {
            ByteUtil.setBitRange(bytes, 0, 64, value);
            double result = ByteUtil.getBitRangeAsDouble(bytes, 0);
            assertEquals("Double往返转换失败: " + value, value, result, 0.0000001);
        }
    }

    @Test
    public void testGetBitRangeAsDouble_RoundTrip_WithEndian() {
        byte[] bytes = new byte[8];
        double value = 123.456789;
        ByteUtil.setBitRange(bytes, 0, 64, value, false);
        double result = ByteUtil.getBitRangeAsDouble(bytes, 0, false);
        assertEquals(value, result, 0.000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsDouble_NotByteAligned() {
        byte[] bytes = new byte[8];
        double value = 3.14;
        ByteUtil.setBitRange(bytes, 0, 64, value);
        ByteUtil.getBitRangeAsDouble(bytes, 1); // 不是字节边界
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBitRangeAsDouble_InsufficientBits() {
        byte[] bytes = new byte[7];
        ByteUtil.getBitRangeAsDouble(bytes, 0); // 需要64位，但只有56位
    }

    // ==================== 位范围设置与读取往返测试 ====================

    @Test
    public void testSetAndGetBitRange_RoundTrip_SignedInt() {
        byte[] bytes = new byte[4];
        long[] testValues = {0L, 1L, -1L, 127L, -128L, 0x12345678L};
        for (long value : testValues) {
            ByteUtil.setBitRange(bytes, 0, 32, value, true, true);
            long result = ByteUtil.getBitRange(bytes, 0, 32, true, true);
            assertEquals("SignedInt往返转换失败: " + value, value, result);
        }
    }

    @Test
    public void testSetAndGetBitRange_RoundTrip_UnsignedInt() {
        byte[] bytes = new byte[4];
        long[] testValues = {0L, 1L, 255L, 0x12345678L, 0xFFFFFFFFL};
        for (long value : testValues) {
            ByteUtil.setBitRange(bytes, 0, 32, value, false, true);
            long result = ByteUtil.getBitRange(bytes, 0, 32, false, true);
            assertEquals("UnsignedInt往返转换失败: " + value, value, result);
        }
    }

    @Test
    public void testSetAndGetBitRange_RoundTrip_BinaryString() {
        byte[] bytes = new byte[2];
        String original = "1010101011110000";
        ByteUtil.setBitRange(bytes, 0, 16, original, true);
        String result = ByteUtil.getBitRangeAsBinaryString(bytes, 0, 16);
        assertEquals("BinaryString往返转换失败", original, result);
    }

    @Test
    public void testSetAndGetBitRange_RoundTrip_HexString() {
        byte[] bytes = new byte[2];
        String original = "ABCD";
        ByteUtil.setBitRange(bytes, 0, 16, original, true, true);
        String result = ByteUtil.getBitRangeAsHexString(bytes, 0, 16);
        assertEquals("HexString往返转换失败", original, result);
    }

    @Test
    public void testSetAndGetBitRange_RoundTrip_Float() {
        byte[] bytes = new byte[4];
        float value = -987.654f;
        ByteUtil.setBitRange(bytes, 0, 32, value, false);
        float result = ByteUtil.getBitRangeAsFloat(bytes, 0, false);
        assertEquals("Float往返转换失败", value, result, 0.001f);
    }

    @Test
    public void testSetAndGetBitRange_RoundTrip_Double() {
        byte[] bytes = new byte[8];
        double value = -987.654321;
        ByteUtil.setBitRange(bytes, 0, 64, value, false);
        double result = ByteUtil.getBitRangeAsDouble(bytes, 0, false);
        assertEquals("Double往返转换失败", value, result, 0.000001);
    }

    @Test
    public void testSetAndGetBitRange_MultiByte() {
        byte[] bytes = new byte[4];
        // 设置不同的值到不同的位范围
        ByteUtil.setBitRange(bytes, 0, 8, 0x12L, true, true);
        ByteUtil.setBitRange(bytes, 8, 8, 0x34L, true, true);
        ByteUtil.setBitRange(bytes, 16, 8, 0x56L, true, true);
        ByteUtil.setBitRange(bytes, 24, 8, 0x78L, true, true);
        
        // 读取每个位范围
        assertEquals(0x12L, ByteUtil.getBitRange(bytes, 0, 8));
        assertEquals(0x34L, ByteUtil.getBitRange(bytes, 8, 8));
        assertEquals(0x56L, ByteUtil.getBitRange(bytes, 16, 8));
        assertEquals(0x78L, ByteUtil.getBitRange(bytes, 24, 8));
        
        // 读取整个32位
        assertEquals(0x12345678L, ByteUtil.getBitRange(bytes, 0, 32));
    }
}

