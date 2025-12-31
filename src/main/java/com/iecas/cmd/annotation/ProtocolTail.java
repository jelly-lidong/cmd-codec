package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议尾部注解
 * 用于标识协议校验部分
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolTail {
    
    /**
     * 校验ID（用于表达式引用）
     */
    String id() default "";
    
    /**
     * 校验名称
     */
    String name() default "";
    
    /**
     * 顺序
     */
    int order() default 0;
} 