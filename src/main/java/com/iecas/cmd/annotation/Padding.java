package com.iecas.cmd.annotation;

import com.iecas.cmd.model.proto.PaddingConfig.PaddingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 填充注解
 * 用于在Java类配置中声明字段的填充规则
 * 
 * 使用示例：
 * <pre>
 * {@code
 * // 固定长度填充 - 填充到80字节
 * @ProtocolField(name = "数据域", length = 64, valueType = ValueType.HEX)
 * @Padding(paddingType = PaddingType.FIXED_LENGTH, targetLength = 640, paddingValue = "0xAA")
 * private String dataField;
 * 
 * // 动态填充 - 根据表达式计算填充长度
 * @ProtocolField(name = "填充数据", length = 0, valueType = ValueType.HEX)
 * @Padding(paddingType = PaddingType.DYNAMIC, lengthExpression = "80 * 8 - usedLength", paddingValue = "0x00")
 * private String paddingData;
 * 
 * // 补齐剩余空间填充
 * @ProtocolField(name = "尾部填充", length = 0, valueType = ValueType.HEX)
 * @Padding(paddingType = PaddingType.FILL_REMAINING, containerNode = "#body", paddingValue = "0xFF")
 * private String tailPadding;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Padding {
    
    /**
     * 填充类型
     */
    PaddingType paddingType() default PaddingType.FIXED_LENGTH;
    
    /**
     * 目标长度（位数）
     * 对于FIXED_LENGTH类型，表示填充后的总长度
     * 对于ALIGNMENT类型，表示对齐的字节数
     */
    int targetLength() default 0;
    
    /**
     * 填充值（十六进制字符串）
     * 例如："0xAA", "0x00", "0xFF"
     */
    String paddingValue() default "0x00";
    
    /**
     * 填充值重复模式
     * true: 重复填充值直到达到目标长度
     * false: 只填充一次指定的值
     */
    boolean repeatPattern() default true;
    
    /**
     * 最小填充长度（位数）
     * 即使计算出的填充长度小于此值，也要填充到最小长度
     */
    int minPaddingLength() default 0;
    
    /**
     * 最大填充长度（位数）
     * 填充长度不能超过此值
     */
    int maxPaddingLength() default Integer.MAX_VALUE;
    
    /**
     * 填充长度计算表达式
     * 用于DYNAMIC类型，支持AviatorScript语法
     * 可以引用其他节点的长度和值
     * 例如："targetLength - currentLength", "align(currentLength, 8) - currentLength"
     */
    String lengthExpression() default "";
    
    /**
     * 参考容器节点
     * 用于FILL_REMAINING类型，指定要填充到哪个容器的总长度
     * 例如："#body", "parent", "protocol"
     */
    String containerNode() default "";
    
    /**
     * 容器固定长度（位数）
     * 用于FILL_CONTAINER类型，指定容器的固定总长度
     * 容器长度 = 所有子节点长度之和 + 填充长度
     */
    int containerFixedLength() default 0;
    
    /**
     * 是否自动计算容器长度
     * true: 从容器节点的length属性获取固定长度
     * false: 使用containerFixedLength指定的长度
     */
    boolean autoCalculateContainerLength() default true;
    
    /**
     * 是否启用填充
     * 可以通过条件表达式动态控制是否进行填充
     */
    boolean enabled() default true;
    
    /**
     * 填充启用条件表达式
     * 当表达式结果为true时才进行填充
     * 例如："remainingLength > 0", "needPadding == true"
     */
    String enableCondition() default "";
    
    /**
     * 填充描述信息
     */
    String description() default "";
} 