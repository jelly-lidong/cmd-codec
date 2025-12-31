package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class RelativeSecondAndTenthMillisecondDecodeFunction extends AbstractFunction {

    /**
     * 日期时间格式（带微秒精度）
     */
    private static final String DATE_TIME_US_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
     * 积毫秒最大值的字节数
     */
    private static final int MILLISECONDS_BYTES = 2;
    private static final int SECOND_BYTES = 4;

    /**
     * 构造函数
     */
    public RelativeSecondAndTenthMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeSecondAndTenthMillisecondDecode";
    }

    /**
     * 执行函数
     *
     * @param env                  执行环境
     * @param baseTimeObj          基准时间对象
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

            long[] secondAndTenthMillisecond = decodeRelativeSecondAndTenthMillisecond(tenthMillisecondsNumber.longValue());
            long second = secondAndTenthMillisecond[0];
            long tenthMillisecond = secondAndTenthMillisecond[1];

            // 解析基准时间
            LocalDateTime baseTime = LocalDateTime.parse(baseTimeStr, DateTimeFormatter.ofPattern(DATE_TIME_US_FORMAT));

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, second, tenthMillisecond);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积0.1毫秒解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据基准时间和积0.1毫秒值计算目标时间
     *
     * @return 目标时间字符串
     */
    private String calculateTargetTime(LocalDateTime baseDateTime, long second, long tenthMillisecond) {
        // 格式化为字符串（带微秒精度）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_US_FORMAT);
        return baseDateTime.plusSeconds(second).plusNanos(tenthMillisecond * NANOS_PER_TENTH_MILLISECOND).format(formatter);
    }


    /**
     * 解码6字节整数为积秒和积0.1毫秒
     *
     * @param encodedValue 编码后的6字节整数值
     * @return 积秒和积0.1毫秒的数组 [积秒, 积毫秒]
     */
    public static long[] decodeRelativeSecondAndTenthMillisecond(long encodedValue) {
        // 提取积0.1毫秒（后2字节）
        long milliseconds = encodedValue & ((1L << (MILLISECONDS_BYTES * 8)) - 1);

        // 提取积秒（前4字节）
        long second = (encodedValue >> (MILLISECONDS_BYTES * 8)) & ((1L << (SECOND_BYTES * 8)) - 1);

        return new long[]{second, milliseconds};
    }
}
