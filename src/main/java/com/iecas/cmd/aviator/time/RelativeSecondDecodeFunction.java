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
 * 积秒（相对秒数）解码函数
 * 
 * <p>根据基准时间和积秒值计算目标时间</p>
 * <p>支持无符号整数范围，可计算未来的时间</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeSecondDecode("2024-01-01 00:00:00", 3600)  // 计算1小时后的时间
 * relativeSecondDecode("2024-01-01 00:00:00", 86400) // 计算1天后的时间
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeSecondDecodeFunction extends AbstractFunction {

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
    public RelativeSecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeSecondDecode";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param secondsObj 积秒值对象
     * @return 计算出的目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject baseTimeObj, AviatorObject secondsObj) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(baseTimeObj, env);
            Number secondsNumber = FunctionUtils.getNumberValue(secondsObj, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            long seconds = secondsNumber.longValue();

            // 验证积秒值范围
            if (seconds < 0 || seconds > MAX_SECONDS) {
                throw new IllegalArgumentException(
                    String.format("积秒值超出范围 [0, %d]，当前值: %d", MAX_SECONDS, seconds));
            }

            // 解析基准时间
            Date baseTime = parseDateTime(baseTimeStr);

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, seconds);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积秒解码失败: " + e.getMessage(), e);
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
     * 根据基准时间和积秒值计算目标时间
     *
     * @param baseTime 基准时间
     * @param seconds 积秒值（非负）
     * @return 目标时间字符串
     */
    private String calculateTargetTime(Date baseTime, long seconds) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算目标时间
        LocalDateTime targetDateTime = baseDateTime.plusSeconds(seconds);
        
        // 格式化为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return targetDateTime.format(formatter);
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
