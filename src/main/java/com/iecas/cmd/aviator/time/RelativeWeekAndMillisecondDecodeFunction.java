package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.util.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 反向计算：根据基准时间、积周和周内秒计算目标时间
 * <p>编码格式：前2字节存储积周，后4字节存储周内秒（相对当周开始）</p>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeWeekAndMillisecondDecodeFunction extends AbstractFunction {

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
    private static final int MILLISECONDS_BYTES = 4;

    /**
     * 积周最大值（2字节有符号整数）
     */
    private static final long MAX_WEEKS = (1L << (WEEKS_BYTES * 8 - 1)) - 1;

    /**
     * 周内秒最大值（4字节无符号整数）
     */
    private static final long MAX_MILLISECONDS = (1L << (MILLISECONDS_BYTES * 8)) - 1;

    /**
     * 构造函数
     */
    public RelativeWeekAndMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeWeekAndMillisecondDecode";
    }

    /**
     * 执行函数（两个参数版本 - 反向计算）
     *
     * @param env          执行环境
     * @param arg1         第一个参数（基准时间）
     * @param encodedValue 第二个参数（编码后的6字节整数值）
     * @return 目标时间字符串
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject encodedValue) {
        try {
            // 获取参数值
            String baseTimeStr = FunctionUtils.getStringValue(arg1, env);
            Number encodedValueNum = FunctionUtils.getNumberValue(encodedValue, env);

            // 验证参数
            if (baseTimeStr == null || baseTimeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("基准时间不能为空");
            }

            // 解析时间字符串
            LocalDateTime baseDateTime = LocalDateTime.parse(baseTimeStr, TimeUtil.TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
            // 解码积周和周内秒
            long[] weekAndSecond = decodeRelativeWeekAndMilllisecond(encodedValueNum.longValue());
            long weeks = weekAndSecond[0];
            long milliseconds = weekAndSecond[1];

            // 计算目标时间
            String targetTime = calculateTargetTime(baseDateTime, weeks, milliseconds);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("反向周时间计算失败: " + e.getMessage(), e);
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
     * 根据基准时间、积周和周内秒计算目标时间
     *
     * @param baseDateTime 基准时间
     * @param weeks        积周
     * @param millSeconds  周内秒
     * @return 目标时间字符串
     */
    private String calculateTargetTime(LocalDateTime baseDateTime, long weeks, long millSeconds) {
        // 验证参数范围
        if (weeks < -MAX_WEEKS || weeks > MAX_WEEKS) {
            throw new IllegalArgumentException("积周超出范围: " + weeks + "，允许范围: [" + (-MAX_WEEKS) + ", " + MAX_WEEKS + "]");
        }
        if (millSeconds < 0 || millSeconds > MAX_MILLISECONDS) {
            throw new IllegalArgumentException("周内秒超出范围: " + millSeconds + "，允许范围: [0, " + MAX_MILLISECONDS + "]");
        }

        // 计算目标周的开始时间
        LocalDateTime targetWeekStart = baseDateTime.plusWeeks(weeks);

        LocalDateTime weekStart = getWeekStart(targetWeekStart);

        // 计算目标时间（当周开始 + 周内秒）
        LocalDateTime targetDateTime = weekStart.plusSeconds(millSeconds / 1000);

        // 格式化返回结果
        return targetDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
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
     * 解码6字节整数为积周和周内秒
     *
     * @param encodedValue 编码后的6字节整数值
     * @return 包含积周和周内秒的数组 [积周, 周内秒]
     */
    public static long[] decodeRelativeWeekAndMilllisecond(long encodedValue) {
        // 提取周内秒（后4字节）
        long milliseconds = encodedValue & ((1L << (MILLISECONDS_BYTES * 8)) - 1);

        // 提取积周（前2字节）
        long encodedWeeks = (encodedValue >> (MILLISECONDS_BYTES * 8)) & ((1L << (WEEKS_BYTES * 8)) - 1);

        // 判断积周是否为负数（最高位为1表示负数）
        long weeks;
        if ((encodedWeeks & (1L << (WEEKS_BYTES * 8 - 1))) != 0) {
            // 负数，需要转换回有符号值
            weeks = encodedWeeks - (1L << (WEEKS_BYTES * 8));
        } else {
            weeks = encodedWeeks;
        }

        return new long[]{weeks, milliseconds};
    }
}
