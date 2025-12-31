package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.iecas.cmd.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 积秒和毫秒
 *
 * <p>计算目标时间相对于基准时间的累积秒数</p>
 * <p>使用4字节存储，支持无符号整数范围</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * relativeSecond("2024-01-01 00:00:00", "2024-01-01 12:30:45")
 * </pre>
 *
 * @author CMD Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RelativeSecondAndMillisecondEncodeFunction extends AbstractFunction {


    /**
     * 积秒字节数
     */
    private static final int SECONDS_BYTES = 4;

    /**
     * 积1毫秒节数
     */
    private static final int MILL_SECOND_BYTES = 2;

    /**
     * 最大积秒和积1毫秒值（无符号6字节）
     */
    private static final long MAX_SECONDS = (1L << ((SECONDS_BYTES + MILL_SECOND_BYTES) * 8)) - 1;

    /**
     * 1毫秒对应的纳秒数
     */
    private static final long NANOS_PER_MILLISECOND = 1000_000L;

    /**
     * 构造函数
     */
    public RelativeSecondAndMillisecondEncodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeSecondAndMillisecond";
    }

    /**
     * 执行函数
     *
     * @param env         执行环境
     * @param baseTimeObj 基准时间对象
     * @param timeObj     目标时间对象
     * @return 积秒值（4字节无符号整数）
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
            LocalDateTime baseTime = TimeUtil.parseLocalDateTime(baseTimeStr);
            LocalDateTime time = TimeUtil.parseLocalDateTime(timeStr);


            // 计算相对秒数
            long seconds = calculateRelativeTime(baseTime, time);

            // 验证范围
            if (seconds < 0 || seconds > MAX_SECONDS) {
                throw new IllegalArgumentException(
                        String.format("积秒值超出范围 [0, %d]，当前值: %d", MAX_SECONDS, seconds));
            }

            return AviatorLong.valueOf(seconds);

        } catch (Exception e) {
            throw new RuntimeException("积秒计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算相对秒数
     *
     * @param baseDateTime   基准时间
     * @param targetDateTime 目标时间
     * @return 相对秒数
     */
    private long calculateRelativeTime(LocalDateTime baseDateTime, LocalDateTime targetDateTime) {
        // 转换为LocalDateTime进行计算
        long seconds = ChronoUnit.SECONDS.between(baseDateTime, targetDateTime);

        // 计算纳秒数差
        long nanos = targetDateTime.getNano();

        // 转换为0.1毫秒单位（四舍五入）
        long tenthMillSecond = Math.round((double) nanos / NANOS_PER_MILLISECOND);
        // 计算秒数差
        return (seconds << (MILL_SECOND_BYTES * 8)) | tenthMillSecond;
    }

}
