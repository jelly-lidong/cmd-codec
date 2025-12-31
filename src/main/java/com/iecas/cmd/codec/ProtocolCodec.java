package com.iecas.cmd.codec;

import cn.hutool.core.collection.CollectionUtil;
import com.iecas.cmd.engine.*;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.*;
import com.iecas.cmd.parser.ProtocolClassParser;
import com.iecas.cmd.registry.ProtocolRegistry;
import com.iecas.cmd.util.*;
import com.iecas.cmd.validator.ProtocolFormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 协议编解码器
 *
 * <p>核心设计原理：</p>
 * <ul>
 *   <li><b>分离关注点</b>：将编码和解码逻辑分离，编码负责数据序列化，解码负责验证数据一致性</li>
 *   <li><b>依赖图驱动</b>：通过构建节点依赖关系图，确保节点按正确的拓扑顺序处理</li>
 *   <li><b>表达式计算</b>：支持动态表达式计算，允许节点值依赖其他节点的值或长度</li>
 *   <li><b>条件处理</b>：支持条件性节点启用/禁用，实现灵活的协议变体处理</li>
 *   <li><b>填充支持</b>：提供多种填充策略，满足协议对齐和长度要求</li>
 *   <li><b>缓存机制</b>：缓存已编码的节点数据，避免重复计算，提高性能</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>构建依赖关系图：分析节点间的依赖关系，确定处理顺序</li>
 *   <li>处理条件依赖：根据条件表达式启用/禁用相关节点</li>
 *   <li>处理填充配置：计算并应用各种填充策略</li>
 *   <li>按拓扑排序编码：确保依赖节点先于被依赖节点处理</li>
 *   <li>按结构顺序输出：按协议定义的结构顺序组装最终数据</li>
 * </ol>
 *
 * <p>解码验证原理：</p>
 * <ul>
 *   <li>解码不是为了重建协议，而是为了验证编码的正确性</li>
 *   <li>从编码数据中按位置截取各节点数据，反向解码后与原始值比较</li>
 *   <li>支持正向和反向表达式，确保数据转换的可逆性</li>
 * </ul>
 *
 * @author 系统生成
 * @since 1.0.0
 */
@Slf4j
public class ProtocolCodec {

    /**
     * 编解码器工厂 - 根据值类型获取对应的编解码器
     * 设计原因：遵循策略模式，不同的数据类型使用不同的编解码策略
     */
    private final CodecFactory codecFactory;

    /**
     * 表达式引擎 - 执行动态表达式计算
     * 设计原因：支持节点值的动态计算，如长度计算、条件判断、数据转换等
     */
    private final ExpressionEngine expressionEngine;

    /**
     * 表达式解析器 - 解析表达式中的依赖关系
     * 设计原因：需要在执行表达式前分析其依赖的变量，确保依赖数据可用
     */
    private final ExpressionParser expressionParser;

    /**
     * 依赖关系图 - 管理节点间的依赖关系
     * 设计原因：通过拓扑排序确保节点按正确顺序处理，避免依赖缺失
     */
    private final DependencyGraph dependencyGraph;

    /**
     * 表达式验证器 - 验证表达式语法的正确性
     * 设计原因：在运行时执行前进行语法检查，避免运行时错误
     */
    private final ExpressionValidator expressionValidator;

    /**
     * 条件依赖处理器 - 处理节点的条件性启用/禁用
     * 设计原因：支持协议的变体处理，某些节点可能只在特定条件下存在
     */
    private final ConditionalDependencyProcessor conditionalDependencyProcessor;

    /**
     * 填充处理器 - 处理各种填充策略
     * 设计原因：协议经常需要对齐或固定长度要求，需要专门的填充处理逻辑
     */
    private final PaddingProcessor paddingProcessor;

    /**
     * 已编码节点缓存 - 存储节点ID到编码数据的映射
     * 设计原因：
     * 1. 避免重复编码：节点可能被多个表达式引用，缓存避免重复计算
     * 2. 支持依赖引用：后续节点可以引用已编码节点的数据
     * 3. 结构体组装：结构体节点需要组装子节点的编码数据
     * 4. 性能优化：减少重复的编码操作
     */
    private final Map<String, byte[]> encodedNodesCache = new HashMap<>();

    /**
     * 协议格式校验器 - 在指令编制前对协议格式进行校验
     * 设计原因：确保协议配置的正确性，包括节点值范围、长度、枚举值等校验
     */
    private final ProtocolFormatValidator protocolFormatValidator;

    private String currentProtocolId;

    /**
     * 构造函数 - 初始化协议编解码器
     *
     * <p>设计原理：</p>
     * <ul>
     *   <li>依赖注入：通过构造函数注入核心依赖，确保对象创建时的完整性</li>
     *   <li>组合模式：将复杂的编解码逻辑分解为多个专门的处理器</li>
     *   <li>单一职责：每个组件负责特定的功能，便于测试和维护</li>
     * </ul>
     *
     * @param codecFactory     编解码器工厂，用于获取不同类型的编解码器
     * @param expressionEngine 表达式引擎，用于执行动态表达式计算
     */
    public ProtocolCodec(CodecFactory codecFactory, ExpressionEngine expressionEngine) {
        this.codecFactory = codecFactory;
        this.expressionEngine = expressionEngine;

        // 初始化解析和验证组件
        this.expressionParser = new ExpressionParser();
        this.dependencyGraph = new DependencyGraph();
        this.expressionValidator = new ExpressionValidator();

        // 初始化条件和填充处理器，传入必要的依赖
        this.conditionalDependencyProcessor = new ConditionalDependencyProcessor(expressionEngine, dependencyGraph);
        this.paddingProcessor = new PaddingProcessor(expressionEngine);

        // 初始化协议格式校验器
        this.protocolFormatValidator = new ProtocolFormatValidator();
    }

    public ProtocolCodec() {
        this.codecFactory = new CodecFactory();
        this.expressionEngine = AviatorExpressionEngine.getInstance();

        // 初始化解析和验证组件
        this.expressionParser = new ExpressionParser();
        this.dependencyGraph = new DependencyGraph();
        this.expressionValidator = new ExpressionValidator();

        // 初始化条件和填充处理器，传入必要的依赖
        this.conditionalDependencyProcessor = new ConditionalDependencyProcessor(expressionEngine, dependencyGraph);
        this.paddingProcessor = new PaddingProcessor(expressionEngine);

        // 初始化协议格式校验器
        this.protocolFormatValidator = new ProtocolFormatValidator();
    }

    /**
     * 编码协议 - 将协议对象转换为字节数组
     *
     * <p>核心设计思想：</p>
     * <ul>
     *   <li><b>两阶段处理</b>：先按依赖关系计算所有节点值，再按结构顺序组装数据</li>
     *   <li><b>依赖驱动</b>：通过拓扑排序确保被依赖的节点先计算</li>
     *   <li><b>缓存复用</b>：缓存已编码的节点数据，避免重复计算</li>
     *   <li><b>上下文传递</b>：通过上下文在节点间传递计算结果</li>
     * </ul>
     *
     * <p>编码流程：</p>
     * <ol>
     *   <li><b>协议格式校验</b>：在指令编制前对协议格式进行全面的校验</li>
     *   <li><b>依赖分析阶段</b>：构建节点依赖图，分析表达式依赖关系</li>
     *   <li><b>预处理阶段</b>：处理条件依赖和填充配置</li>
     *   <li><b>计算阶段</b>：按拓扑顺序计算节点值并编码</li>
     *   <li><b>组装阶段</b>：按协议结构顺序组装最终数据</li>
     * </ol>
     *
     * <p>为什么需要协议格式校验？</p>
     * <ul>
     *   <li>配置正确性：确保协议节点配置的正确性，避免运行时错误</li>
     *   <li>值范围检查：验证节点值是否在配置的范围内</li>
     *   <li>长度验证：确保节点长度配置合理且不超过限制</li>
     *   <li>枚举值验证：检查枚举值是否有效</li>
     *   <li>表达式语法验证：确保表达式语法正确</li>
     * </ul>
     *
     * @param protocol 要编码的协议对象
     * @return 编码后的字节数组
     * @throws CodecException 编码过程中发生的异常
     */
    public byte[] encode(Protocol protocol) throws CodecException {
        return encode(protocol, new LinkedHashMap<>());
    }


    /**
     * @param protocol 协议配置对象
     * @param context  上下文用于在节点间传递数据，存储已计算的值和中间结果
     * @return 编码结果
     * @throws CodecException 异常信息
     */
    public byte[] encode(Protocol protocol, Map<String, Object> context) throws CodecException {
        // 第一步：协议格式校验
        //原理：在指令编制前对协议格式进行全面的校验，确保协议配置的正确性
        log.debug("[编码] 开始协议格式校验");
        protocolFormatValidator.validateProtocolFormat(protocol);

        log.debug("[编码] 协议格式校验完成，未发现异常情况");

        // 第二步：初始化和清理
        // 清空编码节点缓存，确保每次编码都是独立的
        encodedNodesCache.clear();

        // 按顺序收集协议中的所有叶子节点
        List<INode> allLeafNodes = collectAllLeafNodes(protocol); // 使用已有的方法获取所有节点

        // 注册到全局协议注册表，支持跨协议引用解析
        ProtocolRegistry.getInstance().registerProtocol(protocol, allLeafNodes);

        context.put("allLeafNodes", allLeafNodes);

        // 在上下文中放入当前协议ID，供条件与表达式解析使用
        context.put("protocolId", protocol.getId());
        this.currentProtocolId = protocol.getId();

        // 第三步：构建依赖关系图
        // 原理：分析协议中所有节点的依赖关系，为拓扑排序做准备
        log.debug("[编码] 开始构建节点依赖关系");
        ProtocolDependencyBuilder depBuilder = new ProtocolDependencyBuilder(expressionParser, expressionValidator, dependencyGraph, allLeafNodes);
        depBuilder.build(protocol);
        log.debug("[编码] 依赖关系构建完成");

        // 第四步：检查基础结构的循环依赖
        // 原理：先检查基础结构（不包括填充前序依赖）是否有循环，确保基础依赖图是有效的
        log.debug("[编码] 检查基础结构循环依赖");
        dependencyGraph.findCycle();
        log.debug("[编码] 基础结构循环依赖检查完成");

        // 第六步：处理条件依赖
        // 原理：根据条件表达式动态启用/禁用节点，支持协议变体
        log.debug("[编码] 处理条件依赖");
        conditionalDependencyProcessor.processConditionalDependencies(context, allLeafNodes);

        Map<String, INode> nodeMap = dependencyGraph.getNodeMap();
        // 注意：移除了填充配置的预处理步骤
        // 原理：填充节点需要依赖其他节点的值，必须在节点编码过程中动态处理
        // 这样可以确保填充计算时，所有依赖的节点值都已经在context中可用

        // 第七步：获取分阶段拓扑排序结果
        // 原理：分两个阶段处理节点，避免填充节点的循环依赖问题
        List<String> orderIds = getStagedTopologicalOrder(allLeafNodes);
        List<String> orderPaths = new ArrayList<>();
        List<INode> orderNodes = new ArrayList<>();
        log.debug("节点处理顺序:");
        for (int i = 0; i < orderIds.size(); i++) {
            String id = orderIds.get(i);
            String path = dependencyGraph.getNodePathById(id);
            log.debug("{}  - {}", i + 1, path != null ? path : id);
            orderPaths.add(path != null ? path : id);
            orderNodes.add(nodeMap.get(id));
        }
        context.put("nodeMap", nodeMap);
        context.put("orderNodePaths", orderPaths);
        context.put("orderNodes", orderNodes);

        // 第八步：按拓扑排序编码所有节点
        // 原理：这个阶段负责计算节点值和编码，填充节点在此阶段动态处理
        // 这样做的好处是：可以在编码时直接使用其他节点的值进行填充计算
        log.debug("=================================================== 编码开始 ==================================================");
        log.debug("[编码] 开始按拓扑顺序编码节点");
        for (String nodeId : orderIds) {
            INode node = dependencyGraph.getNodeById(nodeId);
            if (node != null) {
                // 跳过禁用的节点
                if (!node.isEnabled()) {
                    String nodePath = dependencyGraph.getNodePathById(nodeId);
                    log.debug("[编码] 跳过禁用节点: {} (原因: {})", nodePath, node.getEnabledReason());
                    continue;
                }

                String nodePath = dependencyGraph.getNodePathById(nodeId);
                log.debug("[编码] 处理节点: {}", nodePath);
                log.debug("- 节点长度: {}", node.getLength());
                log.debug("- 节点值: {}", node.getValue());
                log.debug("- 节点值类型: {}", node.getValueType());

                // 更新上下文中的当前节点信息
                context.put("current", node.getName());
                if (nodePath != null) {
                    context.put("currentPath", nodePath);
                    log.debug("- 节点路径: {}", nodePath);
                }

                try {
                    // 动态处理填充节点
                    // 原理：在编码前检查是否为填充节点，如果是则先进行填充计算
                    // 此时context中已经包含了所有依赖节点的值，可以正确计算填充长度
                    if (node.isPaddingNode()) {
                        log.debug("- 检测到填充节点，开始动态填充处理");
                        processPaddingNodeDynamically(node, context);
                        log.debug("- 填充节点处理完成，新长度: {} 位", node.getLength());
                    }
                    context.put("encodedNodesCache", encodedNodesCache);
                    // 编码当前节点
                    log.debug("- 开始编码节点数据");
                    byte[] nodeData = encodeNode(node, context, allLeafNodes);

                    // 验证编码结果的正确性
                    log.debug("- 验证编码结果");
                    validateEncodedData(node, nodeData);

                    // 缓存编码结果，供后续节点引用
                    if (nodeId != null) {
                        encodedNodesCache.put(nodeId, nodeData);
                        log.debug("- 缓存节点编码结果: #{}", nodeId);
                    }

                    log.debug("- 节点编码完成，数据: 0x{}", ByteUtil.bytesToHexString(nodeData));
                } catch (Exception e) {
                    log.error("[错误] 编码节点失败: {}", e.getMessage());
                    throw new CodecException("编码节点[" + nodePath + "]失败: " + e.getMessage(), e);
                }
            }
        }

        // 第九步：按协议结构顺序组装最终数据
        // 原理：虽然节点已按依赖关系编码，但最终数据必须按协议定义的结构顺序排列
        // 这样做的好处是：依赖计算和数据布局分离，既保证了计算正确性，又保证了结构正确性
        BitBuffer buffer = new BitBuffer();
        log.debug("[编码] 开始按协议结构顺序写入数据");
        writeProtocolDataInOrder(protocol, buffer, context);

        // 第十步：生成最终结果
        byte[] result = buffer.toByteArray();
        log.debug("[编码] 协议编码完成");
        log.debug("- 总编码长度: {} 字节", result.length);
        log.debug("- 编码结果：{}", ByteUtil.formatHex(ByteUtil.bytesToHexString(result)));
        return result;
    }

