package com.iecas.cmd.parser;

import com.iecas.cmd.annotation.ProtocolEnum;
import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.annotation.*;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 协议类解析器
 * 将带注解的Java类解析为Protocol对象
 */
@Slf4j
public class ProtocolClassParser {

    /**
     * 解析协议实例为Protocol对象
     *
     * @param protocolInstance 协议实例对象
     * @return Protocol对象
     * @throws CodecException 解析异常
     */
    public static Protocol parseProtocol(Object protocolInstance) throws CodecException {
        if (protocolInstance == null) {
            throw new IllegalArgumentException("协议实例不能为空");
        }

        Class<?> protocolClass = protocolInstance.getClass();

        ProtocolDefinition protocolDefinition = protocolClass.getAnnotation(ProtocolDefinition.class);
        if (protocolDefinition == null) {
            throw new IllegalArgumentException("协议类必须带有@ProtocolDefinition注解");
        }

        Protocol protocol = new Protocol();
        protocol.setId(protocolDefinition.id());
        protocol.setName(protocolDefinition.name());

        // 获取所有字段
        Field[] fields = protocolClass.getDeclaredFields();

        // 按order排序
        Arrays.sort(fields, Comparator.comparingInt(field -> {
            ProtocolNode protocolNode = field.getAnnotation(ProtocolNode.class);
            if (protocolNode != null) {
                return protocolNode.order();
            }
            ProtocolHeader protocolHeader = field.getAnnotation(ProtocolHeader.class);
            if (protocolHeader != null) {
                return protocolHeader.order();
            }
            ProtocolBody protocolBody = field.getAnnotation(ProtocolBody.class);
            if (protocolBody != null) {
                return protocolBody.order();
            }
            ProtocolTail protocolTail = field.getAnnotation(ProtocolTail.class);
            if (protocolTail != null) {
                return protocolTail.order();
            }
            ProtocolNodeGroup protocolNodeGroup = field.getAnnotation(ProtocolNodeGroup.class);
            if (protocolNodeGroup != null) {
                return protocolNodeGroup.order();
            }
            return Integer.MAX_VALUE;
        }));

        // 解析字段
        for (Field field : fields) {
            field.setAccessible(true);

            try {
                Object fieldValue = field.get(protocolInstance);

                if (field.isAnnotationPresent(ProtocolHeader.class)) {
                    log.debug("[协议解析] 处理字段 {}: @ProtocolHeader", field.getName());
                    Header header = parseHeader(field, fieldValue);
                    protocol.setHeader(header);
                } else if (field.isAnnotationPresent(ProtocolBody.class)) {
                    log.debug("[协议解析] 处理字段 {}: @ProtocolBody", field.getName());
                    Body body = parseBody(field, fieldValue);
                    protocol.setBody(body);
                } else if (field.isAnnotationPresent(ProtocolTail.class)) {
                    log.debug("[协议解析] 处理字段 {}: @ProtocolTail", field.getName());
                    Tail tail = parseCheck(field, fieldValue);
                    protocol.setTail(tail);
                } else if (field.isAnnotationPresent(ProtocolNode.class)) {
                    log.debug("[协议解析] 处理字段 {}: @INode", field.getName());
                    Node node = parseFieldToNode(field, fieldValue);
                    protocol.getNodes().add(node);
                } else if (field.isAnnotationPresent(ProtocolNodes.class)) {
                    List<Node> nodes = parseNodesField(field, fieldValue);
                    protocol.getNodes().addAll(nodes);
                } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
                    log.debug("[协议解析] 处理字段 {}: @ProtocolNodeGroup", field.getName());
                    List<Node> nodes = parseNodeGroupField(field, fieldValue);
                    log.debug("[协议解析] 字段 {} 解析出 {} 个节点", field.getName(), nodes.size());
                    protocol.getNodes().addAll(nodes);
                } else {
                    log.debug("[协议解析] 字段 {} 没有协议注解，跳过", field.getName());
                }
            } catch (IllegalAccessException e) {
                log.error("访问字段失败: " + field.getName() + ", 错误: " + e.getMessage());
                throw new CodecException("访问字段失败: " + field.getName(), e);
            }
        }

        log.debug("协议解析完成");
        log.debug("协议树形结构:");
        printProtocolTree(protocol);

