package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议头注解
 * 用于标识协议头部分
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolHeader {
    
    /**
     * 协议头ID（用于表达式引用）
     */
    String id() default "";
    
    /**
     * 协议头名称
     */
    String name() default "";
    
    /**
     * 顺序
     */
    int order() default 0;
} 