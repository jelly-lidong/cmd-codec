package com.iecas.cmd.aviator.validator;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.VerifyUtils;

import java.util.Map;

/**
 * CRC16计算函数
 *
 * <p>基于Aviator表达式的CRC16计算函数，使用多项式 X^16+X^12+X^5+1 (0x1021)</p>
 * <p>该多项式是CRC-16-CCITT标准，广泛应用于通信协议中</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * crc16("Hello World")
 * crc16("Hello World", "UTF-8")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class CRC16Function extends AbstractFunction {

    /**
     * CRC16多项式 (X^16+X^12+X^5+1)
     */
    private static final int POLYNOMIAL = 0x1021;

    /**
     * 初始值
     */
    private static final int INITIAL_VALUE = 0xFFFF;

    /**
     * 异或值
     */
    private static final int XOR_VALUE = 0x0000;

    /**
     * 构造函数
     */
    public CRC16Function() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "crc16";
    }

    /**
     * 执行函数（一个参数版本 - 使用默认UTF-8编码）
     *
     * @param env 执行环境
     * @param arg1 第一个参数（要计算CRC16的字符串）
     * @return CRC16校验值（16位整数）
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            // 获取参数值
            String data = FunctionUtils.getStringValue(arg1, env);

            byte[] ar1Bytes = (byte[]) env.get(data);

            String crc16 = VerifyUtils.calCRC16(ar1Bytes);

            return new AviatorString(crc16);

        } catch (Exception e) {
            throw new RuntimeException("CRC16计算失败: " + e.getMessage(), e);
        }
    }


    /**
     * 执行函数（两个参数版本 - 指定字符编码）
     *
     * @param env 执行环境
     * @param arg1 第一个参数（要计算CRC16的字符串）
     * @param arg2 第二个参数（字符编码）
     * @return CRC16校验值（16位整数）
     */
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            // 获取参数值
            String arg1Id = FunctionUtils.getStringValue(arg1, env);
            String arg2Id = FunctionUtils.getStringValue(arg2, env);

            byte[] ar1Bytes = (byte[]) env.get(arg1Id);
            byte[] ar2Bytes = (byte[]) env.get(arg2Id);

            byte[] bytes = ByteUtil.mergeByteArrays(ar1Bytes, ar2Bytes);

            String crc16 = VerifyUtils.calCRC16(bytes);

            return new AviatorString(crc16);

        } catch (Exception e) {
            throw new RuntimeException("CRC16计算失败: " + e.getMessage(), e);
        }
    }

}
