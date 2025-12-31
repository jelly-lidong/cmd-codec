package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议节点组注解
 * 用于标注协议中的动态节点组字段，支持运行时动态添加多个协议组
 * 支持多组多层协议嵌套，组内可以是任意类型的对象
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * // 简单节点组
 * @ProtocolNodeGroup(
 *     id = "data_nodes"
 * )
 * private List<Node> dataNodes;
 *
 * // 协议对象组
 * @ProtocolNodeGroup(
 *     id = "sensor_groups",
 *     resolveStrategy = GroupResolveStrategy.GROUP_CONTAINER
 * )
 * private List<SensorProtocol> sensorGroups;
 *
 * // 自定义对象组
 * @ProtocolNodeGroup(
 *     id = "custom_blocks"
 * )
 * private List<CustomDataBlock> customBlocks;
 * }
 * </pre>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>支持多组协议重复</li>
 *   <li>支持多层协议嵌套</li>
 *   <li>支持任意类型的组内对象</li>
 *   <li>自动处理节点ID和名称的唯一性</li>
 *   <li>支持固定重复次数和表达式计算</li>
 *   <li>提供多种解析策略</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolNodeGroup {
    
    /**
     * 节点组ID（用于表达式引用和依赖管理）
     * 如果为空，则使用字段名作为ID
     */
    String id() default "";
    
    /**
     * 节点组名称
     * 如果为空，则使用字段名作为名称
     */
    String name() default "";
    
    /**
     * 节点组描述
     */
    String description() default "";
    
    /**
     * 节点组的最大容量
     * 0表示无限制
     */
    int maxSize() default 0;
    
    /**
     * 节点组的最小容量
     * 默认为0，表示可以为空
     */
    int minSize() default 0;
    
    /**
     * 是否可选
     * 如果为true，则该节点组可以为空或不存在
     */
    boolean optional() default false;
    
    /**
     * 字段顺序（用于确定编解码顺序）
     */
    int order() default 0;
    
    /**
     * 节点组的前置条件表达式
     * 当表达式为true时，该节点组才会被处理
     */
    String condition() default "";





    
    /**
     * 是否递归解析组内对象
     * 当为true时，会深度解析组内对象的协议结构
     */
    boolean recursive() default true;
    
    /**
     * 组内对象的解析策略
     */
    GroupResolveStrategy resolveStrategy() default GroupResolveStrategy.FLATTEN;

    /**
     * 重复时用于区分节点ID的后缀格式，使用String.format风格，占位符为一个整数索引（从1开始）。
     * 例如："_%d" => 原ID为"sensor"，第2组为"sensor_2"。
     */
    String idSuffixPattern() default "_%d";

    /**
     * 重复时用于区分节点名称的后缀格式，使用String.format风格，占位符为一个整数索引（从1开始）。
     * 例如："[%d]" => 原名称为"传感器"，第2组为"传感器[2]"。
     */
    String nameSuffixPattern() default "[%d]";
} 