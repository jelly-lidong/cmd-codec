package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.iecas.cmd.util.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * 相对日时间计算函数（积日和积毫秒）
 *
 * <p>该函数计算指定时间相对于基准时间的积日和积毫秒，并将结果编码为6字节整数。</p>
 * <p>编码格式：前2字节存储积日，后4字节存储积毫秒（相对当天零点）</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeDayAndMillisecond("2024-01-01 00:00:00", "2024-01-15 14:30:25.123")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeDayAndMillisecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式（包含毫秒）
     */
    private static final String DATE_TIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);

    /**
     * 日期时间毫秒格式化器
     */
    private static final SimpleDateFormat DATE_TIME_MS_FORMATTER = new SimpleDateFormat(DATE_TIME_MS_FORMAT);

    /**
     * 积日最大值的字节数
     */
    private static final int DAYS_BYTES = 2;

    /**
     * 积毫秒最大值的字节数
     */
    private static final int MILLISECONDS_BYTES = 4;

    /**
     * 构造函数
     */
    public RelativeDayAndMillisecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeDayAndMillisecond";
    }

    /**
     * 执行函数
     *
     * @param env         执行环境
     * @param baseTimeObj 第一个参数（基准时间）
     * @param timeObj     第二个参数（目标时间）
     * @return 编码后的6字节整数值
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
            Date time = parseDateTimeWithMilliseconds(timeStr);

            // 计算相对时间
            long result = calculateRelativeDayAndMillisecond(baseTime, time);
            return AviatorLong.valueOf(result);

        } catch (ParseException e) {
            throw new RuntimeException("时间格式解析失败，请使用格式: " + DATE_TIME_FORMAT + " 或 " + DATE_TIME_MS_FORMAT, e);
        } catch (Exception e) {
            throw new RuntimeException("相对日时间计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析日期时间字符串（不包含毫秒）
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的Date对象
     * @throws ParseException 解析异常
     */
    private Date parseDateTime(String dateTimeStr) throws ParseException {
        return DATE_TIME_FORMATTER.parse(dateTimeStr.trim());
    }

    /**
     * 解析日期时间字符串（包含毫秒）
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析后的Date对象
     * @throws ParseException 解析异常
     */
    private Date parseDateTimeWithMilliseconds(String dateTimeStr) throws ParseException {
        String trimmedStr = dateTimeStr.trim();

        // 尝试解析包含毫秒的格式
        if (trimmedStr.contains(".")) {
            try {
                return DATE_TIME_MS_FORMATTER.parse(trimmedStr);
            } catch (ParseException e) {
                // 如果毫秒格式解析失败，尝试标准格式
                return DATE_TIME_FORMATTER.parse(trimmedStr);
            }
        } else {
            // 标准格式
            return DATE_TIME_FORMATTER.parse(trimmedStr);
        }
    }

    /**
     * 计算相对日时间并编码为6字节整数
     *
     * @param baseTime 基准时间
     * @param time     目标时间
     * @return 编码后的6字节整数值
     */
    private long calculateRelativeDayAndMillisecond(Date baseTime, Date time) {
        // 转换为LocalDateTime便于计算
        LocalDateTime baseDateTime = LocalDateTime.ofInstant(baseTime.toInstant(), ZoneId.systemDefault());
        LocalDateTime targetDateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());

        // 计算积日（相对天数）
        long days = ChronoUnit.DAYS.between(baseDateTime, targetDateTime);

        // 计算积毫秒（相对当天零点）
        LocalDateTime targetMidnight = targetDateTime.toLocalDate().atStartOfDay();
        long milliseconds = ChronoUnit.MILLIS.between(targetMidnight, targetDateTime);

        return TimeUtil.mergeLong(days, DAYS_BYTES, milliseconds, MILLISECONDS_BYTES);
    }

}
