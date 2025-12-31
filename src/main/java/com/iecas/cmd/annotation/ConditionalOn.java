package com.iecas.cmd.annotation;

import com.iecas.cmd.model.proto.ConditionalDependency.ConditionalAction;

import java.lang.annotation.*;

/**
 * 条件依赖注解
 * 用于在Java类配置中声明字段的条件依赖关系
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @ProtocolField(name = "控制位", length = 1, valueType = ValueType.UINT)
 * private int controlFlag;
 * 
 * @ProtocolField(name = "可选数据", length = 16, valueType = ValueType.HEX)
 * @ConditionalOn(conditionNode = "controlFlag", condition = "value == 1")
 * private String optionalData;
 * 
 * // 多个条件依赖
 * @ConditionalOn(conditionNode = "controlFlag", condition = "value == 1")
 * @ConditionalOn(conditionNode = "modeSelect", condition = "value >= 2")
 * private String complexData;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConditionalOn.List.class)
public @interface ConditionalOn {
    
    /**
     * 条件节点的引用（仅支持ID引用）
     * 格式要求：
     * - "#nodeId"              引用当前协议内节点
     * - "#protocolId:nodeId"   跨协议引用
     */
    String conditionNode();
    
    /**
     * 条件表达式，支持AviatorScript语法
     * 在表达式中使用 'value' 变量引用条件节点的值
     * 
     * 示例：
     * - "value == 1" - 当值等于1时
     * - "value > 0" - 当值大于0时
     * - "value in [1, 2, 3]" - 当值在指定范围内时
     * - "value == '0x01'" - 当值等于十六进制0x01时
     * - "value != null && value > 10" - 复合条件
     */
    String condition();
    
    /**
     * 条件匹配时的动作
     */
    ConditionalAction action() default ConditionalAction.ENABLE;
    
    /**
     * 条件不匹配时的动作
     */
    ConditionalAction elseAction() default ConditionalAction.DISABLE;
    
    /**
     * 条件依赖的优先级（数字越小优先级越高）
     * 当一个字段有多个条件依赖时，按优先级顺序执行
     */
    int priority() default 0;
    
    /**
     * 条件依赖的描述信息
     */
    String description() default "";
    
    /**
     * 容器注解，用于支持@ConditionalOn的重复使用
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ConditionalOn[] value();
    }
} 