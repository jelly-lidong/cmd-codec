package com.iecas.cmd.annotation;

import com.iecas.cmd.model.enums.EndianType;
import com.iecas.cmd.model.enums.ValueType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议字段注解
 * 用于标注协议节点的属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolNode {
    
    /**
     * 字段ID（用于表达式引用）
     */
    String id() default "";
    
    /**
     * 字段名称
     */
    String name() default "";
    
    /**
     * 字段长度（位数）
     */
    int length() default 0;
    
    /**
     * 编制时用的值类型，根据该类型获取匹配的编解码器
     */
    ValueType valueType() default ValueType.HEX;
    
    /**
     * 字节序
     */
    EndianType endian() default EndianType.BIG;
    
    /**
     * 字符集（用于字符串类型）
     */
    String charset() default "UTF-8";
    
    /**
     * 默认值
     */
    String value() default "";
    
    /**
     * 前向表达式
     * 表达式中的节点引用仅支持：#nodeId 或 #protocolId:nodeId
     */
    String fwdExpr() default "";
    
    /**
     * 反向表达式
     * 表达式中的节点引用仅支持：#nodeId 或 #protocolId:nodeId
     */
    String bwdExpr() default "";
    
    /**
     * 取值范围
     */
    String range() default "";
    
    /**
     * 是否可选
     */
    boolean optional() default false;
    
    /**
     * 字段顺序（用于确定编解码顺序）
     */
    int order() default 0;

    /**
     * 描述
     */
    String desc() default "";
} 