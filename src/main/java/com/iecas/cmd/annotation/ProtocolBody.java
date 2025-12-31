package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议体注解
 * 用于标识协议体部分
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolBody {
    
    /**
     * 协议体ID（用于表达式引用）
     */
    String id() default "";
    
    /**
     * 协议体名称
     */
    String name() default "";
    
    /**
     * 顺序
     */
    int order() default 0;
} 