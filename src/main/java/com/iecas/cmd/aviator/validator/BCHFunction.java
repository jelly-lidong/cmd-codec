package com.iecas.cmd.aviator.validator;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * BCH编码计算函数
 * 
 * <p>基于Aviator表达式的BCH编码计算函数，支持多种BCH码类型</p>
 * <p>BCH码是一种纠错码，广泛应用于数字通信和存储系统中</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * bch("Hello World", 15, 7)  // BCH(15,7)编码
 * bch("Hello World", 31, 21) // BCH(31,21)编码
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class BCHFunction extends AbstractFunction {

    /**
     * 默认BCH码类型：BCH(15,7)
     */
    private static final int DEFAULT_N = 15;
    private static final int DEFAULT_K = 7;

    /**
     * 构造函数
     */
    public BCHFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "bch";
    }

    /**
     * 执行函数（一个参数版本 - 使用默认BCH(15,7)编码）
     *
     * @param env 执行环境
     * @param arg1 第一个参数（要编码的字符串）
     * @return BCH编码结果（长整型）
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            // 获取参数值
            String data = FunctionUtils.getStringValue(arg1, env);

            // 验证参数
            if (data == null || data.trim().isEmpty()) {
                throw new IllegalArgumentException("输入字符串不能为空");
            }

            // 使用默认BCH(15,7)编码
            long encoded = encodeBCH(data, DEFAULT_N, DEFAULT_K);

            return AviatorLong.valueOf(encoded);

        } catch (Exception e) {
            throw new RuntimeException("BCH编码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行函数（三个参数版本 - 指定BCH码类型）
     *
     * @param env 执行环境
     * @param arg1 第一个参数（要编码的字符串）
     * @param arg2 第二个参数（码字长度n）
     * @param arg3 第三个参数（信息位长度k）
     * @return BCH编码结果（长整型）
     */
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, 
                             AviatorObject arg2, AviatorObject arg3) {
        try {
            // 获取参数值
            String data = FunctionUtils.getStringValue(arg1, env);
            Number nObj = FunctionUtils.getNumberValue(arg2, env);
            Number kObj = FunctionUtils.getNumberValue(arg3, env);

            // 验证参数
            if (data == null || data.trim().isEmpty()) {
                throw new IllegalArgumentException("输入字符串不能为空");
            }

            int n = nObj.intValue();
            int k = kObj.intValue();

            // 验证BCH参数
            validateBCHParameters(n, k);

            // 执行BCH编码
            long encoded = encodeBCH(data, n, k);

            return AviatorLong.valueOf(encoded);

        } catch (Exception e) {
            throw new RuntimeException("BCH编码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证BCH参数
     *
     * @param n 码字长度
     * @param k 信息位长度
     * @throws IllegalArgumentException 如果参数无效
     */
    private void validateBCHParameters(int n, int k) {
        if (n <= 0 || k <= 0) {
            throw new IllegalArgumentException("码字长度n和信息位长度k必须大于0");
        }
        if (k >= n) {
            throw new IllegalArgumentException("信息位长度k必须小于码字长度n");
        }
        if (n > 63) {
            throw new IllegalArgumentException("码字长度n不能超过63位（长整型限制）");
        }
    }

    /**
     * 执行BCH编码
     *
     * @param data 要编码的数据
     * @param n 码字长度
     * @param k 信息位长度
     * @return BCH编码结果
     */
    private long encodeBCH(String data, int n, int k) {
        // 将字符串转换为字节数组
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        
        // 计算校验位数量
        int t = (n - k) / 2; // 纠错能力
        
        // 生成生成多项式
        long generator = generateBCHGenerator(n, k);
        
        // 执行编码
        return performBCHEncoding(bytes, n, k, generator);
    }

    /**
     * 生成BCH生成多项式
     *
     * @param n 码字长度
     * @param k 信息位长度
     * @return 生成多项式
     */
    private long generateBCHGenerator(int n, int k) {
        // 这里使用简化的生成多项式生成方法
        // 实际应用中可能需要更复杂的算法
        
        // 对于BCH(15,7)，生成多项式通常是 x^8 + x^7 + x^6 + x^4 + 1
        if (n == 15 && k == 7) {
            return 0x1D0; // 二进制：111010000
        }
        
        // 对于BCH(31,21)，生成多项式通常是 x^10 + x^9 + x^8 + x^6 + x^5 + x^3 + 1
        if (n == 31 && k == 21) {
            return 0x3A5; // 二进制：1110100101
        }
        
        // 默认生成多项式（简化版本）
        return generateDefaultGenerator(n, k);
    }

    /**
     * 生成默认的BCH生成多项式
     *
     * @param n 码字长度
     * @param k 信息位长度
     * @return 默认生成多项式
     */
    private long generateDefaultGenerator(int n, int k) {
        int t = (n - k) / 2;
        long generator = 1L;
        
        // 生成一个简单的生成多项式
        for (int i = 0; i < t; i++) {
            generator = (generator << 1) | 1L;
        }
        
        return generator;
    }

    /**
     * 执行BCH编码
     *
     * @param data 数据字节数组
     * @param n 码字长度
     * @param k 信息位长度
     * @param generator 生成多项式
     * @return 编码结果
     */
    private long performBCHEncoding(byte[] data, int n, int k, long generator) {
        // 将字节数组转换为长整型（限制在k位内）
        long message = 0L;
        int maxBytes = Math.min(data.length, (k + 7) / 8);
        
        for (int i = 0; i < maxBytes; i++) {
            message = (message << 8) | (data[i] & 0xFF);
        }
        
        // 限制在k位内
        message = message & ((1L << k) - 1);
        
        // 计算校验位
        long parity = calculateBCHParity(message, generator, n, k);
        
        // 组合信息位和校验位
        return (message << (n - k)) | parity;
    }

    /**
     * 计算BCH校验位
     *
     * @param message 信息位
     * @param generator 生成多项式
     * @param n 码字长度
     * @param k 信息位长度
     * @return 校验位
     */
    private long calculateBCHParity(long message, long generator, int n, int k) {
        // 将信息位移到高位
        long shiftedMessage = message << (n - k);
        
        // 执行多项式除法
        long remainder = shiftedMessage;
        int generatorDegree = getPolynomialDegree(generator);
        
        for (int i = n - 1; i >= n - k; i--) {
            if ((remainder & (1L << i)) != 0) {
                // 执行异或运算
                remainder ^= (generator << (i - generatorDegree));
            }
        }
        
        // 返回校验位
        return remainder & ((1L << (n - k)) - 1);
    }

    /**
     * 获取多项式的次数
     *
     * @param polynomial 多项式
     * @return 多项式次数
     */
    private int getPolynomialDegree(long polynomial) {
        int degree = 0;
        long temp = polynomial;
        
        while (temp > 0) {
            degree++;
            temp >>= 1;
        }
        
        return degree - 1;
    }

    /**
     * 解码BCH码
     *
     * @param encoded 编码后的数据
     * @param n 码字长度
     * @param k 信息位长度
     * @return 解码后的信息位
     */
    public static long decodeBCH(long encoded, int n, int k) {
        // 提取信息位
        return (encoded >> (n - k)) & ((1L << k) - 1);
    }

    /**
     * 检查BCH码是否有错误
     *
     * @param encoded 编码后的数据
     * @param n 码字长度
     * @param k 信息位长度
     * @param generator 生成多项式
     * @return 如果没有错误返回true，否则返回false
     */
    public static boolean checkBCH(long encoded, int n, int k, long generator) {
        // 计算校验位
        long message = (encoded >> (n - k)) & ((1L << k) - 1);
        long parity = encoded & ((1L << (n - k)) - 1);
        
        // 重新计算校验位
        long calculatedParity = calculateBCHParityStatic(message, generator, n, k);
        
        return parity == calculatedParity;
    }

    /**
     * 静态版本的BCH校验位计算方法
     *
     * @param message 信息位
     * @param generator 生成多项式
     * @param n 码字长度
     * @param k 信息位长度
     * @return 校验位
     */
    private static long calculateBCHParityStatic(long message, long generator, int n, int k) {
        // 将信息位移到高位
        long shiftedMessage = message << (n - k);
        
        // 执行多项式除法
        long remainder = shiftedMessage;
        int generatorDegree = getPolynomialDegreeStatic(generator);
        
        for (int i = n - 1; i >= n - k; i--) {
            if ((remainder & (1L << i)) != 0) {
                // 执行异或运算
                remainder ^= (generator << (i - generatorDegree));
            }
        }
        
        // 返回校验位
        return remainder & ((1L << (n - k)) - 1);
    }

    /**
     * 静态版本的多项式次数计算方法
     *
     * @param polynomial 多项式
     * @return 多项式次数
     */
    private static int getPolynomialDegreeStatic(long polynomial) {
        int degree = 0;
        long temp = polynomial;
        
        while (temp > 0) {
            degree++;
            temp >>= 1;
        }
        
        return degree - 1;
    }

    /**
     * 获取默认BCH参数信息
     *
     * @return 默认BCH参数信息字符串
     */
    public static String getDefaultBCHInfo() {
        return String.format("默认BCH码类型: BCH(%d,%d)", DEFAULT_N, DEFAULT_K);
    }

    /**
     * 获取支持的BCH码类型
     *
     * @return 支持的BCH码类型字符串
     */
    public static String getSupportedBCHTypes() {
        return "支持的BCH码类型: BCH(15,7), BCH(31,21), 自定义(n,k)";
    }
}
