package com.iecas.cmd.engine;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.exception.ExpressionRuntimeException;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.iecas.cmd.exception.ExpressionException;
import com.iecas.cmd.model.proto.INode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aviator表达式引擎实现
 */
@Slf4j
public class AviatorExpressionEngine implements ExpressionEngine {
    private static final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private boolean enableCache = true;

    @Getter
    private final AviatorEvaluatorInstance engine;
    private ExpressionEngineConfig config;
    private static AviatorExpressionEngine aviatorExpressionEngine;

    private static boolean registeredDefPackageFunctions = false;

    private static final String FUNC_PACKAGE = "com.iecas.cmd.aviator";

    public static AviatorExpressionEngine getInstance() {
        if (aviatorExpressionEngine == null) {
            aviatorExpressionEngine = new AviatorExpressionEngine();
        }
        return aviatorExpressionEngine;
    }

    private AviatorExpressionEngine() {
        this.engine = AviatorEvaluator.newInstance();
        this.engine.setCachedExpressionByDefault(true);

        // 自动注册aviator包下的所有自定义函数
        registerAviatorFunctions();
    }

    /**
     * 自动注册aviator包下的所有自定义函数
     */
    private void registerAviatorFunctions() {
        if (registeredDefPackageFunctions) {
            return;
        }
        registerAviatorFunctionsPackage(AviatorExpressionEngine.FUNC_PACKAGE);
        registeredDefPackageFunctions = true;
    }

    public void registerAviatorFunctionsPackage(String packageName) {
        Set<Class<?>> classes = ClassUtil.scanPackage(packageName);
        for (Class<?> candidateClass : classes) {
            try {
                if (!ClassUtil.isNormalClass(candidateClass)) {
                    continue;
                }
                if (!AbstractFunction.class.isAssignableFrom(candidateClass)) {
                    continue;
                }
                AbstractFunction function = (AbstractFunction) ReflectUtil.newInstance(candidateClass);
                engine.addFunction(function);
            } catch (Exception ex) {
                log.warn("跳过无法注册的Aviator函数类: {}", candidateClass.getName(), ex);
            }
        }
    }

    @Override
    public void setConfig(ExpressionEngineConfig config) {
        this.config = config;
        engine.setCachedExpressionByDefault(config.isCacheEnabled());
    }

    /**
     * 处理表达式中的特殊字符
     *
     * @param expression 表达式
     * @return 处理后的表达式
     */
    private String unescapeExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        // 处理HTML实体编码字符
        String result = expression
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");

        // 处理右移运算符
        Pattern rightShiftPattern = Pattern.compile("(\\S+)\\s*>>\\s*(\\d+)");
        Matcher rightShiftMatcher = rightShiftPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (rightShiftMatcher.find()) {
            String operand = rightShiftMatcher.group(1);
            String bits = rightShiftMatcher.group(2);
            rightShiftMatcher.appendReplacement(sb, "rightShift(" + operand + ", " + bits + ")");
        }
        rightShiftMatcher.appendTail(sb);
        result = sb.toString();

        // 处理左移运算符
        Pattern leftShiftPattern = Pattern.compile("(\\S+)\\s*<<\\s*(\\d+)");
        Matcher leftShiftMatcher = leftShiftPattern.matcher(result);
        sb = new StringBuffer();
        while (leftShiftMatcher.find()) {
            String operand = leftShiftMatcher.group(1);
            String bits = leftShiftMatcher.group(2);
            leftShiftMatcher.appendReplacement(sb, "leftShift(" + operand + ", " + bits + ")");
        }
        leftShiftMatcher.appendTail(sb);

