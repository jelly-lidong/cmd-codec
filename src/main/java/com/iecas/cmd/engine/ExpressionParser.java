package com.iecas.cmd.engine;

import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.util.Constants;
import com.iecas.cmd.annotation.BetweenFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表达式解析器
 * 用于分析表达式中的依赖关系
 */
@Slf4j
public class ExpressionParser {


    // 函数引用模式，匹配形如 任意方法名(#id) 的表达式，支持小数点
    private static final Pattern FUNCTION_PATTERN = Constants.PATTERN_FUNCTION;

    // 复杂引用模式，匹配形如 #id.property 或 #id[index] 的表达式，支持小数点
    private static final Pattern COMPLEX_REF_PATTERN = Constants.PATTERN_COMPLEX_REF;

    /**
     * 构造函数
     */
    public ExpressionParser() {
    }


    /**
     * 反转义表达式中的HTML实体编码
     *
     * @param expression 原始表达式
     * @return 反转义后的表达式
     */
    public String unescapeExpression(String expression) {
        if (expression == null) {
            return null;
        }

        // 处理常见的HTML实体编码
        String unescaped = expression;

        // 首先处理双重转义的情况
        unescaped = unescaped.replace("&amp;amp;", "&amp;")
                .replace("&amp;lt;", "&lt;")
                .replace("&amp;gt;", "&gt;");

        // 然后处理单次转义
        unescaped = unescaped.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        // 处理位运算符
        unescaped = unescaped.replace("&lt;&lt;", "<<")
                .replace("&gt;&gt;", ">>")
                .replace("&apos;", "'")
                .replace("&quot;", "\"");

        // 修复位运算符转义的问题
        if (unescaped.contains("&amp;gt;>") || unescaped.contains("&amp;lt;<")) {
            unescaped = unescaped.replace("&amp;gt;>", ">>").replace("&amp;lt;<", "<<");
        }

        if (unescaped.contains("&amp;gt;") || unescaped.contains("&amp;lt;")) {
            unescaped = unescaped.replace("&amp;gt;", ">").replace("&amp;lt;", "<");
        }

        // 特殊处理表达式中的'value'
        if (unescaped.contains("'value'")) {
            unescaped = unescaped.replace("'value'", "value");
        }

        if (!unescaped.equals(expression)) {
            log.debug("[表达式解析] 表达式反转义: {} -> {}", expression, unescaped);
        }

        return unescaped;
    }

    /**
     * 解析表达式中的依赖
     *
     * @param expression   表达式
     * @param allLeafNodes
     * @return 依赖的节点ID集合
     */
    public Set<String> parseDependencies(String expression, List<INode> allLeafNodes) throws CodecException {
        //log.debug("[表达式解析] 开始解析表达式依赖: {}", expression);
        if (expression == null || expression.trim().isEmpty()) {
            //log.debug("[表达式解析] 表达式为空");
            return new HashSet<>();
        }
        // 反转义表达式中的HTML实体编码
        String unescapedExpression = unescapeExpression(expression);
        Set<String> dependencies = new LinkedHashSet<>();
        // 只解析函数参数中的引用
        parseFunctionReferences(unescapedExpression, dependencies, allLeafNodes);
        // 解析复杂引用 #id.property 或 #id[index]
//        parseComplexReferences(unescapedExpression, dependencies);
        log.debug("[表达式解析] 解析结果: {}", String.join(", ", dependencies));
        return dependencies;
    }

    /**
     * 带当前协议ID上下文的依赖解析：支持同协议和跨协议引用
     * - #节点ID -> 同协议内引用，返回原样
     * - #协议ID:节点ID -> 跨协议引用，返回原样
     * 说明：同/跨协议的消歧与拓扑边加入由上层构建器处理。
     */
    public Set<String> parseDependencies(String expression, List<INode> allLeafNodes, String currentProtocolId) throws CodecException {
        // 解析表达式，返回的依赖ID可能是简单的节点ID或协议ID:节点ID格式
        return parseDependencies(expression, allLeafNodes);
    }

