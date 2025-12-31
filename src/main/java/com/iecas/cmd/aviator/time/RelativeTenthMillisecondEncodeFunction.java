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
 * 积0.1毫秒（相对0.1毫秒数）编码函数
 * 
 * <p>计算目标时间相对于基准时间的累积0.1毫秒数</p>
 * <p>使用4字节存储，支持无符号整数范围</p>
 * <p>精度为0.1毫秒（100微秒）</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeTenthMillisecond("2024-01-01 00:00:00", "2024-01-01 00:00:01.1234")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeTenthMillisecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式（带微秒精度）
     */
    private static final String DATE_TIME_US_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    
    /**
     * 日期时间格式化器（带微秒精度）
     */
    private static final SimpleDateFormat DATE_TIME_US_FORMATTER = new SimpleDateFormat(DATE_TIME_US_FORMAT);
    
    /**
     * 积0.1毫秒字节数
     */
    private static final int TENTH_MILLISECONDS_BYTES = 4;
    
    /**
     * 最大积0.1毫秒值（无符号4字节）
     */
    private static final long MAX_TENTH_MILLISECONDS = (1L << (TENTH_MILLISECONDS_BYTES * 8)) - 1;
    
    /**
     * 0.1毫秒对应的纳秒数
     */
    private static final long NANOS_PER_TENTH_MILLISECOND = 100_000L; // 100微秒

    /**
     * 构造函数
     */
    public RelativeTenthMillisecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeTenthMillisecond";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param timeObj 目标时间对象
     * @return 积0.1毫秒值（4字节无符号整数）
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

            // 计算相对0.1毫秒数
            long tenthMilliseconds = calculateRelativeTenthMilliseconds(baseTime, time);

            // 验证范围
            if (tenthMilliseconds < 0 || tenthMilliseconds > MAX_TENTH_MILLISECONDS) {
                throw new IllegalArgumentException(
                    String.format("积0.1毫秒值超出范围 [0, %d]，当前值: %d", MAX_TENTH_MILLISECONDS, tenthMilliseconds));
            }

            return AviatorLong.valueOf(tenthMilliseconds);

        } catch (Exception e) {
            throw new RuntimeException("积0.1毫秒计算失败: " + e.getMessage(), e);
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
        // 尝试解析带微秒的格式
        try {
            return DATE_TIME_US_FORMATTER.parse(dateTimeStr);
        } catch (ParseException e1) {
            // 尝试解析带毫秒的格式
            try {
                SimpleDateFormat msFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                return msFormatter.parse(dateTimeStr);
            } catch (ParseException e2) {
                // 使用标准格式
                SimpleDateFormat standardFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return standardFormatter.parse(dateTimeStr);
            }
        }
    }

    /**
     * 计算相对0.1毫秒数
     *
     * @param baseTime 基准时间
     * @param time 目标时间
     * @return 相对0.1毫秒数
     */
    private long calculateRelativeTenthMilliseconds(Date baseTime, Date time) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime targetDateTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算纳秒数差
        long nanos = ChronoUnit.NANOS.between(baseDateTime, targetDateTime);
        
        // 转换为0.1毫秒单位（四舍五入）
        return Math.round((double) nanos / NANOS_PER_TENTH_MILLISECOND);
    }

    /**
     * 获取最大积0.1毫秒值
     *
     * @return 最大积0.1毫秒值
     */
    public static long getMaxTenthMilliseconds() {
        return MAX_TENTH_MILLISECONDS;
    }

    /**
     * 获取积0.1毫秒字节数
     *
     * @return 积0.1毫秒字节数
     */
    public static int getTenthMillisecondsBytes() {
        return TENTH_MILLISECONDS_BYTES;
    }

    /**
     * 获取0.1毫秒对应的纳秒数
     *
     * @return 0.1毫秒对应的纳秒数
     */
    public static long getNanosPerTenthMillisecond() {
        return NANOS_PER_TENTH_MILLISECOND;
    }
}
