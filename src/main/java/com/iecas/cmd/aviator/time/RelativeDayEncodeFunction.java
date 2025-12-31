package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * 积日（相对天数）编码函数
 * 
 * <p>计算目标时间相对于基准时间的累积天数</p>
 * <p>使用2字节存储，支持有符号整数范围</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeDay("2024-01-01 00:00:00", "2024-01-15 12:00:00")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeDayEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);
    
    /**
     * 积日字节数
     */
    private static final int DAYS_BYTES = 2;
    
    /**
     * 最大积日值（有符号2字节）
     */
    private static final long MAX_DAYS = (1L << (DAYS_BYTES * 8 - 1)) - 1;
    
    /**
     * 最小积日值（有符号2字节）
     */
    private static final long MIN_DAYS = -(1L << (DAYS_BYTES * 8 - 1));

    /**
     * 构造函数
     */
    public RelativeDayEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeDay";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param timeObj 目标时间对象
     * @return 积日值（2字节有符号整数）
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject baseTimeObj, AviatorObject timeObj) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(baseTimeObj, env);
            String timeStr = FunctionUtils.getStringValue(timeObj, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }
            if (timeStr == null || timeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("目标时间不能为空");
            }

            // 解析时间字符串
            Date baseTime = parseDateTime(baseTimeStr);
            Date time = parseDateTime(timeStr);

            // 计算相对天数
            long days = calculateRelativeDays(baseTime, time);

            // 验证范围
            if (days > MAX_DAYS || days < MIN_DAYS) {
                throw new IllegalArgumentException(
                    String.format("积日值超出范围 [%d, %d]，当前值: %d", MIN_DAYS, MAX_DAYS, days));
            }

            return AviatorLong.valueOf(days);

        } catch (Exception e) {
            throw new RuntimeException("积日计算失败: " + e.getMessage(), e);
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
        return DATE_TIME_FORMATTER.parse(dateTimeStr);
    }

    /**
     * 计算相对天数
     *
     * @param baseTime 基准时间
     * @param time 目标时间
     * @return 相对天数
     */
    private long calculateRelativeDays(Date baseTime, Date time) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime targetDateTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算天数差
        return ChronoUnit.DAYS.between(baseDateTime, targetDateTime);
    }

    /**
     * 获取最大积日值
     *
     * @return 最大积日值
     */
    public static long getMaxDays() {
        return MAX_DAYS;
    }

    /**
     * 获取最小积日值
     *
     * @return 最小积日值
     */
    public static long getMinDays() {
        return MIN_DAYS;
    }

    /**
     * 获取积日字节数
     *
     * @return 积日字节数
     */
    public static int getDaysBytes() {
        return DAYS_BYTES;
    }
}
