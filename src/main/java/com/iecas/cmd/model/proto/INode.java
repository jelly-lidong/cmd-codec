package com.iecas.cmd.model.proto;

import com.iecas.cmd.model.enums.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * 协议节点接口
 *
 * <p>
 * <b>作用与用途:</b>
 * 统一描述协议结构中每个节点（字段/元素）的属性和行为。
 * 便于协议树的递归遍历、依赖分析、表达式计算、数据编解码等操作。
 * 作为各类节点（如 ProtocolHeader、ProtocolBody、ProtocolCheck、普通Node等）的通用父接口，支持多态。
 * 典型场景
 * 协议解析、建模时，所有节点都实现该接口，便于统一处理。
 * 编解码、依赖图、表达式引擎等模块通过该接口与节点交互，无需关心具体实现。
 * 便于扩展和维护协议结构。
 * </p>
 */
public interface INode {



    /**
     * 获取节点ID
     */
    String getId();

    /**
     * 获取节点名称
     */
    String getName();

    /**
     * 获取节点长度
     */
    int getLength();

    /**
     * 获取值类型
     */
    ValueType getValueType();

    /**
     * 获取节点值
     */
    Object getValue();

    /**
     * 设置节点值
     */
    void setValue(Object value);


    /**
     * 获取子节点列表
     */
    default List<Node> getChildren(){
        return new ArrayList<>();
    }

    /**
     * 获取正向表达式
     */
    String getFwdExpr();

    void setFwdExprResult(Object value);

    String getFwdExprResult();


    /**
     * 获取反向表达式
     */
    String getBwdExpr();

    /**
     * 获取范围
     */
    String getRange();

    /**
     * 是否大端序
     */
    boolean isBigEndian();

    /**
     * 获取字符集
     */
    String getCharset();


    /**
     * 获取标签名称
     */
    String getTagName();

    /**
     * 获取父节点
     */
    INode getParent();

    /**
     * 根据名称获取子节点
     */
    INode getChild(String name);

    /**
     * 是否可选
     */
    boolean isOptional();
    
    /**
     * 获取节点的二进制数据
     * @return 节点的二进制数据
     */
    byte[] getData();


    String getPath();

    void setPath(String path);

    /**
     * 获取枚举范围列表
     * @return 枚举范围列表，如果没有枚举定义则返回null或空列表
     */
    default List<EnumRange> getEnumRanges() {
        return new ArrayList<>();
    }

    /**
     * 获取条件依赖配置列表
     * @return 条件依赖配置列表
     */
    default List<ConditionalDependency> getConditionalDependencies() {
        return null;
    }

    /**
     * 设置条件依赖配置列表
     * @param conditionalDependencies 条件依赖配置列表
     */
    default void setConditionalDependencies(List<ConditionalDependency> conditionalDependencies) {
        // 默认空实现
    }

    /**
     * 添加条件依赖配置
     * @param conditionalDependency 条件依赖配置
     */
    default void addConditionalDependency(ConditionalDependency conditionalDependency) {
        // 默认空实现
    }

    /**
     * 获取节点的当前启用状态
     * @return 节点是否启用
     */
    default boolean isEnabled() {
        return true; // 默认启用
    }

    /**
     * 设置节点的启用状态
     * @param enabled 是否启用
     */
    default void setEnabled(boolean enabled) {
        // 默认空实现
    }

    /**
     * 获取节点启用状态的原因描述
     * @return 状态原因描述
     */
    default String getEnabledReason() {
        return null;
    }

    /**
     * 设置节点启用状态的原因描述
     * @param reason 状态原因描述
     */
    default void setEnabledReason(String reason) {
        // 默认空实现
    }

    /**
     * 获取反演结果值
     * @return 反演后的值
     */
    default Object getDecodedValue() {
        return null;
    }

    /**
     * 设置反演结果值
     * @param decodedValue 反演后的值
     */
    default void setDecodedValue(Object decodedValue) {
        // 默认空实现
    }

    /**
     * 获取变换后的反演值（应用反向表达式后的值）
     * @return 变换后的反演值
     */
    default Object getTransformedValue() {
        return null;
    }

    /**
     * 设置变换后的反演值
     * @param transformedValue 变换后的反演值
     */
    default void setTransformedValue(Object transformedValue) {
        // 默认空实现
    }

    /**
     * 获取反演源码数据
     * @return 反演时使用的源码数据
     */
    default String getSourceData() {
        return null;
    }

    /**
     * 设置反演源码数据
     * @param sourceData 反演时使用的源码数据
     */
    default void setSourceData(String sourceData) {
        // 默认空实现
    }

    /**
     * 获取验证结果
     * @return 验证是否通过
     */
    default Boolean getValidationResult() {
        return null;
    }

    /**
     * 设置验证结果
     * @param validationResult 验证是否通过
     */
    default void setValidationResult(Boolean validationResult) {
        // 默认空实现
    }

    /**
     * 获取验证状态
     * @return 验证状态描述（PASS、FAIL、ERROR、SKIPPED_STRUCTURE等）
     */
    default String getValidationStatus() {
        return null;
    }

    /**
     * 设置验证状态
     * @param validationStatus 验证状态描述
     */
    default void setValidationStatus(String validationStatus) {
        // 默认空实现
    }

    /**
     * 获取验证错误信息
     * @return 验证错误信息
     */
    default String getValidationError() {
        return null;
    }

    /**
     * 设置验证错误信息
     * @param validationError 验证错误信息
     */
    default void setValidationError(String validationError) {
        // 默认空实现
    }

    /**
     * 获取填充配置
     * @return 填充配置，如果没有配置则返回null
     */
    default PaddingConfig getPaddingConfig() {
        return null;
    }

    /**
     * 设置填充配置
     * @param paddingConfig 填充配置
     */
    default void setPaddingConfig(PaddingConfig paddingConfig) {
        // 默认空实现
    }

    /**
     * 是否为填充节点
     * @return 是否为填充节点
     */
    default boolean isPaddingNode() {
        return getPaddingConfig() != null;
    }

    /**
     * 获取实际数据长度（不包括填充）
     * @return 实际数据长度
     */
    default int getActualDataLength() {
        return getLength();
    }

    /**
     * 设置实际数据长度
     * @param actualLength 实际数据长度
     */
    default void setActualDataLength(int actualLength) {
        // 默认空实现
    }

    /**
     * 是否是结构体
     */
     boolean isStructureNode();
} 