package com.iecas.cmd.util;

import java.util.regex.Pattern;

/**
 * 常量定义
 */
public class Constants {
    // 表达式引擎相关常量
    public static final boolean DEFAULT_CACHE_ENABLED = true;
    public static final int DEFAULT_MAX_CACHE_SIZE = 1000;
    public static final boolean DEFAULT_TRACE_ENABLED = false;
    public static final int DEFAULT_MAX_LOOP_COUNT = 1000;

    // 其他常量
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    // 错误消息
    public static final String ERROR_INVALID_LENGTH_UNIT = "无效的长度单位: %s";
    public static final String ERROR_INVALID_VALUE_TYPE = "无效的值类型: %s";
    public static final String ERROR_INVALID_ENDIAN = "无效的字节序: %s";
    public static final String ERROR_EXPRESSION_EVALUATION = "表达式计算错误: %s";
    public static final String ERROR_CODEC_NOT_FOUND = "未找到对应的编解码器: %s";
    public static final String ERROR_INVALID_NODE = "无效的节点: %s";
    public static final String ERROR_INVALID_DATA = "无效的数据: %s";

    // 表达式相关正则表达式
    /**
     * 函数调用正则表达式
     * 匹配形如: functionName('stringParam') 或 functionName(#idParam) 的函数调用
     * 支持无限多个参数，参数可以是字符串或ID引用
     * 例如: length('abc'), crc16(#1), sum(#1, #2, #3, 'test')
     * 支持ID中包含连字符，如: length(#param-id_1)
     */
    public static final String REGEX_FUNCTION = "([a-zA-Z0-9_]+)\\s*\\(\\s*([^)]+)\\s*\\)";

    /**
     * 复杂引用正则表达式
     * 匹配形如: #id.property 或 #id[index] 的复杂引用
     * 例如: #1.length, #2[0]
     * 支持ID中包含连字符，如: #param-id.property
     */
    public static final String REGEX_COMPLEX_REF = "#([a-zA-Z0-9_.-]+)(?:\\.([a-zA-Z0-9_]+)|\\[([0-9]+)\\])";

    /**
     * 括号匹配正则表达式
     * 用于检查表达式中的括号是否完整
     * 匹配未闭合的#引用或非法的#引用
     */
    public static final String REGEX_BRACKET = "#[a-zA-Z0-9_]*$|[^#]#[^a-zA-Z0-9_]";

    /**
     * 引号匹配正则表达式
     * 用于检查表达式中的引号是否完整
     * 匹配未闭合的单引号或双引号
     */
    public static final String REGEX_QUOTE = "'[^']*$|\"[^\"]*$";

    /**
     * 函数语法正则表达式
     * 用于检查函数调用的语法是否正确
     * 匹配未闭合的函数调用
     */
    public static final String REGEX_FUNCTION_SYNTAX = "([a-zA-Z0-9_]+)\\s*\\([^)]*$";

    /**
     * 编译后的函数调用正则表达式模式
     */
    public static final Pattern PATTERN_FUNCTION = Pattern.compile(REGEX_FUNCTION);

    /**
     * 编译后的复杂引用正则表达式模式
     */
    public static final Pattern PATTERN_COMPLEX_REF = Pattern.compile(REGEX_COMPLEX_REF);

    /**
     * 编译后的括号匹配正则表达式模式
     */
    public static final Pattern PATTERN_BRACKET = Pattern.compile(REGEX_BRACKET);

    /**
     * 编译后的引号匹配正则表达式模式
     */
    public static final Pattern PATTERN_QUOTE = Pattern.compile(REGEX_QUOTE);

    /**
     * 编译后的函数语法正则表达式模式
     */
    public static final Pattern PATTERN_FUNCTION_SYNTAX = Pattern.compile(REGEX_FUNCTION_SYNTAX);
} 