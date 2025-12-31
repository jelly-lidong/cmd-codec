package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 反向计算：根据基准时间、积日和积毫秒计算目标时间
 * <p>编码格式：前2字节存储积日，后4字节存储积毫秒（相对当天零点）</p>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeDayAndMillisecondDecodeFunction extends AbstractFunction {

    /**
     * 积日最大值的字节数
     */
    private static final int DAYS_BYTES = 2;

    /**
     * 积毫秒最大值的字节数
     */
    private static final int MILLISECONDS_BYTES = 4;

    /**
     * 积日最大值（2字节有符号整数）
     */
    private static final long MAX_DAYS = (1L << (DAYS_BYTES * 8 - 1)) - 1;

    /**
     * 积毫秒最大值（4字节无符号整数）
     */
    private static final long MAX_MILLISECONDS = (1L << (MILLISECONDS_BYTES * 8)) - 1;

    /**
     * 构造函数
     */
    public RelativeDayAndMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeDayAndMillisecondDecode";
    }

    /**
     * 执行函数（两个参数版本 - 反向计算）
     *
     * @param env          执行环境
     * @param arg1         第一个参数（基准时间）
     * @param encodedValue 第二个参数（编码后的6字节整数值）
     * @return 目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject encodedValue) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(arg1, env);
            Number encodedValueNum = FunctionUtils.getNumberValue(encodedValue, env);

            long encodeValueLong = encodedValueNum.longValue();
            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            // 解析时间字符串
            LocalDateTime baseDateTime = TimeUtil.parseLocalDateTime(baseTimeStr);

            // 解码积日和积毫秒
            long[] dayAndMillisecond = TimeUtil.splitLong(encodeValueLong,2,4);

            long days = dayAndMillisecond[0];
            long milliseconds = dayAndMillisecond[1];

            // 计算目标时间
            String targetTime = calculateTargetTime(baseDateTime, days, milliseconds);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("反向日时间计算失败: " + e.getMessage(), e);
        }
    }


    /**
     * 根据基准时间、积日和积毫秒计算目标时间
     *
     * @param baseDateTime 基准时间
     * @param days         积日
     * @param milliseconds 积毫秒
     * @return 目标时间字符串
     */
    private String calculateTargetTime(LocalDateTime baseDateTime, long days, long milliseconds) {
        // 验证参数范围
        if (days < -MAX_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("积日超出范围: " + days + "，允许范围: [" + (-MAX_DAYS) + ", " + MAX_DAYS + "]");
        }
        if (milliseconds < 0 || milliseconds > MAX_MILLISECONDS) {
            throw new IllegalArgumentException("积毫秒超出范围: " + milliseconds + "，允许范围: [0, " + MAX_MILLISECONDS + "]");
        }

        // 计算目标日期
        LocalDateTime targetDate = baseDateTime.plusDays(days);

        // 计算目标时间（当天零点 + 积毫秒）
        LocalDateTime targetDateTime = targetDate.toLocalDate().atStartOfDay().plusNanos(milliseconds * 1_000_000);

        // 格式化返回结果（包含毫秒）
        return targetDateTime.format(TimeUtil.TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS_SSS);
    }
}
