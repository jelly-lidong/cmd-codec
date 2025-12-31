package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * 积秒（相对秒数）编码函数
 * 
 * <p>计算目标时间相对于基准时间的累积秒数</p>
 * <p>使用4字节存储，支持无符号整数范围</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeSecond("2024-01-01 00:00:00", "2024-01-01 12:30:45")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeSecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);
    
    /**
     * 积秒字节数
     */
    private static final int SECONDS_BYTES = 4;
    
    /**
     * 最大积秒值（无符号4字节）
     */
    private static final long MAX_SECONDS = (1L << (SECONDS_BYTES * 8)) - 1;

    /**
     * 构造函数
     */
    public RelativeSecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeSecond";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param timeObj 目标时间对象
     * @return 积秒值（4字节无符号整数）
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
            LocalDateTime baseTime = LocalDateTime.parse(baseTimeStr, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));

            // 计算相对秒数
            long seconds = calculateRelativeSeconds(baseTime, time);

            // 验证范围
            if (seconds < 0 || seconds > MAX_SECONDS) {
                throw new IllegalArgumentException(
                    String.format("积秒值超出范围 [0, %d]，当前值: %d", MAX_SECONDS, seconds));
            }

            return AviatorLong.valueOf(seconds);

        } catch (Exception e) {
            throw new RuntimeException("积秒计算失败: " + e.getMessage(), e);
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
     * 计算相对秒数
     *
     * @param baseDateTime 基准时间
     * @param targetDateTime 目标时间
     * @return 相对秒数
     */
    private long calculateRelativeSeconds(LocalDateTime baseDateTime, LocalDateTime targetDateTime) {
        // 转换为LocalDateTime进行计算

        // 计算秒数差
        return ChronoUnit.SECONDS.between(baseDateTime, targetDateTime);
    }

    /**
     * 获取最大积秒值
     *
     * @return 最大积秒值
     */
    public static long getMaxSeconds() {
        return MAX_SECONDS;
    }

    /**
     * 获取积秒字节数
     *
     * @return 积秒字节数
     */
    public static int getSecondsBytes() {
        return SECONDS_BYTES;
    }
}
