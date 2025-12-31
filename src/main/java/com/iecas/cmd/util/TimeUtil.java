package com.iecas.cmd.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间计算工具类
 */
public class TimeUtil {

    /**
     * 时间格式
     */
    public static final String PATTERN_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String PATTERN_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String yyyyMMdd = "yyyyMMdd";

    public static final String yyyy_MM_dd = "yyyy-MM-dd";

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    /**
     * 格式化时间时需要使用
     */
    private static final List<DateTimeFormatter> FORMATTERS = new ArrayList<>();

    static {
        FORMATTERS.add(DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD_HH_MM_SS));
        FORMATTERS.add(DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD_HH_MM_SS_SSS));
        FORMATTERS.add(DateTimeFormatter.ofPattern(yyyyMMddHHmmss));
        FORMATTERS.add(DateTimeFormatter.ofPattern(yyyy_MM_dd));
        FORMATTERS.add(DateTimeFormatter.ofPattern(yyyyMMdd));
    }


    /**
     * 时间格式对象
     */
    public static final DateTimeFormatter TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD_HH_MM_SS);

    public static final DateTimeFormatter TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS_SSS = DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD_HH_MM_SS_SSS);

    public static final DateTimeFormatter TIME_FORMATTER_yyyyMMddHHmmss = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);


    /**
     * 常用基准时间
     */
    public static final LocalDateTime BASE_TIME_2006_1_1 = LocalDateTime.of(2006, 1, 1, 0, 0, 0);

    public static final LocalDateTime BASE_TIME_2000_1_1 = LocalDateTime.of(2000, 1, 1, 0, 0, 0);


    public static String getYyyyMMddHHmmssNow(){
        return LocalDateTime.now().format(TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /**
     * 计算两个时间之间的天数差
     *
     * @param timeStr     时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @param baseTimeStr 基准时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @return 天数差
     */
    public static long calculateDays(String timeStr, String baseTimeStr) {
        LocalDateTime time = LocalDateTime.parse(timeStr, TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        LocalDateTime baseTime = LocalDateTime.parse(baseTimeStr, TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        return ChronoUnit.DAYS.between(baseTime, time);
    }

    /**
     * 计算两个时间之间的秒数差
     *
     * @param time     时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @param baseTime 基准时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @return 秒数差
     */
    public static long calculateSeconds(LocalDateTime baseTime, LocalDateTime time) {
        return ChronoUnit.SECONDS.between(baseTime, time);
    }

    /**
     * 计算两个时间之间的毫秒数差
     *
     * @param timeStr     时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @param baseTimeStr 基准时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @return 毫秒数差
     */
    public static long calculateMilliseconds(String timeStr, String baseTimeStr) {
        LocalDateTime time = LocalDateTime.parse(timeStr, TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        LocalDateTime baseTime = LocalDateTime.parse(baseTimeStr, TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        return ChronoUnit.MILLIS.between(baseTime, time);
    }

    /**
     * 将时间戳转换为时间字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @return 时间字符串，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String timestampToString(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC);
        return dateTime.format(TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 将时间字符串转换为时间戳
     *
     * @param timeStr 时间字符串，格式：yyyy-MM-dd HH:mm:ss
     * @return 时间戳（毫秒）
     */
    public static long stringToTimestamp(String timeStr) {
        LocalDateTime dateTime = LocalDateTime.parse(timeStr, TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * 获取当前时间戳（秒）
     *
     * @return 当前时间戳（秒）
     */
    public static long currentTimestamp() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳（毫秒）
     */
    public static long currentTimestampMillis() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 时间戳转日期时间字符串
     *
     * @param timestamp 时间戳（秒）
     * @param pattern   日期时间格式
     * @return 日期时间字符串
     */
    public static String timestampToString(long timestamp, String pattern) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp),
                ZoneId.systemDefault()
        );
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 日期时间字符串转时间戳
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     日期时间格式
     * @return 时间戳（秒）
     */
    public static long stringToTimestamp(String dateTimeStr, String pattern) {
        LocalDateTime dateTime = LocalDateTime.parse(
                dateTimeStr,
                DateTimeFormatter.ofPattern(pattern)
        );
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * 时间戳转 LocalDateTime
     *
     * @param timestamp 时间戳（秒）
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToDateTime(long timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp),
                ZoneId.systemDefault()
        );
    }

    /**
     * LocalDateTime 转时间戳
     *
     * @param dateTime LocalDateTime
     * @return 时间戳（秒）
     */
    public static long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }


    public static LocalDateTime parseLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("时间字符出串不能为空");
        }
        String trimmedStr = dateTimeStr.trim();
        DateTimeParseException lastException = null;

        // 尝试所有已知格式
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmedStr, formatter);
            } catch (DateTimeParseException e) {
                lastException = e;
                // 继续尝试下一个格式
            }
        }
        // 如果所有格式都失败，抛出最后一个异常
        throw new DateTimeParseException("无法解析时间字符串：" + dateTimeStr + "。", dateTimeStr, 0, lastException);
    }



    /**
     * 将一个整数值拆成两个整数值
     */
    public static long[] splitLong(long longValue, int length1, int length2) {
        long value1 = longValue & ((1L << (length2 * 8)) - 1);

        long value2 = (longValue >> (length2 * 8)) & ((1L << (length1 * 8)) - 1);

        return new long[]{value2, value1};
    }

    /**
     * 合并两个整数值变一个值
     * @param value1 值1
     * @param length1 值1 占的字节数
     * @param value2 值2
     * @param length2 值2 占的字节数
     */
    public static long mergeLong(long value1, int length1,long value2, int length2) {
        // 将积日转换为无符号2字节
        long encodedDays = value1 < 0 ? (1L << (length1 * 8)) + value1 : value1;

        // 编码：前2字节存储积日，后4字节存储积毫秒
        return (encodedDays << (length2 * 8)) | value2;
    }
} 