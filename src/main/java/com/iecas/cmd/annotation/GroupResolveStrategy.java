package com.iecas.cmd.annotation;

/**
 * 组解析策略枚举
 * 用于指定如何处理组内对象的解析结果
 */
public enum GroupResolveStrategy {
    /**
     * 扁平化：将所有解析结果平铺到同一层级
     */
    FLATTEN,
    
    /**
     * 分组保持：保持组的结构，每组作为一个容器
     */
    GROUP_CONTAINER,
    
    /**
     * 混合模式：根据对象类型选择最佳策略
     */
    MIXED
} 