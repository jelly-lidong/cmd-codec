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
 * 积日（相对天数）解码函数
 * 
 * <p>根据基准时间和积日值计算目标时间</p>
 * <p>支持有符号整数范围，可计算过去和未来的时间</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeDayDecode("2024-01-01 00:00:00", 14)  // 计算14天后的时间
 * relativeDayDecode("2024-01-01 00:00:00", -7)  // 计算7天前的时间
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeDayDecodeFunction extends AbstractFunction {

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
    public RelativeDayDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeDayDecode";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 基准时间对象
     * @param daysObj 积日值对象
     * @return 计算出的目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject baseTimeObj, AviatorObject daysObj) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(baseTimeObj, env);
            Number daysNumber = FunctionUtils.getNumberValue(daysObj, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            long days = daysNumber.longValue();

            // 验证积日值范围
            if (days > MAX_DAYS || days < MIN_DAYS) {
                throw new IllegalArgumentException(
                    String.format("积日值超出范围 [%d, %d]，当前值: %d", MIN_DAYS, MAX_DAYS, days));
            }

            // 解析基准时间
            Date baseTime = parseDateTime(baseTimeStr);

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, days);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积日解码失败: " + e.getMessage(), e);
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
     * 根据基准时间和积日值计算目标时间
     *
     * @param baseTime 基准时间
     * @param days 积日值（可正可负）
     * @return 目标时间字符串
     */
    private String calculateTargetTime(Date baseTime, long days) {
        // 转换为LocalDateTime进行计算
        LocalDateTime baseDateTime = baseTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        // 计算目标时间
        LocalDateTime targetDateTime = baseDateTime.plusDays(days);
        
        // 格式化为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return targetDateTime.format(formatter);
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
