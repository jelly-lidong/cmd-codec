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
 * 积0.1毫秒（相对0.1毫秒数）解码函数
 * 
 * <p>根据基准时间和积0.1毫秒值计算目标时间</p>
 * <p>支持无符号整数范围，可计算未来的时间</p>
 * <p>精度为0.1毫秒（100微秒）</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeTenthMillisecondDecode("2024-01-01 00:00:00.000000", 10)  // 计算1毫秒后的时间
 * relativeTenthMillisecondDecode("2024-01-01 00:00:00.000000", 15)  // 计算1.5毫秒后的时间
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeTenthMillisecondDecodeFunction extends AbstractFunction {

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
    public RelativeTenthMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeTenthMillisecondDecode";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param tenthMillisecondsObj 积0.1毫秒值对象
     * @return 计算出的目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject baseTimeObj, AviatorObject tenthMillisecondsObj) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(baseTimeObj, env);
            Number tenthMillisecondsNumber = FunctionUtils.getNumberValue(tenthMillisecondsObj, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            long tenthMilliseconds = tenthMillisecondsNumber.longValue();

            // 验证积0.1毫秒值范围
            if (tenthMilliseconds < 0 || tenthMilliseconds > MAX_TENTH_MILLISECONDS) {
                throw new IllegalArgumentException(
                    String.format("积0.1毫秒值超出范围 [0, %d]，当前值: %d", MAX_TENTH_MILLISECONDS, tenthMilliseconds));
            }

            // 解析基准时间
            Date baseTime = parseDateTime(baseTimeStr);

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, tenthMilliseconds);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积0.1毫秒解码失败: " + e.getMessage(), e);
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
     * 根据基准时间和积0.1毫秒值计算目标时间
     *
     * @param baseTime 基准时间
     * @param tenthMilliseconds 积0.1毫秒值（非负）
     * @return 目标时间字符串
     */
    private String calculateTargetTime(Date baseTime, long tenthMilliseconds) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算目标时间（转换为纳秒）
        long totalNanos = tenthMilliseconds * NANOS_PER_TENTH_MILLISECOND;
        LocalDateTime targetDateTime = baseDateTime.plusNanos(totalNanos);
        
        // 格式化为字符串（带微秒精度）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_US_FORMAT);
        return targetDateTime.format(formatter);
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
