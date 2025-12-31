package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 协议枚举注解
 * 用于定义枚举值映射
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolEnum {
    
    /**
     * 枚举值映射
     * 格式: {"value1:desc1", "value2:desc2", ...}
     * 注意位数要和length对应，比如length=8，如果类型是bit,则"0b00000011:哈哈"是正确的，"0b11:哈哈"是错误的
     */
    String[] values();
} 