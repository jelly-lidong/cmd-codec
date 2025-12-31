package com.iecas.cmd.model.proto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 条件依赖配置
 * 用于描述节点之间的条件依赖关系
 * 
 * 示例：当节点A的值为1时，节点B有效；当节点A的值为0时，节点B无效
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ConditionalDependency {
    
    /**
     * 条件节点的引用（支持节点名称、节点ID或路径）
     */
    @XmlAttribute
    private String conditionNode;
    
    /**
     * 条件表达式，支持AviatorScript语法
     * 例如：
     * - "value == 1" 当值等于1时
     * - "value > 0" 当值大于0时  
     * - "value in [1, 2, 3]" 当值在指定范围内时
     * - "value == '0x01'" 当值等于十六进制0x01时
     */
    @XmlAttribute
    private String condition;
    
    /**
     * 条件匹配时的动作
     * ENABLE - 启用节点（默认）
     * DISABLE - 禁用节点
     */
    @XmlAttribute
    private ConditionalAction action = ConditionalAction.ENABLE;
    
    /**
     * 条件不匹配时的动作
     * DISABLE - 禁用节点（默认）
     * ENABLE - 启用节点
     */
    @XmlAttribute
    private ConditionalAction elseAction = ConditionalAction.DISABLE;
    
    /**
     * 条件依赖的优先级（数字越小优先级越高）
     * 当一个节点有多个条件依赖时，按优先级顺序执行
     */
    @XmlAttribute
    private int priority = 0;
    
    /**
     * 条件依赖的描述信息
     */
    @XmlAttribute
    private String description;
    
    /**
     * 条件依赖动作枚举
     */
    public enum ConditionalAction {
        /**
         * 启用节点 - 节点参与编解码过程
         */
        ENABLE,
        
        /**
         * 禁用节点 - 节点不参与编解码过程，跳过该节点
         */
        DISABLE,
        
        /**
         * 设置默认值 - 节点使用默认值参与编解码
         */
        SET_DEFAULT,
        
        /**
         * 清空值 - 节点值设置为空或零值
         */
        CLEAR_VALUE
    }
    
    /**
     * 默认构造函数
     */
    public ConditionalDependency() {
    }
    
    /**
     * 便捷构造函数
     * 
     * @param conditionNode 条件节点引用
     * @param condition 条件表达式
     */
    public ConditionalDependency(String conditionNode, String condition) {
        this.conditionNode = conditionNode;
        this.condition = condition;
    }
    
    /**
     * 便捷构造函数
     * 
     * @param conditionNode 条件节点引用
     * @param condition 条件表达式
     * @param action 条件匹配时的动作
     * @param elseAction 条件不匹配时的动作
     */
    public ConditionalDependency(String conditionNode, String condition, 
                                ConditionalAction action, ConditionalAction elseAction) {
        this.conditionNode = conditionNode;
        this.condition = condition;
        this.action = action;
        this.elseAction = elseAction;
    }
    
    /**
     * 验证条件依赖配置的有效性
     * 
     * @return 验证结果
     */
    public boolean isValid() {
        return conditionNode != null && !conditionNode.trim().isEmpty() &&
               condition != null && !condition.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("ConditionalDependency{conditionNode='%s', condition='%s', action=%s, elseAction=%s}", 
                           conditionNode, condition, action, elseAction);
    }
} 