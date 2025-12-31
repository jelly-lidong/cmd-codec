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
 * 积毫秒（相对毫秒数）解码函数
 * 
 * <p>根据基准时间和积毫秒值计算目标时间</p>
 * <p>支持无符号整数范围，可计算未来的时间</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeMillisecondDecode("2024-01-01 00:00:00.000", 1000)  // 计算1秒后的时间
 * relativeMillisecondDecode("2024-01-01 00:00:00.000", 1500)  // 计算1.5秒后的时间
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeMillisecondDecodeFunction extends AbstractFunction {

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
    public RelativeMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeMillisecondDecode";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param millisecondsObj 积毫秒值对象
     * @return 计算出的目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject baseTimeObj, AviatorObject millisecondsObj) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(baseTimeObj, env);
            Number millisecondsNumber = FunctionUtils.getNumberValue(millisecondsObj, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            long milliseconds = millisecondsNumber.longValue();

            // 验证积毫秒值范围
            if (milliseconds < 0 || milliseconds > MAX_MILLISECONDS) {
                throw new IllegalArgumentException(
                    String.format("积毫秒值超出范围 [0, %d]，当前值: %d", MAX_MILLISECONDS, milliseconds));
            }

            // 解析基准时间
            Date baseTime = parseDateTime(baseTimeStr);

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, milliseconds);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积毫秒解码失败: " + e.getMessage(), e);
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
     * 根据基准时间和积毫秒值计算目标时间
     *
     * @param baseTime 基准时间
     * @param milliseconds 积毫秒值（非负）
     * @return 目标时间字符串
     */
    private String calculateTargetTime(Date baseTime, long milliseconds) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算目标时间（使用纳秒精度）
        LocalDateTime targetDateTime = baseDateTime.plusNanos(milliseconds * 1_000_000);
        
        // 格式化为字符串（带毫秒）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_MS_FORMAT);
        return targetDateTime.format(formatter);
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
