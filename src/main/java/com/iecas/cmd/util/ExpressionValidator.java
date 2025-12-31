package com.iecas.cmd.util;

import com.googlecode.aviator.AviatorEvaluator;
import com.iecas.cmd.model.proto.INode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式验证器
 * 用于验证表达式的语法正确性和引用有效性
 */
@Slf4j
public class ExpressionValidator {

    // 节点引用模式，匹配 #nodeId 或 #协议ID:nodeId 格式，支持连字符和冒号
    // 支持两种格式：
    // - #节点ID（同协议内引用，如 #nodeId）
    // - #协议ID:节点ID（跨协议引用，如 #protocolId:nodeId）
    private static final Pattern NODE_REF_PATTERN = Pattern.compile("#([a-zA-Z0-9_-]+(?::[a-zA-Z0-9_-]+)?)");

    /**
     * 验证表达式语法
     *
     * @param expression 表达式
     * @return 语法是否正确
     */
    public boolean validateSyntax(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        // 先处理HTML转义字符
        String unescapedExpression = unescapeHtmlEntities(expression);
        // 使用Aviator编译表达式来验证语法
        log.debug("[表达式验证] 使用Aviator编译表达式来验证语法: {}", expression);
        AviatorEvaluator.compile(unescapedExpression);
        log.debug("[表达式验证] 语法验证通过: {}", expression);
        return true;

    }

    /**
     * 反转义HTML实体编码
     *
     * @param expression 原始表达式
     * @return 反转义后的表达式
     */
    private String unescapeHtmlEntities(String expression) {
        if (expression == null) {
            return null;
        }

        return expression
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    /**
     * 验证表达式引用的节点是否存在
     *
     * @param expression 表达式
     * @param nodeMap    可用的节点映射
     * @return 引用是否有效
     */
    public boolean validateReferences(String expression, Map<String, INode> nodeMap) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        // 先处理HTML转义字符，然后提取表达式中的节点引用
        String unescapedExpression = unescapeHtmlEntities(expression);
        Set<String> references = extractNodeReferences(unescapedExpression);

        boolean allValid = true;
        for (String reference : references) {
            if (!nodeMap.containsKey(reference)) {
                throw new IllegalArgumentException(String.format("[表达式验证] 引用的节点不存在: %s 在表达式: %s", reference, expression));
            }
        }

        return allValid;
    }

    /**
     * 验证表达式引用的节点是否存在于依赖图中
     * 支持验证 #节点ID 和 #协议ID:节点ID 两种格式
     *
     * @param expression 表达式
     * @param dependencyGraph 依赖图
     * @param currentProtocolId 当前协议ID，用于解析简单的节点ID
     * @return 引用是否有效
     */
    public boolean validateReferencesInGraph(String expression, com.iecas.cmd.engine.DependencyGraph dependencyGraph, String currentProtocolId) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        // 先处理HTML转义字符，然后提取表达式中的节点引用
        String unescapedExpression = unescapeHtmlEntities(expression);
        Set<String> references = extractNodeReferences(unescapedExpression);

        boolean allValid = true;
        for (String reference : references) {
            // 使用支持协议作用域的检查方法
            if (!dependencyGraph.hasNodeId(reference, currentProtocolId)) {
                throw new IllegalArgumentException(String.format("[表达式验证] 引用的节点不存在: %s 在表达式: %s", reference, expression));
            }
        }

        return allValid;
    }
    
    /**
     * 验证表达式引用的节点是否存在于依赖图中（兼容旧版本）
     * 
     * @deprecated 请使用 validateReferencesInGraph(String, DependencyGraph, String) 并明确指定协议ID
     */
    @Deprecated
    public boolean validateReferencesInGraph(String expression, com.iecas.cmd.engine.DependencyGraph dependencyGraph) {
        // 如果没有指定协议ID，尝试从第一个引用中提取，或使用默认值
        return validateReferencesInGraph(expression, dependencyGraph, null);
    }

    /**
     * 验证表达式在给定上下文中是否可执行
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 是否可执行
     */
    public boolean validateContext(String expression, Map<String, Object> context) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        // 先处理HTML转义字符
        String unescapedExpression = unescapeHtmlEntities(expression);
        Set<String> references = extractNodeReferences(unescapedExpression);

        for (String reference : references) {
            // 检查直接匹配
            if (context.containsKey(reference)) {
                continue;
            }

            // 检查带后缀的匹配
            boolean found = false;
            String[] suffixes = {"_value", "_encoded", "_node"};
            for (String suffix : suffixes) {
                if (context.containsKey(reference + suffix)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                log.debug("[表达式验证] 上下文中缺少节点: " + reference + " 在表达式: " + expression);
                return false;
            }
        }

        return true;
    }

    /**
     * 获取表达式中缺失的依赖项
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 缺失的依赖项集合
     */
    public Set<String> getMissingDependencies(String expression, Map<String, Object> context) {
        Set<String> missing = new HashSet<>();

        if (expression == null || expression.trim().isEmpty()) {
            return missing;
        }

        // 先处理HTML转义字符
        String unescapedExpression = unescapeHtmlEntities(expression);
        Set<String> references = extractNodeReferences(unescapedExpression);

        for (String reference : references) {
            // 检查直接匹配
            if (context.containsKey(reference)) {
                continue;
            }

            // 检查带后缀的匹配
            boolean found = false;
            String[] suffixes = {"_value", "_encoded", "_node"};
            for (String suffix : suffixes) {
                if (context.containsKey(reference + suffix)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                missing.add(reference);
            }
        }

        return missing;
    }

    /**
     * 完整验证表达式（语法、引用、上下文）
     *
     * @param expression 表达式
     * @param nodeMap    可用的节点映射
     * @param context    上下文
     * @return 是否完全有效
     */
    public boolean validateExpression(String expression, Map<String, INode> nodeMap, Map<String, Object> context) {
        return validateSyntax(expression) &&
                validateReferences(expression, nodeMap) &&
                validateContext(expression, context);
    }

    /**
     * 检查表达式是否可以执行
     *
     * @param expression 表达式
     * @param context    上下文
     * @return 是否可以执行
     */
    public boolean canExecuteExpression(String expression, Map<String, Object> context) {
        return validateSyntax(expression) && validateContext(expression, context);
    }

    /**
     * 从表达式中提取节点引用
     *
     * @param expression 表达式
     * @return 节点引用集合
     */
    private Set<String> extractNodeReferences(String expression) {
        Set<String> references = new HashSet<>();

        if (expression == null || expression.trim().isEmpty()) {
            return references;
        }

        Matcher matcher = NODE_REF_PATTERN.matcher(expression);
        while (matcher.find()) {
            references.add(matcher.group(1));
        }

        return references;
    }
} 