        return sb.toString();
    }

    @Override
    public Object evaluate(String expression, Map<String, Object> context) throws ExpressionException {
        try {
            log.debug("[表达式引擎] 开始计算表达式: {}", expression);

            // 预处理表达式：将 #nodeId 转换为字符串引用，避免Aviator将其解析为数学表达式
            String processedExpression = preprocessExpression(expression);
            log.debug("[表达式引擎] 预处理后表达式: {}", processedExpression);

            Object result = engine.execute(processedExpression, context);
            log.debug("[表达式引擎] 计算结果: {}", result);
            return result;
        } catch (ExpressionRuntimeException e) {
            log.error("[表达式引擎] 表达式计算失败: {}", e.getMessage());
            throw new ExpressionException("表达式计算失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[表达式引擎] 表达式计算异常: {}", e.getMessage());
            throw new ExpressionException("表达式计算异常: " + e.getMessage(), e);
        }
    }

    /**
     * 预处理表达式，将 #nodeId 或 #protocolId:nodeId 转换为字符串引用
     *
     * <p>支持两种引用格式：</p>
     * <ul>
     *   <li><code>#节点ID</code> - 同协议内引用</li>
     *   <li><code>#协议ID:节点ID</code> - 跨协议引用</li>
     * </ul>
     *
     * @param expression 原始表达式
     * @return 预处理后的表达式
     */
    private String preprocessExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return expression;
        }

        // 将 #nodeId 或 #protocolId:nodeId 转换为 'nodeId' 或 'protocolId:nodeId'
        // 正则匹配：# 后跟字母数字下划线点横线或冒号（用于协议ID:节点ID格式）
        String processed = expression.replaceAll("#([a-zA-Z0-9_.-]+(?::[a-zA-Z0-9_.-]+)?)", "'$1'");

        // 处理函数调用中的参数，确保格式正确
        // 例如：length(#simple-protocol-body) -> length('simple-protocol-body')
        //      length(#proto1:node1) -> length('proto1:node1')
        processed = processed.replaceAll("([a-zA-Z0-9_]+)\\s*\\(\\s*'([^']+)'\\s*\\)", "$1('$2')");

        return processed;
    }


    @Override
    public String getStringResult(String expression, ExpressionContext context) throws ExpressionException {
        Object result = evaluate(expression, context.getVariables());
        if (result == null) {
            return "";
        }
        return String.valueOf(result);
    }

    @Override
    public Number getNumberResult(String expression, ExpressionContext context) throws ExpressionException {
        Object result = evaluate(expression, context.getVariables());
        if (result instanceof Number) {
            return (Number) result;
        } else if (result instanceof String) {
            try {
                return Double.parseDouble((String) result);
            } catch (NumberFormatException e) {
                throw new ExpressionException("无法将结果转换为数值: " + result, e);
            }
        }
        throw new ExpressionException("表达式未返回数值类型: " + result);
    }

    @Override
    public boolean getBooleanResult(String expression, ExpressionContext context) throws ExpressionException {
        Object result = evaluate(expression, context.getVariables());
        if (result instanceof Boolean) {
            return (Boolean) result;
        } else if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0;
        } else if (result instanceof String) {
            String strResult = (String) result;
            if (strResult.equalsIgnoreCase("true")) {
                return true;
            } else if (strResult.equalsIgnoreCase("false")) {
                return false;
            } else {
                try {
                    return Double.parseDouble(strResult) != 0;
                } catch (NumberFormatException e) {
                    return !strResult.isEmpty();
                }
            }
        }
        return result != null;
    }

    @Override
    public void registerFunction(AbstractFunction function) {
        engine.addFunction(function);
    }

    @Override
    public void registerVariable(String name, Object value) {
        // 在表达式执行时通过环境变量传入
    }

    @Override
    public void clearVariables() {
        // 不需要实现，变量在每次执行时通过环境变量传入
    }

    @Override
    public ExpressionEngineConfig getConfig() {
        return config;
    }

    /**
     * 根据节点名称查找节点
     */
    private INode findNodeByName(String nodeName, java.util.Map<String, Object> env) {
        // 从上下文中查找节点
        Object node = env.get(nodeName);
        if (node instanceof INode) {
            return (INode) node;
        }
        return null;
    }

    /**
     * 计算CRC16
     */
    private int calculateCRC16(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xFFFF;
            crc ^= (b & 0xFF);
            crc ^= ((crc & 0xFF) >> 4);
            crc ^= (crc << 12) & 0xFFFF;
            crc ^= ((crc & 0xFF) << 5) & 0xFFFF;
        }
        return crc & 0xFFFF;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }
}