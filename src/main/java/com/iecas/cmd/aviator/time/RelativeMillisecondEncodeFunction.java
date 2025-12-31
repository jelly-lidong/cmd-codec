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
 * 积毫秒（相对毫秒数）编码函数
 * 
 * <p>计算目标时间相对于基准时间的累积毫秒数</p>
 * <p>使用4字节存储，支持无符号整数范围</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeMillisecond("2024-01-01 00:00:00", "2024-01-01 00:00:01.500")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeMillisecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式（带毫秒）
     */
    private static final String DATE_TIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /**
     * 日期时间格式化器（带毫秒）
     */
    private static final SimpleDateFormat DATE_TIME_MS_FORMATTER = new SimpleDateFormat(DATE_TIME_MS_FORMAT);
    
    /**
     * 积毫秒字节数
     */
    private static final int MILLISECONDS_BYTES = 4;
    
    /**
     * 最大积毫秒值（无符号4字节）
     */
    private static final long MAX_MILLISECONDS = (1L << (MILLISECONDS_BYTES * 8)) - 1;

    /**
     * 构造函数
     */
    public RelativeMillisecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeMillisecond";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param timeObj 目标时间对象
     * @return 积毫秒值（4字节无符号整数）
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

            // 计算相对毫秒数
            long milliseconds = calculateRelativeMilliseconds(baseTime, time);

            // 验证范围
            if (milliseconds < 0 || milliseconds > MAX_MILLISECONDS) {
                throw new IllegalArgumentException(
                    String.format("积毫秒值超出范围 [0, %d]，当前值: %d", MAX_MILLISECONDS, milliseconds));
            }

            return AviatorLong.valueOf(milliseconds);

        } catch (Exception e) {
            throw new RuntimeException("积毫秒计算失败: " + e.getMessage(), e);
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
        // 尝试解析带毫秒的格式
        try {
            return DATE_TIME_MS_FORMATTER.parse(dateTimeStr);
        } catch (ParseException e) {
            // 如果不带毫秒，则使用标准格式
            SimpleDateFormat standardFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return standardFormatter.parse(dateTimeStr);
        }
    }

    /**
     * 计算相对毫秒数
     *
     * @param baseTime 基准时间
     * @param time 目标时间
     * @return 相对毫秒数
     */
    private long calculateRelativeMilliseconds(Date baseTime, Date time) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime targetDateTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算毫秒数差
        return ChronoUnit.MILLIS.between(baseDateTime, targetDateTime);
    }

    /**
     * 获取最大积毫秒值
     *
     * @return 最大积毫秒值
     */
    public static long getMaxMilliseconds() {
        return MAX_MILLISECONDS;
    }

    /**
     * 获取积毫秒字节数
     *
     * @return 积毫秒字节数
     */
    public static int getMillisecondsBytes() {
        return MILLISECONDS_BYTES;
    }
}
