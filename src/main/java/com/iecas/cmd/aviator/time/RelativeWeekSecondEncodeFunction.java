package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.iecas.cmd.util.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 相对周时间计算函数
 *
 * <p>该函数计算指定时间相对于基准时间的周内秒，并将结果编码为6字节整数。</p>
 * <p>编码格式：4字节存储周内秒（相对当周开始）</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeWeekSecond("2024-01-01 00:00:00", "2024-01-15 14:30:25")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeWeekSecondEncodeFunction extends AbstractFunction {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式化器
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);


    /**
     * 周内秒最大值的字节数
     */
    private static final int SECONDS_BYTES = 4;


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
    public RelativeWeekSecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeWeekSecond";
    }

    /**
     * 执行函数
     *
     * @param env 执行环境
     * @param timeObj 第以个参数（目标时间）
     * @return 编码后的6字节整数值
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject timeObj) {
        try {
            // 获取参数值
            String timeStr = FunctionUtils.getStringValue(timeObj, env);

            if (timeStr == null || timeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("目标时间不能为空");
            }

            // 解析时间字符串
            LocalDateTime time = TimeUtil.parseLocalDateTime(timeStr);

            // 计算相对时间
            // 计算周内秒（相对当周开始）
            LocalDateTime weekStart = getWeekStart(time);

            long result =  ChronoUnit.SECONDS.between(weekStart, time);

            return AviatorLong.valueOf(result);

        } catch (Exception e) {
            throw new RuntimeException("相对周时间计算失败: " + e.getMessage(), e);
        }
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

}