    /**
     * 编码单个节点 - 将节点转换为字节数组
     *
     * <p>设计原理：</p>
     * <ul>
     *   <li><b>类型分派</b>：根据节点类型选择不同的编码策略</li>
     *   <li><b>表达式计算</b>：支持正向表达式对节点值进行转换</li>
     *   <li><b>上下文管理</b>：将编码结果加入上下文供其他节点使用</li>
     *   <li><b>错误恢复</b>：编码失败时恢复节点原始值</li>
     * </ul>
     *
     * <p>编码流程：</p>
     * <ol>
     *   <li>检查节点类型：结构体节点和叶子节点使用不同策略</li>
     *   <li>执行正向表达式：如果存在，先计算表达式得到最终值</li>
     *   <li>获取编解码器：根据值类型获取对应的编解码器</li>
     *   <li>执行编码：将节点值转换为字节数组</li>
     *   <li>更新上下文：将编码结果保存到上下文中</li>
     * </ol>
     *
     * @param node    要编码的节点
     * @param context 执行上下文，包含其他节点的值和编码结果
     * @return 编码后的字节数组
     * @throws CodecException 编码过程中发生的异常
     */
    private byte[] encodeNode(INode node, Map<String, Object> context, List<INode> allLeafNodes) throws CodecException {
        // 第一步：区分节点类型
        // 原理：结构体节点（如header、body、check）需要组装子节点数据
        //      叶子节点直接使用编解码器转换值
        if (node.isStructureNode()) {
            log.debug("- 识别为结构体节点，开始拼接子节点编码结果");
            return encodeStructureNode(node, context);
        }

        // 第二步：获取对应的编解码器
        // 原理：不同的值类型（INT、HEX、STRING、BIT等）需要不同的编码逻辑
        // 所有类型都应该通过对应的编解码器处理，保持设计一致性
        Codec codec = codecFactory.getCodec(node.getValueType());
        log.debug("- 使用编解码器: {}", codec.getClass().getSimpleName());

        // 第三步：保存原始值
        // 原理：如果编码过程中发生错误，需要恢复节点的原始状态
        Object originalValue = node.getValue();
        log.debug("- 编码前节点值: {}", originalValue);

        // 第四步：执行正向表达式计算（如果存在）
        // 原理：正向表达式用于在编码前对节点值进行转换或计算
        // 例如：将多个字段组合成一个值，或者进行数据格式转换
        if (StringUtils.isNotEmpty(node.getFwdExpr())) {
            String fwdExpr = node.getFwdExpr();
            try {
                log.debug("- 执行正向表达式: {}", fwdExpr);

                // 验证表达式语法
                // 原理：提前发现语法错误，避免运行时异常
                expressionValidator.validateSyntax(fwdExpr);

                // 准备表达式执行所需的依赖数据
                // 原理：表达式可能引用其他节点的值，需要确保这些依赖数据在上下文中可用
                prepareExpressionDependencies(node.getPath(), fwdExpr, context);

                // 将当前节点的值添加到上下文中作为value变量
                // 原理：表达式中可以使用'value'变量引用当前节点的原始值
                if (originalValue != null) {
                    Object contextValue = originalValue;

                    // 智能类型转换：如果表达式包含数学运算，尝试将字符串转换为数字
                    // 原理：提高表达式的灵活性，允许对字符串形式的数字进行数学运算
                    if (originalValue instanceof String && fwdExpr.matches(".*[+\\-*/].*")) {
                        String strValue = (String) originalValue;
                        try {
                            // 尝试转换为整数
                            if (strValue.matches("-?\\d+")) {
                                contextValue = Long.parseLong(strValue);
                            }
                            // 尝试转换为浮点数
                            else if (strValue.matches("-?\\d*\\.\\d+")) {
                                contextValue = Double.parseDouble(strValue);
                            }
                        } catch (NumberFormatException e) {
                            // 转换失败，保持原值
                            log.debug("- 无法将值转换为数字，保持原值: {}", originalValue);
                        }
                    }
                    context.put("value", contextValue);
                    log.debug("- 添加当前节点值到上下文: value = {} (类型: {})", contextValue, contextValue.getClass().getSimpleName());
                }

                // 执行表达式计算
                // 原理：使用表达式引擎计算新的节点值
                Object result = expressionEngine.evaluate(fwdExpr, context);
                log.debug("- 表达式计算结果: {}", result);

                // 验证结果有效性
                // 原理：确保表达式计算的结果不为null，避免后续编码错误
                if (result == null) {
                    log.debug("[警告] 表达式结果为null");
                    throw new CodecException("表达式[" + fwdExpr + "]结果为null");
                }
                // 保存表达式计算结果
                node.setFwdExprResult(result);
                if (node.getValue() == null) {
                    node.setValue(result);
                }
            } catch (Exception e) {
                log.error("[错误] 执行表达式失败: {}", e.getMessage());
                throw new CodecException("执行表达式[" + fwdExpr + "]失败: " + e.getMessage(), e);
            }
        }

        try {
            // 第五步：执行实际的编码操作
            // 原理：使用对应的编解码器将节点值转换为字节数组
            // 所有类型（包括BIT类型）都通过编解码器处理，保持架构一致性
            log.debug("- 开始编码节点值: {}", node.getValue());
            byte[] encodedData = codec.encode(node, context);
            String encodeDataHex = ByteUtil.bytesToHexString(encodedData);

            // 记录编码结果
            log.debug("- 编码结果: {}", encodeDataHex);

            // 第六步：更新上下文
            // 原理：将编码结果保存到上下文中，供其他节点的表达式引用

            // 按节点名称保存值和编码数据
            context.put(node.getName() + "_originalValue", originalValue);
            context.put(node.getName() + "_fwdExprResult", node.getFwdExprResult());
            context.put(node.getName() + "_encoded", encodeDataHex);
            context.put(node.getName() + "_node", node);

            // 按节点ID保存数据（如果存在ID）
            // 原理：节点ID是全局唯一的标识符，便于跨层级引用
            String nodeId = dependencyGraph.getNodeIdByPath(node.getPath());
            if (nodeId != null) {
                context.put(nodeId + "_encoded", encodeDataHex);               // ID -> 编码数据
                context.put(nodeId + "_originalValue", originalValue); // 原始值
                context.put(nodeId + "_fwdExprResult", node.getFwdExprResult()); // ID_fwdExprResult -> 节点结算结果值
                context.put(nodeId + "_node", node);             // ID_node -> 节点对象
                log.debug("- 添加ID映射到上下文: #{}", nodeId);
            }

            return encodedData;
        } catch (Exception e) {
            log.error("[错误] 编码失败: {}", e.getMessage());

            // 第七步：错误恢复
            // 原理：编码失败时恢复节点的原始值，避免影响后续处理或重试
            node.setValue(originalValue);
            throw new CodecException("编码节点[" + node.getName() + "]失败: " + e.getMessage(), e);
        }
    }


    /**
     * 解析源码数据为协议对象实例
     *
     * <p>核心设计原理：</p>
     * <ul>
     *   <li><b>按协议结构顺序解析</b>：按照协议定义的顺序（header → body → tail → nodes）解析数据</li>
     *   <li><b>位级数据读取</b>：使用BitUtil工具进行精确的位级数据读取</li>
     *   <li><b>反向表达式处理</b>：如果配置了反向表达式，使用反向表达式反算得到最终值</li>
     *   <li><b>字段值映射</b>：将解析出的值通过反射设置到对象实例的对应字段</li>
     *   <li><b>嵌套结构支持</b>：完全支持多层协议嵌套结构的解析</li>
     * </ul>
     *
     * <p>解析流程：</p>
     * <ol>
     *   <li>创建协议对象实例</li>
     *   <li>构建协议结构和依赖图</li>
     *   <li>按协议结构顺序解析数据</li>
     *   <li>处理反向表达式（如果存在）</li>
     *   <li>字段值映射和设置</li>
     *   <li>返回完整的协议对象实例</li>
     * </ol>
     *
     * @param sourceData       源码字节数组
     * @param protocolInstance 协议配置类
     * @return 解析后的协议对象实例
     * @throws CodecException 解析异常
     */
    public <T> T parse(byte[] sourceData, T protocolInstance) throws CodecException {
        // 第一步：初始化和验证
        log.debug("[源码解析] 开始解析源码数据为协议对象");
        log.debug("- 源码数据长度: {} 字节", sourceData.length);
        log.debug("- 源码数据: {}", ByteUtil.bytesToHexString(sourceData));

        if (sourceData.length == 0) {
            throw new CodecException("源码数据不能为空");
        }
        Map<String, Object> context = new HashMap<>();


        Protocol protocol = ProtocolClassParser.parseProtocol(protocolInstance);
        if (protocol.getId() != null) {
            context.put("protocolId", protocol.getId());
            this.currentProtocolId = protocol.getId();
        } else {
            this.currentProtocolId = null;
        }
        // 按顺序收集协议中的所有叶子节点
        List<INode> allLeafNodes = collectAllLeafNodes(protocol); // 使用已有的方法获取所有节点
        // 注册到全局协议注册表，支持跨协议引用解析
        com.iecas.cmd.registry.ProtocolRegistry.getInstance().registerProtocol(protocol, allLeafNodes);
        context.put("allLeafNodes", allLeafNodes);

        ProtocolDependencyBuilder depBuilder = new ProtocolDependencyBuilder(expressionParser, expressionValidator, dependencyGraph, allLeafNodes);
        depBuilder.build(protocol);

        // 第三步：构建协议结构
        log.debug("[源码解析] 构建协议结构");
        log.debug("- 协议结构构建完成，节点总数: {}", countTotalNodes(protocol));


        // 第六步：按协议结构顺序解析数据
        log.debug("[源码解析] 开始按协议结构顺序解析数据");
        BitBuffer buffer = new BitBuffer(sourceData);

        try {
            // 解析协议头部
            if (protocol.getHeader() != null) {
                log.debug("[源码解析] 解析协议头部");
                parseStructureInOrder(protocolInstance, protocol.getHeader(), buffer, context, "header");
            }

            // 解析协议主体
            if (protocol.getBody() != null) {
                log.debug("[源码解析] 解析协议主体");
                parseBodyInOrder(protocolInstance, protocol.getBody(), buffer, context, "body");
            }

            // 解析协议校验
            if (protocol.getTail() != null) {
                log.debug("[源码解析] 解析协议校验");
                parseStructureInOrder(protocolInstance, protocol.getTail(), buffer, context, "tail");
            }

            // 解析协议子节点列表
            if (!protocol.getNodes().isEmpty()) {
                log.debug("[源码解析] 解析协议子节点列表");
                List<Node> sortedNodes = new ArrayList<>(protocol.getNodes());
                sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

                for (Node node : sortedNodes) {
                    parseNodeFromBuffer(protocolInstance, node, buffer, context);
                }
            }

        } catch (Exception e) {
            log.error("[源码解析] 解析过程中发生异常: {}", e.getMessage());
            throw new CodecException("源码解析失败: " + e.getMessage(), e);
        }

        // 第七步：验证解析结果
        log.debug("[源码解析] 源码解析完成");
        log.debug("- 剩余未解析数据: {} 字节", buffer.getReadableBytes());
        if (buffer.getReadableBytes() > 0) {
            log.warn("[源码解析] 存在未解析的数据: {} 字节", buffer.getReadableBytes());
        }

        return protocolInstance;
    }

