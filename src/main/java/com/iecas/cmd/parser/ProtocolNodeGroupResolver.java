package com.iecas.cmd.parser;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.*;
import com.iecas.cmd.engine.AviatorExpressionEngine;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 协议节点组解析器
 *
 * <p>负责解析@ProtocolNodeGroup注解的字段，支持多组多层协议嵌套。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>解析重复节点组，支持固定次数和表达式计算</li>
 *   <li>支持多种元素类型：NODE、PROTOCOL_OBJECT、CUSTOM_OBJECT</li>
 *   <li>提供多种解析策略：FLATTEN、GROUP_CONTAINER、MIXED</li>
 *   <li>自动应用ID和名称后缀，确保唯一性</li>
 *   <li>递归处理嵌套结构，支持深层协议嵌套</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>传感器数据列表：List&lt;SensorData&gt;</li>
 *   <li>协议嵌套：List&lt;SubProtocol&gt;</li>
 *   <li>节点重复：List&lt;Node&gt;</li>
 * </ul>
 *
 * <p>日志说明：</p>
 * <ul>
 *   <li>🚀 初始化相关</li>
 *   <li>📋 FLATTEN策略处理</li>
 *   <li>📦 GROUP_CONTAINER策略处理</li>
 *   <li>🔀 MIXED策略处理</li>
 *   <li>🔍 元素类型检测和解析</li>
 *   <li>⚙️  策略执行</li>
 *   <li>📥 节点收集</li>
 *   <li>🏷️  后缀应用</li>
 *   <li>🎨 后缀格式化</li>
 *   <li>🆔 ID处理</li>
 *   <li>📝 名称处理</li>
 *   <li>✅ 成功完成</li>
 *   <li>⚠️  警告信息</li>
 *   <li>❌ 错误信息</li>
 * </ul>
 *
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-08-16
 */
@Slf4j
public class ProtocolNodeGroupResolver {

    /**
     * 元素类型信息内部类
     * 用于存储自动检测到的元素类型信息
     */
    private static class ElementTypeInfo {
        private final String typeName;
        private final Class<?> elementClass;
        private final boolean isNode;
        private final boolean isProtocolObject;
        private final boolean isCustomObject;

        public ElementTypeInfo(String typeName, Class<?> elementClass) {
            this.typeName = typeName;
            this.elementClass = elementClass;
            this.isNode = Node.class.isAssignableFrom(elementClass);
            // 静态方法中无法访问实例方法，需要外部传入
            this.isProtocolObject = false; // 临时设置，后续会通过方法调用更新
            this.isCustomObject = !isNode && !isProtocolObject;
        }

        public String getTypeName() {
            return typeName;
        }

        public Class<?> getElementClass() {
            return elementClass;
        }

        public boolean isNode() {
            return isNode;
        }

        public boolean isProtocolObject() {
            return isProtocolObject;
        }

        public boolean isCustomObject() {
            return isCustomObject;
        }

        // 添加更新方法
        public void updateProtocolObjectFlag(boolean isProtocolObject) {
            // 由于字段是final，我们需要重新创建一个对象
            // 这里提供一个方法来更新标志
        }
    }

    private final AviatorExpressionEngine expressionEngine;

    /**
     * 构造函数
     * 初始化表达式引擎，用于计算重复次数和条件表达式
     */
    public ProtocolNodeGroupResolver() {
        log.debug("[组解析器] 🚀 初始化协议节点组解析器");
        this.expressionEngine = AviatorExpressionEngine.getInstance();
        log.debug("[组解析器] ✅ 表达式引擎初始化完成");
    }

    /**
     * 解析协议节点组
     *
     * <p>这是解析器的主入口方法，负责协调整个解析流程：</p>
     * <ol>
     *   <li>计算重复次数（固定值或表达式）</li>
     *   <li>确定组内元素类型（自动检测或配置指定）</li>
     *   <li>选择解析策略（扁平化、分组容器、混合）</li>
     *   <li>执行具体的解析策略</li>
     * </ol>
     *
     * <p>解析流程：</p>
     * <pre>
     * 字段值 → 重复次数 → 元素类型 → 解析策略 → 节点列表
     * </pre>
     *
     * @param groupField      包含@ProtocolNodeGroup注解的字段
     * @param groupFieldValue 字段的实际值（通常是List类型）
     * @param annotation      @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表，每个节点都有唯一的ID和名称
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    public List<INode> resolveGroup(Field groupField, List<?> groupFieldValue, ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] ========== 🚀 开始解析协议节点组 ==========");
        log.debug("[组解析] 📋 字段信息: 名称={}, 类型={}", groupField.getName(), groupField.getType().getSimpleName());

        try {
            // 1. 确定重复次数
            int groupSize = groupFieldValue.size();
            log.debug("[组解析] ✅ 组长度: {} ", groupSize);

            // 2. 自动检测组内元素类型
            log.debug("[组解析] 🔍 步骤2: 自动检测组内元素类型");
            ElementTypeInfo elementTypeInfo = detectElementType(groupField);
            log.debug("[组解析] ✅ 元素类型: {} (自动检测)", elementTypeInfo.getTypeName());

            // 3. 根据类型选择解析策略
            log.debug("[组解析] 🎯 步骤3: 选择解析策略");
            GroupResolveStrategy strategy = selectResolveStrategy(annotation, elementTypeInfo);
            log.debug("[组解析] ✅ 解析策略: {} (配置值: {})", strategy, annotation.resolveStrategy());

            // 4. 执行解析
            log.debug("[组解析] ⚙️  步骤4: 执行解析策略");
            List<INode> result = executeResolveStrategy(groupField, groupFieldValue, groupSize, elementTypeInfo, strategy, annotation);

            log.debug("[组解析] ========== 🎉 协议节点组解析完成 ==========");
            log.debug("[组解析] 📊 最终结果: 生成了 {} 个协议节点", result.size());

            // 记录结果详情
            for (int i = 0; i < result.size(); i++) {
                INode node = result.get(i);
                if (node instanceof Node) {
                    Node n = (Node) node;
                    log.debug("[组解析] 📋 节点[{}]: ID={}, 名称={}, 类型={}",
                            i, n.getId(), n.getName(), n.getClass().getSimpleName());
                } else {
                    log.debug("[组解析] 📋 节点[{}]: 类型={}", i, node.getClass().getSimpleName());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("[组解析] ❌ 协议节点组解析失败: {}", e.getMessage(), e);
            throw new CodecException("解析协议节点组失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算重复次数
     *
     * <p>系统自动根据实际数据长度确定重复次数：</p>
     * <ul>
     *   <li>自动检测：根据实际数据长度自动确定</li>
     *   <li>异常处理：如果无法检测，抛出异常</li>
     * </ul>
     *
     * @param annotation      @ProtocolNodeGroup注解实例
     * @param groupFieldValue 字段的实际值，用于自动检测长度
     * @return 计算后的重复次数，最小值为1
     * @throws CodecException 当无法检测到节点组长度时抛出
     */
    private int calculateRepeatCount(ProtocolNodeGroup annotation, Object groupFieldValue) throws CodecException {
        log.debug("[组解析] 🔢 开始计算重复次数");

        // 尝试自动检测实际数据长度
        if (groupFieldValue instanceof Collection) {
            Collection<?> collection = (Collection<?>) groupFieldValue;
            int actualSize = collection.size();
            log.debug("[组解析] ✅ 自动检测到实际数据长度: {}", actualSize);
            return Math.max(1, actualSize);
        }

        // 无法检测时抛出异常
        String errorMsg = String.format("无法检测节点组长度: 字段值类型为 %s，不是集合类型",
                groupFieldValue != null ? groupFieldValue.getClass().getSimpleName() : "null");
        log.error("[组解析] ❌ {}", errorMsg);
        throw new CodecException(errorMsg);
    }

