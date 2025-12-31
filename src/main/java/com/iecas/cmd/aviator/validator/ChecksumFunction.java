package com.iecas.cmd.aviator.validator;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 累加和校验函数（1字节）
 *
 * <p>与 {@link CRC16Function} 一致的风格，提供一个参数（默认UTF-8）和两个参数（指定编码）两个版本。</p>
 * <p>返回按字节累加并取低8位后的结果（0x00~0xFF）。</p>
 *
 * 使用示例：
 * <pre>
 * checksum("Hello World")
 * checksum("数据", "GBK")
 * </pre>
 */
public class ChecksumFunction extends AbstractFunction {

    public ChecksumFunction() {
        super();
    }

    @Override
    public String getName() {
        return "checksum";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            String data = FunctionUtils.getStringValue(arg1, env);
            if (data == null || data.trim().isEmpty()) {
                throw new IllegalArgumentException("输入字符串不能为空");
            }
            int sum = calculateChecksum(data.getBytes(StandardCharsets.UTF_8));
            return AviatorLong.valueOf(sum);
        } catch (Exception e) {
            throw new RuntimeException("校验和计算失败: " + e.getMessage(), e);
        }
    }

    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            String data = FunctionUtils.getStringValue(arg1, env);
            String charsetName = FunctionUtils.getStringValue(arg2, env);
            if (data == null || data.trim().isEmpty()) {
                throw new IllegalArgumentException("输入字符串不能为空");
            }
            if (charsetName == null || charsetName.trim().isEmpty()) {
                throw new IllegalArgumentException("字符编码不能为空");
            }
            Charset charset = Charset.forName(charsetName);
            int sum = calculateChecksum(data.getBytes(charset));
            return AviatorLong.valueOf(sum);
        } catch (Exception e) {
            throw new RuntimeException("校验和计算失败: " + e.getMessage(), e);
        }
    }

    private int calculateChecksum(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        int sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF);
        }
        return sum & 0xFF;
    }
}