    /**
     * 按Body结构顺序解析数据
     */
    private void parseBodyInOrder(Object instance, Body body, BitBuffer buffer,
                                  Map<String, Object> context, String structureName) throws CodecException {
        log.debug("- 解析{}", structureName);

        // 1. 解析Body的header（如果存在）
        if (body.getHeader() != null) {
            parseStructureInOrder(instance, body.getHeader(), buffer, context, structureName + ".header");
        }

        // 2. 解析Body的直接子节点（按order排序）
        List<Node> nodes = body.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                parseNodeFromBuffer(instance, node, buffer, context);
            }
        }

        // 3. 解析Body的嵌套body（如果存在）
        if (body.getBody() != null) {
            parseBodyInOrder(instance, body.getBody(), buffer, context, structureName + ".body");
        }

        // 4. 解析Body的check（如果存在）
        if (body.getTail() != null) {
            parseStructureInOrder(instance, body.getTail(), buffer, context, structureName + ".tail");
        }
    }

    /**
     * 按结构体顺序解析数据
     */
    private void parseStructureInOrder(Object instance, INode structure, BitBuffer buffer,
                                       Map<String, Object> context, String structureName) throws CodecException {
        log.debug("- 解析{}", structureName);

        List<Node> nodes = structure.getChildren();
        if (nodes != null && !nodes.isEmpty()) {
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                parseNodeFromBuffer(instance, node, buffer, context);
            }
        }
    }


    /**
     * 从缓冲区解析单个节点
     */
    private void parseNodeFromBuffer(Object instance, Node node, BitBuffer buffer,
                                     Map<String, Object> context) throws CodecException {
        // 检查节点是否启用
        if (!node.isEnabled()) {
            log.debug("[源码解析] 跳过禁用节点: {} (原因: {})", node.getName(), node.getEnabledReason());
            return;
        }

        log.debug("==============[源码解析] 处理节点: {}", node.getName());
        log.debug("- 节点类型: {}", node.getValueType());
        log.debug("- 节点长度: {} 位", node.getLength());

        // 添加当前节点名称到上下文
        context.put("current", node.getName());
        context.put("currentPath", node.getPath());

        try {
            // 特殊处理GroupContainer：递归解析其子节点
            if (node instanceof NodeGroup) {
                log.debug("- 发现GroupContainer节点，递归解析其子节点");
                NodeGroup nodeGroup = (NodeGroup) node;
                List<Node> groupNodes = nodeGroup.getGroupNodes();

                if (groupNodes != null && !groupNodes.isEmpty()) {
                    log.debug("- GroupContainer包含 {} 个子节点，开始递归解析", groupNodes.size());
                    for (Node childNode : groupNodes) {
                        parseNodeFromBuffer(instance, childNode, buffer, context);
                    }
                    log.debug("- GroupContainer子节点解析完成");
                } else {
                    log.debug("- GroupContainer没有子节点，跳过");
                }
                return;
            }

            // 跳过其他结构体节点的解析
            if (node.isStructureNode()) {
                log.debug("- 结构体节点，跳过解析");
                return;
            }

            // 从数据中读取该节点的源码
            Object rawValue = readNodeDataFromBuffer(buffer, node);
            log.debug("- 读取的原始值: {}", rawValue);

            // 处理反向表达式（如果存在）
            Object finalValue = processBackwardExpression(node, rawValue, context);
            log.debug("- 反向表达式处理后的值: {}", finalValue);

            // 将解析出的值设置到实例对象
            setNodeValueToInstance(instance, node, finalValue);

            // 将解析结果添加到上下文
            context.put(node.getName(), finalValue);
            context.put(node.getName() + "_raw", rawValue);
            context.put(node.getName() + "_parsed", finalValue);

            // 如果节点有ID，也用ID作为键添加到上下文中
            String nodeId = dependencyGraph.getNodeIdByPath(node.getPath());
            if (nodeId != null) {
                context.put(nodeId, finalValue);
                context.put(nodeId + "_raw", rawValue);
                context.put(nodeId + "_parsed", finalValue);
            }

        } catch (CodecException e) {
            log.error("[源码解析] 解析节点失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 将节点值设置到实例对象
     */
    private void setNodeValueToInstance(Object instance, Node node, Object value) throws CodecException {
        try {
            // 通过反射设置字段值
            Field field = findFieldByName(instance.getClass(), node.getFieldName());
            if (field == null) {
                log.debug("- 未找到对应字段: {}，跳过设置", node.getName());
                return;
            }
            field.setAccessible(true);

            field.set(instance, EnumHelper.processEnumForDecode(node, value));

            log.debug("- 成功设置字段值: {} = {}", node.getName(), value);

        } catch (Exception e) {
            Field[] declaredFields = instance.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.getType().getName().equals("java.util.List")) {
                    System.out.println();
                    try {
                        List<Node> nodes = (List<Node>) declaredField.get(instance);
                        for (Node n : nodes) {
                            if (n.getName().equals(node.getName())) {
                                String valueStr = formatTargetType(value.toString(), node.getValueType());
                                n.setValue(EnumHelper.processEnumForDecode(node, valueStr));
                                break;
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
//
//            log.error("[源码解析] 设置字段值失败: {} = {}", node.getName(), value);
//            throw new CodecException("设置字段值失败: " + node.getName() + "，错误: " + e.getMessage(), e);
        }
    }

    /**
     * 根据名称查找字段
     */
    private Field findFieldByName(Class<?> clazz, String fieldName) {
        // 先查找当前类
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 查找父类
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findFieldByName(superClass, fieldName);
            }
        }
        return null;
    }

    /**
     * 处理反向表达式
     */
    private Object processBackwardExpression(Node node, Object rawValue, Map<String, Object> context) throws CodecException {
        String bwdExpr = node.getBwdExpr();
        if (StringUtils.isEmpty(bwdExpr)) {
            log.debug("- 无反向表达式，使用原始值");
            return rawValue;
        }

        try {
            log.debug("- 执行反向表达式: {}", bwdExpr);

            // 验证表达式语法
            expressionValidator.validateSyntax(bwdExpr);

            // 准备表达式执行所需的依赖数据
            prepareExpressionDependencies(node.getPath(), bwdExpr, context);

            // 将原始值添加到上下文中作为value变量
            context.put("value", rawValue);
            context.put("rawValue", rawValue);

            // 执行反向表达式计算
            Object result = expressionEngine.evaluate(bwdExpr, context);
            log.debug("- 反向表达式计算结果: {}", result);

            // 验证结果有效性
            if (result == null) {
                log.warn("[源码解析] 反向表达式结果为null，使用原始值");
                return rawValue;
            }

            return result;

        } catch (Exception e) {
            log.error("[源码解析] 执行反向表达式失败: {}", e.getMessage());
            log.debug("- 反向表达式执行失败，使用原始值");
            return rawValue;
        }
    }

    /**
     * 从缓冲区读取节点数据
     */
    private Object readNodeDataFromBuffer(BitBuffer buffer, Node node) throws CodecException {
        int bitLength = node.getLength();
        if (bitLength <= 0) {
            log.debug("- 节点长度为0，跳过读取");
            return null;
        }

        // 检查是否有足够的数据
        if (buffer.getReadableBits() < bitLength) {
            throw new CodecException("数据不足，无法读取完整的节点数据: " + node.getName() +
                    "，需要 " + bitLength + " 位，可用 " + buffer.getReadableBits() + " 位");
        }

        try {
            if (bitLength <= 32) {
                // 对于32位以内的数据，使用BitBuffer的readBits方法
                int value = buffer.readBits(bitLength);
                log.debug("- 使用readBits读取{}位数据: {}", bitLength, value);
                return convertToTargetType(value, node.getValueType(), bitLength);
            } else {
                // 对于超过32位的数据，按位读取
                return readLargeNodeData(buffer, bitLength, node.getValueType());
            }
        } catch (Exception e) {
            throw new CodecException("读取节点数据失败: " + node.getName() + "，错误: " + e.getMessage(), e);
        }
    }

    /**
     * 读取超过32位的大节点数据
     */
    private Object readLargeNodeData(BitBuffer buffer, int bitLength, ValueType valueType) throws CodecException {
        log.debug("- 使用逐位读取{}位数据", bitLength);

        // 计算需要的字节数
        int byteLength = (bitLength + 7) / 8;
        byte[] nodeData = new byte[byteLength];

        // 按位读取数据
        for (int i = 0; i < bitLength; i++) {
            boolean bit = buffer.readBit();
            if (bit) {
                int byteIndex = i / 8;
                int bitIndex = 7 - (i % 8); // 大端序位索引
                nodeData[byteIndex] |= (byte) (1 << bitIndex);
            }
        }

        return convertByteArrayToTargetType(nodeData, valueType, bitLength);
    }

    /**
     * 将字节数组转换为目标类型
     */
    private Object convertByteArrayToTargetType(byte[] data, ValueType valueType, int bitLength) {
        switch (valueType) {
            case TIME:
            case INT:
            case UINT:
                return BitUtil.byteArrayToBits(data);
            case HEX:
                return BitUtil.byteArrayToHexString(data);
            case BIT:
                return BitUtil.bytesToBitString(data);
            case FLOAT:
                if (bitLength == 32) {
                    int intValue = (int) BitUtil.byteArrayToBits(data);
                    return Float.intBitsToFloat(intValue);
                } else if (bitLength == 64) {
                    long longValue = BitUtil.byteArrayToBits(data);
                    return Double.longBitsToDouble(longValue);
                } else {
                    return BitUtil.byteArrayToBits(data);
                }
            case STRING:
                return new String(data);
            default:
                return data;
        }
    }


    /**
     * 将值转换为目标类型
     */
    private Object convertToTargetType(int value, ValueType valueType, int bitLength) {
        switch (valueType) {
            case TIME:
            case INT:
            case UINT:
                return (long) value;
            case HEX:
                return "0x" + Integer.toHexString(value).toUpperCase();
            case BIT:
                return BitUtil.toBinaryString(value, bitLength);
            case FLOAT:
                if (bitLength == 32) {
                    return Float.intBitsToFloat(value);
                } else {
                    return (double) value;
                }
            case STRING:
                return String.valueOf(value);
            default:
                return value;
        }
    }

    /**
     * 将值转换为目标类型
     */
    private String formatTargetType(String value, ValueType valueType) {

        switch (valueType) {
            case INT:
            case UINT:
                Double v = Double.parseDouble(value);
                return String.valueOf(v.longValue());
            case HEX:
                if (!value.startsWith("0x") && !value.startsWith("0X") && value.endsWith("H")) {
                    return "0x" + value.toUpperCase();
                }
                return value;
            case BIT:
                if (!value.startsWith("0b")) {
                    return "0b" + value.toUpperCase();
                }
                return value;
            default:
                return value;
        }
    }

    /**
     * 统计协议中的总节点数
     */
    private int countTotalNodes(Protocol protocol) {
        int count = 0;

        if (protocol.getHeader() != null) {
            count += countNodesRecursively(protocol.getHeader());
        }
        if (protocol.getBody() != null) {
            count += countNodesRecursively(protocol.getBody());
        }
        if (protocol.getTail() != null) {
            count += countNodesRecursively(protocol.getTail());
        }
        count += protocol.getNodes().size();

        return count;
    }

    /**
     * 递归统计节点数
     */
    private int countNodesRecursively(INode node) {
        int count = 1; // 当前节点

        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                count += countNodesRecursively(child);
            }
        }

        // 处理特殊结构节点
        if (node instanceof Body) {
            Body body = (Body) node;
            if (body.getHeader() != null) {
                count += countNodesRecursively(body.getHeader());
            }
            if (body.getBody() != null) {
                count += countNodesRecursively(body.getBody());
            }
            if (body.getTail() != null) {
                count += countNodesRecursively(body.getTail());
            }
        }

        return count;
    }

    /**
     * 准备表达式执行所需的依赖数据
     *
     * <p>核心设计原理：</p>
     * <ul>
     *   <li><b>依赖解析</b>：从表达式中提取所有变量引用</li>
     *   <li><b>数据注入</b>：将依赖节点的数据注入到执行上下文中</li>
     *   <li><b>多重映射</b>：支持按名称和ID两种方式引用节点数据</li>
     *   <li><b>缓存优先</b>：优先从缓存中获取已编码的节点数据</li>
     * </ul>
     *
     * <p>依赖类型：</p>
     * <ul>
     *   <li>节点ID引用：如 #nodeId</li>
     *   <li>节点名称引用：如 nodeName</li>
     *   <li>节点值引用：如 nodeName_value</li>
     *   <li>编码数据引用：如 nodeName_encoded</li>
     *   <li>节点对象引用：如 nodeName_node</li>
     * </ul>
     *
     * @param nodePath
     * @param expression 要执行的表达式
     * @param context    执行上下文，将在其中注入依赖数据
     */
    private void prepareExpressionDependencies(String nodePath, String expression, Map<String, Object> context) throws CodecException {
        log.debug("- 准备表达式依赖:{}", expression);

        // 第一步：解析表达式中的所有依赖变量
        // 原理：表达式可能引用其他节点的值，需要确保这些依赖在执行前可用
        // todo 这里需要验证是否这样是否能获取到所有依赖
//        Set<String> dependencies = expressionParser.parseDependencies(expression, allLeafNodes);
        Set<String> dependencies = dependencyGraph.getDependencies(nodePath);


        // 第二步：为每个依赖变量准备数据
        for (String dependency : dependencies) {
            boolean found = false;

            // 第三步：检查上下文中是否已存在该依赖
            // 原理：避免重复添加，提高性能
            for (String key : context.keySet()) {
                if (key.equals(dependency) ||
                        key.equals(dependency + "_value") ||    // 节点值
                        key.equals(dependency + "_encoded") ||  // 编码数据
                        key.equals(dependency + "_node")) {     // 节点对象
                    found = true;
                    log.debug("  - 依赖已在上下文中: {}", key);
                    break;
                }
            }

            // 第四步：从缓存中补充缺失的依赖数据
            // 原理：如果上下文中没有，但节点已经编码并缓存，则从缓存获取
            if (!found && encodedNodesCache.containsKey(dependency)) {
                byte[] encodedData = encodedNodesCache.get(dependency);
                context.put(dependency, encodedData);
                log.debug("  - 从缓存添加依赖到上下文: #{}", dependency);

                // 第五步：补充节点的完整信息
                // 原理：表达式可能需要访问节点的多种属性（值、对象等）
                INode depNode = dependencyGraph.getNodeById(dependency);
                if (depNode != null) {
                    context.put(dependency + "_node", depNode);        // 节点对象
                    context.put(dependency + "_value", depNode.getValue()); // 节点值
                    log.debug("  - 添加依赖节点对象: #{}", dependency);
                }
            }
        }

        // 第六步：输出依赖准备的调试信息
        // 原理：便于排查表达式执行问题，确认所有依赖都已正确准备
//        log.debug("  - 表达式上下文中的依赖项:");
//        for (String dep : dependencies) {
//            if (context.containsKey(dep)) {
//                if (context.get(dep) instanceof byte[]) {
//                    log.debug("    {}: {}", dep, ByteUtil.bytesToHexString((byte[]) context.get(dep)));
//                } else {
//                    log.debug("    {}: {}", dep, context.get(dep));
//                }
//            } else {
//                log.error("    {}: [未找到]", dep);
//            }
//        }
    }

    /**
     * 验证编码后的数据
     */
    private void validateEncodedData(INode node, byte[] encodedData) throws CodecException {
        // 对结构体节点跳过长度验证
        if (node.isStructureNode()) {
            log.debug("- 结构体节点，跳过长度验证");
            return;
        }

        // 验证长度
        if (node.getLength() > 0) {
            int expectedLength = (node.getLength() + 7) / 8; // 将位长度转换为字节长度
            if (encodedData.length != expectedLength) {
                throw new CodecException(String.format("节点[%s]编码后的长度[%d]与定义的长度[%d]不匹配",
                        node.getName(), encodedData.length, expectedLength));
            }
            log.debug("- 长度验证通过: {} 字节", encodedData.length);
        }
    }

    /**
     * 解码协议 - 验证编码正确性
     *
     * <p>核心设计思想：</p>
     * <ul>
     *   <li><b>验证导向</b>：解码的目的不是重建协议，而是验证编码的正确性</li>
     *   <li><b>位置精确</b>：按协议结构顺序精确定位每个节点在数据中的位置</li>
     *   <li><b>逆向验证</b>：通过反向编解码验证数据的一致性</li>
     *   <li><b>表达式支持</b>：支持反向表达式进行数据转换验证</li>
     * </ul>
     *
     * <p>解码流程：</p>
     * <ol>
     *   <li><b>预处理阶段</b>：构建依赖图，处理条件和填充</li>
     *   <li><b>数据解析阶段</b>：按协议结构顺序从数据中提取各节点数据</li>
     *   <li><b>反向解码阶段</b>：使用对应编解码器将字节数据转换回值</li>
     *   <li><b>表达式转换阶段</b>：执行反向表达式进行数据转换</li>
     *   <li><b>验证阶段</b>：将反编结果与原始值比较，判断一致性</li>
     * </ol>
     *
     * <p>为什么按结构顺序而不是依赖顺序？</p>
     * <ul>
     *   <li>数据位置：编码数据是按协议结构顺序排列的，必须按相同顺序解析</li>
     *   <li>位置精确性：任何顺序错误都会导致数据位置偏移，无法正确解码</li>
     *   <li>验证目标：解码是为了验证编码正确性，不需要重新计算依赖</li>
     * </ul>
     *
     * @param data     编码后的字节数组数据
     * @param protocol 协议对象，解码结果将填充到其节点中
     * @return 所有节点的解码验证结果集合，按处理顺序排列
     * @throws CodecException 解码验证过程中发生的异常
     */
    public List<Node> decode(byte[] data, Protocol protocol) throws CodecException {
        // 第一步：初始化解码过程
        log.debug("[反编验证] 开始协议反编验证过程");
        log.debug("- 输入数据长度: {} 字节", data.length);
        log.debug("- 输入数据: {}", ByteUtil.bytesToHexString(data));

        // 第五步：创建解码上下文
        Map<String, Object> context = new HashMap<>();

        // 按顺序收集协议中的所有叶子节点
        List<INode> allLeafNodes = collectAllLeafNodes(protocol); // 使用已有的方法获取所有节点
        context.put("allLeafNodes", allLeafNodes);

        // 第二步：构建依赖图（与编码过程相同）
        // 原理：虽然解码不需要依赖计算，但需要依赖图来管理节点信息和ID映射
        log.debug("[反编验证] 构建节点依赖关系");
        ProtocolDependencyBuilder depBuilder = new ProtocolDependencyBuilder(expressionParser, expressionValidator, dependencyGraph, allLeafNodes);
        depBuilder.build(protocol);

        // 第三步：解码不需要分析填充依赖关系
        // 原理：解码按协议结构顺序进行，不需要依赖图排序，避免循环依赖问题


        // 第六步：处理条件依赖（与编码保持一致）
        log.debug("[反编验证] 处理条件依赖");
        conditionalDependencyProcessor.processConditionalDependencies(context, allLeafNodes);

        // 注意：移除了填充配置的预处理步骤
        // 原理：解码时不需要预先计算填充，而是在解码过程中识别和处理填充数据

        // 第七步：按协议结构顺序解码数据
        // 原理：解码必须按照协议定义的结构顺序进行，这与编码时的最终数据布局一致
        BitBuffer buffer = new BitBuffer(data);
        List<Node> decodedNodes = new ArrayList<>();
        log.debug("[反编验证] 开始按协议结构顺序解码数据");

        // 解码协议头部
        if (protocol.getHeader() != null) {
            log.debug("[反编验证] 解码协议头部");
            decodeStructureInOrder(protocol.getHeader(), buffer, context, decodedNodes, "header");
        }

        // 解码协议主体
        if (protocol.getBody() != null) {
            log.debug("[反编验证] 解码协议主体");
            decodeBodyInOrder(protocol.getBody(), buffer, context, decodedNodes, "body");
        }

        // 解码协议校验
        if (protocol.getTail() != null) {
            log.debug("[反编验证] 解码协议校验");
            decodeStructureInOrder(protocol.getTail(), buffer, context, decodedNodes, "tail");
        }
        if (!protocol.getNodes().isEmpty()) {
            log.debug("[反编验证] 解码协议子节点列表");
            // 按order属性排序
            List<Node> sortedNodes = new ArrayList<>(protocol.getNodes());
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                decodeNodeFromBuffer(node, buffer, context, decodedNodes);
            }
        }


        // 第八步：验证解码结果
        log.debug("[反编验证] 协议反编验证完成");
        log.debug("- 解码节点数量: {}", decodedNodes.size());
        log.debug("- 剩余数据长度: {} 字节", buffer.getReadableBytes());

        if (buffer.getReadableBytes() > 0) {
            log.debug("[反编验证] 存在未解码的数据: {} 字节", buffer.getReadableBytes());
        }

        // 打印表头
        // 动态计算列宽，最大化显示数据内容
        int[] columnWidths = calculateOptimalColumnWidths(decodedNodes);

        // 打印表头
        printTableHeader(columnWidths);

        // 打印节点数据
        for (Node decodedNode : decodedNodes) {
            String name = padString(String.valueOf(decodedNode.getName()), columnWidths[0]);
            String id = padString(String.valueOf(decodedNode.getId()), columnWidths[1]);
            String type = padString(String.valueOf(decodedNode.getValueType()), columnWidths[2]);
            String length = padString(formatNodeLength(decodedNode), columnWidths[3]);
            String value = padString(String.valueOf(decodedNode.getValue()), columnWidths[4]);
            String decodedValue = padString(String.valueOf(decodedNode.getDecodedValue()), columnWidths[5]);
            String result = padString(decodedNode.getValidationResult() != null && decodedNode.getValidationResult() ? "通过" : "失败", columnWidths[6]);
            String position = padString(decodedNode.getBitPositionRange(), columnWidths[7]);

            log.debug("│ {} │ {} │ {} │ {} │ {} │ {} │ {} │ {} │",
                    name, id, type, length, value, decodedValue, result, position);

            if (decodedNode.getValidationError() != null) {
                // 错误信息列宽为所有列的总宽度减去分隔符
                int errorColumnWidth = columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3] +
                        columnWidths[4] + columnWidths[5] + columnWidths[6] + columnWidths[7] + 15; // 15是分隔符的总长度
                String error = padString(decodedNode.getValidationError(), errorColumnWidth);
                log.error("│ 错误信息: {} │", error);
            }
        }

        // 打印表尾
        log.debug("└" + repeat("─", columnWidths[0]) + "┴" + repeat("─", columnWidths[1]) + "┴" + repeat("─", columnWidths[2]) + "┴" + repeat("─", columnWidths[3]) + "┴" + repeat("─", columnWidths[4]) + "┴" + repeat("─", columnWidths[5]) + "┴" + repeat("─", columnWidths[6]) + "┴" + repeat("─", columnWidths[7]) + "┘");


        return decodedNodes;
    }

    private static String formatNodeLength(Node decodedNode) {
        int nodeLength = decodedNode.getLength();
        String lengthStr;
        if (nodeLength % 8 == 0) {
            lengthStr = (nodeLength / 8) + "字节";
        } else {
            lengthStr = nodeLength + "比特";
        }
        return lengthStr;
    }

    /**
     * 按Body结构顺序解码数据
     */
    private void decodeBodyInOrder(Body body, BitBuffer buffer, Map<String, Object> context,
                                   List<Node> decodedNodes, String structureName) throws CodecException {
        log.debug("- 解码{}", structureName);

        // 1. 解码Body的header（如果存在）
        if (body.getHeader() != null) {
            decodeStructureInOrder(body.getHeader(), buffer, context, decodedNodes, structureName + ".header");
        }

        // 2. 解码Body的直接子节点（按order排序）
        List<Node> nodes = body.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            // 按order属性排序
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                decodeNodeFromBuffer(node, buffer, context, decodedNodes);
            }
        }

        // 3. 解码Body的嵌套body（如果存在）
        if (body.getBody() != null) {
            decodeBodyInOrder(body.getBody(), buffer, context, decodedNodes, structureName + ".body");
        }

        // 4. 解码Body的check（如果存在）
        if (body.getTail() != null) {
            decodeStructureInOrder(body.getTail(), buffer, context, decodedNodes, structureName + ".tail");
        }
    }

    /**
     * 按结构体顺序解码数据
     */
    private void decodeStructureInOrder(INode structure, BitBuffer buffer, Map<String, Object> context,
                                        List<Node> decodedNodes, String structureName) throws CodecException {
        log.debug("- 解码{}", structureName);

        List<Node> nodes = structure.getChildren();
        if (nodes != null && !nodes.isEmpty()) {
            // 按order属性排序
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                decodeNodeFromBuffer(node, buffer, context, decodedNodes);
            }
        }
    }

    /**
     * 从缓冲区解码单个节点
     */
    private void decodeNodeFromBuffer(Node node, BitBuffer buffer, Map<String, Object> context,
                                      List<Node> decodedNodes) throws CodecException {
        // 检查节点是否启用
        if (!node.isEnabled()) {
            log.debug("[反编验证] 跳过禁用节点: {} (原因: {})", node.getName(), node.getEnabledReason());
            node.setValidationStatus("SKIPPED_DISABLED");
            node.setValidationResult(null);
            decodedNodes.add(node);
            return;
        }

        log.debug("==============[反编验证] 处理节点: {}", node.getName());
        log.debug("- 节点类型: {}", node.getValueType());
        log.debug("- 节点长度: {} 位", node.getLength());
        log.debug("- 原始值: {}", node.getValue());

        // 添加当前节点名称到上下文
        context.put("current", node.getName());
        context.put("currentPath", node.getPath());

        try {
            // 对于结构体节点，需要递归验证其子节点
            if (node.isStructureNode()) {
                log.debug("- 结构体节点，开始递归验证子节点");
                node.setValidationStatus("STRUCTURE_VALIDATING");

                // 递归验证结构体节点的子节点
                validateStructureNodeRecursively(node, buffer, context, decodedNodes);
                return;
            }

            // 检查普通节点是否有子节点，如果有也需要递归处理
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                log.debug("- 普通节点 {} 有 {} 个子节点，开始递归验证子节点", node.getName(), node.getChildren().size());
                node.setValidationStatus("CHILDREN_VALIDATING");

                // 递归验证子节点
                validateStructureNodeRecursively(node, buffer, context, decodedNodes);

                // 设置节点的位置范围（基于其子节点的位置范围）
                if (!node.getChildren().isEmpty()) {
                    Node firstChild = node.getChildren().get(0);
                    Node lastChild = node.getChildren().get(node.getChildren().size() - 1);
                    node.setStartBitPosition(firstChild.getStartBitPosition());
                    node.setEndBitPosition(lastChild.getEndBitPosition());
                    log.debug("- 普通节点 {} 位置范围: [{}:{}]", node.getName(),
                            node.getStartBitPosition(), node.getEndBitPosition());
                }

                // 将节点添加到结果列表中
                decodedNodes.add(node);
                return;
            }

            // 记录节点在编码结果中的起始位位置
            int startBitPosition = buffer.getReadBitPosition();
            node.setStartBitPosition(startBitPosition);

            // 从数据中截取该节点的源码
            byte[] nodeSourceData = extractNodeData(buffer, node.getLength());
            log.debug("- 截取的源码数据: {}", ByteUtil.bytesToHexString(nodeSourceData));

            // 记录节点在编码结果中的结束位位置
            int endBitPosition = buffer.getReadBitPosition() - 1;
            node.setEndBitPosition(endBitPosition);

            log.debug("- 位置范围: [{}:{}] ({}位)", startBitPosition, endBitPosition, node.getLength());

            // 反编验证节点，结果直接填充到节点中
            validateAndFillNodeDecoding(nodeSourceData, node, context);

            // 将处理过的节点添加到结果集合中
            decodedNodes.add(node);

        } catch (CodecException e) {
            log.error("[错误] 反编验证节点失败: {}", e.getMessage());
            node.setValidationStatus("失败");
            node.setValidationResult(false);
            node.setValidationError(e.getMessage());
            decodedNodes.add(node);
        }
    }

    /**
     * 从位缓冲区中截取指定长度的节点数据
     */
    private byte[] extractNodeData(BitBuffer buffer, int bitLength) throws CodecException {
        if (bitLength <= 0) {
            return new byte[0];
        }

        // 检查是否有足够的数据
        if (buffer.getReadableBits() < bitLength) {
            throw new CodecException("数据不足，无法读取完整的节点数据");
        }

        // 计算需要的字节数
        int byteLength = (bitLength + 7) / 8;
        byte[] nodeData = new byte[byteLength];

        // 直接读取数据，让缓冲区位置自然前进
        if (bitLength <= 32) {
            // 对于32位以内的数据，使用readBits方法
            int value = buffer.readBits(bitLength);

            // 将读取的值转换为字节数组（大端序）
            for (int i = 0; i < byteLength; i++) {
                int shiftBits = (byteLength - 1 - i) * 8;
                if (shiftBits < bitLength) {
                    nodeData[i] = (byte) ((value >> shiftBits) & 0xFF);
                }
            }
        } else {
            // 对于超过32位的数据，按位读取
            for (int i = 0; i < bitLength; i++) {
                boolean bit = buffer.readBit();
                if (bit) {
                    int byteIndex = i / 8;
                    int bitIndex = 7 - (i % 8); // 大端序位索引
                    nodeData[byteIndex] |= (byte) (1 << bitIndex);
                }
            }
        }

        return nodeData;
    }

    /**
     * 验证节点解码 - 反编并与原始值比较，结果直接填充到节点中
     */
    private void validateAndFillNodeDecoding(byte[] sourceData, Node node, Map<String, Object> context) throws CodecException {
        String nodeName = node.getName();

        // 获取对应的编解码器
        Codec codec = codecFactory.getCodec(node.getValueType());
        log.debug("- 使用编解码器: {}", codec.getClass().getSimpleName());

        try {
            // 从源码数据反编出值
            log.debug("- 开始从源码反编节点数据");
            Object decodedValue = codec.decode(sourceData, node, context);
            log.debug("- 反编结果: {}", decodedValue);

            // 获取原始值进行比较
            Object originalValue = node.getValue();
            log.debug("- 原始值: {}", originalValue);

            // 执行反向表达式（如果存在）
            boolean isEqual;
            if (StringUtils.isNotEmpty(node.getBwdExpr())) {
                log.debug("- 执行反向表达式: {}", node.getBwdExpr());
                context.put("value", decodedValue);
                Object transformedValue = expressionEngine.evaluate(node.getBwdExpr(), context);
                log.debug("- 反向表达式计算结果: {}", transformedValue);
                // 使用对应的比较器进行比较
                isEqual = compareValues(originalValue, transformedValue, node.getValueType(), node);
                node.setTransformedValue(transformedValue);
                node.setDecodedValue(transformedValue);
            } else {
                // 使用对应的比较器进行比较
                isEqual = compareValues(originalValue, decodedValue, node.getValueType(), node);
                node.setDecodedValue(decodedValue);
            }
            node.setSourceData("0x" + ByteUtil.bytesToHexString(sourceData));
            node.setValidationResult(isEqual);
            node.setValidationStatus(isEqual ? "成功" : "失败");

            if (isEqual) {
                log.debug("- 验证结果: 通过 ✓");
            } else {
                log.error("- 验证结果: 失败 ✗");
            }

            // 将反编后的值添加到上下文
            context.put(nodeName + "transformedValue", node.getTransformedValue());
            context.put(nodeName + "_source", node.getSourceData());

            // 如果节点有ID，也用ID作为键添加到上下文中
            String nodeId = dependencyGraph.getNodeIdByPath(node.getPath());
            if (nodeId != null) {
                context.put(nodeId + "_source", sourceData);
                context.put(nodeId + "_decodeValue", node.getDecodedValue());
                context.put(nodeName + "_transformedValue", node.getTransformedValue());
                context.put(nodeId + "_node", node);
            }

        } catch (Exception e) {
            log.error("[错误] 反编验证失败: {}", e.getMessage());
            node.setValidationStatus("失败");
            node.setValidationResult(false);
            node.setValidationError(e.getMessage());
            throw new CodecException("反编验证节点[" + nodeName + "]失败: " + e.getMessage(), e);
        }
    }

    /**
     * 递归验证结构体节点的子节点
     */
    private void validateStructureNodeRecursively(Node structureNode, BitBuffer buffer, Map<String, Object> context, List<Node> decodedNodes) throws CodecException {
        log.debug("- 开始递归验证结构体节点: {} 的子节点", structureNode.getName());

        // 获取结构体节点的子节点
        List<Node> children = structureNode.getChildren();
        if (children == null || children.isEmpty()) {
            log.debug("- 结构体节点 {} 没有子节点", structureNode.getName());
            return;
        }

        log.debug("- 结构体节点 {} 有 {} 个子节点需要验证", structureNode.getName(), children.size());

        // 递归验证每个子节点
        for (Node child : children) {
            try {
                log.debug("-----------------[反编验证] 处理子节点: {}", child.getName());
                log.debug("- 子节点类型: {}, 长度: {} 位", child.getValueType(), child.getLength());

                // 记录子节点在编码结果中的起始位位置
                int startBitPosition = buffer.getReadBitPosition();
                child.setStartBitPosition(startBitPosition);

                // 从数据中截取该子节点的源码
                byte[] childSourceData = extractNodeData(buffer, child.getLength());
                log.debug("- 子节点 {} 截取的源码数据: {}", child.getName(), ByteUtil.bytesToHexString(childSourceData));

                // 缓冲区位置已经由extractNodeData自动更新，无需手动设置

                // 记录子节点在编码结果中的结束位位置
                int endBitPosition = startBitPosition + child.getLength() - 1;
                child.setEndBitPosition(endBitPosition);

                log.debug("- 子节点 {} 位置范围: [{}:{}] ({}位)", child.getName(), startBitPosition, endBitPosition, child.getLength());

                // 反编验证子节点
                validateAndFillNodeDecoding(childSourceData, child, context);

                // 如果子节点本身也是结构体节点，递归处理其子节点
                if (child.isStructureNode()) {
                    log.debug("- 子节点 {} 也是结构体节点，递归处理其子节点", child.getName());
                    validateStructureNodeRecursively(child, buffer, context, decodedNodes);
                }

                log.debug("- 子节点 {} 处理完成", child.getName());

            } catch (CodecException e) {
                log.error("- 子节点 {} 验证失败: {}", child.getName(), e.getMessage());
                child.setValidationStatus("失败");
                child.setValidationResult(false);
                child.setValidationError(e.getMessage());
                // 继续验证其他子节点，不中断整个验证过程
            }
        }

        // 将所有子节点添加到结果列表中
        log.debug("- 将 {} 个子节点添加到结果列表", children.size());
        decodedNodes.addAll(children);

        log.debug("- 结构体节点 {} 的子节点验证完成，总共处理了 {} 个子节点", structureNode.getName(), children.size());
    }


    /**
     * 根据值类型比较两个值是否相等
     */
    private boolean compareValues(Object originalValue, Object decodedValue, ValueType valueType, Node node) {
        if (originalValue == null && decodedValue == null) {
            return true;
        }
        if (originalValue == null || decodedValue == null) {
            return false;
        }

        // 优先处理枚举类型
        if (node.getEnumRanges() != null && !node.getEnumRanges().isEmpty()) {
            // 枚举类型比较 - 转换为字符串比较
            String originalStr = originalValue.toString();
            String decodedStr = decodedValue.toString();
            return originalStr.equals(decodedStr);
        }

        try {
            switch (valueType) {
                case HEX:
                    // 十六进制值比较 - 转换为字符串比较
                    return compareHexValues(originalValue, decodedValue);

                case BIT:
                    // 二进制值比较 - 转换为布尔值比较
                    return compareBitValues(originalValue, decodedValue);

                case INT:
                case UINT:
                    // 整数值比较 - 转换为长整型比较
                    return compareIntegerValues(originalValue, decodedValue);

                case FLOAT:
                    // 浮点数值比较 - 使用精度容差比较
                    return compareFloatValues(originalValue, decodedValue, node);

                case STRING:
                    // 字符串值比较 - 直接字符串比较
                    return compareStringValues(originalValue, decodedValue);
                case TIME:
                    return compareTimeValues(originalValue, decodedValue);
                default:
                    // 默认使用对象equals比较
                    return originalValue.equals(decodedValue);
            }
        } catch (Exception e) {
            log.debug("值比较异常: " + e.getMessage() + "，使用默认比较");
            return originalValue.equals(decodedValue);
        }
    }

    private boolean compareHexValues(Object original, Object decoded) {
        return ByteUtil.compareHexValues(original, decoded);
    }

    private boolean compareBitValues(Object original, Object decoded) {
        return BitUtil.compareBitValues(original, decoded);
    }

    private boolean compareIntegerValues(Object original, Object decoded) {
        try {
            long originalLong = parseFlexibleLong(original);
            long decodedLong = parseFlexibleLong(decoded);
            return originalLong == decodedLong;
        } catch (Exception e) {
            Double originalDouble = convertToDouble(original);
            Double decodedDouble = convertToDouble(decoded);
            float tolerance = 1e-9f;
            return Math.abs(originalDouble - decodedDouble) < tolerance;
        }

    }

    public boolean compareFloatValues(Object original, Object decoded, Node node) throws CodecException {
        log.debug("- 比较浮点数值, 原始值 = {}, 反编值 = {}", original, decoded);
        int length = node.getLength();
        if (length == 32) {
            Float originalFloat = convertToFloat(original);
            Float decodedDFloat = convertToFloat(decoded);
            float tolerance = 1e-6f;
            return Math.abs(originalFloat - decodedDFloat) < tolerance;
        } else if (length == 64) {
            Double originalDouble = convertToDouble(original);
            Double decodedDouble = convertToDouble(decoded);
            float tolerance = 1e-9f;
            return Math.abs(originalDouble - decodedDouble) < tolerance;
        } else {
            throw new CodecException(String.format("【错误】%s 不支持的浮点数长度:%s 位", node.getName(), length));
        }

    }

    private boolean compareStringValues(Object original, Object decoded) {
        String originalStr = original.toString();
        String decodedStr = decoded.toString();
        return originalStr.equals(decodedStr);
    }

    private boolean compareTimeValues(Object original, Object decoded) {
        // 时间值比较，可以根据具体的时间格式进行转换比较
        LocalDateTime time1 = TimeUtil.parseLocalDateTime(original.toString());
        LocalDateTime time2 = TimeUtil.parseLocalDateTime(decoded.toString());
        return time1.isEqual(time2);
    }

    // 辅助转换方法
    private String convertToHexString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return Long.toHexString(((Number) value).longValue()).toUpperCase();
        } else if (value instanceof byte[]) {
            return ByteUtil.bytesToHexString((byte[]) value);
        }
        return value.toString();
    }

    private long parseFlexibleLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return 0L;
        }
        // 支持 0x/0X 前缀十六进制
        if (text.startsWith("0x") || text.startsWith("0X")) {
            return Long.parseUnsignedLong(text.substring(2), 16);
        }
        // 兼容可能的十六进制后缀 H/h
        if (text.endsWith("H") || text.endsWith("h")) {
            return Long.parseUnsignedLong(text.substring(0, text.length() - 1), 16);
        }
        // 默认按十进制解析（允许无符号）
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            // 回退：尝试按十六进制解析（无前缀场景，例如 "11" 实际想表示0x11 的情况不处理，避免误判）
            return Long.parseUnsignedLong(text, 10);
        }
    }


    /**
     * 编码结构体节点
     */
    private byte[] encodeStructureNode(INode structureNode, Map<String, Object> context) throws CodecException {
        log.debug("- 结构体节点类型: {}", structureNode.getTagName());

        // 使用BitBuffer来处理位级别的数据拼接
        BitBuffer buffer = new BitBuffer();
        int totalBits = 0;

        // 对于Body类型，需要特殊处理其子结构
        if (structureNode instanceof Body) {
            Body body = (Body) structureNode;
            log.debug("- Body节点，收集子结构编码数据");

            // 收集header数据
            if (body.getHeader() != null) {
                totalBits += appendChildDataToBuffer(buffer, body.getHeader(), context, "header");
            }

            // 收集嵌套body数据
            if (body.getBody() != null) {
                totalBits += appendChildDataToBuffer(buffer, body.getBody(), context, "嵌套body");
            }

            // 收集check数据
            if (body.getTail() != null) {
                totalBits += appendChildDataToBuffer(buffer, body.getTail(), context, "tail");
            }

            // 收集直接子节点数据
            for (Node node : body.getNodes()) {
                totalBits += appendChildDataToBuffer(buffer, node, context, "直接子节点[" + node.getName() + "]");
            }
        } else {
            // 对于其他结构体类型（ProtocolHeader、Check等），使用原有逻辑
            List<Node> children = structureNode.getChildren();
            log.debug("- 普通结构体节点，使用getChildren()获取子节点");

            if (children != null && !children.isEmpty()) {
                log.debug("- 开始收集子节点编码数据，子节点数量: {}", children.size());

                for (Node child : children) {
                    totalBits += appendChildDataToBuffer(buffer, child, context, "子节点[" + child.getName() + "]");
                }
            } else {
                log.debug("- 结构体节点没有子节点");
            }
        }

        // 确保字节对齐
        buffer.alignToByte();
        byte[] result = buffer.toByteArray();
        String resultHex = ByteUtil.bytesToHexString(result);
        log.debug("- 结构体编码完成，总位数: {} 位，总字节数: {} 字节", totalBits, result.length);
        log.debug("- 结构体【{}】编码结果: {}", structureNode.getName(), resultHex);

        // 将结构体的编码结果添加到上下文
        context.put(structureNode.getName(), resultHex);

        // 如果结构体有ID，也用ID作为键添加到上下文中
        String structureId = dependencyGraph.getNodeIdByPath(structureNode.getPath());
        if (structureId != null) {
            context.put(structureId, result);
            context.put(structureId + "_length", result.length);
            context.put(structureId + "_node", structureNode);
            log.debug("- 添加结构体ID映射到上下文: #{}", structureId);
        }

        return result;
    }

    /**
     * 将子节点数据按位追加到缓冲区
     */
    private int appendChildDataToBuffer(BitBuffer buffer, INode childNode, Map<String, Object> context, String nodeType) throws CodecException {
        // 构建子节点的完整路径
        String currentPath = (String) context.get("currentPath");
        String childPath = currentPath + "." + childNode.getName();
        String childId = childNode.getId();

        log.debug("  - 查找{}: {}, 路径: {}, ID: {}", nodeType, childNode.getName(), childPath, childId);

        // 从缓存中获取子节点的编码数据
        byte[] childData;
        if (childId != null && encodedNodesCache.containsKey(childId)) {
            childData = encodedNodesCache.get(childId);
        } else {
            // 尝试通过节点名称查找
            String childNodeId = dependencyGraph.getNodeIdByPath(childNode.getName());
            if (childNodeId != null && encodedNodesCache.containsKey(childNodeId)) {
                childData = encodedNodesCache.get(childNodeId);
                log.debug("  - 通过节点名称从缓存获取{}数据: {}", nodeType, ByteUtil.bytesToHexString(childData));
            } else {
                log.debug("  - [警告] 未找到{}的编码数据", nodeType);
                log.debug("    尝试的路径: {}", childPath);
                log.debug("    尝试的ID: {}", childId);
                log.debug("    尝试的节点名: {}", childNode.getName());
                log.debug("    缓存中的键: {}", encodedNodesCache.keySet());
                return 0; // 没有数据，返回0位
            }
        }

        if (childData != null && childData.length > 0) {
            // 获取子节点的实际位长度
            int childBitLength = childNode.getLength();
            if (childBitLength <= 0) {
                // 如果没有定义位长度，使用字节长度 * 8
                childBitLength = childData.length * 8;
            }

            log.debug("  - {}位长度: {} 位，字节长度: {} 字节", nodeType, childBitLength, childData.length);

            // 按位写入数据，确保只写入指定的位数
            if (childBitLength <= 32) {
                // 对于32位以内的数据，使用BitBuffer的writeBits方法
                long value = ByteUtil.bytesToUnsignedInt(childData, true);

                // 清除超出位长度的高位
                long mask = (1L << childBitLength) - 1;
                value &= mask;

                buffer.writeBits((int) value, childBitLength);
                log.debug("  - 使用writeBits写入{}位数据: {}", childBitLength, value);
            } else {
                // 对于超过32位的数据，按位写入
                int bitsToWrite = Math.min(childBitLength, childData.length * 8);
                for (int i = 0; i < bitsToWrite; i++) {
                    int byteIndex = i / 8;
                    int bitIndex = 7 - (i % 8); // 大端序位索引
                    boolean bit = ((childData[byteIndex] >> bitIndex) & 1) == 1;
                    buffer.writeBit(bit);
                }
                log.debug("  - 使用逐位写入{}位数据", bitsToWrite);
            }

            log.debug("  - 成功写入{}数据: {} 位", nodeType, childBitLength);
            return childBitLength;
        }

        return 0;
    }

    /**
     * 按协议结构顺序写入数据到buffer
     */
    private void writeProtocolDataInOrder(Protocol protocol, BitBuffer buffer, Map<String, Object> context) throws CodecException {
        log.debug("- 按协议结构顺序写入数据");

        // 1. 写入协议头数据
        if (protocol.getHeader() != null) {
            writeStructureDataInOrder(protocol.getHeader(), buffer, "协议头");
        }

        // 2. 写入协议体数据
        if (protocol.getBody() != null) {
            writeBodyDataInOrder(protocol.getBody(), buffer, context, "协议体");
        }

        // 3. 写入协议校验数据
        if (protocol.getTail() != null) {
            writeStructureDataInOrder(protocol.getTail(), buffer, "协议校验");
        }

        if (CollectionUtil.isNotEmpty(protocol.getNodes())) {
            List<Node> nodes = protocol.getNodes();
            if (nodes != null && !nodes.isEmpty()) {
                // 按order属性排序
                List<Node> sortedNodes = new ArrayList<>(nodes);
                sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

                for (Node node : sortedNodes) {
                    writeNodeDataToBuffer(node, getNodeData(node), buffer);
                }
            }
        }


        log.debug("- 协议结构数据写入完成");
    }

    /**
     * 按Body结构顺序写入数据（Body可能包含嵌套的header、body、check）
     */
    private void writeBodyDataInOrder(Body body, BitBuffer buffer, Map<String, Object> context, String structureName) throws CodecException {
        log.debug("- 写入{}", structureName);

        // 1. 写入Body的header（如果存在）
        if (body.getHeader() != null) {
            writeStructureDataInOrder(body.getHeader(), buffer, structureName + ".header");
        }

        // 2. 写入Body的直接子节点（按order排序）
        List<Node> nodes = body.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            // 按order属性排序
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                writeNodeDataToBuffer(node, getNodeData(node), buffer);
            }
        }

        // 3. 写入Body的嵌套body（如果存在）
        if (body.getBody() != null) {
            writeBodyDataInOrder(body.getBody(), buffer, context, structureName + ".body");
        }

        // 4. 写入Body的check（如果存在）
        if (body.getTail() != null) {
            writeStructureDataInOrder(body.getTail(), buffer, structureName + ".check");
        }
    }

    /**
     * 按结构体顺序写入数据（ProtocolHeader、Check等）
     */
    private void writeStructureDataInOrder(INode structure, BitBuffer buffer, String structureName) throws CodecException {
        log.debug("- 写入{}", structureName);

        List<Node> nodes = structure.getChildren();
        if (nodes != null && !nodes.isEmpty()) {
            // 按order属性排序
            List<Node> sortedNodes = new ArrayList<>(nodes);
            sortedNodes.sort(Comparator.comparingDouble(Node::getOrder));

            for (Node node : sortedNodes) {
                writeNodeDataToBuffer(node, getNodeData(node), buffer);
            }
        }
    }

    public byte[] getNodeData(Node node) throws CodecException {

        // 从缓存中获取节点的编码数据
        String nodePath = node.getPath();
        String nodeId = node.getId();
        byte[] nodeData;
        if (nodeId != null && encodedNodesCache.containsKey(nodeId)) {
            nodeData = encodedNodesCache.get(nodeId);
        } else {
            // 尝试通过节点名称查找
            String nodeNameId = dependencyGraph.getNodeIdByPath(node.getName());
            if (nodeNameId != null && encodedNodesCache.containsKey(nodeNameId)) {
                nodeData = encodedNodesCache.get(nodeNameId);
            } else {
                throw new CodecException(String.format("未找到节点[%s]的编码数据，路径: %s, ID: %s", node.getName(), nodePath, nodeId));
            }
        }
        return nodeData;
    }

    /**
     * 将单个节点数据写入buffer
     */
    public void writeNodeDataToBuffer(Node node, byte[] nodeData, BitBuffer buffer) throws CodecException {
        // 检查节点是否启用
        if (!node.isEnabled()) {
            log.debug("- 跳过禁用节点: {} (原因: {})", node.getName(), node.getEnabledReason());
            return;
        }

        if (nodeData != null && nodeData.length > 0) {
            // log.debug(" - 写入节点[{}]数据: {} (长度: {}位)", node.getName(), ByteUtil.bytesToHexString(nodeData), node.getLength());

            // 统一的位写入逻辑：所有类型的节点都按位写入
            // 原理：协议中的数据应该连续排列，不应该因为节点类型不同而有不同的处理方式
            int bitsLen = node.getLength();

            if (bitsLen <= 32) {
                // 对于32位以内的数据，使用BitBuffer的writeBits方法
                // 这种方式效率更高，适用于大多数节点
                long value = ByteUtil.bytesToUnsignedInt(nodeData, true);

                // 清除超出位长度的高位，确保只写入指定的位数
                long mask = (1L << bitsLen) - 1;
                value &= mask;

                buffer.writeBits((int) value, bitsLen);
                // log.debug(" - 使用writeBits写入{}位数据: {} (节点类型: {})", bitsLen, value, node.getValueType());
            } else {
                // 对于超过32位的数据，按位逐个写入
                // 这种方式虽然慢一些，但能处理任意长度的数据
                int bitsToWrite = Math.min(bitsLen, nodeData.length * 8);
                for (int i = 0; i < bitsToWrite; i++) {
                    int byteIndex = i / 8;
                    int bitIndex = 7 - (i % 8); // 大端序位索引
                    boolean bit = ((nodeData[byteIndex] >> bitIndex) & 1) == 1;
                    buffer.writeBit(bit);
                }
                // log.debug(" - 使用逐位写入{}位数据 (节点类型: {})", bitsToWrite, node.getValueType());
            }
        }
    }


    /**
     * 获取分阶段拓扑排序结果
     *
     * <p>分阶段处理原理：</p>
     * <ul>
     *   <li>第一阶段：处理非填充节点，使用基础依赖图进行拓扑排序</li>
     *   <li>第二阶段：处理填充节点，根据前序关系插入到合适位置</li>
     *   <li>避免循环依赖：填充节点不参与基础拓扑排序，而是通过位置插入</li>
     * </ul>
     */
    private List<String> getStagedTopologicalOrder(List<INode> allLeafNodes) throws CodecException {
        log.debug("[分阶段排序] 开始分阶段拓扑排序");

        // 第一阶段：获取非填充节点的拓扑排序
        List<String> baseOrder = dependencyGraph.getTopologicalOrder();
        log.debug("[分阶段排序] 基础拓扑排序完成，共 {} 个节点", baseOrder.size());

        // 收集所有填充节点
        List<INode> paddingNodes = new ArrayList<>();
        for (INode node : allLeafNodes) {
            if (node.isPaddingNode()) {
                paddingNodes.add(node);
            }
        }

        if (paddingNodes.isEmpty()) {
            log.debug("[分阶段排序] 没有填充节点，直接返回基础排序");
            return baseOrder;
        }

        log.debug("[分阶段排序] 发现 {} 个填充节点，开始插入处理", paddingNodes.size());

        // 第二阶段：将填充节点插入到合适的位置
        List<String> finalOrder = new ArrayList<>(baseOrder);

        for (INode paddingNode : paddingNodes) {
            String paddingPath = paddingNode.getPath();

            // 找到填充节点应该插入的位置
            int insertPosition = findPaddingInsertPosition(paddingNode, finalOrder, allLeafNodes);

            // 插入填充节点
            if (insertPosition >= 0 && insertPosition <= finalOrder.size()) {
                finalOrder.add(insertPosition, paddingPath);
                log.debug("[分阶段排序] 填充节点 '{}' 插入到位置 {}", paddingNode.getName(), insertPosition);
            } else {
                // 如果找不到合适位置，添加到末尾
                finalOrder.add(paddingPath);
                log.debug("[分阶段排序] 填充节点 '{}' 添加到末尾", paddingNode.getName());
            }
        }

        log.debug("[分阶段排序] 分阶段拓扑排序完成，最终 {} 个节点", finalOrder.size());
        return finalOrder;
    }

    /**
     * 找到填充节点应该插入的位置
     */
    private int findPaddingInsertPosition(INode paddingNode, List<String> currentOrder, List<INode> allNodes) {
        // 获取填充节点的容器
        INode container = findParentContainer(paddingNode, allNodes);
        if (container == null) {
            return currentOrder.size(); // 添加到末尾
        }

        // 找到容器在当前排序中的位置
        String containerPath = container.getPath();
        int containerIndex = currentOrder.indexOf(containerPath);

        if (containerIndex == -1) {
            return currentOrder.size(); // 容器不在列表中，添加到末尾
        }

        // 对于对齐填充，需要在所有前序节点之后
        if (paddingNode.getPaddingConfig() != null &&
                paddingNode.getPaddingConfig().getPaddingType() == PaddingConfig.PaddingType.ALIGNMENT) {

            // 找到同一容器中最后一个非填充节点的位置
            int lastDataNodeIndex = containerIndex;
            for (int i = containerIndex + 1; i < currentOrder.size(); i++) {
                String nodePath = currentOrder.get(i);
                INode node = findNodeByPath(nodePath, allNodes);
                if (node != null && !node.isPaddingNode() &&
                        isSameContainer(node, container, allNodes)) {
                    lastDataNodeIndex = i;
                }
            }
            return lastDataNodeIndex + 1;
        }

        // 其他类型的填充节点，插入到容器之后
        return containerIndex + 1;
    }

    /**
     * 检查两个节点是否在同一个容器中
     */
    private boolean isSameContainer(INode node1, INode container, List<INode> allNodes) {
        INode parent1 = findParentContainer(node1, allNodes);
        return parent1 != null && parent1.getPath().equals(container.getPath());
    }

    /**
     * 根据路径查找节点
     */
    private INode findNodeByPath(String nodePath, List<INode> allNodes) {
        for (INode node : allNodes) {
            if (nodePath.equals(node.getPath())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 字符串填充或截断到指定长度 - 用于格式化日志输出
     * <p>
     * <p>
     * /**
     * 分析填充节点的依赖关系并添加到依赖图中
     *
     * <p>核心设计原理：</p>
     * <ul>
     *   <li><b>填充依赖识别</b>：识别填充节点对容器或其他节点的依赖关系</li>
     *   <li><b>容器依赖</b>：FILL_CONTAINER类型的填充节点依赖其容器的长度</li>
     *   <li><b>对齐依赖</b>：ALIGNMENT类型的填充节点依赖前序节点的累计长度</li>
     *   <li><b>动态依赖</b>：DYNAMIC类型的填充节点依赖表达式中引用的节点</li>
     *   <li><b>拓扑保证</b>：确保填充节点在其依赖节点之后处理</li>
     * </ul>
     *
     * <p>依赖类型分析：</p>
     * <ol>
     *   <li><b>容器填充依赖</b>：填充节点 → 容器节点 → 容器的所有子节点</li>
     *   <li><b>对齐填充依赖</b>：填充节点 → 所有前序节点（用于计算累计长度）</li>
     *   <li><b>动态填充依赖</b>：填充节点 → 表达式中引用的节点</li>
     *   <li><b>固定长度填充</b>：无额外依赖（仅依赖自身配置）</li>
     * </ol>
     *
     * @param protocol 协议对象
     * @throws CodecException 分析过程中发生的异常
     */
    private void analyzePaddingDependencies(Protocol protocol) throws CodecException {
        log.debug("[填充依赖] 开始分析填充节点依赖关系");

        // 第一步：收集所有节点
        List<INode> allNodes = collectAllLeafNodes(protocol);
        log.debug("[填充依赖] 共收集到 {} 个节点", allNodes.size());

        // 第二步：识别填充节点
        List<INode> paddingNodes = new ArrayList<>();
        for (INode node : allNodes) {
            if (node.isPaddingNode()) {
                paddingNodes.add(node);
                log.debug("[填充依赖] 发现填充节点: {}", node.getName());
            }
        }

        if (paddingNodes.isEmpty()) {
            log.debug("[填充依赖] 未发现填充节点，跳过依赖分析");
            return;
        }

        log.debug("[填充依赖] 发现 {} 个填充节点，开始分析依赖关系", paddingNodes.size());

        // 第三步：为每个填充节点分析并添加依赖关系
        for (INode paddingNode : paddingNodes) {
            analyzeSinglePaddingNodeDependencies(paddingNode, allNodes);
        }

        log.debug("[填充依赖] 填充节点依赖关系分析完成");
    }

    /**
     * 分析单个填充节点的依赖关系
     *
     * @param paddingNode 填充节点
     * @param allNodes    所有节点列表
     * @throws CodecException 分析异常
     */
    private void analyzeSinglePaddingNodeDependencies(INode paddingNode, List<INode> allNodes) throws CodecException {
        PaddingConfig config = paddingNode.getPaddingConfig();
        if (config == null || !config.isValid()) {
            log.debug("[填充依赖] 节点 '{}' 的填充配置无效，跳过依赖分析", paddingNode.getName());
            return;
        }

        String paddingNodePath = paddingNode.getPath();
        log.debug("[填充依赖] 分析填充节点: {} (类型: {})", paddingNode.getName(), config.getPaddingType());

        switch (config.getPaddingType()) {
            case FILL_CONTAINER:
                analyzeFillContainerDependencies(paddingNode, paddingNodePath, config, allNodes);
                break;

            case ALIGNMENT:
                analyzeAlignmentDependencies(paddingNode, paddingNodePath, allNodes);
                break;

            case DYNAMIC:
                analyzeDynamicDependencies(paddingNode, paddingNodePath, config, allNodes);
                break;

            case FIXED_LENGTH:
                // 固定长度填充无额外依赖
                log.debug("[填充依赖] 固定长度填充节点 '{}' 无额外依赖", paddingNode.getName());
                break;

            default:
                log.debug("[填充依赖] 不支持的填充类型: {}", config.getPaddingType());
                break;
        }
    }

    /**
     * 分析容器填充依赖关系
     *
     * <p>容器填充的依赖逻辑（避免循环依赖）：</p>
     * <ul>
     *   <li>填充节点依赖容器内所有其他子节点（计算已使用长度）</li>
     *   <li>不依赖容器节点本身（避免循环依赖，容器长度从配置获取）</li>
     *   <li>确保填充节点在所有兄弟节点之后处理</li>
     * </ul>
     *
     * <p>循环依赖问题说明：</p>
     * <ul>
     *   <li>容器节点编码时需要组装所有子节点数据（包括填充节点）</li>
     *   <li>如果填充节点依赖容器节点，就会形成：填充节点 → 容器节点 → 填充节点</li>
     *   <li>解决方案：填充节点只依赖兄弟节点，容器长度从配置直接获取</li>
     * </ul>
     */
    private void analyzeFillContainerDependencies(INode paddingNode, String paddingNodePath,
                                                  PaddingConfig config, List<INode> allNodes) throws CodecException {

        // 查找容器节点（仅用于查找兄弟节点，不添加依赖关系）
        INode containerNode = null;

        if (config.isAutoCalculateContainerLength()) {
            // 自动从容器节点获取长度
            String containerNodeRef = config.getContainerNode();
            containerNode = findNodeByReference(containerNodeRef, allNodes);

            if (containerNode == null) {
                // 尝试查找父容器
                containerNode = findParentContainer(paddingNode, allNodes);
            }
        } else {
            // 使用指定的固定长度，仍需要找到父容器来计算已使用长度
            containerNode = findParentContainer(paddingNode, allNodes);
        }

        if (containerNode == null) {
            log.debug("[填充依赖] 无法找到填充节点 '{}' 的容器节点", paddingNode.getName());
            return;
        }

        log.debug("[填充依赖] 容器填充节点 '{}' 位于容器 '{}' 中", paddingNode.getName(), containerNode.getName());

        // 重要：不添加对容器节点的依赖，避免循环依赖
        // 原因：容器节点在编码时需要组装包括填充节点在内的所有子节点数据
        // 如果填充节点依赖容器节点，就会形成循环依赖
        log.debug("[填充依赖] 跳过对容器节点的依赖（避免循环依赖）");

        // 添加对容器内所有其他子节点的依赖
        // 这样确保填充节点在所有兄弟节点编码完成后再处理
        List<INode> siblingNodes = getContainerChildren(containerNode);
        int addedDependencies = 0;

        for (INode sibling : siblingNodes) {
            // 跳过自己和其他填充节点
            if (sibling == paddingNode || sibling.isPaddingNode()) {
                continue;
            }

            // 跳过结构体节点（结构体节点不是叶子节点）
            if (sibling.isStructureNode()) {
                continue;
            }

            dependencyGraph.addDependency(paddingNode.getId(), sibling.getId());
            log.debug("[填充依赖] 添加兄弟节点依赖(ID): {} → {}", paddingNode.getId(), sibling.getId());
            addedDependencies++;
        }

        log.debug("[填充依赖] 容器填充节点 '{}' 共添加了 {} 个兄弟节点依赖", paddingNode.getName(), addedDependencies);
    }

    /**
     * 分析对齐填充依赖关系
     *
     * <p>对齐填充的依赖逻辑：</p>
     * <ul>
     *   <li>填充节点依赖所有前序节点（用于计算累计长度）</li>
     *   <li>前序节点是指在协议结构中位于填充节点之前的所有叶子节点</li>
     *   <li>确保填充节点在所有前序节点之后处理</li>
     * </ul>
     */
    private void analyzeAlignmentDependencies(INode paddingNode, String paddingNodePath, List<INode> allNodes) throws CodecException {
        log.debug("[填充依赖] 分析对齐填充节点 '{}' 的前序依赖", paddingNode.getName());

        // 获取按编码顺序排列的叶子节点列表
        List<INode> orderedLeafNodes = getOrderedLeafNodes(allNodes);

        // 添加对所有前序节点的依赖
        for (INode node : orderedLeafNodes) {
            if (node == paddingNode) {
                // 找到当前节点，停止添加依赖
                break;
            }

            // 跳过其他填充节点和结构节点
            if (node.isPaddingNode() || node.isStructureNode()) {
                continue;
            }

            dependencyGraph.addDependency(paddingNode.getId(), node.getId());
            log.debug("[填充依赖] 添加前序节点依赖(ID): {} → {}", paddingNode.getId(), node.getId());
        }
    }

    /**
     * 分析动态填充依赖关系
     *
     * <p>动态填充的依赖逻辑：</p>
     * <ul>
     *   <li>解析长度表达式中的变量引用</li>
     *   <li>添加对表达式中引用节点的依赖</li>
     *   <li>确保填充节点在所有引用节点之后处理</li>
     * </ul>
     */
    private void analyzeDynamicDependencies(INode paddingNode, String paddingNodePath, PaddingConfig config, List<INode> allLeafNodes) throws CodecException {
        String lengthExpression = config.getLengthExpression();
        if (lengthExpression == null || lengthExpression.trim().isEmpty()) {
            log.debug("[填充依赖] 动态填充节点 '{}' 的长度表达式为空", paddingNode.getName());
            return;
        }

        log.debug("[填充依赖] 分析动态填充节点 '{}' 的表达式依赖: {}", paddingNode.getName(), lengthExpression);

        // 解析表达式中的依赖
        Set<String> dependencies = expressionParser.parseDependencies(lengthExpression, allLeafNodes, currentProtocolId);

        for (String dependency : dependencies) {
            // 通过ID查找目标节点路径
            if (dependencyGraph.hasNodeId(dependency)) {
                dependencyGraph.addDependency(paddingNode.getId(), dependency);
                log.debug("[填充依赖] 添加表达式依赖(ID): {} → {}", paddingNode.getId(), dependency);
            } else {
                log.debug("[填充依赖] 无法找到表达式依赖的节点: {}", dependency);
            }
        }
    }

    /**
     * 从协议中收集所有叶子节点
     */
    private List<INode> collectAllLeafNodes(Protocol protocol) {
        List<INode> allNodes = new ArrayList<>();
        if (protocol.getHeader() != null) {
            collectHeadNodes(protocol.getHeader(), allNodes);
        }
        if (protocol.getBody() != null) {
            collectBodyNodes(protocol.getBody(), allNodes);
        }
        if (protocol.getTail() != null) {
            collectTailNodes(protocol.getTail(), allNodes);
        }
        // 递归收集子节点
        List<Node> children = protocol.getChildren();
        if (children != null) {
            allNodes.addAll(children);
        }
        return allNodes;
    }

    private void collectTailNodes(Tail tail, List<INode> allNodes) {
        // 递归收集子节点
        if (tail.getChildren() != null) {
            allNodes.addAll(tail.getChildren());
        }
    }

    private void collectHeadNodes(Header header, List<INode> allNodes) {
        // 递归收集子节点
        if (CollectionUtil.isNotEmpty(header.getChildren())) {
            allNodes.addAll(header.getChildren());
        }
    }

    /**
     * 递归收集所有节点
     */
    private void collectBodyNodes(Body body, List<INode> collector) {
        Header header = body.getHeader();
        if (header != null) {
            collectHeadNodes(header, collector);
        }
        if (body.getBody() != null) {
            collectBodyNodes(body.getBody(), collector);
        }
        if (body.getTail() != null) {
            collectTailNodes(body.getTail(), collector);
        }
        List<Node> children = body.getChildren();
        if (CollectionUtil.isNotEmpty(children)) {
            collector.addAll(children);
        }
    }

    /**
     * 根据引用查找节点
     */
    private INode findNodeByReference(String nodeRef, List<INode> allNodes) {
        if (nodeRef == null || nodeRef.trim().isEmpty()) {
            return null;
        }

        // 按名称查找
        for (INode node : allNodes) {
            if (nodeRef.equals(node.getName())) {
                return node;
            }
        }

        // 按ID查找（支持#前缀）
        String nodeId = nodeRef.startsWith("#") ? nodeRef.substring(1) : nodeRef;
        for (INode node : allNodes) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
        }

        return null;
    }

    /**
     * 查找节点的父容器
     */
    private INode findParentContainer(INode node, List<INode> allNodes) {
        for (INode candidate : allNodes) {
            if (candidate.getChildren() != null) {
                for (INode child : candidate.getChildren()) {
                    if (child == node || (child.getId() != null && child.getId().equals(node.getId()))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取容器的子节点
     */
    private List<INode> getContainerChildren(INode containerNode) {
        List<INode> children = new ArrayList<>();

        if (containerNode.getChildren() != null) {
            children.addAll(containerNode.getChildren());
        }

        // 处理Body类型的特殊结构
        if (containerNode instanceof Body) {
            Body body = (Body) containerNode;
            if (body.getNodes() != null) {
                children.addAll(body.getNodes());
            }
        }

        return children;
    }

    /**
     * 获取按编码顺序排列的叶子节点列表
     */
    private List<INode> getOrderedLeafNodes(List<INode> allNodes) {
        return allNodes.stream()
                .filter(node -> !node.isStructureNode())  // 过滤掉结构节点
                .sorted((n1, n2) -> {
                    // 按order属性排序
                    float order1 = (n1 instanceof Node) ? ((Node) n1).getOrder() : 0;
                    float order2 = (n2 instanceof Node) ? ((Node) n2).getOrder() : 0;
                    return Double.compare(order1, order2);
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 动态处理填充节点
     *
     * <p>核心设计原理：</p>
     * <ul>
     *   <li><b>延迟计算</b>：在节点编码过程中动态计算填充长度，确保依赖数据可用</li>
     *   <li><b>上下文依赖</b>：利用已编码节点的值和长度信息进行填充计算</li>
     *   <li><b>实时更新</b>：根据计算结果实时更新节点的长度和值</li>
     *   <li><b>类型适配</b>：支持不同类型的填充策略动态计算</li>
     * </ul>
     *
     * <p>与预处理方式的区别：</p>
     * <ul>
     *   <li>预处理：在所有节点编码前统一处理，但此时context中缺少依赖数据</li>
     *   <li>动态处理：在拓扑排序的编码过程中处理，确保依赖数据已在context中</li>
     * </ul>
     *
     * @param paddingNode 要处理的填充节点
     * @param context     包含已编码节点信息的上下文
     * @throws CodecException 处理过程中发生的异常
     */
    private void processPaddingNodeDynamically(INode paddingNode, Map<String, Object> context) throws CodecException {
        PaddingConfig config = paddingNode.getPaddingConfig();
        if (config == null || !config.isValid()) {
            log.debug("[动态填充] 节点 '{}' 的填充配置无效，跳过处理", paddingNode.getName());
            return;
        }

        log.debug("[动态填充] 处理填充节点: {} (类型: {})", paddingNode.getName(), config.getPaddingType());

        // 检查填充启用条件
        if (!isPaddingEnabledInContext(config, context)) {
            log.debug("[动态填充] 节点 '{}' 的填充被禁用", paddingNode.getName());
            return;
        }

        // 根据填充类型计算填充长度
        int paddingLength = calculatePaddingLengthDynamically(config, paddingNode, context);
        log.debug("[动态填充] 节点 '{}' 计算出的填充长度: {} 位", paddingNode.getName(), paddingLength);

        // 应用长度限制
        paddingLength = Math.max(paddingLength, config.getMinPaddingLength());
        paddingLength = Math.min(paddingLength, config.getMaxPaddingLength());

        if (paddingLength <= 0) {
            log.debug("[动态填充] 节点 '{}' 不需要填充", paddingNode.getName());
            return;
        }

        // 生成填充数据
        byte[] paddingData = generatePaddingDataDynamically(config, paddingLength);

        // 更新节点信息
        updatePaddingNodeDynamically(paddingNode, paddingLength, paddingData);

        log.debug("[动态填充] 节点 '{}' 填充完成，填充长度: {} 位", paddingNode.getName(), paddingLength);
    }

    /**
     * 检查填充是否在当前上下文中启用
     */
    private boolean isPaddingEnabledInContext(PaddingConfig config, Map<String, Object> context) throws CodecException {
        if (!config.isEnabled()) {
            return false;
        }

        String enableCondition = config.getEnableCondition();
        if (enableCondition == null || enableCondition.trim().isEmpty()) {
            return true;
        }

        try {
            Object result = expressionEngine.evaluate(enableCondition, context);
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0.0;
            } else {
                return result != null;
            }
        } catch (Exception e) {
            log.error("[动态填充] 评估填充启用条件失败: {}", enableCondition);
            throw new CodecException("评估填充启用条件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 动态计算填充长度
     *
     * <p>关键优势：此时context中已包含所有依赖节点的值和编码数据</p>
     */
    private int calculatePaddingLengthDynamically(PaddingConfig config, INode paddingNode, Map<String, Object> context) throws CodecException {
        switch (config.getPaddingType()) {
            case FIXED_LENGTH:
                return calculateFixedLengthPaddingDynamic(config, paddingNode);
            case ALIGNMENT:
                return calculateAlignmentPaddingDynamic(config, paddingNode, context);
            case DYNAMIC:
                return calculateDynamicPaddingDynamic(config, paddingNode, context);
            case FILL_CONTAINER:
                return calculateFillContainerPaddingDynamic(config, paddingNode, context);
            default:
                throw new CodecException("不支持的填充类型: " + config.getPaddingType());
        }
    }

    /**
     * 动态计算固定长度填充
     */
    private int calculateFixedLengthPaddingDynamic(PaddingConfig config, INode paddingNode) {
        int currentLength = paddingNode.getActualDataLength();
        if (currentLength <= 0) {
            currentLength = paddingNode.getLength();
        }
        int targetLength = config.getTargetLength();
        return Math.max(0, targetLength - currentLength);
    }

    /**
     * 动态计算对齐填充
     *
     * <p>优势：可以从context中获取前序节点的实际编码长度</p>
     */
    private int calculateAlignmentPaddingDynamic(PaddingConfig config, INode paddingNode, Map<String, Object> context) {
        int alignmentBits = config.getTargetLength();
        if (alignmentBits <= 0) {
            return 0;
        }

        // 从context中计算累计长度
        int cumulativeLength = calculateCumulativeLengthFromContext(paddingNode, context);

        log.debug("[动态对齐填充] 节点 '{}' 之前的累计长度: {} 位", paddingNode.getName(), cumulativeLength);
        log.debug("[动态对齐填充] 对齐长度: {} 位", alignmentBits);

        int remainder = cumulativeLength % alignmentBits;
        int paddingLength = remainder == 0 ? 0 : alignmentBits - remainder;

        log.debug("[动态对齐填充] 余数: {} 位，需要填充: {} 位", remainder, paddingLength);
        return paddingLength;
    }

    /**
     * 动态计算动态填充
     *
     * <p>优势：表达式可以引用context中已编码节点的值</p>
     */
    private int calculateDynamicPaddingDynamic(PaddingConfig config, INode paddingNode, Map<String, Object> context) throws CodecException {
        String expression = config.getLengthExpression();

        // 准备表达式上下文
        Map<String, Object> expressionContext = new HashMap<>(context);
        expressionContext.put("currentLength", paddingNode.getActualDataLength());
        expressionContext.put("targetLength", config.getTargetLength());
        expressionContext.put("node", paddingNode);

        try {
            Object result = expressionEngine.evaluate(expression, expressionContext);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            } else if (result instanceof String) {
                // 如果结果是字符串，尝试转换为数字
                try {
                    return Integer.parseInt((String) result);
                } catch (NumberFormatException e) {
                    throw new CodecException("填充长度表达式结果无法转换为数字: " + result, e);
                }
            } else {
                throw new CodecException("填充长度表达式结果不是数字: " + result);
            }
        } catch (Exception e) {
            log.error("[动态填充] 评估填充长度表达式失败: {}", expression);
            throw new CodecException("评估填充长度表达式失败: " + e.getMessage(), e);
        }
    }

    /**
     * 动态计算容器填充
     *
     * <p>优势：可以从context中获取兄弟节点的实际编码长度</p>
     * <p>注意：容器长度直接从配置获取，不依赖容器节点的编码结果（避免循环依赖）</p>
     */
    private int calculateFillContainerPaddingDynamic(PaddingConfig config, INode paddingNode, Map<String, Object> context) throws CodecException {
        // 获取容器固定长度（直接从配置获取，避免循环依赖）
        int containerFixedLength;

        if (config.isAutoCalculateContainerLength()) {
            // 从容器节点配置获取长度（不依赖容器的编码结果）
            String containerNodeRef = config.getContainerNode();
            INode containerNode = findContainerNodeFromContext(containerNodeRef, context);
            if (containerNode == null) {
                throw new CodecException("未找到容器节点: " + containerNodeRef);
            }

            // 重要：直接使用容器节点配置的长度，不使用其编码结果
            // 这样避免了对容器编码结果的依赖，从而避免循环依赖
            containerFixedLength = containerNode.getLength();
            log.debug("[动态容器填充] 从容器节点配置获取长度: {} 位", containerFixedLength);
        } else {
            // 使用配置中指定的固定长度
            containerFixedLength = config.getContainerFixedLength();
            log.debug("[动态容器填充] 使用配置指定的容器长度: {} 位", containerFixedLength);
        }

        // 从context中计算已使用长度（只计算兄弟节点，不包括容器本身）
        int usedLength = calculateUsedLengthFromContext(paddingNode, context);

        log.debug("[动态容器填充] 容器配置长度: {} 位, 已使用长度: {} 位, 需填充: {} 位",
                containerFixedLength, usedLength, Math.max(0, containerFixedLength - usedLength));

        return Math.max(0, containerFixedLength - usedLength);
    }

    /**
     * 从context中计算累计长度
     *
     * <p>优势：使用已编码节点的实际长度，而不是配置的长度</p>
     */
    private int calculateCumulativeLengthFromContext(INode currentNode, Map<String, Object> context) {
        int cumulativeLength = 0;

        // 获取拓扑排序的顺序（ID）
        List<String> order = dependencyGraph.getTopologicalOrder();

        for (String nodeId : order) {
            INode node = dependencyGraph.getNodeById(nodeId);
            if (node == currentNode) {
                // 找到当前节点，停止累计
                break;
            }

            if (node == null || node.isPaddingNode() || node.isStructureNode()) {
                continue;
            }

            // 从context中获取节点的实际编码长度
            if (nodeId != null && encodedNodesCache.containsKey(nodeId)) {
                byte[] encodedData = encodedNodesCache.get(nodeId);
                int nodeLength = node.getLength(); // 使用节点定义的位长度
                cumulativeLength += nodeLength;
                log.debug("[累计长度] 节点 '{}': {} 位，累计: {} 位", node.getName(), nodeLength, cumulativeLength);
            }
        }

        return cumulativeLength;
    }

    /**
     * 从context中计算已使用长度
     *
     * <p>计算逻辑：</p>
     * <ul>
     *   <li>只计算已编码的兄弟节点长度</li>
     *   <li>跳过填充节点和结构体节点</li>
     *   <li>不包括容器节点本身（避免循环依赖）</li>
     * </ul>
     */
    private int calculateUsedLengthFromContext(INode paddingNode, Map<String, Object> context) {
        int usedLength = 0;

        // 查找父容器
        INode parentContainer = findParentContainerFromContext(paddingNode, context);
        if (parentContainer == null) {
            log.debug("[动态容器填充] 无法找到填充节点 '{}' 的父容器", paddingNode.getName());
            return 0;
        }

        log.debug("[动态容器填充] 计算容器 '{}' 的已使用长度", parentContainer.getName());

        // 计算容器中所有已编码子节点的长度
        List<INode> children = getContainerChildren(parentContainer);
        int countedNodes = 0;

        for (INode child : children) {
            // 跳过当前填充节点和其他填充节点
            if (child == paddingNode || child.isPaddingNode()) {
                log.debug("[已使用长度] 跳过填充节点: {}", child.getName());
                continue;
            }

            // 跳过结构体节点
            if (child.isStructureNode()) {
                log.debug("[已使用长度] 跳过结构体节点: {}", child.getName());
                continue;
            }

            String childPath = child.getPath();
            String childId = dependencyGraph.getNodeIdByPath(childPath);
            if (childId != null && encodedNodesCache.containsKey(childId)) {
                int childLength = child.getLength();
                usedLength += childLength;
                countedNodes++;
                log.debug("[已使用长度] 子节点 '{}': {} 位 (累计: {} 位)", child.getName(), childLength, usedLength);
            } else {
                log.debug("[已使用长度] 子节点 '{}' 未编码或未缓存", child.getName());
            }
        }

        log.debug("[动态容器填充] 容器 '{}' 已使用长度: {} 位 (计算了 {} 个节点)",
                parentContainer.getName(), usedLength, countedNodes);
        return usedLength;
    }

    /**
     * 从context中查找容器节点
     */
    private INode findContainerNodeFromContext(String containerNodeRef, Map<String, Object> context) {
        if (containerNodeRef == null || containerNodeRef.trim().isEmpty()) {
            return null;
        }

        // 尝试通过ID查找
        String nodeId = containerNodeRef.startsWith("#") ? containerNodeRef.substring(1) : containerNodeRef;
        return dependencyGraph.getNodeById(nodeId);
    }

    /**
     * 从context中查找父容器
     */
    private INode findParentContainerFromContext(INode node, Map<String, Object> context) {
        // 通过依赖图查找父容器
        Map<String, INode> nodeMap = dependencyGraph.getNodeMap();
        for (INode candidate : nodeMap.values()) {
            if (candidate.getChildren() != null) {
                for (INode child : candidate.getChildren()) {
                    if (child == node || (child.getId() != null && child.getId().equals(node.getId()))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 动态生成填充数据
     */
    private byte[] generatePaddingDataDynamically(PaddingConfig config, int paddingLengthBits) {
        if (paddingLengthBits <= 0) {
            return new byte[0];
        }

        byte[] paddingPattern = config.getPaddingBytes();
        int paddingLengthBytes = (paddingLengthBits + 7) / 8; // 向上取整到字节

        if (!config.isRepeatPattern()) {
            // 不重复模式，只填充一次模式
            int copyLength = Math.min(paddingPattern.length, paddingLengthBytes);
            byte[] result = new byte[paddingLengthBytes];
            System.arraycopy(paddingPattern, 0, result, 0, copyLength);
            return result;
        } else {
            // 重复模式，重复填充模式直到达到目标长度
            byte[] result = new byte[paddingLengthBytes];
            for (int i = 0; i < paddingLengthBytes; i++) {
                result[i] = paddingPattern[i % paddingPattern.length];
            }
            return result;
        }
    }

    /**
     * 动态更新填充节点信息
     */
    private void updatePaddingNodeDynamically(INode paddingNode, int paddingLength, byte[] paddingData) {
        // 保存原始数据长度（只在第一次处理时保存）
        if (paddingNode.getActualDataLength() == 0 || paddingNode.getActualDataLength() == paddingNode.getLength()) {
            paddingNode.setActualDataLength(paddingNode.getLength());
        }

        // 更新节点长度为填充后的实际长度
        if (paddingNode instanceof Node) {
            ((Node) paddingNode).setLength(paddingLength);
        }

        // 设置填充数据作为节点值（如果需要）
        if (paddingData.length > 0) {
            StringBuilder hexValue = new StringBuilder("0x");
            for (byte b : paddingData) {
                hexValue.append(String.format("%02X", b & 0xFF));
            }
            paddingNode.setValue(hexValue.toString());
        }

        log.debug("[动态填充] 节点 '{}' 更新完成: 原长度={}, 填充长度={}, 新长度={}",
                paddingNode.getName(), paddingNode.getActualDataLength(), paddingLength, paddingLength);
    }


    /**
     * 获取类型的默认值
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == char.class || type == Character.class) return '\0';
        if (type == byte.class || type == Byte.class) return (byte) 0;
        if (type == short.class || type == Short.class) return (short) 0;
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == float.class || type == Float.class) return 0.0f;
        if (type == double.class || type == Double.class) return 0.0;
        return null;
    }


    private Float convertToFloat(Object value) {
        if (value instanceof Number) return ((Number) value).floatValue();
        if (value instanceof String) return Float.parseFloat((String) value);
        return 0.0f;
    }

    private Double convertToDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0.0;
    }


    /**
     * 计算字符串的显示宽度（中文字符计为2个宽度单位）
     *
     * @param str 要计算的字符串
     * @return 显示宽度
     */
    private int getDisplayWidth(String str) {
        if (str == null) return 0;
        int width = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 中文字符范围：[\u4e00-\u9fa5]，计为2个宽度单位
            if (c >= '\u4e00' && c <= '\u9fa5') {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }

    /**
     * 计算最优列宽
     *
     * @param decodedNodes 解码后的节点列表
     * @return 每列的最优宽度数组
     */
    private int[] calculateOptimalColumnWidths(List<Node> decodedNodes) {
        // 列名
        String[] columnNames = {"节点名称", "ID", "类型", "长度", "值", "反编值", "验证结果", "位置范围"};

        // 初始化列宽为列名长度
        int[] columnWidths = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columnWidths[i] = getDisplayWidth(columnNames[i]);
        }

        // 遍历所有节点，找到每列的最大内容长度
        for (Node node : decodedNodes) {
            String[] values = {
                    node.getName(),
                    node.getId(),
                    node.getValueType().getDesc(),
                    node.getLength() + "字节",
                    String.valueOf(node.getTransformedValue()),
                    String.valueOf(node.getDecodedValue()),
                    Boolean.TRUE.equals(node.getValidationResult()) ? "通过" : "失败",
                    "[" + node.getStartBitPosition() + ":" + node.getEndBitPosition() + "]"
            };

            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    int displayWidth = getDisplayWidth(values[i]);
                    if (displayWidth > columnWidths[i]) {
                        columnWidths[i] = displayWidth;
                    }
                }
            }
        }

        // 为每列添加固定边距
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2;
        }

        return columnWidths;
    }

    /**
     * 打印表头
     *
     * @param columnWidths 列宽数组
     */
    private void printTableHeader(int[] columnWidths) {
        // 构建上边框
        StringBuilder topBorder = new StringBuilder("┌");
        for (int i = 0; i < columnWidths.length; i++) {
            topBorder.append(repeat("─", columnWidths[i]));
            if (i < columnWidths.length - 1) {
                topBorder.append("┬");
            }
        }
        topBorder.append("┐");
        log.debug(topBorder.toString());

        // 打印列标题
        String[] columnNames = {"节点名称", "ID", "类型", "长度", "值", "反编值", "验证结果", "位置范围"};
        StringBuilder headerRow = new StringBuilder("│");
        for (int i = 0; i < columnNames.length; i++) {
            headerRow.append(padString(columnNames[i], columnWidths[i]));
            if (i < columnNames.length - 1) {
                headerRow.append("│");
            }
        }
        headerRow.append("│");
        log.debug(headerRow.toString());

        // 构建分隔线
        StringBuilder separator = new StringBuilder("├");
        for (int i = 0; i < columnWidths.length; i++) {
            separator.append(repeat("─", columnWidths[i]));
            if (i < columnWidths.length - 1) {
                separator.append("┼");
            }
        }
        separator.append("┤");
        log.debug(separator.toString());
    }


    /**
     * 字符串填充，考虑中文字符的显示宽度
     *
     * @param str         要填充的字符串
     * @param targetWidth 目标宽度
     * @return 填充后的字符串
     */
    private String padString(String str, int targetWidth) {
        if (str == null) str = "";

        int currentWidth = getDisplayWidth(str);
        if (currentWidth >= targetWidth) {
            // 如果当前宽度已经达到或超过目标宽度，需要截断
            StringBuilder result = new StringBuilder();
            int accumulatedWidth = 0;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                int charWidth = (c >= '\u4e00' && c <= '\u9fa5') ? 2 : 1;

                if (accumulatedWidth + charWidth <= targetWidth) {
                    result.append(c);
                    accumulatedWidth += charWidth;
                } else {
                    break;
                }
            }
            return result.toString();
        } else {
            // 需要填充空格
            int spacesNeeded = targetWidth - currentWidth;
            return str + repeat(" ", spacesNeeded);
        }
    }

    /**
     * 重复字符串指定次数（Java 8兼容性）
     *
     * @param str   要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
} 