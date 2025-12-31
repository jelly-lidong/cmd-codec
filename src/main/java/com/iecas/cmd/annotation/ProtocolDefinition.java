package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议定义注解
 * 用于标识协议类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolDefinition {
    
    /**
     * 协议ID
     */
    String id() default "";
    
    /**
     * 协议名称
     */
    String name();
    
    /**
     * 协议版本
     */
    String version() default "1.0";
    
    /**
     * 协议描述
     */
    String description() default "";
} 