package com.iecas.cmd.engine;

import com.iecas.cmd.model.proto.INode;

import java.util.HashMap;
import java.util.Map;

/**
 * 表达式执行上下文
 * 用于管理表达式执行过程中的各种数据
 */
public class ExpressionContext {
    // 节点值缓存
    private final Map<String, Object> valueCache = new HashMap<>();
    // 编码结果缓存
    private final Map<String, byte[]> encodedCache = new HashMap<>();
    // 临时变量
    private final Map<String, Object> tempVars = new HashMap<>();
    // 当前节点
    private INode currentNode;
    // 父节点
    private INode parentNode;

    /**
     * 设置当前节点
     */
    public void setCurrentNode(INode node) {
        this.currentNode = node;
        tempVars.put("current", node.getName());
    }

    /**
     * 设置父节点
     */
    public void setParentNode(INode node) {
        this.parentNode = node;
        tempVars.put("parent", node);
    }

    /**
     * 获取节点值
     */
    public Object getValue(String nodeName) {
        // 先查找临时变量
        if (tempVars.containsKey(nodeName)) {
            return tempVars.get(nodeName);
        }
        // 再查找值缓存
        return valueCache.get(nodeName);
    }

    /**
     * 设置节点值
     */
    public void setValue(String nodeName, Object value) {
        valueCache.put(nodeName, value);
    }

    /**
     * 获取编码结果
     */
    public byte[] getEncodedData(String nodeName) {
        return encodedCache.get(nodeName);
    }

    /**
     * 设置编码结果
     */
    public void setEncodedData(String nodeName, byte[] data) {
        encodedCache.put(nodeName, data);
    }

    /**
     * 设置临时变量
     */
    public void setTempVar(String name, Object value) {
        tempVars.put(name, value);
    }

    /**
     * 获取当前节点
     */
    public INode getCurrentNode() {
        return currentNode;
    }

    /**
     * 获取父节点
     */
    public INode getParentNode() {
        return parentNode;
    }

    /**
     * 清除临时变量
     */
    public void clearTempVars() {
        tempVars.clear();
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        valueCache.clear();
        encodedCache.clear();
        tempVars.clear();
        currentNode = null;
        parentNode = null;
    }
    
    /**
     * 获取所有变量
     * 返回包含临时变量和节点值的完整Map
     * @return 变量Map
     */
    public Map<String, Object> getVariables() {
        Map<String, Object> allVars = new HashMap<>();
        allVars.putAll(valueCache);
        allVars.putAll(tempVars);
        return allVars;
    }
} 