        return protocol;
    }
    /**
     * 解析动态节点列表字段
     */
    @SuppressWarnings("unchecked")
    private static List<Node> parseNodesField(Field field, Object fieldValue) throws CodecException {
        log.debug("[协议解析] 开始解析动态节点列表字段: {}", field.getName());

        // 确保字段类型是List
        if (!List.class.isAssignableFrom(field.getType())) {
            throw new CodecException("字段 " + field.getName() + " 的类型必须是List");
        }

        ProtocolNodes protocolNodesAnnotation = field.getAnnotation(ProtocolNodes.class);

        if (protocolNodesAnnotation != null) {
            return (List<Node>) fieldValue;
        }
        ProtocolNodeGroup protocolNodeGroupAnnotation = field.getAnnotation(ProtocolNodeGroup.class);
        if (protocolNodeGroupAnnotation == null) {
            throw new CodecException("字段 " + field.getName() + " 缺少 @ProtocolNodeGroup 注解");
        }

        log.debug("[协议解析] 字段 {} 类型检查通过，开始使用ProtocolNodeGroupResolver解析", field.getName());

        // 使用新的组解析器
        ProtocolNodeGroupResolver resolver = new ProtocolNodeGroupResolver();
        List<INode> resolvedNodes = resolver.resolveGroup(field, (List<?>) fieldValue, protocolNodeGroupAnnotation);

        log.debug("[协议解析] ProtocolNodeGroupResolver解析完成，原始节点数: {}", resolvedNodes.size());

        // 转换为Node列表，保留所有类型的节点
        List<Node> result = new ArrayList<>();
        int nodeCount = 0;
        int wrapperCount = 0;

        for (INode node : resolvedNodes) {
            if (node instanceof Node) {
                result.add((Node) node);
                nodeCount++;
                log.debug("[协议解析] 添加Node类型节点: ID={}, 名称={}", node.getId(), node.getName());
            } else {
                // 如果不是Node类型，尝试转换为Node
                // 这通常发生在FLATTEN策略中，子节点可能是其他类型
                log.debug("[协议解析] 发现非Node类型节点: {} (类型: {})",
                        node.getName(), node.getClass().getSimpleName());

                // 尝试将ProtocolNode转换为Node
                if (node instanceof Node) {
                    // 创建一个新的Node来包装这个ProtocolNode
                    Node wrapperNode = new Node();
                    wrapperNode.setId(node.getId());
                    wrapperNode.setName(node.getName());
                    wrapperNode.setValue(node.getValue());
                    wrapperNode.setLength(node.getLength());
                    wrapperNode.setValueType(node.getValueType());
                    wrapperNode.setOrder(0); // 默认顺序
                    wrapperNode.setPath(node.getPath());

                    // 如果有子节点，也添加进去
                    if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                        wrapperNode.setChildren(node.getChildren());
                        log.debug("[协议解析] 包装节点 {} 包含 {} 个子节点", wrapperNode.getName(), node.getChildren().size());
                    }

                    result.add(wrapperNode);
                    wrapperCount++;
                    log.debug("[协议解析] 成功包装节点: ID={}, 名称={}", wrapperNode.getId(), wrapperNode.getName());
                }
            }
        }

        log.debug("[协议解析] 字段 {} 解析完成: 原始节点={}, Node类型={}, 包装节点={}, 最终结果={}",
                field.getName(), resolvedNodes.size(), nodeCount, wrapperCount, result.size());

        return result;
    }

    /**
     * 解析协议头
     */
    private static Header parseHeader(Field field, Object fieldValue) throws CodecException {
        ProtocolHeader protocolHeaderAnnotation = field.getAnnotation(ProtocolHeader.class);
        Header header = new Header();

        String name = protocolHeaderAnnotation.name().isEmpty() ? field.getName() : protocolHeaderAnnotation.name();
        header.setName(name);

        // 设置ID
        String id = protocolHeaderAnnotation.id().isEmpty() ? field.getName() : protocolHeaderAnnotation.id();
        header.setId(id);
        header.setOrder(protocolHeaderAnnotation.order());

        // 如果字段类型是基本类型，直接解析为节点
        if (field.isAnnotationPresent(ProtocolNode.class)) {
            log.debug("[协议解析] 字段 {} 有@ProtocolNode注解，直接解析为节点", field.getName());
            Node node = parseFieldToNode(field, fieldValue);
            header.setNodes(Collections.singletonList(node));
        } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
            // 如果字段有@ProtocolNodeGroup注解，不应该在这里处理，跳过
            log.debug("[协议解析] 字段 {} 有@ProtocolNodeGroup注解，跳过嵌套结构解析", field.getName());
        } else if (fieldValue != null) {
            // 如果字段类型是复杂类型，解析其嵌套结构
            log.debug("[协议解析] 字段 {} 是复杂类型，调用parseNestedStructure", field.getName());
            parseNestedStructure(header, fieldValue);
        } else {
            // 如果实例为空，解析类型定义
            log.debug("[协议解析] 字段 {} 实例为空，从类型定义解析", field.getName());
            parseNestedStructureFromType(header, field.getType());
        }

        return header;
    }

    /**
     * 解析协议体
     */
    private static Body parseBody(Field field, Object fieldValue) throws CodecException {
        ProtocolBody protocolBodyAnnotation = field.getAnnotation(ProtocolBody.class);
        Body body = new Body();

        String name = protocolBodyAnnotation.name();
        body.setName(name);

        // 设置ID
        String id = protocolBodyAnnotation.id().isEmpty() ? field.getName() : protocolBodyAnnotation.id();
        body.setId(id);
        body.setOrder(protocolBodyAnnotation.order());

        // 如果字段类型是基本类型，直接解析为节点
        if (field.isAnnotationPresent(ProtocolNode.class)) {
            log.debug("[协议解析] 字段 {} 有@ProtocolNode注解，直接解析为节点", field.getName());
            Node node = parseFieldToNode(field, fieldValue);
            body.setNodes(Collections.singletonList(node));
        } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
            // 如果字段有@ProtocolNodeGroup注解，不应该在这里处理，跳过
            log.debug("[协议解析] 字段 {} 有@ProtocolNodeGroup注解，跳过嵌套结构解析", field.getName());
        } else if (fieldValue != null) {
            // 如果字段类型是复杂类型，解析其嵌套结构
            log.debug("[协议解析] 字段 {} 是复杂类型，调用parseNestedStructure", field.getName());
            parseNestedStructure(body, fieldValue);
        } else {
            // 如果实例为空，解析类型定义
            log.debug("[协议解析] 字段 {} 实例为空，从类型定义解析", field.getName());
            parseNestedStructureFromType(body, field.getType());
        }

        return body;
    }

    /**
     * 解析协议校验
     */
    private static Tail parseCheck(Field field, Object fieldValue) throws CodecException {
        ProtocolTail protocolTailAnnotation = field.getAnnotation(ProtocolTail.class);
        Tail tail = new Tail();

        String name = protocolTailAnnotation.name().isEmpty() ? field.getName() : protocolTailAnnotation.name();
        tail.setName(name);

        // 设置ID
        String id = protocolTailAnnotation.id().isEmpty() ? field.getName() : protocolTailAnnotation.id();
        tail.setId(id);
        tail.setOrder(protocolTailAnnotation.order());

        // 如果字段类型是基本类型，直接解析为节点
        if (field.isAnnotationPresent(ProtocolNode.class)) {
            log.debug("[协议解析] 字段 {} 有@ProtocolNode注解，直接解析为节点", field.getName());
            Node node = parseFieldToNode(field, fieldValue);
            tail.setNodes(Arrays.asList(node));
        } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
            // 如果字段有@ProtocolNodeGroup注解，不应该在这里处理，跳过
            log.debug("[协议解析] 字段 {} 有@ProtocolNodeGroup注解，跳过嵌套结构解析", field.getName());
        } else if (fieldValue != null) {
            // 如果字段类型是复杂类型，解析其嵌套结构
            log.debug("[协议解析] 字段 {} 是复杂类型，调用parseNestedStructure", field.getName());
            parseNestedStructure(tail, fieldValue);
        } else {
            // 如果实例为空，解析类型定义
            log.debug("[协议解析] 字段 {} 实例为空，从类型定义解析", field.getName());
            parseNestedStructureFromType(tail, field.getType());
        }

        return tail;
    }
