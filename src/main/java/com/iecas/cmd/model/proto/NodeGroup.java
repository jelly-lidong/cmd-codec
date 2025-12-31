package com.iecas.cmd.model.proto;

import com.iecas.cmd.model.enums.ValueType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 组容器类
 * 用于在分组保持策略中包装每组元素，保持组的结构层次
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NodeGroup extends Node {
    
    /**
     * 组索引（从1开始）
     */
    private int groupIndex;
    
    /**
     * 组ID后缀
     */
    private String groupIdSuffix;
    
    /**
     * 组名称后缀
     */
    private String groupNameSuffix;
    
    /**
     * 组内元素类型
     */
    private String elementType;
    
    /**
     * 组内解析策略
     */
    private String resolveStrategy;
    
    /**
     * 组内节点列表
     */
    private List<Node> groupNodes;
    
    public NodeGroup() {
        super();
        // 注意：tagName是final字段，无法通过setter修改
        this.setValueType(ValueType.HEX);
        this.setLength(0); // 动态计算
    }
    
    /**
     * 创建组容器
     */
    public static NodeGroup create(String baseId, String baseName, int groupIndex,
                                   String idSuffix, String nameSuffix,
                                   String elementType, String resolveStrategy) {
        NodeGroup container = new NodeGroup();
        container.setId(baseId + idSuffix);
        container.setName(baseName + nameSuffix);
        container.setGroupIndex(groupIndex);
        container.setGroupIdSuffix(idSuffix);
        container.setGroupNameSuffix(nameSuffix);
        container.setElementType(elementType);
        container.setResolveStrategy(resolveStrategy);
        return container;
    }
    
    @Override
    public int getLength() {
        if (groupNodes == null || groupNodes.isEmpty()) {
            return 0;
        }
        
        int totalLength = 0;
        for (Node node : groupNodes) {
            totalLength += node.getLength();
        }
        return totalLength;
    }
    
    @Override
    public List<Node> getChildren() {
        return groupNodes;
    }
    
    @Override
    public void setChildren(List<Node> children) {
        this.groupNodes = children;
    }
    
    @Override
    public String getTagName() {
        return "group";
    }

    @Override
    public boolean isStructureNode() {
        return true;
    }
} 