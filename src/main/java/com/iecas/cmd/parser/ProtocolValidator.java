package com.iecas.cmd.parser;

import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.model.proto.INode;

import java.util.List;

/**
 * 协议验证器
 * 负责验证协议结构的正确性
 */
public class ProtocolValidator {
    /**
     * 验证协议结构
     *
     * @param protocol 协议模型
     * @throws ProtocolParseException 验证失败时抛出异常
     */
    public void validate(Protocol protocol) throws ProtocolParseException {
        // 1. 验证协议基本信息
        validateBasicInfo(protocol);

        // 2. 验证所有子节点（header/body/check）
        validateNodes(protocol.getChildren(), "protocol");
    }

    /**
     * 验证协议基本信息
     */
    private void validateBasicInfo(Protocol protocol) throws ProtocolParseException {
        // 不再验证protocol的name属性
    }

    /**
     * 验证节点列表
     */
    private void validateNodes(List<Node> nodes, String section) throws ProtocolParseException {
        for (Node node : nodes) {
            validateNode(node, section);
        }
    }

    /**
     * 验证单个节点
     */
    private void validateNode(INode node, String section) throws ProtocolParseException {
        // 如果是 header/body/check 标签，则跳过验证
        String tagName = node.getTagName();
        if ("protocol".equals(tagName) || "header".equals(tagName) || "body".equals(tagName) || "tail".equals(tagName)) {
            // 只验证子节点
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                validateNodes(node.getChildren(), section + "." + node.getName());
            }
            return;
        }

        // 1. 验证必填属性
        if (node.getName() == null || node.getName().isEmpty()) {
            throw new ProtocolParseException(String.format("%s 中的节点名称不能为空", section));
        }

        // 3. 验证值类型
        if (node.getValueType() == null) {
            throw new ProtocolParseException(String.format("%s 中的节点 %s 值类型不能为空", section, node.getName()));
        }

        // 4. 递归校验子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            validateNodes(node.getChildren(), section + "." + node.getName());
        }
    }
} 