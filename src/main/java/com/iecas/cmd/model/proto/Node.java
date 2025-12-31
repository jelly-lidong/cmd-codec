package com.iecas.cmd.model.proto;

import com.iecas.cmd.model.enums.EndianType;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * INode 接口的实现类
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Node implements INode {

    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String name;

    private String fieldName;

    @XmlAttribute
    private int length;
    
    @XmlAttribute
    private ValueType valueType = ValueType.HEX;
    
    @XmlAttribute
    private EndianType endianType = EndianType.BIG;
    
    @XmlAttribute
    private String value;
    
    @XmlAttribute
    private String fwdExpr;

    @XmlAttribute
    private String fwdExprResult;
    
    @XmlAttribute
    private String bwdExpr;
    
    @XmlAttribute
    private String range;
    
    @XmlAttribute
    private String charset = "UTF-8";

    @XmlAttribute
    private boolean optional = false;

    @XmlAttribute
    private float order;

    @XmlElement(name = "node")
    private List<Node> children = new ArrayList<>();

    @XmlElement(name = "enumRange")
    private List<EnumRange> enumRanges = new ArrayList<>();

    // 枚举映射，用于存储枚举值和描述的对应关系
    private Map<String, String> enumMap;

    private final String tagName = "node";

    private String path;

    // 反演验证相关字段
    /**
     * 反演结果值
     */
    private Object decodedValue;

    /**
     * 变换后的反演值（应用反向表达式后的值）
     */
    private Object transformedValue;

    /**
     * 反演源码数据
     */
    private String sourceData;

    /**
     * 验证结果（是否通过）
     */
    private Boolean validationResult;

    /**
     * 验证状态描述
     */
    private String validationStatus;

    /**
     * 验证错误信息
     */
    private String validationError;

    /**
     * 节点在编码结果中的起始位位置（从0开始）
     */
    private int startBitPosition = -1;

    /**
     * 节点在编码结果中的结束位位置（不包含，即[start, end)）
     */
    private int endBitPosition = -1;

    // 条件依赖相关字段
    /**
     * 条件依赖配置列表
     */
    @XmlElement(name = "conditionalDependency")
    private List<ConditionalDependency> conditionalDependencies = new ArrayList<>();

    /**
     * 节点当前启用状态
     */
    private boolean enabled = true;

    /**
     * 节点启用状态的原因描述
     */
    private String enabledReason;

    // 填充配置相关字段
    /**
     * 填充配置
     */
    @XmlElement(name = "paddingConfig")
    private PaddingConfig paddingConfig;

    /**
     * 实际数据长度（不包括填充）
     */
    private int actualDataLength;

    @Override
    public String getTagName() {
        return tagName;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public int getLength() {
        return length;
    }



    @Override
    public ValueType getValueType() {
        return valueType;
    }


    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value != null ? value.toString() : null;
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }


    /**
     * 获取前向表达式
     * @return 前向表达式
     */
    public String getFwdExpr() {
        // 处理CDATA包装的表达式
        if (fwdExpr != null && fwdExpr.startsWith("<![CDATA[") && fwdExpr.endsWith("]]>")) {
            // 移除CDATA包装
            return fwdExpr.substring(9, fwdExpr.length() - 3);
        }
        return fwdExpr;
    }

    @Override
    public void setFwdExprResult(Object fwdExprResult) {
        this.fwdExprResult = fwdExprResult != null ? fwdExprResult.toString() : null;
    }

    /**
     * 获取后向表达式
     * @return 后向表达式
     */
    public String getBwdExpr() {
        // 处理CDATA包装的表达式
        if (bwdExpr != null && bwdExpr.startsWith("<![CDATA[") && bwdExpr.endsWith("]]>")) {
            // 移除CDATA包装
            return bwdExpr.substring(9, bwdExpr.length() - 3);
        }
        return bwdExpr;
    }


    @Override
    public String getRange() {
        return range;
    }


    public EndianType getEndianType() {
        return endianType;
    }

    public void setEndianType(EndianType endianType) {
        this.endianType = endianType;
    }

    @Override
    public boolean isBigEndian() {
        return endianType == EndianType.BIG;
    }


    @Override
    public String getCharset() {
        return charset;
    }


    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public INode getParent() {
        return null;
    }

    @Override
    public INode getChild(String name) {
        for (INode child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "INode{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", lengthUnit='BIT'" +
                ", valueType=" + valueType +
                ", value='" + value + '\'' +
                '}';
    }

    // 反演验证相关方法实现
    @Override
    public Object getDecodedValue() {
        return decodedValue;
    }

    @Override
    public void setDecodedValue(Object decodedValue) {
        this.decodedValue = decodedValue;
    }

    @Override
    public Object getTransformedValue() {
        return transformedValue;
    }

    @Override
    public void setTransformedValue(Object transformedValue) {
        this.transformedValue = transformedValue;
    }

    @Override
    public String getSourceData() {
        return sourceData;
    }

    @Override
    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    @Override
    public Boolean getValidationResult() {
        return validationResult;
    }

    @Override
    public void setValidationResult(Boolean validationResult) {
        this.validationResult = validationResult;
    }

    @Override
    public String getValidationStatus() {
        return validationStatus;
    }

    @Override
    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public String getValidationError() {
        return validationError;
    }

    @Override
    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }

    /**
     * 获取枚举范围列表
     * @return 枚举范围列表
     */
    @Override
    public List<EnumRange> getEnumRanges() {
        return enumRanges;
    }

    // 条件依赖相关方法实现
    @Override
    public List<ConditionalDependency> getConditionalDependencies() {
        return conditionalDependencies;
    }

    @Override
    public void setConditionalDependencies(List<ConditionalDependency> conditionalDependencies) {
        this.conditionalDependencies = conditionalDependencies != null ? conditionalDependencies : new ArrayList<>();
    }

    @Override
    public void addConditionalDependency(ConditionalDependency conditionalDependency) {
        if (conditionalDependency != null && conditionalDependency.isValid()) {
            if (this.conditionalDependencies == null) {
                this.conditionalDependencies = new ArrayList<>();
            }
            this.conditionalDependencies.add(conditionalDependency);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getEnabledReason() {
        return enabledReason;
    }

    @Override
    public void setEnabledReason(String enabledReason) {
        this.enabledReason = enabledReason;
    }

    // 填充配置相关方法实现
    @Override
    public PaddingConfig getPaddingConfig() {
        return paddingConfig;
    }

    @Override
    public void setPaddingConfig(PaddingConfig paddingConfig) {
        this.paddingConfig = paddingConfig;
    }

    @Override
    public boolean isPaddingNode() {
        return paddingConfig != null;
    }

    @Override
    public int getActualDataLength() {
        return actualDataLength > 0 ? actualDataLength : length;
    }

    @Override
    public void setActualDataLength(int actualDataLength) {
        this.actualDataLength = actualDataLength;
    }

    @Override
    public boolean isStructureNode() {
        return false;
    }

    /**
     * 获取节点在编码结果中的起始位位置
     * @return 起始位位置（从0开始），-1表示未设置
     */
    public int getStartBitPosition() {
        return startBitPosition;
    }

    /**
     * 设置节点在编码结果中的起始位位置
     * @param startBitPosition 起始位位置（从0开始）
     */
    public void setStartBitPosition(int startBitPosition) {
        this.startBitPosition = startBitPosition;
    }

    /**
     * 获取节点在编码结果中的结束位位置
     * @return 结束位位置（不包含，即[start, end)），-1表示未设置
     */
    public int getEndBitPosition() {
        return endBitPosition;
    }

    /**
     * 设置节点在编码结果中的结束位位置
     * @param endBitPosition 结束位位置（不包含，即[start, end)）
     */
    public void setEndBitPosition(int endBitPosition) {
        this.endBitPosition = endBitPosition;
    }

    /**
     * 获取位置范围的字符串表示
     * @return 位置范围字符串，格式为"[start:end]"，如果未设置则返回"[-:-]"
     */
    public String getBitPositionRange() {
        if (startBitPosition >= 0 && endBitPosition >= 0) {
            return String.format("[%d:%d]", startBitPosition, endBitPosition);
        }
        return "[-:-]";
    }
} 