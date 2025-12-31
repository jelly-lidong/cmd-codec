package com.iecas.cmd.model.proto;

import com.iecas.cmd.model.enums.ValueType;
import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 协议校验类
 */
@Data
@XmlRootElement(name = "tail")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tail implements INode {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private Integer length;

    @XmlAttribute
    private int order;

    private String path;

    @XmlElement(name = "node")
    private List<Node> nodes = new ArrayList<>();

    private String tagName = "tail";

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getLength() {
        int totalLength = 0;
        for (INode node : nodes) {
            totalLength += node.getLength();
        }
        return totalLength;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.HEX;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {
        // 协议校验不支持设置值
    }

    @Override
    public List<Node> getChildren() {
        return nodes;
    }

    @Override
    public String getFwdExpr() {
        return null;
    }

    @Override
    public void setFwdExprResult(Object value) {

    }

    @Override
    public String getFwdExprResult() {
        return "";
    }

    @Override
    public String getBwdExpr() {
        return null;
    }

    @Override
    public String getRange() {
        return null;
    }

    @Override
    public boolean isBigEndian() {
        return true;
    }

    @Override
    public String getCharset() {
        return "UTF-8";
    }

    @Override
    public boolean isOptional() {
        return false;
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
    public boolean isStructureNode() {
        return true;
    }

    @Override
    public INode getParent() {
        return null;
    }

    @Override
    public INode getChild(String name) {
        for (INode child : nodes) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }
} 