//
//    /**
//     * 解析复杂类型实例（嵌套结构）
//     */
//    private static List<Node> parseComplexTypeInstance(Object instance) throws CodecException {
//        List<Node> nodes = new ArrayList<>();
//
//        Class<?> clazz = instance.getClass();
//        Field[] fields = clazz.getDeclaredFields();
//
//        // 按order排序 - 支持所有类型的协议注解
//        Arrays.sort(fields, Comparator.comparingInt(field -> {
//            INode protocolNode = field.getAnnotation(INode.class);
//            if (protocolNode != null) {
//                return protocolNode.order();
//            }
//            ProtocolHeader protocolHeader = field.getAnnotation(ProtocolHeader.class);
//            if (protocolHeader != null) {
//                return protocolHeader.order();
//            }
//            ProtocolBody protocolBody = field.getAnnotation(ProtocolBody.class);
//            if (protocolBody != null) {
//                return protocolBody.order();
//            }
//            ProtocolCheck protocolCheck = field.getAnnotation(ProtocolCheck.class);
//            if (protocolCheck != null) {
//                return protocolCheck.order();
//            }
//            ProtocolNodeGroup protocolNodesAnnotation = field.getAnnotation(ProtocolNodeGroup.class);
//            if (protocolNodesAnnotation != null) {
//                return protocolNodesAnnotation.order();
//            }
//            return Integer.MAX_VALUE;
//        }));
//
//        for (Field field : fields) {
//            field.setAccessible(true);
//            try {
//                Object fieldValue = field.get(instance);
//
//                if (field.isAnnotationPresent(INode.class)) {
//                    // 处理基本字段 - 直接作为节点
//                    Node node = parseFieldToNode(field, fieldValue);
//                    nodes.add(node);
//                } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
//                    // 处理动态节点列表字段
//                    List<Node> dynamicNodes = parseDynamicNodeListField(field, fieldValue);
//                    int lastNodeOrder = nodes.get(nodes.size() - 1).getOrder();
//                    for (Node dynamicNode : dynamicNodes) {
//                        dynamicNode.setOrder(lastNodeOrder+=1);
//                    }
//                    nodes.addAll(dynamicNodes);
//                } else if (field.isAnnotationPresent(ProtocolHeader.class) ||
//                          field.isAnnotationPresent(ProtocolBody.class) ||
//                          field.isAnnotationPresent(ProtocolCheck.class)) {
//                    // 对于嵌套结构字段，递归解析其内部字段
//                    if (fieldValue != null) {
//                        List<Node> nestedNodes = parseComplexTypeInstance(fieldValue);
//                        nodes.addAll(nestedNodes);
//                    }
//                }
//            } catch (IllegalAccessException e) {
//                log.error("访问字段失败: " + field.getName() + ", 错误: " + e.getMessage());
//                throw new CodecException("访问字段失败: " + field.getName(), e);
//            }
//        }
//
//        return nodes;
//    }

    /**
     * 解析复杂类型（嵌套结构）
     */