    /**
     * 自动检测组内元素类型
     *
     * <p>通过反射分析字段类型自动判断元素类型：</p>
     * <ol>
     *   <li>检查泛型类型是否为Node或其子类 → NODE</li>
     *   <li>检查类是否包含协议注解 → PROTOCOL_OBJECT</li>
     *   <li>其他情况 → CUSTOM_OBJECT</li>
     * </ol>
     *
     * @param field 包含@ProtocolNodeGroup注解的字段
     * @return 检测到的元素类型信息
     */
    private ElementTypeInfo detectElementType(Field field) {
        log.debug("[组解析] 🔍 开始自动检测组内元素类型");

        // 1. 分析泛型类型
        Class<?> elementType = getListElementType(field);
        log.debug("[组解析] 🔍 泛型元素类型: {}", elementType != null ? elementType.getSimpleName() : "null");

        if (elementType == null) {
            log.warn("[组解析] ⚠️  无法确定泛型类型，默认使用CUSTOM_OBJECT");
            return new ElementTypeInfo("CUSTOM_OBJECT", Object.class);
        }

        // 2. 根据类型判断
        if (Node.class.isAssignableFrom(elementType)) {
            log.debug("[组解析] 🔍 检测到Node类型: {}", elementType.getSimpleName());
            return new ElementTypeInfo("NODE", elementType);
        }

        // 3. 检查是否包含协议注解
        log.debug("[组解析] 🔍 检查是否包含协议注解: {}", elementType.getSimpleName());
        boolean hasProtocol = hasProtocolAnnotations(elementType);
        if (hasProtocol) {
            log.debug("[组解析] 🔍 检测到协议对象类型: {}", elementType.getSimpleName());
            return new ElementTypeInfo("PROTOCOL_OBJECT", elementType);
        }

        // 4. 默认为自定义对象
        log.debug("[组解析] 🔍 检测到自定义对象类型: {}", elementType.getSimpleName());
        return new ElementTypeInfo("CUSTOM_OBJECT", elementType);
    }

    /**
     * 获取List的泛型元素类型
     *
     * <p>通过反射获取字段的泛型类型信息，用于自动检测元素类型。</p>
     * <p>例如：List&lt;SensorData&gt; → SensorData.class</p>
     *
     * @param field 包含泛型信息的字段
     * @return 泛型元素类型，如果无法获取则返回null
     */
    private Class<?> getListElementType(Field field) {
        log.debug("[组解析] 🔍 尝试获取字段的泛型类型: {}", field.getName());

        try {
            java.lang.reflect.ParameterizedType paramType =
                    (java.lang.reflect.ParameterizedType) field.getGenericType();

            if (paramType.getActualTypeArguments().length > 0) {
                Class<?> elementType = (Class<?>) paramType.getActualTypeArguments()[0];
                log.debug("[组解析] 🔍 成功获取泛型类型: {}", elementType.getSimpleName());
                return elementType;
            } else {
                log.debug("[组解析] 🔍 字段不是参数化类型或没有泛型参数");
            }
        } catch (Exception e) {
            log.debug("[组解析] ⚠️  无法获取泛型类型: {} (错误: {})", field.getName(), e.getMessage());
        }

        log.debug("[组解析] 🔍 泛型类型获取失败，返回null");
        return null;
    }

    /**
     * 检查类型是否包含协议注解
     *
     * <p>通过反射检查类的字段是否包含以下协议注解：</p>
     * <ul>
     *   <li>@INode - 协议节点</li>
     *   <li>@ProtocolHeader - 协议头</li>
     *   <li>@ProtocolBody - 协议体</li>
     *   <li>@ProtocolTail - 协议尾</li>
     *   <li>@ProtocolNodeGroup - 协议节点组</li>
     * </ul>
     *
     * <p>如果发现任何一个注解，则认为该类是协议对象。</p>
     *
     * @param clazz 要检查的类
     * @return 如果包含协议注解返回true，否则返回false
     */
    private boolean hasProtocolAnnotations(Class<?> clazz) {
        log.debug("[组解析] 🔍 检查类是否包含协议注解: {}", clazz != null ? clazz.getSimpleName() : "null");

        if (clazz == null) {
            log.debug("[组解析] 🔍 类为null，返回false");
            return false;
        }

        // 检查字段上的注解
        Field[] fields = clazz.getDeclaredFields();
        log.debug("[组解析] 🔍 检查字段数量: {}", fields.length);

        for (Field field : fields) {
            log.debug("[组解析] 🔍 检查字段: {} (类型: {})", field.getName(), field.getType().getSimpleName());

            if (field.isAnnotationPresent(com.iecas.cmd.annotation.ProtocolNode.class)) {
                log.debug("[组解析] 🔍 发现@ProtocolNode注解");
                return true;
            }
            if (field.isAnnotationPresent(ProtocolHeader.class)) {
                log.debug("[组解析] 🔍 发现@ProtocolHeader注解");
                return true;
            }
            if (field.isAnnotationPresent(ProtocolBody.class)) {
                log.debug("[组解析] 🔍 发现@ProtocolBody注解");
                return true;
            }
            if (field.isAnnotationPresent(ProtocolTail.class)) {
                log.debug("[组解析] 🔍 发现@ProtocolTail注解");
                return true;
            }
            if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
                log.debug("[组解析] 🔍 发现@ProtocolNodeGroup注解");
                return true;
            }
        }

