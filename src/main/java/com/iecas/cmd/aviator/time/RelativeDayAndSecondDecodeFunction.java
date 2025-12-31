package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 反向计算：根据基准时间、积日和积秒计算目标时间
 * <p>编码格式：前2字节存储积日，后4字节存储积秒（相对当天零点）</p>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeDayAndSecondDecodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);

    /**
     * 积日最大值的字节数
     */
    private static final int DAYS_BYTES = 2;

    /**
     * 积秒最大值的字节数
     */
    private static final int SECONDS_BYTES = 4;

    /**
     * 积日最大值（2字节有符号整数）
     */
    private static final long MAX_DAYS = (1L << (DAYS_BYTES * 8 - 1)) - 1;

    /**
     * 积秒最大值（4字节无符号整数）
     */
    private static final long MAX_SECONDS = (1L << (SECONDS_BYTES * 8)) - 1;

    /**
     * 构造函数
     */
    public RelativeDayAndSecondDecodeFunction() {
        super();
    }


    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeDayAndSecondDecode";
    }


    /**
     * 执行函数（两个参数版本 - 正向计算）
     *
     * @param env 执行环境
     * @param arg1 第一个参数（基准时间）
     * @param relativeTime 第二个参数（目标时间）
     * @return 编码后的6字节整数值
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject relativeTime) {
        try {
            // 获取参数值
            String baseTimeStr     = FunctionUtils.getStringValue(arg1, env);
            Number relativeTimeNum = FunctionUtils.getNumberValue(relativeTime, env);

            // 解析时间字符串
            Date          baseTime     = parseDateTime(baseTimeStr);
            LocalDateTime baseDateTime = LocalDateTime.ofInstant(baseTime.toInstant(), ZoneId.systemDefault());

            long[] times = decodeRelativeTime(relativeTimeNum.longValue());

            return new AviatorString(baseDateTime.plusDays(times[0]).plusSeconds(times[1]).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));

        } catch (ParseException e) {
            throw new RuntimeException("时间格式解析失败，请使用格式: " + DATE_TIME_FORMAT, e);
        } catch (Exception e) {
            throw new RuntimeException("相对时间计算失败: " + e.getMessage(), e);
        }
    }


    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的Date对象
     * @throws ParseException 解析异常
     */
    private Date parseDateTime(String dateTimeStr) throws ParseException {
        return DATE_TIME_FORMATTER.parse(dateTimeStr.trim());
    }


    /**
     * 编码相对时间为6字节整数
     *
     * @param days 积日
     * @param seconds 积秒
     * @return 编码后的6字节整数值
     */
    private long encodeRelativeTime(long days, long seconds) {
        // 验证范围
        if (days < -MAX_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("积日超出范围: " + days + "，允许范围: [" + (-MAX_DAYS) + ", " + MAX_DAYS + "]");
        }
        if (seconds < 0 || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException("积秒超出范围: " + seconds + "，允许范围: [0, " + MAX_SECONDS + "]");
        }

        // 将积日转换为无符号2字节
        long encodedDays = days < 0 ? (1L << (DAYS_BYTES * 8)) + days : days;

        // 编码：前2字节存储积日，后4字节存储积秒
        long result = (encodedDays << (SECONDS_BYTES * 8)) | seconds;

        return result;
    }

    /**
     * 解码6字节整数为积日和积秒
     *
     * @param encodedValue 编码后的6字节整数值
     * @return 包含积日和积秒的数组 [积日, 积秒]
     */
    public static long[] decodeRelativeTime(long encodedValue) {
        // 提取积秒（后4字节）
        long seconds = encodedValue & ((1L << (SECONDS_BYTES * 8)) - 1);

        // 提取积日（前2字节）
        long encodedDays = (encodedValue >> (SECONDS_BYTES * 8)) & ((1L << (DAYS_BYTES * 8)) - 1);

        // 判断积日是否为负数（最高位为1表示负数）
        long days;
        if ((encodedDays & (1L << (DAYS_BYTES * 8 - 1))) != 0) {
            // 负数，需要转换回有符号值
            days = encodedDays - (1L << (DAYS_BYTES * 8));
        } else {
            days = encodedDays;
        }

        return new long[]{days, seconds};
    }

}
