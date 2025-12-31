package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态节点列表注解
 * 用于标注协议中的动态节点列表字段，支持运行时动态添加多个节点
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Nodes(
 *     id = "dynamic_sensors",
 *     name = "传感器数据列表",
 *     description = "动态传感器数据节点列表"
 * )
 * private List<ProtocolNode> sensorNodes;
 * }
 * </pre>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>支持动态数量的节点</li>
 *   <li>保持节点间的依赖关系处理</li>
 *   <li>支持条件依赖和表达式计算</li>
 *   <li>自动处理拓扑排序</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolNodes {
    
    /**
     * 节点列表ID（用于表达式引用和依赖管理）
     * 如果为空，则使用字段名作为ID
     */
    String id() default "";
    
    /**
     * 节点列表名称
     * 如果为空，则使用字段名作为名称
     */
    String name() default "";
    
    /**
     * 节点列表描述
     */
    String description() default "";
    
    /**
     * 节点列表的最大容量
     * 0表示无限制
     */
    int maxSize() default 0;
    
    /**
     * 节点列表的最小容量
     * 默认为0，表示可以为空
     */
    int minSize() default 0;
    
    /**
     * 是否可选
     * 如果为true，则该节点列表可以为空或不存在
     */
    boolean optional() default false;
    
    /**
     * 字段顺序（用于确定编解码顺序）
     */
    int order() default 0;
    
    /**
     * 节点列表的前置条件表达式
     * 当表达式为true时，该节点列表才会被处理
     */
    String condition() default "";
    
    /**
     * 节点列表长度的计算表达式
     * 用于动态确定列表中应该包含多少个节点
     * 例如："#header_count" 或 "length(#data_buffer) / 4"
     */
    String lengthExpr() default "";
} 