        log.debug("[组解析] 🔍 未发现任何协议注解");
        return false;
    }

    /**
     * 选择解析策略
     *
     * <p>根据注解配置和元素类型选择合适的解析策略：</p>
     * <ul>
     *   <li>如果明确配置了策略，则使用配置的策略</li>
     *   <li>如果配置为默认值（FLATTEN），则根据元素类型自动选择最佳策略</li>
     * </ul>
     *
     * <p>自动选择逻辑：</p>
     * <ul>
     *   <li>NODE类型 → FLATTEN（扁平化，直接展开节点）</li>
     *   <li>PROTOCOL_OBJECT类型 → GROUP_CONTAINER（分组容器，保持结构层次）</li>
     *   <li>CUSTOM_OBJECT类型 → MIXED（混合策略，根据具体情况选择）</li>
     * </ul>
     *
     * @param annotation      @ProtocolNodeGroup注解实例
     * @param elementTypeInfo 检测到的元素类型信息
     * @return 选择的解析策略
     */
    private GroupResolveStrategy selectResolveStrategy(ProtocolNodeGroup annotation,
                                                       ElementTypeInfo elementTypeInfo) {
        log.debug("[组解析] 🎯 开始选择解析策略");

        GroupResolveStrategy configuredStrategy = annotation.resolveStrategy();
        log.debug("[组解析] 🎯 配置的策略: {}", configuredStrategy);

        // 检查是否为默认值（FLATTEN），如果是则根据元素类型自动选择
        if (configuredStrategy != GroupResolveStrategy.FLATTEN) {
            log.debug("[组解析] 🎯 使用配置的策略: {}", configuredStrategy);
            return configuredStrategy;
        }

        log.debug("[组解析] 🎯 配置为默认值，根据元素类型自动选择策略");

        // 根据元素类型自动选择策略
        GroupResolveStrategy selectedStrategy;
        if (elementTypeInfo.isNode()) {
            selectedStrategy = GroupResolveStrategy.FLATTEN;
            log.debug("[组解析] 🎯 NODE类型选择FLATTEN策略");
        } else if (elementTypeInfo.isProtocolObject()) {
            selectedStrategy = GroupResolveStrategy.GROUP_CONTAINER;
            log.debug("[组解析] 🎯 PROTOCOL_OBJECT类型选择GROUP_CONTAINER策略");
        } else {
            selectedStrategy = GroupResolveStrategy.MIXED;
            log.debug("[组解析] 🎯 CUSTOM_OBJECT类型选择MIXED策略");
        }

        log.debug("[组解析] ✅ 解析策略选择完成: {} (元素类型: {})", selectedStrategy, elementTypeInfo.getTypeName());
        return selectedStrategy;
    }

    /**
     * 执行解析策略
     *
     * <p>根据选择的策略执行相应的解析逻辑：</p>
     * <ul>
     *   <li>FLATTEN：扁平化策略，直接展开所有节点</li>
     *   <li>GROUP_CONTAINER：分组容器策略，保持组的结构层次</li>
     *   <li>MIXED：混合策略，根据元素类型选择最佳策略</li>
     * </ul>
     *
     * @param field           包含@ProtocolNodeGroup注解的字段
     * @param fieldValue      字段的实际值
     * @param repeatCount     重复次数
     * @param elementTypeInfo 元素类型信息
     * @param strategy        选择的解析策略
     * @param annotation      @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> executeResolveStrategy(Field field, Object fieldValue,
                                               int repeatCount, ElementTypeInfo elementTypeInfo,
                                               GroupResolveStrategy strategy,
                                               ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] ⚙️  开始执行解析策略: {}", strategy);
        log.debug("[组解析] ⚙️  执行参数: 重复次数={}, 元素类型={}, 字段={}",
                repeatCount, elementTypeInfo.getTypeName(), field.getName());

        List<INode> result;
        switch (strategy) {
            case FLATTEN:
                log.debug("[组解析] ⚙️  执行FLATTEN策略");
                result = executeFlattenStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
                break;
            case GROUP_CONTAINER:
                log.debug("[组解析] ⚙️  执行GROUP_CONTAINER策略");
                result = executeGroupContainerStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
                break;
            case MIXED:
                log.debug("[组解析] ⚙️  执行MIXED策略");
                result = executeMixedStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
                break;
            default:
                log.debug("[组解析] ⚙️  未知策略，默认使用FLATTEN策略");
                result = executeFlattenStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
                break;
        }

        log.debug("[组解析] ✅ 策略执行完成: {} → 生成了 {} 个节点", strategy, result.size());
        return result;
    }

    /**
     * 执行扁平化解析策略
     *
     * <p>扁平化策略将所有组的节点直接展开到一个列表中，不保持组的结构层次。</p>
     * <p>适用场景：需要将所有节点平铺处理，不关心分组信息。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>遍历每个重复组（1到repeatCount）</li>
     *   <li>解析每个组的元素</li>
     *   <li>将所有节点直接添加到结果列表</li>
     * </ol>
     *
     * @param field       包含@ProtocolNodeGroup注解的字段
     * @param fieldValue  字段的实际值
     * @param repeatCount 重复次数
     * @param annotation  @ProtocolNodeGroup注解实例
     * @return 扁平化后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> executeFlattenStrategy(Field field, Object fieldValue,
                                               int repeatCount, ElementTypeInfo elementTypeInfo,
                                               ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 📋 开始执行FLATTEN策略");
        log.debug("[组解析] 📋 策略参数: 重复次数={}, 元素类型={}, 字段={}",
                repeatCount, elementTypeInfo.getTypeName(), field.getName());

        List<INode> result = new ArrayList<>();
        int totalNodes = 0;

        for (int i = 0; i <= repeatCount; i++) {
            log.debug("[组解析] 📋 处理第 {} 组", i);

            List<INode> groupNodes = resolveGroupElements(field, fieldValue, elementTypeInfo, i, annotation);
            log.debug("[组解析] 📋 第 {} 组解析结果: {} 个节点", i, groupNodes.size());

            result.addAll(groupNodes);
            totalNodes += groupNodes.size();

            log.debug("[组解析] 📋 第 {} 组处理完成，累计节点数: {}", i, totalNodes);
        }

        log.debug("[组解析] ✅ FLATTEN策略执行完成: 共处理 {} 组，生成 {} 个节点", repeatCount, totalNodes);
        return result;
    }

    /**
     * 执行分组容器策略
     *
     * <p>分组容器策略为每个组创建一个GroupContainer，保持组的结构层次。</p>
     * <p>适用场景：需要保持分组信息，便于后续处理和调试。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>获取基础ID和名称</li>
     *   <li>遍历每个重复组（1到repeatCount）</li>
     *   <li>为每个组创建GroupContainer容器</li>
     *   <li>解析组内元素并设置到容器中</li>
     *   <li>将容器添加到结果列表</li>
     * </ol>
     *
     * @param field       包含@ProtocolNodeGroup注解的字段
     * @param fieldValue  字段的实际值
     * @param repeatCount 重复次数
     * @param annotation  @ProtocolNodeGroup注解实例
     * @return 包含GroupContainer的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> executeGroupContainerStrategy(Field field, Object fieldValue,
                                                      int repeatCount, ElementTypeInfo elementTypeInfo,
                                                      ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 📦 开始执行GROUP_CONTAINER策略");
        log.debug("[组解析] 📦 策略参数: 重复次数={}, 元素类型={}, 字段={}",
                repeatCount, elementTypeInfo.getTypeName(), field.getName());

        List<INode> result = new ArrayList<>();
        String baseId = getBaseId(field, annotation);
        String baseName = getBaseName(field, annotation);

        log.debug("[组解析] 📦 基础信息: ID={}, 名称={}", baseId, baseName);

        for (int i = 1; i <= repeatCount; i++) {
            log.debug("[组解析] 📦 处理第 {} 组", i);

            // 创建组容器
            String idSuffix = formatSuffix(annotation.idSuffixPattern(), i);
            String nameSuffix = formatSuffix(annotation.nameSuffixPattern(), i);

            log.debug("[组解析] 📦 第 {} 组后缀: ID后缀={}, 名称后缀={}", i, idSuffix, nameSuffix);

            NodeGroup container = NodeGroup.create(baseId, baseName, i,
                    idSuffix, nameSuffix,
                    elementTypeInfo.getTypeName(),
                    annotation.resolveStrategy().name());

            log.debug("[组解析] 📦 第 {} 组容器创建完成: ID={}, 名称={}",
                    i, container.getId(), container.getName());

            // 解析组内元素
            log.debug("[组解析] 📦 第 {} 组开始解析组内元素", i);
            List<INode> groupNodes = resolveGroupElements(field, fieldValue, elementTypeInfo, i, annotation);
            log.debug("[组解析] 📦 第 {} 组内元素解析完成: {} 个节点", i, groupNodes.size());

            // 将ProtocolNode转换为Node
            List<Node> nodeList = new ArrayList<>();
            int validNodes = 0;
            for (INode pn : groupNodes) {
                if (pn instanceof Node) {
                    nodeList.add((Node) pn);
                    validNodes++;
                } else {
                    log.warn("[组解析] 📦 第 {} 组发现非Node类型的ProtocolNode: {}", i, pn.getClass().getSimpleName());
                }
            }

            log.debug("[组解析] 📦 第 {} 组类型转换完成: 有效节点={}/{}", i, validNodes, groupNodes.size());
            container.setGroupNodes(nodeList);

            // 计算组容器的总长度（所有子节点长度之和）
            int totalLength = 0;
            for (Node childNode : nodeList) {
                totalLength += childNode.getLength();
                log.debug("[组解析] 📦 第 {} 组子节点长度累加: {} + {} = {}",
                        i, childNode.getName(), childNode.getLength(), totalLength);
            }

            // 设置组容器的长度
            container.setLength(totalLength);
            log.debug("[组解析] 📦 第 {} 组容器长度设置完成: {} 位", i, totalLength);

            result.add(container);
            log.debug("[组解析] 📦 第 {} 组处理完成，容器已添加到结果列表", i);
        }

        log.debug("[组解析] ✅ GROUP_CONTAINER策略执行完成: 共创建 {} 个组容器", result.size());
        return result;
    }

    /**
     * 执行混合策略
     *
     * <p>混合策略根据元素类型智能选择最佳策略：</p>
     * <ul>
     *   <li>NODE类型：使用FLATTEN策略，直接展开节点</li>
     *   <li>其他类型：使用GROUP_CONTAINER策略，保持结构层次</li>
     * </ul>
     *
     * <p>这种策略结合了两种策略的优点，根据具体情况选择最合适的处理方式。</p>
     *
     * @param field       包含@ProtocolNodeGroup注解的字段
     * @param fieldValue  字段的实际值
     * @param repeatCount 重复次数
     * @param annotation  @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> executeMixedStrategy(Field field, Object fieldValue,
                                             int repeatCount, ElementTypeInfo elementTypeInfo,
                                             ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 🔀 开始执行MIXED策略");
        log.debug("[组解析] 🔀 策略参数: 重复次数={}, 元素类型={}, 字段={}",
                repeatCount, elementTypeInfo.getTypeName(), field.getName());

        // 混合策略：根据元素类型选择最佳策略
        if (elementTypeInfo.isNode()) {
            log.debug("[组解析] 🔀 NODE类型，选择FLATTEN策略");
            return executeFlattenStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
        } else {
            log.debug("[组解析] 🔀 非NODE类型，选择GROUP_CONTAINER策略");
            return executeGroupContainerStrategy(field, fieldValue, repeatCount, elementTypeInfo, annotation);
        }
    }

    /**
     * 解析组内元素
     *
     * <p>根据元素类型选择相应的解析方法：</p>
     * <ul>
     *   <li>NODE：直接解析Node对象</li>
     *   <li>PROTOCOL_OBJECT：解析包含协议注解的对象</li>
     *   <li>CUSTOM_OBJECT：解析自定义对象（默认作为协议对象处理）</li>
     *   <li>AUTO：自动检测类型并选择解析方法</li>
     * </ul>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param fieldValue 字段的实际值
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> resolveGroupElements(Field field, Object fieldValue,
                                             ElementTypeInfo elementTypeInfo, int groupIndex,
                                             ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 🔍 开始解析组内元素: 组索引={}, 元素类型={}", groupIndex, elementTypeInfo.getTypeName());

        List<INode> result;
        if (elementTypeInfo.isNode()) {
            log.debug("[组解析] 🔍 使用NODE解析方法");
            result = resolveNodeElements(fieldValue, groupIndex, annotation);
        } else if (elementTypeInfo.isProtocolObject()) {
            log.debug("[组解析] 🔍 使用PROTOCOL_OBJECT解析方法");
            result = resolveProtocolObjectElements(field, fieldValue, groupIndex, annotation);
        } else {
            log.debug("[组解析] 🔍 使用CUSTOM_OBJECT解析方法");
            result = resolveCustomObjectElements(field, fieldValue, groupIndex, annotation);
        }

        log.debug("[组解析] 🔍 组内元素解析完成: 组索引={}, 元素类型={}, 结果节点数={}",
                groupIndex, elementTypeInfo.getTypeName(), result.size());
        return result;
    }

    /**
     * 解析节点元素
     *
     * <p>专门处理List&lt;Node&gt;类型的字段值。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>验证字段值是否为List类型</li>
     *   <li>遍历List中的每个Node对象</li>
     *   <li>深拷贝每个Node对象</li>
     *   <li>递归应用ID和名称后缀</li>
     *   <li>将处理后的Node添加到结果列表</li>
     * </ol>
     *
     * @param fieldValue 字段值，期望是List&lt;Node&gt;类型
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> resolveNodeElements(Object fieldValue, int groupIndex,
                                            ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 🔍 开始解析节点元素: 组索引={}", groupIndex);

        if (!(fieldValue instanceof List)) {
            log.warn("[组解析] 🔍 字段值不是List类型: {}",
                    fieldValue != null ? fieldValue.getClass().getSimpleName() : "null");
            return Collections.emptyList();
        }

        List<?> list = (List<?>) fieldValue;
        log.debug("[组解析] 🔍 节点列表大小: {}", list.size());

        List<INode> result = new ArrayList<>();
        int processedNodes = 0;
        int skippedNodes = 0;

        for (Object item : list) {
            if (item instanceof Node) {
                log.debug("[组解析] 🔍 处理节点: ID={}, 名称={}",
                        ((Node) item).getId(), ((Node) item).getName());

                Node node = deepCloneNode((Node) item);
                log.debug("[组解析] 🔍 节点深拷贝完成");

                Map<String, String> idMapping = new HashMap<>();
                applySuffixRecursively(node, annotation.idSuffixPattern(), annotation.nameSuffixPattern(), groupIndex, idMapping);
                assert node != null;
                log.debug("[组解析] 🔍 节点后缀应用完成: ID={}, 名称={}",node.getId(), node.getName());

                result.add((INode) node);
                processedNodes++;

                log.debug("[组解析] 🔍 节点处理完成，已添加到结果列表");
            } else {
                log.warn("[组解析] 🔍 跳过非Node类型的元素: {} (类型: {})",
                        item, item != null ? item.getClass().getSimpleName() : "null");
                skippedNodes++;
            }
        }

        log.debug("[组解析] ✅ 节点元素解析完成: 组索引={}, 处理={}, 跳过={}, 结果={}",
                groupIndex, processedNodes, skippedNodes, result.size());
        return result;
    }

    /**
     * 解析协议对象元素
     *
     * <p>专门处理List&lt;ProtocolObject&gt;类型的字段值。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>验证字段值是否为List类型</li>
     *   <li>遍历List中的每个协议对象</li>
     *   <li>使用ProtocolClassParser递归解析每个协议对象</li>
     *   <li>收集所有解析出的节点</li>
     * </ol>
     *
     * <p>适用场景：字段包含嵌套的协议定义，如List&lt;SensorProtocol&gt;</p>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param fieldValue 字段值，期望是List&lt;ProtocolObject&gt;类型
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> resolveProtocolObjectElements(Field field, Object fieldValue,
                                                      int groupIndex, ProtocolNodeGroup annotation)
            throws CodecException {

        log.debug("[组解析] 🔍 开始解析协议对象元素: 组索引={}", groupIndex);

        if (!(fieldValue instanceof List)) {
            log.warn("[组解析] 🔍 字段值不是List类型: {}",
                    fieldValue != null ? fieldValue.getClass().getSimpleName() : "null");
            return Collections.emptyList();
        }

        List<?> list = (List<?>) fieldValue;
        log.debug("[组解析] 🔍 协议对象列表大小: {}", list.size());

        List<INode> result = new ArrayList<>();
        int processedObjects = 0;
        int nullObjects = 0;
        Object item = list.get(groupIndex - 1);

        if (item != null) {
            log.debug("[组解析] 🔍 处理协议对象: 类型={}", item.getClass().getSimpleName());

            // 递归解析协议对象
            List<INode> parsedNodes = parseProtocolObject(item, groupIndex, annotation);
            log.debug("[组解析] 🔍 协议对象解析完成: 生成了 {} 个节点", parsedNodes.size());

            result.addAll(parsedNodes);
            processedObjects++;

            log.debug("[组解析] 🔍 协议对象处理完成，节点已添加到结果列表");
        } else {
            log.warn("[组解析] 🔍 跳过null协议对象");
            nullObjects++;
        }

        log.debug("[组解析] ✅ 协议对象元素解析完成: 组索引={}, 处理={}, 跳过null={}, 结果={}",
                groupIndex, processedObjects, nullObjects, result.size());
        return result;
    }

    /**
     * 解析自定义对象元素
     *
     * <p>处理自定义对象类型的字段值，默认尝试作为协议对象进行解析。</p>
     * <p>如果自定义对象包含协议注解，则按协议对象处理；</p>
     * <p>如果不包含协议注解，则按普通对象处理。</p>
     *
     * <p>当前实现：直接委托给resolveProtocolObjectElements方法</p>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param fieldValue 字段值，自定义对象类型
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> resolveCustomObjectElements(Field field, Object fieldValue,
                                                    int groupIndex, ProtocolNodeGroup annotation)
            throws CodecException {

        log.debug("[组解析] 🔍 开始解析自定义对象元素: 组索引={}, 对象类型={}",
                groupIndex, fieldValue != null ? fieldValue.getClass().getSimpleName() : "null");

        // 如果fieldValue为null，尝试根据字段类型动态创建子节点
        if (fieldValue == null) {
            log.debug("[组解析] 🔍 fieldValue为null，尝试根据字段类型动态创建子节点");
            return createNodesFromFieldType(field, groupIndex, annotation);
        }

        // 默认实现：尝试作为协议对象解析
        log.debug("[组解析] 🔍 使用默认实现：尝试作为协议对象解析");
        List<INode> result = resolveProtocolObjectElements(field, fieldValue, groupIndex, annotation);

        log.debug("[组解析] 🔍 自定义对象元素解析完成: 组索引={}, 结果节点数={}", groupIndex, result.size());
        return result;
    }

    /**
     * 解析自动检测的元素
     *
     * <p>当元素类型配置为AUTO时，自动检测字段的实际类型并选择合适的解析方法。</p>
     * <p>检测逻辑：</p>
     * <ol>
     *   <li>分析字段的泛型类型</li>
     *   <li>检查是否包含协议注解</li>
     *   <li>根据检测结果选择解析策略</li>
     * </ol>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param fieldValue 字段值
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析后的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> resolveAutoDetectedElements(Field field, Object fieldValue,
                                                    int groupIndex, ProtocolNodeGroup annotation)
            throws CodecException {

        log.debug("[组解析] 🔍 开始自动检测元素类型: 组索引={}, 字段={}", groupIndex, field.getName());

        ElementTypeInfo detectedTypeInfo = detectElementType(field);
        log.debug("[组解析] 🔍 自动检测结果: 元素类型={}", detectedTypeInfo.getTypeName());

        List<INode> result = resolveGroupElements(field, fieldValue, detectedTypeInfo, groupIndex, annotation);

        log.debug("[组解析] 🔍 自动检测元素解析完成: 组索引={}, 检测类型={}, 结果节点数={}",
                groupIndex, detectedTypeInfo.getTypeName(), result.size());
        return result;
    }

    /**
     * 解析协议对象
     *
     * <p>使用ProtocolClassParser解析包含协议注解的对象，提取其中的所有节点。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>使用ProtocolClassParser.parseProtocol解析协议对象</li>
     *   <li>收集协议中的所有节点（Header、Body、Tail、直接节点）</li>
     *   <li>为每个Node类型的节点应用ID和名称后缀</li>
     *   <li>确保类型转换正确，返回结果列表</li>
     * </ol>
     *
     * <p>适用场景：解析包含@ProtocolDefinition等注解的类实例</p>
     *
     * @param obj        要解析的协议对象
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 解析出的协议节点列表
     * @throws CodecException 当解析过程中发生错误时抛出
     */
    private List<INode> parseProtocolObject(Object obj, int groupIndex,
                                            ProtocolNodeGroup annotation) throws CodecException {

        log.debug("[组解析] 🔍 开始解析协议对象: 组索引={}, 对象类型={}",
                groupIndex, obj != null ? obj.getClass().getSimpleName() : "null");

        // 使用现有的ProtocolClassParser解析协议对象
        try {
            log.debug("[组解析] 🔍 调用ProtocolClassParser.parseProtocol解析协议对象");
            Protocol protocol = ProtocolClassParser.parseProtocol(obj);
            log.debug("[组解析] 🔍 协议对象解析完成，开始收集节点");

            List<INode> nodes = new ArrayList<>();

            // 收集所有节点
            collectAllNodes(protocol, nodes);
            log.debug("[组解析] 🔍 节点收集完成，原始节点数: {}", nodes.size());

            // 将原始对象的字段值设置到对应的节点中
            setNodeValuesFromObject(obj, nodes);
            log.debug("[组解析] 🔍 节点值设置完成");

            // 应用后缀并确保类型正确
            List<INode> result = new ArrayList<>();
            int processedNodes = 0;
            int nonNodeTypes = 0;
            Map<String, String> idMapping = new HashMap<>();

            for (INode node : nodes) {
                if (node instanceof Node) {
                    Node nodeObj = (Node) node;
                    log.debug("[组解析] 🔍 处理Node类型节点: ID={}, 名称={}, 类型={}, 长度={}",
                            nodeObj.getId(), nodeObj.getName(), nodeObj.getValueType(), nodeObj.getLength());

                    // 应用后缀
                    applySuffixRecursively(nodeObj, annotation.idSuffixPattern(),
                            annotation.nameSuffixPattern(), groupIndex, idMapping);
                    log.debug("[组解析] 🔍 节点后缀应用完成: ID={}, 名称={}",
                            nodeObj.getId(), nodeObj.getName());

                    // 确保类型转换正确
                    result.add(nodeObj);
                    processedNodes++;

                    log.debug("[组解析] 🔍 Node类型节点处理完成，已添加到结果列表");
                } else {
                    log.debug("[组解析] 🔍 发现非Node类型节点: {}", node.getClass().getSimpleName());
                    // 如果不是Node类型，直接添加
                    result.add(node);
                    nonNodeTypes++;
                }
            }

            log.debug("[组解析] ✅ 协议对象解析完成: 组索引={}, 原始节点={}, 处理Node={}, 非Node={}, 结果={}",
                    groupIndex, nodes.size(), processedNodes, nonNodeTypes, result.size());

            // 打印最终结果的详细信息
            for (INode node : result) {
                if (node instanceof Node) {
                    Node nodeObj = (Node) node;
                    log.debug("[组解析] 🔍 最终节点: ID={}, 名称={}, 类型={}, 长度={}, 值={}",
                            nodeObj.getId(), nodeObj.getName(), nodeObj.getValueType(),
                            nodeObj.getLength(), nodeObj.getValue());
                }
            }

            return result;

        } catch (Exception e) {
            log.warn("[组解析] ⚠️  解析协议对象失败: {} (错误: {})",
                    obj != null ? obj.getClass().getSimpleName() : "null", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 将原始对象的字段值设置到对应的节点中
     *
     * <p>通过反射获取原始对象的字段值，并将这些值设置到对应的协议节点中。</p>
     * <p>这是解决CUSTOM_OBJECT类型节点组的关键：确保节点有实际的值。</p>
     *
     * @param obj   原始对象（如ParamGroup实例）
     * @param nodes 要设置值的协议节点列表
     */
    private void setNodeValuesFromObject(Object obj, List<INode> nodes) {
        if (obj == null || nodes == null || nodes.isEmpty()) {
            log.debug("[组解析] 🏷️  跳过节点值设置：对象或节点列表为空");
            return;
        }

        log.debug("[组解析] 🏷️  开始设置节点值，对象类型: {}, 节点数量: {}",
                obj.getClass().getSimpleName(), nodes.size());

        try {
            // 获取原始对象的所有字段
            Field[] fields = obj.getClass().getDeclaredFields();
            log.debug("[组解析] 🏷️  原始对象字段数量: {}", fields.length);

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                try {
                    Object fieldValue = field.get(obj);
                    //log.debug("[组解析] 🏷️  字段: {} = {}", fieldName, fieldValue);

                    // 查找对应的节点并设置值和ID
                    for (INode node : nodes) {
                        if (node instanceof Node) {
                            Node nodeObj = (Node) node;
                            String nodeId = nodeObj.getId();

                            // 通过ID匹配字段（去掉后缀）
                            if (nodeId != null && nodeId.contains(fieldName)) {
                                log.debug("[组解析] 🏷️  找到匹配节点: ID={}, 字段={}, 值={}",
                                        nodeId, fieldName, fieldValue);
                                nodeObj.setValue(fieldValue);
                                break;
                            }

                            // 如果节点ID为null，尝试通过名称匹配并设置ID
                            if (nodeId == null && nodeObj.getName() != null &&
                                    nodeObj.getName().toLowerCase().contains(fieldName.toLowerCase())) {
                                // 生成节点ID：字段名_组索引
                                String generatedId = fieldName + "_" + getGroupIndexFromNode(nodeObj);
                                log.debug("[组解析] 🏷️  为节点生成ID: 名称={}, 生成ID={}, 字段={}, 值={}",
                                        nodeObj.getName(), generatedId, fieldName, fieldValue);
                                nodeObj.setId(generatedId);
                                nodeObj.setValue(fieldValue);
                                break;
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.warn("[组解析] ⚠️  无法访问字段: {} (错误: {})", fieldName, e.getMessage());
                }
            }

            log.debug("[组解析] 🏷️  节点值设置完成");
        } catch (Exception e) {
            log.warn("[组解析] ⚠️  设置节点值时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 从节点名称中提取组索引
     *
     * <p>从节点名称中提取组索引，用于生成唯一的节点ID。</p>
     * <p>例如：ParamID[1] → 1, ParamValue[2] → 2</p>
     *
     * @param node 要提取组索引的节点
     * @return 组索引，如果无法提取则返回1
     */
    private int getGroupIndexFromNode(Node node) {
        if (node == null || node.getName() == null) {
            return 1;
        }

        String name = node.getName();
        // 匹配 [数字] 格式，提取组索引
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[(\\d+)]");
        java.util.regex.Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.debug("[组解析] 🏷️  无法解析组索引: {}", name);
            }
        }

        // 如果无法提取，返回默认值1
        return 1;
    }

    /**
     * 收集协议中的所有节点
     *
     * <p>递归收集协议结构中的所有节点，包括：</p>
     * <ul>
     *   <li>Header中的节点</li>
     *   <li>Body中的节点</li>
     *   <li>Tail中的节点</li>
     *   <li>直接节点</li>
     *   <li>嵌套结构中的节点</li>
     * </ul>
     *
     * @param protocol  要收集节点的协议对象
     * @param collector 节点收集器，用于存储收集到的节点
     */
    private void collectAllNodes(Protocol protocol, List<INode> collector) {
        log.debug("[组解析] 📥 开始收集协议中的所有节点");

        int initialCount = collector.size();

        if (protocol.getHeader() != null) {
            log.debug("[组解析] 📥 收集Header中的节点");
            collectStructureNodes(protocol.getHeader(), collector);
        } else {
            log.debug("[组解析] 📥 Header为null，跳过");
        }

        if (protocol.getBody() != null) {
            log.debug("[组解析] 📥 收集Body中的节点");
            collectStructureNodes(protocol.getBody(), collector);
        } else {
            log.debug("[组解析] 📥 Body为null，跳过");
        }

        if (protocol.getTail() != null) {
            log.debug("[组解析] 📥 收集Tail中的节点");
            collectStructureNodes(protocol.getTail(), collector);
        } else {
            log.debug("[组解析] 📥 Tail为null，跳过");
        }

        if (protocol.getNodes() != null) {
            log.debug("[组解析] 📥 收集直接节点，数量: {}", protocol.getNodes().size());
            // 确保类型转换正确
            int validNodes = 0;
            for (Node node : protocol.getNodes()) {
                if (node != null) {
                    log.debug("[组解析] 📥 添加直接节点: ID={}, 名称={}, 类型={}, 长度={}",
                            node.getId(), node.getName(), node.getValueType(), node.getLength());
                    collector.add((INode) node);
                    validNodes++;
                } else {
                    log.warn("[组解析] 📥 发现null节点，跳过");
                }
            }
            log.debug("[组解析] 📥 直接节点收集完成: 有效节点={}/{}", validNodes, protocol.getNodes().size());
        } else {
            log.debug("[组解析] 📥 直接节点为null，跳过");
        }

        int finalCount = collector.size();
        log.debug("[组解析] ✅ 协议节点收集完成: 新增 {} 个节点，总计 {} 个节点",
                finalCount - initialCount, finalCount);
    }

    /**
     * 收集结构体中的节点
     *
     * <p>递归收集协议结构体（Header、Body、Tail）中的所有节点。</p>
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>Header：收集其直接节点</li>
     *   <li>Body：收集其直接节点，并递归处理嵌套的Header、Body、Tail</li>
     *   <li>Tail：收集其直接节点</li>
     * </ul>
     *
     * <p>递归处理：Body可以包含嵌套的Header、Body、Tail，形成多层结构</p>
     *
     * @param structure 要收集节点的结构体对象
     * @param collector 节点收集器，用于存储收集到的节点
     */
    private void collectStructureNodes(Object structure, List<INode> collector) {
        log.debug("[组解析] 📥 开始收集结构体节点: 类型={}",
                structure != null ? structure.getClass().getSimpleName() : "null");

        if (structure instanceof Header) {
            Header header = (Header) structure;
            log.debug("[组解析] 📥 处理Header结构体");

            if (header.getNodes() != null) {
                log.debug("[组解析] 📥 Header节点数量: {}", header.getNodes().size());
                // 确保类型转换正确
                int validNodes = 0;
                for (Node node : header.getNodes()) {
                    if (node != null) {
                        log.debug("[组解析] 📥 添加Header节点: ID={}, 名称={}, 类型={}, 长度={}",
                                node.getId(), node.getName(), node.getValueType(), node.getLength());
                        collector.add((INode) node);
                        validNodes++;
                    } else {
                        log.warn("[组解析] 📥 Header中发现null节点，跳过");
                    }
                }
                log.debug("[组解析] 📥 Header节点收集完成: 有效节点={}/{}", validNodes, header.getNodes().size());
            } else {
                log.debug("[组解析] 📥 Header无节点，跳过");
            }

        } else if (structure instanceof Body) {
            Body body = (Body) structure;
            log.debug("[组解析] 📥 处理Body结构体");

            if (body.getNodes() != null) {
                log.debug("[组解析] 📥 Body直接节点数量: {}", body.getNodes().size());
                // 确保类型转换正确
                int validNodes = 0;
                for (Node node : body.getNodes()) {
                    if (node != null) {
                        log.debug("[组解析] 📥 添加Body直接节点: ID={}, 名称={}, 类型={}, 长度={}",
                                node.getId(), node.getName(), node.getValueType(), node.getLength());
                        collector.add((INode) node);
                        validNodes++;
                    } else {
                        log.warn("[组解析] 📥 Body中发现null节点，跳过");
                    }
                }
                log.debug("[组解析] 📥 Body直接节点收集完成: 有效节点={}/{}", validNodes, body.getNodes().size());
            } else {
                log.debug("[组解析] 📥 Body无直接节点");
            }

            // 递归处理嵌套结构
            if (body.getHeader() != null) {
                log.debug("[组解析] 📥 递归处理Body的嵌套Header");
                collectStructureNodes(body.getHeader(), collector);
            }
            if (body.getBody() != null) {
                log.debug("[组解析] 📥 递归处理Body的嵌套Body");
                collectStructureNodes(body.getBody(), collector);
            }
            if (body.getTail() != null) {
                log.debug("[组解析] 📥 递归处理Body的嵌套Tail");
                collectStructureNodes(body.getTail(), collector);
            }

        } else if (structure instanceof Tail) {
            Tail tail = (Tail) structure;
            log.debug("[组解析] 📥 处理Tail结构体");

            if (tail.getNodes() != null) {
                log.debug("[组解析] 📥 Tail节点数量: {}", tail.getNodes().size());
                // 确保类型转换正确
                int validNodes = 0;
                for (Node node : tail.getNodes()) {
                    if (node != null) {
                        log.debug("[组解析] 📥 添加Tail节点: ID={}, 名称={}, 类型={}, 长度={}",
                                node.getId(), node.getName(), node.getValueType(), node.getLength());
                        collector.add((INode) node);
                        validNodes++;
                    } else {
                        log.warn("[组解析] 📥 Tail中发现null节点，跳过");
                    }
                }
                log.debug("[组解析] 📥 Tail节点收集完成: 有效节点={}/{}", validNodes, tail.getNodes().size());
            } else {
                log.debug("[组解析] 📥 Tail无节点，跳过");
            }

        } else {
            log.warn("[组解析] 📥 未知的结构体类型: {}",
                    structure != null ? structure.getClass().getSimpleName() : "null");
        }

        log.debug("[组解析] 📥 结构体节点收集完成: 类型={}, 收集器当前大小={}",
                structure != null ? structure.getClass().getSimpleName() : "null", collector.size());
    }

    /**
     * 深拷贝节点
     *
     * <p>创建Node对象的完整深拷贝，包括所有字段和嵌套对象。</p>
     * <p>拷贝内容：</p>
     * <ul>
     *   <li>基本字段：ID、名称、长度、类型、值等</li>
     *   <li>条件依赖：条件节点、条件表达式、动作等</li>
     *   <li>枚举范围：枚举值、描述等</li>
     *   <li>子节点：递归深拷贝所有子节点</li>
     * </ul>
     *
     * <p>深拷贝确保：</p>
     * <ul>
     *   <li>原始对象和拷贝对象完全独立</li>
     *   <li>修改拷贝对象不会影响原始对象</li>
     *   <li>支持多组重复时的节点独立性</li>
     * </ul>
     *
     * @param source 要拷贝的源节点
     * @return 深拷贝后的新节点，如果源节点为null则返回null
     */
    private Node deepCloneNode(Node source) {
        if (source == null) {
            log.debug("[组解析] 📋 源节点为null，跳过深拷贝");
            return null;
        }

        log.debug("[组解析] 📋 开始深拷贝节点: ID={}, 名称={}", source.getId(), source.getName());

        Node target = new Node();

        // 基本字段
        log.debug("[组解析] 📋 拷贝基本字段");
        target.setId(source.getId());
        target.setName(source.getName());
        target.setFieldName(source.getFieldName());
        target.setLength(source.getLength());
        target.setValueType(source.getValueType());
        target.setEndianType(source.getEndianType());
        target.setValue(source.getValue());
        target.setFwdExpr(source.getFwdExpr());
        target.setFwdExprResult(source.getFwdExprResult());
        target.setBwdExpr(source.getBwdExpr());
        target.setRange(source.getRange());
        target.setCharset(source.getCharset());
        target.setOptional(source.isOptional());
        target.setOrder(source.getOrder());
        target.setPath(source.getPath());
        target.setPaddingConfig(source.getPaddingConfig());
        target.setActualDataLength(source.getActualDataLength());
        target.setEnabled(source.isEnabled());
        target.setEnabledReason(source.getEnabledReason());
        target.setValidationError(source.getValidationError());
        target.setValidationResult(source.getValidationResult());
        target.setValidationStatus(source.getValidationStatus());
        target.setStartBitPosition(source.getStartBitPosition());
        target.setEndBitPosition(source.getEndBitPosition());
        log.debug("[组解析] 📋 基本字段拷贝完成");

        // 条件依赖
        if (source.getConditionalDependencies() != null) {
            log.debug("[组解析] 📋 拷贝条件依赖，数量: {}", source.getConditionalDependencies().size());
            List<ConditionalDependency> copied = new ArrayList<>(source.getConditionalDependencies().size());
            int validDeps = 0;
            for (ConditionalDependency dep : source.getConditionalDependencies()) {
                if (dep == null) {
                    log.debug("[组解析] 📋 跳过null条件依赖");
                    continue;
                }
                ConditionalDependency c = new ConditionalDependency();
                c.setConditionNode(dep.getConditionNode());
                c.setCondition(dep.getCondition());
                c.setAction(dep.getAction());
                c.setElseAction(dep.getElseAction());
                c.setPriority(dep.getPriority());
                c.setDescription(dep.getDescription());
                copied.add(c);
                validDeps++;
            }
            target.setConditionalDependencies(copied);
            log.debug("[组解析] 📋 条件依赖拷贝完成: 有效={}/{}", validDeps, source.getConditionalDependencies().size());
        } else {
            log.debug("[组解析] 📋 无条件依赖，跳过");
        }

        // 枚举范围
        if (source.getEnumRanges() != null) {
            log.debug("[组解析] 📋 拷贝枚举范围，数量: {}", source.getEnumRanges().size());
            List<EnumRange> enumRanges = new ArrayList<>(source.getEnumRanges().size());
            int validRanges = 0;
            for (EnumRange e : source.getEnumRanges()) {
                if (e == null) {
                    log.debug("[组解析] 📋 跳过null枚举范围");
                    continue;
                }
                EnumRange ne = new EnumRange();
                ne.setValue(e.getValue());
                ne.setDesc(e.getDesc());
                enumRanges.add(ne);
                validRanges++;
            }
            target.setEnumRanges(enumRanges);
            log.debug("[组解析] 📋 枚举范围拷贝完成: 有效={}/{}", validRanges, source.getEnumRanges().size());
        } else {
            log.debug("[组解析] 📋 无枚举范围，跳过");
        }

        // 子节点
        if (source.getChildren() != null) {
            log.debug("[组解析] 📋 拷贝子节点，数量: {}", source.getChildren().size());
            List<Node> children = new ArrayList<>(source.getChildren().size());
            for (Node child : source.getChildren()) {
                Node clonedChild = deepCloneNode(child);
                children.add(clonedChild);
            }
            target.setChildren(children);
            log.debug("[组解析] 📋 子节点拷贝完成: 数量={}", children.size());
        } else {
            log.debug("[组解析] 📋 无子节点，跳过");
        }

        log.debug("[组解析] ✅ 节点深拷贝完成: ID={}, 名称={}", target.getId(), target.getName());
        return target;
    }

    /**
     * 为节点及其子孙节点追加id/name后缀
     *
     * <p>递归遍历节点树，为每个节点添加唯一标识符。</p>
     * <p>后缀格式：</p>
     * <ul>
     *   <li>ID后缀：使用配置的pattern格式化，失败时使用"_index"</li>
     *   <li>名称后缀：使用配置的pattern格式化，失败时使用"[index]"</li>
     * </ul>
     *
     * <p>重要：同时更新表达式中的节点引用，确保引用关系正确</p>
     * <p>表达式更新规则：</p>
     * <ul>
     *   <li>正向表达式：更新所有#nodeId引用</li>
     *   <li>反向表达式：更新所有#nodeId引用</li>
     *   <li>条件表达式：更新条件节点引用</li>
     * </ul>
     *
     * <p>递归处理：</p>
     * <ol>
     *   <li>处理当前节点的ID和名称</li>
     *   <li>更新表达式中的节点引用</li>
     *   <li>递归处理所有子节点</li>
     *   <li>确保整个节点树都有唯一标识</li>
     * </ol>
     *
     * @param node              要处理的节点
     * @param idSuffixPattern   ID后缀格式模式
     * @param nameSuffixPattern 名称后缀格式模式
     * @param index             组索引（从1开始）
     * @param idMapping         ID映射表，记录原始ID到新ID的映射关系
     */
    private void applySuffixRecursively(Node node, String idSuffixPattern,
                                        String nameSuffixPattern, int index,
                                        Map<String, String> idMapping) {
        if (node == null) {
            log.debug("[组解析] 🏷️  节点为null，跳过后缀处理");
            return;
        }

        log.debug("[组解析] 🏷️  开始处理节点后缀: ID={}, 名称={}, 索引={}",
                node.getId(), node.getName(), index);

        // 记录原始ID，用于后续的表达式更新
        String originalId = node.getId();

        // 处理ID后缀
        String id = node.getId();
        if (id != null && !id.isEmpty()) {
            try {
                String newId = id + formatSuffix(idSuffixPattern, index);
                node.setId(newId);
                // 记录ID映射关系
                idMapping.put(originalId, newId);
                log.debug("[组解析] 🏷️  ID后缀应用成功: {} → {}", id, newId);
            } catch (Exception e) {
                String fallbackId = id + "_" + index;
                node.setId(fallbackId);
                // 记录ID映射关系
                idMapping.put(originalId, fallbackId);
                log.warn("[组解析] 🏷️  ID后缀格式化失败，使用备用格式: {} → {} (错误: {})",
                        id, fallbackId, e.getMessage());
            }
        } else {
            log.debug("[组解析] 🏷️  节点ID为空，跳过ID后缀处理");
        }

        // 处理名称后缀
        String name = node.getName();
        if (name != null && !name.isEmpty()) {
            try {
                String newName = name + formatSuffix(nameSuffixPattern, index);
                node.setName(newName);
                log.debug("[组解析] 🏷️  名称后缀应用成功: {} → {}", name, newName);
            } catch (Exception e) {
                String fallbackName = name + "[" + index + "]";
                node.setName(fallbackName);
                log.warn("[组解析] 🏷️  名称后缀格式化失败，使用备用格式: {} → {} (错误: {})",
                        name, fallbackName, e.getMessage());
            }
        } else {
            log.debug("[组解析] 🏷️  节点名称为空，跳过名称后缀处理");
        }

        // 更新表达式中的节点引用
        updateExpressionsWithIdMapping(node, idMapping);

        // 递归处理子节点
        if (node.getChildren() != null) {
            log.debug("[组解析] 🏷️  处理子节点，数量: {}", node.getChildren().size());
            for (Node child : node.getChildren()) {
                applySuffixRecursively(child, idSuffixPattern, nameSuffixPattern, index, idMapping);
            }
            log.debug("[组解析] 🏷️  子节点后缀处理完成");
        } else {
            log.debug("[组解析] 🏷️  节点无子节点，跳过子节点处理");
        }

        log.debug("[组解析] 🏷️  节点后缀处理完成: ID={}, 名称={}", node.getId(), node.getName());
    }

    /**
     * 更新表达式中的节点引用
     *
     * <p>根据ID映射表，更新节点所有表达式中的节点引用。</p>
     * <p>更新范围：</p>
     * <ul>
     *   <li>正向表达式：fwdExpr</li>
     *   <li>反向表达式：bwdExpr</li>
     *   <li>条件依赖：条件节点引用</li>
     * </ul>
     *
     * <p>更新规则：</p>
     * <ul>
     *   <li>查找表达式中的#nodeId模式</li>
     *   <li>根据映射表替换为新的ID</li>
     *   <li>保持表达式语法不变</li>
     * </ul>
     *
     * @param node      要更新表达式的节点
     * @param idMapping ID映射表，记录原始ID到新ID的映射关系
     */
    private void updateExpressionsWithIdMapping(Node node, Map<String, String> idMapping) {
        if (idMapping == null || idMapping.isEmpty()) {
            log.debug("[组解析] 🔄 无ID映射关系，跳过表达式更新");
            return;
        }

        log.debug("[组解析] 🔄 开始更新表达式中的节点引用，映射表大小: {}", idMapping.size());

        // 更新正向表达式
        String fwdExpr = node.getFwdExpr();
        if (fwdExpr != null && !fwdExpr.isEmpty()) {
            String updatedFwdExpr = updateExpressionReferences(fwdExpr, idMapping);
            if (!fwdExpr.equals(updatedFwdExpr)) {
                node.setFwdExpr(updatedFwdExpr);
                log.debug("[组解析] 🔄 正向表达式更新: {} → {}", fwdExpr, updatedFwdExpr);
            }
        }

        // 更新反向表达式
        String bwdExpr = node.getBwdExpr();
        if (bwdExpr != null && !bwdExpr.isEmpty()) {
            String updatedBwdExpr = updateExpressionReferences(bwdExpr, idMapping);
            if (!bwdExpr.equals(updatedBwdExpr)) {
                node.setBwdExpr(updatedBwdExpr);
                log.debug("[组解析] 🔄 反向表达式更新: {} → {}", bwdExpr, updatedBwdExpr);
            }
        }

        // 更新条件依赖中的条件节点引用
        if (node.getConditionalDependencies() != null) {
            for (ConditionalDependency dep : node.getConditionalDependencies()) {
                if (dep != null && dep.getCondition() != null) {
                    String originalCondition = dep.getCondition();
                    String updatedCondition = updateExpressionReferences(originalCondition, idMapping);
                    if (!originalCondition.equals(updatedCondition)) {
                        dep.setCondition(updatedCondition);
                        log.debug("[组解析] 🔄 条件表达式更新: {} → {}", originalCondition, updatedCondition);
                    }
                }
            }
        }

        log.debug("[组解析] 🔄 表达式更新完成");
    }

    /**
     * 更新表达式中的节点引用
     *
     * <p>使用正则表达式查找并替换表达式中的#nodeId引用。</p>
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>使用正则表达式查找#nodeId模式</li>
     *   <li>提取节点ID（不包含#符号）</li>
     *   <li>在映射表中查找对应的新ID</li>
     *   <li>替换为新的引用格式</li>
     * </ol>
     *
     * <p>示例：</p>
     * <ul>
     *   <li>原始表达式：length(#param-id) + 10</li>
     *   <li>ID映射：param-id → param-id_1</li>
     *   <li>更新后：length(#param-id_1) + 10</li>
     * </ul>
     *
     * @param expression 原始表达式
     * @param idMapping  ID映射表
     * @return 更新后的表达式
     */
    private String updateExpressionReferences(String expression, Map<String, String> idMapping) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }

        log.debug("[组解析] 🔄 更新表达式引用: {}", expression);

        // 使用正则表达式查找#nodeId模式
        // 匹配#后面跟着字母、数字、下划线、连字符的组合
        String pattern = "#([a-zA-Z0-9_-]+)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(expression);

        StringBuffer result = new StringBuffer();
        int updateCount = 0;

        while (matcher.find()) {
            String fullMatch = matcher.group(0);  // 完整的匹配，如 #param-id
            String nodeId = matcher.group(1);     // 节点ID，如 param-id

            // 在映射表中查找新的ID
            String newId = idMapping.get(nodeId);
            if (newId != null) {
                String replacement = "#" + newId;
                matcher.appendReplacement(result, replacement);
                updateCount++;
                log.debug("[组解析] 🔄 引用更新: {} → {}", fullMatch, replacement);
            } else {
                // 如果没有找到映射，保持原样
                log.debug("[组解析] 🔄 引用未找到映射，保持原样: {}", fullMatch);
            }
        }
        matcher.appendTail(result);

        String updatedExpression = result.toString();
        if (updateCount > 0) {
            log.debug("[组解析] 🔄 表达式引用更新完成: 更新数量={}, 结果={}", updateCount, updatedExpression);
        } else {
            log.debug("[组解析] 🔄 表达式无引用需要更新");
        }

        return updatedExpression;
    }

    /**
     * 格式化后缀
     *
     * <p>使用String.format格式化后缀模式，支持各种格式字符串。</p>
     * <p>常见模式示例：</p>
     * <ul>
     *   <li>"_%d" → "_1", "_2", "_3"</li>
     *   <li>"[%02d]" → "[01]", "[02]", "[03]"</li>
     *   <li>"-%03d" → "-001", "-002", "-003"</li>
     * </ul>
     *
     * @param pattern 格式化模式字符串
     * @param index   要格式化的索引值
     * @return 格式化后的后缀字符串，失败时返回"_index"
     */
    private String formatSuffix(String pattern, int index) {
        log.debug("[组解析] 🎨 格式化后缀: 模式={}, 索引={}", pattern, index);

        try {
            String result = String.format(pattern, index);
            log.debug("[组解析] 🎨 后缀格式化成功: {} → {}", pattern, result);
            return result;
        } catch (Exception e) {
            String fallback = "_" + index;
            log.warn("[组解析] 🎨 后缀格式化失败，使用备用格式: {} → {} (错误: {})",
                    pattern, fallback, e.getMessage());
            return fallback;
        }
    }

    /**
     * 获取基础ID
     *
     * <p>从注解中获取基础ID，如果注解中未配置则使用字段名。</p>
     * <p>优先级：注解配置 > 字段名</p>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 基础ID字符串
     */
    private String getBaseId(Field field, ProtocolNodeGroup annotation) {
        String configuredId = annotation.id();
        String baseId = configuredId.isEmpty() ? field.getName() : configuredId;

        log.debug("[组解析] 🆔 获取基础ID: 配置值={}, 字段名={}, 最终值={}",
                configuredId, field.getName(), baseId);

        return baseId;
    }

    /**
     * 获取基础名称
     *
     * <p>从注解中获取基础名称，如果注解中未配置则使用字段名。</p>
     * <p>优先级：注解配置 > 字段名</p>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 基础名称字符串
     */
    private String getBaseName(Field field, ProtocolNodeGroup annotation) {
        String configuredName = annotation.name();
        String baseName = configuredName.isEmpty() ? field.getName() : configuredName;

        log.debug("[组解析] 📝 获取基础名称: 配置值={}, 字段名={}, 最终值={}",
                configuredName, field.getName(), baseName);

        return baseName;
    }

    /**
     * 根据字段类型动态创建子节点
     *
     * <p>当fieldValue为null时，根据字段的泛型类型动态创建协议节点结构。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>获取字段的泛型类型</li>
     *   <li>创建该类型的实例</li>
     *   <li>使用ProtocolClassParser解析协议结构</li>
     *   <li>收集所有节点并应用后缀</li>
     * </ol>
     *
     * @param field      包含@ProtocolNodeGroup注解的字段
     * @param groupIndex 组索引（从1开始）
     * @param annotation @ProtocolNodeGroup注解实例
     * @return 动态创建的协议节点列表
     * @throws CodecException 当创建过程中发生错误时抛出
     */
    private List<INode> createNodesFromFieldType(Field field, int groupIndex,
                                                 ProtocolNodeGroup annotation) throws CodecException {
        log.debug("[组解析] 🔧 开始根据字段类型动态创建子节点: 字段={}, 组索引={}", field.getName(), groupIndex);

        try {
            // 获取泛型类型
            Class<?> elementType = getListElementType(field);
            if (elementType == null) {
                log.warn("[组解析] 🔧 无法获取泛型类型，返回空列表");
                return Collections.emptyList();
            }

            log.debug("[组解析] 🔧 泛型类型: {}", elementType.getSimpleName());

            // 创建实例
            Object instance = elementType.getDeclaredConstructor().newInstance();
            log.debug("[组解析] 🔧 成功创建实例: {}", instance.getClass().getSimpleName());

            // 解析协议结构
            List<INode> nodes = parseProtocolObject(instance, groupIndex, annotation);
            log.debug("[组解析] 🔧 协议结构解析完成，节点数: {}", nodes.size());

            // 应用后缀
            List<INode> result = new ArrayList<>();
            Map<String, String> idMapping = new HashMap<>();

            for (INode node : nodes) {
                if (node instanceof Node) {
                    Node nodeObj = (Node) node;
                    applySuffixRecursively(nodeObj, annotation.idSuffixPattern(),
                            annotation.nameSuffixPattern(), groupIndex, idMapping);
                    result.add(nodeObj);
                } else {
                    result.add(node);
                }
            }

            log.debug("[组解析] ✅ 动态创建子节点完成: 字段={}, 组索引={}, 结果节点数={}",
                    field.getName(), groupIndex, result.size());
            return result;

        } catch (Exception e) {
            log.error("[组解析] ❌ 动态创建子节点失败: 字段={}, 组索引={}, 错误={}",
                    field.getName(), groupIndex, e.getMessage(), e);
            throw new CodecException("动态创建子节点失败: " + e.getMessage(), e);
        }
    }
} 