//    private static List<Node> parseComplexType(Class<?> clazz) throws CodecException {
//        List<Node> nodes = new ArrayList<>();
//
//        Field[] fields = clazz.getDeclaredFields();
//
//        // 按order排序 - 支持所有类型的协议注解
//        Arrays.sort(fields, Comparator.comparingInt(field -> {
//            INode protocolNode = field.getAnnotation(INode.class);
//            if (protocolNode != null) {
//                return protocolNode.order();
//            }
//            ProtocolHeader protocolHeader = field.getAnnotation(ProtocolHeader.class);
//            if (protocolHeader != null) {
//                return protocolHeader.order();
//            }
//            ProtocolBody protocolBody = field.getAnnotation(ProtocolBody.class);
//            if (protocolBody != null) {
//                return protocolBody.order();
//            }
//            ProtocolCheck protocolCheck = field.getAnnotation(ProtocolCheck.class);
//            if (protocolCheck != null) {
//                return protocolCheck.order();
//            }
//            ProtocolNodeGroup protocolNodesAnnotation = field.getAnnotation(ProtocolNodeGroup.class);
//            if (protocolNodesAnnotation != null) {
//                return protocolNodesAnnotation.order();
//            }
//            return Integer.MAX_VALUE;
//        }));
//
//        for (Field field : fields) {
//            field.setAccessible(true);
//
//            if (field.isAnnotationPresent(INode.class)) {
//                // 处理基本字段
//                Node node = parseFieldToNode(field, null);
//                nodes.add(node);
//            } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
//                // 处理动态节点列表字段
//                List<Node> dynamicNodes = parseDynamicNodeListField(field, null);
//                nodes.addAll(dynamicNodes);
//            } else if (field.isAnnotationPresent(ProtocolHeader.class)) {
//                // 处理嵌套协议头类型
//                Header header = parseHeader(field, null);
//                nodes.addAll(header.getNodes());
//            } else if (field.isAnnotationPresent(ProtocolBody.class)) {
//                // 处理嵌套协议体类型
//                Body body = parseBody(field, null);
//                nodes.addAll(body.getNodes());
//            } else if (field.isAnnotationPresent(ProtocolCheck.class)) {
//                // 处理嵌套协议校验类型
//                Check check = parseCheck(field, null);
//                nodes.addAll(check.getNodes());
//            }
//        }
//
//        return nodes;
//    }

    /**
     * 将字段解析为节点
     */
    private static Node parseFieldToNode(Field field, Object fieldValue) throws CodecException {
        ProtocolNode protocolField = field.getAnnotation(ProtocolNode.class);
        if (protocolField == null) {
            throw new CodecException("字段 " + field.getName() + " 缺少 @INode 注解");
        }

        Node node = new Node();

        // 设置基本属性
        String name = protocolField.name().isEmpty() ? field.getName() : protocolField.name();
        node.setName(name);
        node.setFieldName(field.getName());

        // 设置ID - 优先使用注解中的id，如果为空则使用字段名
        String id = protocolField.id().isEmpty() ? field.getName() : protocolField.id();
        node.setId(id);

        node.setLength(protocolField.length());
        node.setValueType(protocolField.valueType());
        node.setEndianType(protocolField.endian());
        node.setCharset(protocolField.charset());
        node.setOptional(protocolField.optional());
        node.setOrder(protocolField.order());

        // 设置值 - 优先使用实例中的实际值，其次使用注解中的默认值
        if (fieldValue != null) {
            node.setValue(fieldValue);
        } else if (!protocolField.value().isEmpty()) {
            node.setValue(protocolField.value());
        }

        // 设置可选属性
        if (!protocolField.fwdExpr().isEmpty()) {
            node.setFwdExpr(protocolField.fwdExpr());
        }
        if (!protocolField.bwdExpr().isEmpty()) {
            node.setBwdExpr(protocolField.bwdExpr());
        }
        if (!protocolField.range().isEmpty()) {
            node.setRange(protocolField.range());
        }

        // 处理枚举定义
        ProtocolEnum anProtocolEnum = field.getAnnotation(ProtocolEnum.class);
        if (anProtocolEnum != null) {
            List<EnumRange> enumRanges = parseEnumValues(anProtocolEnum.values());
            node.setEnumRanges(enumRanges);
        }

        // 处理条件依赖注解
        ConditionalOn[] conditionalOns = field.getAnnotationsByType(ConditionalOn.class);
        if (conditionalOns.length > 0) {
            List<ConditionalDependency> conditionalDependencies = new ArrayList<>();
            for (ConditionalOn conditionalOn : conditionalOns) {
                ConditionalDependency dependency = new ConditionalDependency(
                        conditionalOn.conditionNode(),
                        conditionalOn.condition(),
                        conditionalOn.action(),
                        conditionalOn.elseAction()
                );
                dependency.setPriority(conditionalOn.priority());
                dependency.setDescription(conditionalOn.description());
                conditionalDependencies.add(dependency);
            }
            node.setConditionalDependencies(conditionalDependencies);
        }

        // 处理填充注解
        Padding paddingAnnotation = field.getAnnotation(Padding.class);
        if (paddingAnnotation != null) {
            PaddingConfig paddingConfig = new PaddingConfig();
            paddingConfig.setPaddingType(paddingAnnotation.paddingType());
            paddingConfig.setTargetLength(paddingAnnotation.targetLength());
            paddingConfig.setPaddingValue(paddingAnnotation.paddingValue());
            paddingConfig.setRepeatPattern(paddingAnnotation.repeatPattern());
            paddingConfig.setMinPaddingLength(paddingAnnotation.minPaddingLength());
            paddingConfig.setMaxPaddingLength(paddingAnnotation.maxPaddingLength());
            paddingConfig.setLengthExpression(paddingAnnotation.lengthExpression());
            paddingConfig.setContainerNode(paddingAnnotation.containerNode());
            paddingConfig.setContainerFixedLength(paddingAnnotation.containerFixedLength());
            paddingConfig.setAutoCalculateContainerLength(paddingAnnotation.autoCalculateContainerLength());
            paddingConfig.setEnabled(paddingAnnotation.enabled());
            paddingConfig.setEnableCondition(paddingAnnotation.enableCondition());
            paddingConfig.setDescription(paddingAnnotation.description());
            node.setPaddingConfig(paddingConfig);
        }

        return node;
    }

    /**
     * 解析动态节点列表字段
     */
    @SuppressWarnings("unchecked")
    private static List<Node> parseNodeGroupField(Field field, Object fieldValue) throws CodecException {
        log.debug("[协议解析] 开始解析动态节点列表字段: {}", field.getName());
        if (fieldValue == null) {
            throw new CodecException("字段 " + field.getName() + " 值不能为 null");
        }


        ProtocolNodes protocolNodesAnnotation = field.getAnnotation(ProtocolNodes.class);

        if (protocolNodesAnnotation != null) {
            return (List<Node>) fieldValue;
        }

        ProtocolNodeGroup protocolNodeGroupAnnotation = field.getAnnotation(ProtocolNodeGroup.class);
        if (protocolNodeGroupAnnotation == null) {
            throw new CodecException("字段 " + field.getName() + " 缺少 @ProtocolNodeGroup 注解");
        }

        // 确保字段类型是List
        if (!List.class.isAssignableFrom(field.getType())) {
            throw new CodecException("字段 " + field.getName() + " 的类型必须是List");
        }

        log.debug("[协议解析] 字段 {} 类型检查通过，开始使用ProtocolNodeGroupResolver解析", field.getName());

        // 使用新的组解析器
        ProtocolNodeGroupResolver resolver = new ProtocolNodeGroupResolver();
        List<INode> resolvedNodes = resolver.resolveGroup(field, (List<?>) fieldValue, protocolNodeGroupAnnotation);

        log.debug("[协议解析] ProtocolNodeGroupResolver解析完成，原始节点数: {}", resolvedNodes.size());

        // 转换为Node列表，保留所有类型的节点
        List<Node> result = new ArrayList<>();
        int nodeCount = 0;
        int wrapperCount = 0;

        for (INode node : resolvedNodes) {
            if (node instanceof Node) {
                result.add((Node) node);
                nodeCount++;
                log.debug("[协议解析] 添加Node类型节点: ID={}, 名称={}", node.getId(), node.getName());
            } else {
                // 如果不是Node类型，尝试转换为Node
                // 这通常发生在FLATTEN策略中，子节点可能是其他类型
                log.debug("[协议解析] 发现非Node类型节点: {} (类型: {})",
                        node.getName(), node.getClass().getSimpleName());

                // 尝试将ProtocolNode转换为Node
                if (node instanceof INode) {
                    // 创建一个新的Node来包装这个ProtocolNode
                    Node wrapperNode = new Node();
                    wrapperNode.setId(node.getId());
                    wrapperNode.setName(node.getName());
                    wrapperNode.setValue(node.getValue());
                    wrapperNode.setLength(node.getLength());
                    wrapperNode.setValueType(node.getValueType());
                    wrapperNode.setOrder(0); // 默认顺序
                    wrapperNode.setPath(node.getPath());

                    // 如果有子节点，也添加进去
                    if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                        wrapperNode.setChildren(node.getChildren());
                        log.debug("[协议解析] 包装节点 {} 包含 {} 个子节点", wrapperNode.getName(), node.getChildren().size());
                    }

                    result.add(wrapperNode);
                    wrapperCount++;
                    log.debug("[协议解析] 成功包装节点: ID={}, 名称={}", wrapperNode.getId(), wrapperNode.getName());
                }
            }
        }

        log.debug("[协议解析] 字段 {} 解析完成: 原始节点={}, Node类型={}, 包装节点={}, 最终结果={}",
                field.getName(), resolvedNodes.size(), nodeCount, wrapperCount, result.size());

        return result;
    }


    /**
     * 解析枚举值
     */
    private static List<EnumRange> parseEnumValues(String[] enumValues) {
        List<EnumRange> enumRanges = new ArrayList<>();

        for (String enumValue : enumValues) {
            String[] parts = enumValue.split(":");
            if (parts.length == 2) {
                EnumRange enumRange = new EnumRange();
                enumRange.setValue(parts[0].trim());
                enumRange.setDesc(parts[1].trim());
                enumRanges.add(enumRange);
            }
        }

        return enumRanges;
    }

    /**
     * 解析嵌套结构实例
     */
    private static void parseNestedStructure(Object container, Object instance) throws CodecException {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();

        // 按order排序
        Arrays.sort(fields, Comparator.comparingInt(field -> {
            ProtocolNode protocolNode = field.getAnnotation(ProtocolNode.class);
            if (protocolNode != null) {
                return protocolNode.order();
            }
            ProtocolHeader protocolHeader = field.getAnnotation(ProtocolHeader.class);
            if (protocolHeader != null) {
                return protocolHeader.order();
            }
            ProtocolBody protocolBody = field.getAnnotation(ProtocolBody.class);
            if (protocolBody != null) {
                return protocolBody.order();
            }
            ProtocolTail protocolTail = field.getAnnotation(ProtocolTail.class);
            if (protocolTail != null) {
                return protocolTail.order();
            }
            // 添加对@ProtocolNodes注解的排序支持
            ProtocolNodes protocolNodes = field.getAnnotation(ProtocolNodes.class);
            if (protocolNodes != null) {
                return protocolNodes.order();
            }
            // 添加对@ProtocolNodes注解的排序支持
            ProtocolNodeGroup protocolNodeGroup = field.getAnnotation(ProtocolNodeGroup.class);
            if (protocolNodeGroup != null) {
                return protocolNodeGroup.order();
            }
            return Integer.MAX_VALUE;
        }));

        List<Node> basicNodes = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(instance);

                if (field.isAnnotationPresent(ProtocolNode.class)) {
                    // 处理基本字段
                    Node node = parseFieldToNode(field, fieldValue);
                    basicNodes.add(node);
                } else if (field.isAnnotationPresent(ProtocolNodes.class)) {
                    // 处理动态节点列表字段
                    log.debug("[协议解析] 发现动态节点列表字段: {}", field.getName());
                    // 处理动态节点列表字段
                    List<Node> dynamicNodes = parseNodesField(field, fieldValue);
                    double lastNodeOrder = 0;
                    if (!basicNodes.isEmpty()) {
                        lastNodeOrder = basicNodes.get(basicNodes.size() - 1).getOrder();
                    }
                    for (Node dynamicNode : dynamicNodes) {
                        dynamicNode.setOrder((float) (lastNodeOrder += 1));
                    }
                    basicNodes.addAll(dynamicNodes);
                } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
                    // 处理动态节点列表字段
                    log.debug("[协议解析] 发现动态节点列表字段: {}", field.getName());
                    // 处理动态节点列表字段
                    List<Node> dynamicNodes = parseNodeGroupField(field, fieldValue);
                    float lastNodeOrder = 0;
                    if (!basicNodes.isEmpty()) {
                        lastNodeOrder = basicNodes.get(basicNodes.size() - 1).getOrder();
                    }
                    for (Node dynamicNode : dynamicNodes) {
                        dynamicNode.setOrder(lastNodeOrder += 1);
                    }
                    basicNodes.addAll(dynamicNodes);
                } else if (field.isAnnotationPresent(ProtocolHeader.class)) {
                    // 处理嵌套协议头
                    Header header = parseHeader(field, fieldValue);
                    if (container instanceof Body) {
                        ((Body) container).setHeader(header);
                    }
                } else if (field.isAnnotationPresent(ProtocolBody.class)) {
                    // 处理嵌套协议体
                    Body body = parseBody(field, fieldValue);
                    if (container instanceof Body) {
                        ((Body) container).setBody(body);
                    }
                } else if (field.isAnnotationPresent(ProtocolTail.class)) {
                    // 处理嵌套协议校验
                    Tail tail = parseCheck(field, fieldValue);
                    if (container instanceof Body) {
                        ((Body) container).setTail(tail);
                    }
                }
            } catch (IllegalAccessException e) {
                log.error("访问字段失败: " + field.getName() + ", 错误: " + e.getMessage());
                throw new CodecException("访问字段失败: " + field.getName(), e);
            }
        }

        // 设置基本节点
        if (container instanceof Body) {
            ((Body) container).setNodes(basicNodes);
        } else if (container instanceof Header) {
            ((Header) container).setNodes(basicNodes);
        } else if (container instanceof Tail) {
            ((Tail) container).setNodes(basicNodes);
        }
    }

    /**
     * 从类型定义解析嵌套结构
     */
    private static void parseNestedStructureFromType(Object container, Class<?> clazz) throws CodecException {
        Field[] fields = clazz.getDeclaredFields();

        // 按order排序
        Arrays.sort(fields, Comparator.comparingInt(field -> {
            ProtocolNode protocolNode = field.getAnnotation(ProtocolNode.class);
            if (protocolNode != null) {
                return protocolNode.order();
            }
            ProtocolHeader protocolHeader = field.getAnnotation(ProtocolHeader.class);
            if (protocolHeader != null) {
                return protocolHeader.order();
            }
            ProtocolBody protocolBody = field.getAnnotation(ProtocolBody.class);
            if (protocolBody != null) {
                return protocolBody.order();
            }
            ProtocolTail protocolTail = field.getAnnotation(ProtocolTail.class);
            if (protocolTail != null) {
                return protocolTail.order();
            }
            // 添加对@ProtocolNodes注解的排序支持
            ProtocolNodes protocolNodes = field.getAnnotation(ProtocolNodes.class);
            if (protocolNodes != null) {
                return protocolNodes.order();
            }
            // 添加对@ProtocolNodes注解的排序支持
            ProtocolNodeGroup protocolNodeGroup = field.getAnnotation(ProtocolNodeGroup.class);
            if (protocolNodeGroup != null) {
                return protocolNodeGroup.order();
            }
            return Integer.MAX_VALUE;
        }));

        List<Node> basicNodes = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(ProtocolNode.class)) {
                // 处理基本字段
                Node node = parseFieldToNode(field, null);
                basicNodes.add(node);
            } else if (field.isAnnotationPresent(ProtocolNodes.class)) {
                // 处理动态节点列表字段（从类型定义）
                log.debug("[协议解析] 发现动态节点列表字段定义: {}", field.getName());
                List<Node> dynamicNodes = parseNodesField(field, null);
                basicNodes.addAll(dynamicNodes);
            } else if (field.isAnnotationPresent(ProtocolNodeGroup.class)) {
                // 处理动态节点列表字段（从类型定义）
                log.debug("[协议解析] 发现动态节点列表字段定义: {}", field.getName());
                List<Node> dynamicNodes = parseNodeGroupField(field, null);
                basicNodes.addAll(dynamicNodes);
            } else if (field.isAnnotationPresent(ProtocolHeader.class)) {
                // 处理嵌套协议头类型
                Header header = parseHeader(field, null);
                if (container instanceof Body) {
                    ((Body) container).setHeader(header);
                }
            } else if (field.isAnnotationPresent(ProtocolBody.class)) {
                // 处理嵌套协议体类型
                Body body = parseBody(field, null);
                if (container instanceof Body) {
                    ((Body) container).setBody(body);
                }
            } else if (field.isAnnotationPresent(ProtocolTail.class)) {
                // 处理嵌套协议校验类型
                Tail tail = parseCheck(field, null);
                if (container instanceof Body) {
                    ((Body) container).setTail(tail);
                }
            }
        }

        // 设置基本节点
        if (container instanceof Body) {
            ((Body) container).setNodes(basicNodes);
        } else if (container instanceof Header) {
            ((Header) container).setNodes(basicNodes);
        } else if (container instanceof Tail) {
            ((Tail) container).setNodes(basicNodes);
        }
    }

    /**
     * 打印协议树形结构
     */
    private static void printProtocolTree(Protocol protocol) {
        String prefix = getIndentPrefix(0);

        if (protocol.getHeader() != null) {
            log.debug("{}├─ Header: {} ({})", prefix, protocol.getHeader().getName(), protocol.getHeader().getId());
            printNodes(protocol.getHeader().getNodes(), 2);
            printNestedStructures(protocol.getHeader(), 2);
        }

        if (protocol.getBody() != null) {
            log.debug("{}├─ Body: {} ({})", prefix, protocol.getBody().getName(), protocol.getBody().getId());
            printNodes(protocol.getBody().getNodes(), 2);
            printNestedStructures(protocol.getBody(), 2);
        }

        if (protocol.getTail() != null) {
            log.debug("{}└─ Check: {} ({})", prefix, protocol.getTail().getName(), protocol.getTail().getId());
            printNodes(protocol.getTail().getNodes(), 2);
            printNestedStructures(protocol.getTail(), 2);
        }
    }

    /**
     * 打印嵌套结构（Header、Body、Check）
     */
    private static void printNestedStructures(Object container, int indent) {
        String prefix = getIndentPrefix(indent);

        if (container instanceof Body) {
            Body body = (Body) container;

            if (body.getHeader() != null) {
                log.debug("{}├─ Nested Header: {} ({})", prefix, body.getHeader().getName(), body.getHeader().getId());
                printNodes(body.getHeader().getNodes(), indent + 2);
                printNestedStructures(body.getHeader(), indent + 2);
            }

            if (body.getBody() != null) {
                log.debug("{}├─ Nested Body: {} ({})", prefix, body.getBody().getName(), body.getBody().getId());
                printNodes(body.getBody().getNodes(), indent + 2);
                printNestedStructures(body.getBody(), indent + 2);
            }

            if (body.getTail() != null) {
                log.debug("{}└─ Nested Check: {} ({})", prefix, body.getTail().getName(), body.getTail().getId());
                printNodes(body.getTail().getNodes(), indent + 2);
                printNestedStructures(body.getTail(), indent + 2);
            }
        }
    }

    /**
     * 生成缩进前缀
     */
    private static String getIndentPrefix(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    /**
     * 打印节点列表
     */
    private static void printNodes(List<Node> nodes, int indent) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        String prefix = getIndentPrefix(indent);
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            boolean isLast = (i == nodes.size() - 1);
            String connector = isLast ? "└─" : "├─";

            // 构建节点信息字符串
            StringBuilder nodeInfo = new StringBuilder();
            nodeInfo.append(prefix).append(connector).append(" Node: ");
            nodeInfo.append(node.getName() != null ? node.getName() : "null");
            nodeInfo.append(" (").append(node.getId() != null ? node.getId() : "null").append(")");
            nodeInfo.append(" - Type: ").append(node.getValueType() != null ? node.getValueType() : "null");
            nodeInfo.append(", Length: ").append(node.getLength());
            nodeInfo.append(", Endian: ").append(node.getEndianType() != null ? node.getEndianType() : "null");
            nodeInfo.append(", Optional: ").append(node.isOptional());
            nodeInfo.append(", Order: ").append(node.getOrder());
            nodeInfo.append(", Charset: ").append(node.getCharset() != null ? node.getCharset() : "null");
            nodeInfo.append(", FwdExpr: ").append(node.getFwdExpr() != null ? node.getFwdExpr() : "null");
            nodeInfo.append(", BwdExpr: ").append(node.getBwdExpr() != null ? node.getBwdExpr() : "null");

            // 如果节点有值，也显示出来
            if (node.getValue() != null) {
                nodeInfo.append(", Value: ").append(node.getValue());
            }

            log.debug(nodeInfo.toString());

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                printNodes(node.getChildren(), indent + 3);
            }
        }
    }
}