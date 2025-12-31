package com.iecas.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 区间函数注解
 * 用于在Java类配置中声明字段的区间函数
 *
 * 使用示例：
 * <pre>
 * {@code
 * @BetweenFunction
 * private class XXX extends AbstractFunction{
 *
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BetweenFunction {

}
