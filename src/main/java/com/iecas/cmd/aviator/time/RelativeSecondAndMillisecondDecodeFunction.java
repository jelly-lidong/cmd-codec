package com.iecas.cmd.aviator.time;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 积1毫秒（相对1毫秒数）解码函数
 *
 * <p>根据基准时间和积1毫秒值计算目标时间</p>
 * <p>支持无符号整数范围，可计算未来的时间</p>
 * <p>精度为1毫秒（100微秒）</p>
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
public class RelativeSecondAndMillisecondDecodeFunction extends AbstractFunction {


    /**
     * 1毫秒对应的纳秒数
     */
    private static final long NANOS_PER_MILLISECOND = 1000_000L; // 100微秒

    /**
     * 构造函数
     */
    public RelativeSecondAndMillisecondDecodeFunction() {
        super();
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    @Override
    public String getName() {
        return "relativeSecondAndMillisecondDecode";
    }

    /**
     * 执行函数
     *
     * @param env                  执行环境
     * @param baseTimeObj          基准时间对象
     * @param tenthMillisecondsObj 积1毫秒值对象
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

            long[] secondAndTenthMillisecond = TimeUtil.splitLong(tenthMillisecondsNumber.longValue(),4,2);
            long second = secondAndTenthMillisecond[0];
            long millisecond = secondAndTenthMillisecond[1];

            // 解析基准时间
            LocalDateTime baseTime = TimeUtil.parseLocalDateTime(baseTimeStr);

            // 计算目标时间
            String targetTime = calculateTargetTime(baseTime, second, millisecond);

            return new AviatorString(targetTime);

        } catch (Exception e) {
            throw new RuntimeException("积1毫秒解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据基准时间和积1毫秒值计算目标时间
     *
     * @return 目标时间字符串
     */
    private String calculateTargetTime(LocalDateTime baseDateTime, long second, long tenthMillisecond) {
        // 格式化为字符串（带微秒精度）
        return baseDateTime.plusSeconds(second).plusNanos(tenthMillisecond * NANOS_PER_MILLISECOND).format(TimeUtil.TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
    }

}
