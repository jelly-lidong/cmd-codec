package com.iecas.cmd.engine;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.iecas.cmd.exception.ExpressionException;

import java.util.Map;

/**
 * 表达式引擎接口
 * 定义了表达式计算的基本方法
 */
public interface ExpressionEngine {
    /**
     * 计算表达式
     * @param expression 表达式字符串
     * @param context 上下文信息
     * @return 计算结果
     * @throws ExpressionException 表达式计算异常
     */
    Object evaluate(String expression, Map<String, Object> context) throws ExpressionException;

    /**
     * 获取字符串结果
     * @param expression 表达式字符串
     * @param context 表达式上下文
     * @return 字符串结果
     * @throws ExpressionException 表达式计算异常
     */
    String getStringResult(String expression, ExpressionContext context) throws ExpressionException;

    /**
     * 获取数值结果
     * @param expression 表达式字符串
     * @param context 表达式上下文
     * @return 数值结果
     * @throws ExpressionException 表达式计算异常
     */
    Number getNumberResult(String expression, ExpressionContext context) throws ExpressionException;

    /**
     * 获取布尔结果
     * @param expression 表达式字符串
     * @param context 表达式上下文
     * @return 布尔结果
     * @throws ExpressionException 表达式计算异常
     */
    boolean getBooleanResult(String expression, ExpressionContext context) throws ExpressionException;

    /**
     * 注册自定义函数
     * @param function 函数对象
     */
    void registerFunction(AbstractFunction function);

    /**
     * 注册自定义变量
     * @param name 变量名
     * @param value 变量值
     */
    void registerVariable(String name, Object value);

    /**
     * 清除所有变量
     */
    void clearVariables();

    /**
     * 获取表达式引擎配置
     * @return 配置对象
     */
    ExpressionEngineConfig getConfig();

    /**
     * 设置表达式引擎配置
     * @param config 配置对象
     */
    void setConfig(ExpressionEngineConfig config);
}