    /**
     * 解析函数引用
     */
    private void parseFunctionReferences(String expression, Set<String> dependencies, List<INode> allLeafNodes) throws CodecException {
        log.debug("[表达式解析] 解析函数引用");
        Matcher matcher = FUNCTION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String function = matcher.group(1);
            String paramsString = matcher.group(2);

            log.debug("- 找到函数引用: {}({})", function, paramsString);

            // 解析所有参数（支持区间函数）
            parseFunctionParameters(paramsString, dependencies, function, allLeafNodes);
        }
    }

    /**
     * 解析函数参数
     */
    private void parseFunctionParameters(String paramsString, Set<String> dependencies) {
        if (paramsString == null || paramsString.trim().isEmpty()) {
            return;
        }

        // 分割参数，考虑引号和逗号的嵌套
        String[] params = splitParameters(paramsString);

        for (int i = 0; i < params.length; i++) {
            String param = params[i].trim();
            if (param.isEmpty()) {
                continue;
            }

            if (param.startsWith("#")) {
                // ID引用参数
                String id = param.substring(1);
                log.debug("  - 参数" + (i + 1) + ": #" + id);
                dependencies.add(id);
                log.debug("  [依赖] 添加函数参数依赖(ID): " + id);
            } else if (param.startsWith("'") && param.endsWith("'")) {
                // 字符串参数
                String strValue = param.substring(1, param.length() - 1);
                log.debug("  - 参数" + (i + 1) + ": '" + strValue + "'");
            } else {
                // 其他类型的参数（数字、变量等）
                log.debug("  - 参数" + (i + 1) + ": " + param);
            }
        }
    }

    /**
     * 解析函数参数（支持区间函数）
     */
    private void parseFunctionParameters(String paramsString, Set<String> dependencies, String functionName, List<INode> allLeafNodes) throws CodecException {
        if (paramsString == null || paramsString.trim().isEmpty()) {
            return;
        }

        // 分割参数，考虑引号和逗号的嵌套
        String[] params = splitParameters(paramsString);

        // 检查是否是区间函数
        if (isBetweenFunction(functionName) && params.length == 2) {
            // 区间函数：依赖从第一个参数到第二个参数之间的所有节点
            parseBetweenFunctionParameters(params, dependencies, functionName, allLeafNodes);
        } else {
            // 普通函数：只依赖指定的参数节点
            parseNormalFunctionParameters(params, dependencies);
        }
    }

    /**
     * 解析普通函数参数
     * 支持两种格式：
     * - #节点ID（同协议内引用）
     * - #协议ID:节点ID（跨协议引用）
     */
    private void parseNormalFunctionParameters(String[] params, Set<String> dependencies) {
        for (String s : params) {
            String param = s.trim();
            if (param.isEmpty()) {
                continue;
            }

            if (param.startsWith("#")) {
                // ID引用参数，支持 #节点ID 或 #协议ID:节点ID 格式
                String id = param.substring(1);
                dependencies.add(id);
            }
        }
    }

    /**
     * 解析区间函数参数
     *
     * <p>支持两种引用格式：</p>
     * <ul>
     *   <li><code>#节点ID</code> - 同协议内引用</li>
     *   <li><code>#协议ID:节点ID</code> - 跨协议引用</li>
     * </ul>
     */
    private void parseBetweenFunctionParameters(String[] params, Set<String> dependencies, String functionName, List<INode> allLeafNodes) throws CodecException {
        if (params.length != 2) {
            throw new CodecException(String.format("区间函数 %s 需要2个参数，实际参数数量: %s", functionName, params.length));
        }

        // 获取起始和结束节点ID引用
        String startParam = params[0].trim();
        String endParam = params[1].trim();

        // 第一个参数作为起始节点
        if (!startParam.startsWith("#")) {
            throw new CodecException("区间函数 " + functionName + " 的第一个参数必须是节点ID引用（格式：#节点ID 或 #协议ID:节点ID），实际: " + startParam);
        }
        // 提取节点ID（可能是简单的节点ID或协议ID:节点ID格式）
        String startNodeId = startParam.substring(1);

        if (!endParam.startsWith("#")) {
            throw new CodecException("区间函数 " + functionName + " 的第二个参数必须是节点ID引用（格式：#节点ID 或 #协议ID:节点ID），实际: " + endParam);
        }
        // 提取节点ID（可能是简单的节点ID或协议ID:节点ID格式）
        String endNodeId = endParam.substring(1);

        // 获取区间内的所有节点ID（getBetweenNodeIds支持协议ID:节点ID格式）
        List<String> betweenNodeIds = getBetweenNodeIds(startNodeId, endNodeId, allLeafNodes);

        dependencies.addAll(betweenNodeIds);
    }

    /**
     * 检查函数是否是区间函数
     */
    private boolean isBetweenFunction(String functionName) {
        try {
            AviatorExpressionEngine instance = AviatorExpressionEngine.getInstance();
            AviatorEvaluatorInstance engine = instance.getEngine();
            AviatorFunction function = engine.getFunction(functionName);
            Class<? extends AviatorFunction> aClass = function.getClass();
            return aClass.isAnnotationPresent(BetweenFunction.class);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取两个节点之间的所有节点ID
     *
     * <p><b>核心设计：</b>支持协议ID:节点ID格式的节点ID。</p>
     * <p>如果startNodeId/endNodeId是协议ID:节点ID格式，提取节点ID部分进行匹配。</p>
     *
     * @param startNodeId 起始节点ID（支持简单节点ID或协议ID:节点ID格式）
     * @param endNodeId   结束节点ID（支持简单节点ID或协议ID:节点ID格式）
     * @param allLeafNodes 所有叶子节点列表
     * @return 起始和结束节点之间的所有节点ID列表（返回简单的节点ID）
     */
    public List<String> getBetweenNodeIds(String startNodeId, String endNodeId, List<INode> allLeafNodes) {
        // 提取节点ID部分（如果是协议ID:节点ID格式，提取冒号后的部分）
        String startId = extractNodeId(startNodeId);
        String endId = extractNodeId(endNodeId);
        
        int startNodeIndex = -1;
        int endNodeIndex = -1;
        for (int i = 0; i < allLeafNodes.size(); i++) {
            INode node = allLeafNodes.get(i);
            String nodeId = node.getId();
            if (nodeId != null) {
                // 比较节点ID（支持直接匹配或协议ID:节点ID格式匹配）
                if (nodeId.equals(startId) || nodeId.equals(startNodeId)) {
                    startNodeIndex = i;
                }
                if (nodeId.equals(endId) || nodeId.equals(endNodeId)) {
                    endNodeIndex = i;
                }
            }
        }
        
        if (startNodeIndex == -1 || endNodeIndex == -1) {
            log.warn("[区间函数] 无法找到起始节点或结束节点: start={}, end={}", startNodeId, endNodeId);
            return new ArrayList<>();
        }
        
        return allLeafNodes.subList(startNodeIndex, endNodeIndex + 1).stream()
                .map(INode::getId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * 从节点ID中提取节点ID部分
     * 如果是协议ID:节点ID格式，返回节点ID部分；否则返回原值
     *
     * @param nodeId 节点ID（可能是简单节点ID或协议ID:节点ID格式）
     * @return 节点ID部分
     */
    private String extractNodeId(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) {
            return nodeId;
        }
        
        int colonIdx = nodeId.indexOf(':');
        if (colonIdx > 0) {
            // 协议ID:节点ID格式，提取节点ID部分
            return nodeId.substring(colonIdx + 1);
        }
        
        // 简单节点ID格式，直接返回
        return nodeId;
    }

    /**
     * 分割函数参数，正确处理引号内的逗号
     */
    private String[] splitParameters(String paramsString) {
        java.util.List<String> params = new java.util.ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        boolean inQuotes = false;
        int depth = 0;

        for (int i = 0; i < paramsString.length(); i++) {
            char c = paramsString.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
                currentParam.append(c);
            } else if (c == '(') {
                depth++;
                currentParam.append(c);
            } else if (c == ')') {
                depth--;
                currentParam.append(c);
            } else if (c == ',' && !inQuotes && depth == 0) {
                // 只有在引号外且括号深度为0时才分割
                params.add(currentParam.toString().trim());
                currentParam = new StringBuilder();
            } else {
                currentParam.append(c);
            }
        }

        // 添加最后一个参数
        if (currentParam.length() > 0) {
            params.add(currentParam.toString().trim());
        }

        return params.toArray(new String[0]);
    }

    /**
     * 解析复杂引用 #id.property 或 #id[index]
     */
    private void parseComplexReferences(String expression, Set<String> dependencies) {
        log.debug("[表达式解析] 解析复杂引用");
        Matcher matcher = COMPLEX_REF_PATTERN.matcher(expression);
        while (matcher.find()) {
            String nodeId = matcher.group(1);
            String property = matcher.group(2);  // 属性名（可能为null）
            String index = matcher.group(3);     // 索引（可能为null）

            // 如果该ID已被依赖集合收集（如函数参数引用已收集），则跳过
            if (dependencies.contains(nodeId)) {
                continue;
            }

            log.debug("- 找到复杂引用: #" + nodeId +
                    (property != null ? "." + property : "") +
                    (index != null ? "[" + index + "]" : ""));

            // 只添加未被收集过的ID
            dependencies.add(nodeId);
            log.debug("  [依赖] 添加复杂引用依赖(ID): " + nodeId);
        }
    }


    /**
     * 判断是否是关键字
     */
    private boolean isKeyword(String word) {
        return word.equals("if") || word.equals("then") || word.equals("else") ||
                word.equals("length") || word.equals("crc16") || word.equals("value") ||
                word.equals("current") || word.equals("parent") || word.equals("verifyCRC16") ||
                word.equals("sum") || word.equals("count") || word.equals("size");
    }

    /**
     * 判断表达式是否需要编码结果
     */
    public boolean needsEncodedData(String expression) {
        // 确保先反转义表达式
        String unescapedExpression = unescapeExpression(expression);
        return unescapedExpression.contains("length(") ||
                unescapedExpression.contains("crc16(") ||
                unescapedExpression.contains("sum(") ||
                unescapedExpression.contains("count(") ||
                unescapedExpression.contains("size(");
    }

    /**
     * 判断是否是条件表达式
     */
    public boolean isConditionalExpression(String expression) {
        // 确保先反转义表达式
        String unescapedExpression = unescapeExpression(expression);
        return unescapedExpression.contains("if") && unescapedExpression.contains("then");
    }

    /**
     * 判断是否是数学表达式
     */
    public boolean isMathExpression(String expression) {
        // 确保先反转义表达式
        String unescapedExpression = unescapeExpression(expression);
        return unescapedExpression.contains("+") || unescapedExpression.contains("-") ||
                unescapedExpression.contains("*") || unescapedExpression.contains("/") ||
                unescapedExpression.contains("%") || unescapedExpression.contains("^");
    }
} 