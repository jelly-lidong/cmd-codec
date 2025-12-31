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
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 相对周时间计算函数
 *
 * <p>该函数计算指定时间相对于基准时间的积周和周内秒，并将结果编码为6字节整数。</p>
 * <p>编码格式：前2字节存储积周，后4字节存储周内秒（相对当周开始）</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeWeekAndSecond("2024-01-01 00:00:00", "2024-01-15 14:30:25")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeWeekAndSecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);

    /**
     * 积周最大值的字节数
     */
    private static final int WEEKS_BYTES = 2;

    /**
     * 周内秒最大值的字节数
     */
    private static final int SECONDS_BYTES = 4;

    /**
     * 积周最大值（2字节有符号整数）
     */
    private static final long MAX_WEEKS = (1L << (WEEKS_BYTES * 8 - 1)) - 1;

    /**
     * 周内秒最大值（4字节无符号整数）
     */
    private static final long MAX_SECONDS = (1L << (SECONDS_BYTES * 8)) - 1;

    /**
     * 一周的秒数
     */
    private static final long SECONDS_PER_WEEK = 7 * 24 * 3600;

    /**
     * 构造函数
     */
    public RelativeWeekAndSecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeWeekAndSecond";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param baseTimeObj 第一个参数（基准时间）
     * @param timeObj 第二个参数（目标时间）
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
            Date time = parseDateTime(timeStr);

            // 计算相对时间
            long result = calculateRelativeWeekAndSecond(baseTime, time);

            return AviatorLong.valueOf(result);

        } catch (ParseException e) {
            throw new RuntimeException("时间格式解析失败，请使用格式: " + DATE_TIME_FORMAT, e);
        } catch (Exception e) {
            throw new RuntimeException("相对周时间计算失败: " + e.getMessage(), e);
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
     * 计算相对周时间并编码为6字节整数
     *
     * @param baseTime 基准时间
     * @param time 目标时间
     * @return 编码后的6字节整数值
     */
    private long calculateRelativeWeekAndSecond(Date baseTime, Date time) {
        // 转换为LocalDateTime便于计算
        LocalDateTime baseDateTime = LocalDateTime.ofInstant(baseTime.toInstant(), ZoneId.systemDefault());
        LocalDateTime targetDateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());

        // 计算积周（相对周数）
        long weeks = ChronoUnit.WEEKS.between(baseDateTime, targetDateTime);

        // 计算周内秒（相对当周开始）
        LocalDateTime weekStart = getWeekStart(targetDateTime);
        long seconds = ChronoUnit.SECONDS.between(weekStart, targetDateTime);

        return encodeRelativeWeekAndSecond(weeks, seconds);
    }

    /**
     * 获取指定时间所在周的开始时间（周一00:00:00）
     *
     * @param dateTime 指定时间
     * @return 周开始时间
     */
    private LocalDateTime getWeekStart(LocalDateTime dateTime) {
        // 使用ISO标准：周一是第一天
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int dayOfWeek = dateTime.get(weekFields.dayOfWeek());
        
        // 计算到本周一的偏移天数
        int daysToMonday = dayOfWeek - 1;
        if (daysToMonday < 0) {
            daysToMonday += 7;
        }
        
        // 返回到本周一的00:00:00
        return dateTime.toLocalDate().minusDays(daysToMonday).atStartOfDay();
    }

    /**
     * 编码相对周时间为6字节整数
     *
     * @param weeks 积周
     * @param seconds 周内秒
     * @return 编码后的6字节整数值
     */
    private long encodeRelativeWeekAndSecond(long weeks, long seconds) {
        // 验证范围
        if (weeks < -MAX_WEEKS || weeks > MAX_WEEKS) {
            throw new IllegalArgumentException("积周超出范围: " + weeks + "，允许范围: [" + (-MAX_WEEKS) + ", " + MAX_WEEKS + "]");
        }
        if (seconds < 0 || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException("周内秒超出范围: " + seconds + "，允许范围: [0, " + MAX_SECONDS + "]");
        }

        // 将积周转换为无符号2字节
        long encodedWeeks = weeks < 0 ? (1L << (WEEKS_BYTES * 8)) + weeks : weeks;

        // 编码：前2字节存储积周，后4字节存储周内秒
        return (encodedWeeks << (SECONDS_BYTES * 8)) | seconds;
    }
}
