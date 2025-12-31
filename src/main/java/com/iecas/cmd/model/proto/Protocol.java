package com.iecas.cmd.model.proto;

import com.iecas.cmd.model.enums.ValueType;
import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 协议类
 */
@Data
@XmlRootElement(name = "protocol")
@XmlAccessorType(XmlAccessType.FIELD)
public class Protocol implements INode {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;


    @XmlElement(name = "header", type = Header.class)
    private Header header;

    @XmlElement(name = "body", type = Body.class)
    private Body body;

    @XmlElement(name = "tail", type = Tail.class)
    private Tail tail;

    @XmlElement(name = "node")
    private List<Node> nodes = new ArrayList<>();

    private String tagName = "protocol";

    private String path;

    public Protocol() {
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public int getLength() {
        int length = 0;
        if (header != null) {
            for (INode node : header.getNodes()) {
                length += node.getLength();
            }
        }
        if (body != null) {
            length += body.getLength();
        }
        if (tail != null) {
            length += tail.getLength();
        }
        return length;
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
        // 协议根节点不支持设置值
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        for (Node node : nodes) {
            addNodeOrGroup(children, node);
        }
        return children;
    }

    /**
     * 递归添加节点或节点组中的所有子节点
     */
    private void addNodeOrGroup(List<Node> collector, Node node) {
        if (node instanceof com.iecas.cmd.model.proto.NodeGroup) {
            NodeGroup group = (NodeGroup) node;
            for (Node child : group.getChildren()) {
                addNodeOrGroup(collector, child);
            }
        } else {
            collector.add(node);
        }
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
    public INode getParent() {
        return null;
    }

    @Override
    public INode getChild(String name) {
        if (header != null && header.getName().equals(name)) {
            return header;
        }
        if (body != null && body.getName().equals(name)) {
            return body;
        }
        if (tail != null && tail.getName().equals(name)) {
            return tail;
        }
        return null;
    }

    @Override
    public byte[] getData() {
        int totalSize = 0;
        List<byte[]> dataList = new ArrayList<>();

        // 收集header数据
        if (header != null) {
            byte[] headerData = header.getData();
            if (headerData != null) {
                totalSize += headerData.length;
                dataList.add(headerData);
            }
        }

        // 收集body数据
        if (body != null) {
            byte[] bodyData = body.getData();
            if (bodyData != null) {
                totalSize += bodyData.length;
                dataList.add(bodyData);
            }
        }

        // 收集check数据
        if (tail != null) {
            byte[] checkData = tail.getData();
            if (checkData != null) {
                totalSize += checkData.length;
                dataList.add(checkData);
            }
        }

        // 合并所有数据
        byte[] result = new byte[totalSize];
        int offset = 0;
        for (byte[] data : dataList) {
            System.arraycopy(data, 0, result, offset, data.length);
            offset += data.length;
        }

        return result;
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
} 