package com.iecas.cmd.validator;

import com.googlecode.aviator.AviatorEvaluator;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.*;
import com.iecas.cmd.util.HexHelper;
import com.iecas.cmd.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 协议格式校验器
 * 在指令编制前对协议格式进行全面的校验
 * <p>
 * 校验内容包括：
 * <li>节点值范围检查</li>
 * <li>节点长度验证</li>
 * <li>枚举值验证</li>
 * <li>表达式语法验证</li>
 * <li>依赖关系验证</li>
 * <li>数据类型一致性验证</li>
 * <li>必填字段验证</li>
 * </p>
 */
@Slf4j
public class ProtocolFormatValidator {

    // 存储节点ID与其对应路径的映射关系
    // key: 协议ID:节点ID (String)，value: 节点在协议中的路径 (String)
    // 该映射用于在同一协议内检查节点ID是否重复
    private final Map<String, String> idMap = new HashMap<String, String>();


    public ProtocolFormatValidator() {
    }

    /**
     * 校验协议格式
     *
     * @param protocol 要校验的协议
     * @throws CodecException 校验失败时抛出异常
     */
    public void validateProtocolFormat(Protocol protocol) throws CodecException {
        // 1. 校验协议本身是否配置了ID和名称
        if (protocol == null) {
            throw new CodecException("协议不能为空");
        }
        
        String protocolId = protocol.getId();
        if (protocolId == null || protocolId.trim().isEmpty()) {
            throw new CodecException("协议ID不能为空，协议必须配置ID以支持协议注册和跨协议引用功能");
        }
        
        String protocolName = protocol.getName();
        if (protocolName == null || protocolName.trim().isEmpty()) {
            throw new CodecException("协议名称不能为空，协议必须配置名称以便于识别和调试");
        }
        
        // 2. 校验协议头
        Header header = protocol.getHeader();
        if (header != null) {
            validateStructure(header, protocol.getName() + "." + header.getName(), protocolId);
        }

        // 3. 校验协议体
        Body body = protocol.getBody();
        if (body != null) {
            validateBody(body, protocol.getName() + "." + body.getName(), protocolId);
        }

        // 4. 校验协议校验部分
        Tail tail = protocol.getTail();
        if (tail != null) {
            validateStructure(tail, protocol.getName() + "." + tail.getName(), protocolId);
        }

        // 递归校验所有子节点
        List<Node> children = protocol.getNodes();
        if (children != null && !children.isEmpty()) {
            for (Node child : children) {
                validateNode(child, protocol.getName() + "." + child.getName(), protocolId);
            }
        }
    }

    private void validateBody(Body body, String path, String protocolId) throws CodecException {

        // 2. 校验协议头
        Header header = body.getHeader();
        if (header != null) {
            validateStructure(header, path + "." + header.getName(), protocolId);
        }

        // 3. 校验协议体
        Body childBody = body.getBody();
        if (childBody != null) {
            validateBody(childBody, path + "." + childBody.getName(), protocolId);
        }

        // 4. 校验协议校验部分
        Tail tail = body.getTail();
        if (tail != null) {
            validateStructure(tail, path + "." + tail.getName(), protocolId);
        }

        // 校验结构基本信息
        validateStructureBasicInfo(body, path, protocolId);

        // 递归校验所有子节点
        List<Node> children = body.getChildren();
        if (children != null && !children.isEmpty()) {
            for (Node child : children) {
                validateNode(child, path + "." + child.getName(), protocolId);
            }
        }
    }

    /**
     * 校验协议结构（Header/Body/Check）
     */
    private void validateStructure(INode structure, String path, String protocolId) throws CodecException {
        // 校验结构基本信息
        validateStructureBasicInfo(structure, path, protocolId);

        // 递归校验所有子节点
        List<Node> children = structure.getChildren();
        for (Node child : children) {
            validateNode(child, path + "." + structure.getName() + "." + child.getName(), protocolId);
        }
    }

    /**
     * 校验结构基本信息
     */
    private void validateStructureBasicInfo(INode structure, String path, String protocolId) throws CodecException {
        String structureName = structure.getName();
        String structureId = structure.getId();
        // 校验结构名称
        if (structure.getName() == null || structure.getName().trim().isEmpty()) {
            throw new CodecException(String.format("%s名称不能为空", structure.getName()));
        }

        // 校验结构ID是否在同一协议内重复
        if (structureId != null && !structureId.trim().isEmpty()) {
            String idKey = protocolId + ":" + structureId;
            String pathOne = idMap.get(idKey);
            if (pathOne != null) {
                throw new CodecException("校验不通过，结构体id重复：" + structureId + "," + pathOne + " 和 " + path);
            }
            idMap.put(idKey, path);
        }

        // 校验结构长度（如果指定了）
        if (structure.getLength() < 0) {
            throw new CodecException(String.format("%s长度不能为负数: %d", structureName, structure.getLength()));
        }
    }

    /**
     * 校验单个节点
     */
    private void validateNode(Node node, String nodePath, String protocolId) throws CodecException {

        // 1. 校验节点基本信息
        validateNodeBasicInfo(node, nodePath, protocolId);

        // 2. 校验节点范围是否合法
        validateNodeRangeValid(node, nodePath);

        // 3. 校验节点值
        validateNodeValue(node, nodePath);

        // 4. 校验节点长度
        validateNodeLength(node, nodePath);

        // 5. 校验节点枚举值
        validateNodeEnum(node);

        // 6. 校验节点表达式
        validateNodeExpressions(node, nodePath);

        // 7. 递归校验子节点
        List<Node> children = node.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (Node child : children) {
                validateNode(child, nodePath + "." + node.getName(), protocolId);
            }
        }

    }

    /**
     * 校验节点基本信息
     */
    private void validateNodeBasicInfo(Node node, String nodePath, String protocolId) throws CodecException {
        String nodeName = node.getName();
        String nodeId = node.getId();
        // 校验节点名称
        if (nodeName == null || nodeName.trim().isEmpty()) {
            throw new CodecException(String.format("节点名称不能为空: %s", nodePath));
        }

        // 校验节点ID是否在同一协议内重复
        if (nodeId != null && !nodeId.trim().isEmpty()) {
            String idKey = protocolId + ":" + nodeId;
            String pathOne = idMap.get(idKey);
            if (pathOne != null) {
                throw new CodecException("校验不通过，节点id重复：" + nodeId + "," + pathOne + " 和 " + nodePath);
            }
            idMap.put(idKey, nodePath);
        }

        // 校验节点ID（如果指定了）
        if (nodeId != null && nodeId.trim().isEmpty()) {
            throw new CodecException(String.format("节点ID不能为空字符串: %s", nodePath));
        }

        // 校验值类型
        if (node.getValueType() == null) {
            throw new CodecException(String.format("节点值类型不能为空: %s", nodePath));
        }
    }

    /**
     * 校验节点值
     */
    private void validateNodeValue(Node node, String nodePath) throws CodecException {
        Object value = node.getValue();

        // 如果节点有默认值，校验默认值的格式
        if (value != null) {
            // 校验值是否为有效的枚举值
            if (node.getEnumRanges() != null && !node.getEnumRanges().isEmpty()) {
                try {
                    EnumValidator.validateEnum(value, node);

                    // 校验枚举值的格式是否与节点值类型匹配
                    for (EnumRange enumRange : node.getEnumRanges()) {
                        String enumRangeValue = enumRange.getValue();
                        validateValueFormat(enumRangeValue, node.getValueType(), nodePath);
                    }
                } catch (Exception e) {
                    throw new CodecException(String.format("节点值不是有效枚举值: %s = %s, 错误: %s",
                            nodePath, value, e.getMessage()));
                }
            } else {
                // 根据值类型校验值的格式
                validateValueFormat(value, node.getValueType(), nodePath);
            }

            // 校验值是否在指定范围内
            if (node.getRange() != null && !node.getRange().trim().isEmpty()) {
                try {
                    RangeValidator.validateRange(value, node.getRange(), node.getValueType());
                } catch (Exception e) {
                    throw new CodecException(String.format("节点值超出范围: %s = %s, 范围: %s", nodePath, value, node.getRange()));
                }
            }
        } else {
            if (node.isOptional()) {
                return;
            }
            // 如果节点没有默认值，检查是否为必填字段
            if (node.isStructureNode()) {
                return;
            }
            if (StringUtils.isNotEmpty(node.getFwdExpr())) {
                return;
            }
            if (node.getPaddingConfig() != null) {
                return;
            }
            throw new CodecException(String.format("必填节点缺少值: %s", nodePath));
        }
    }

    /**
     * 校验节点长度
     */
    private void validateNodeLength(Node node, String nodePath) throws CodecException {
        int length = node.getLength();

        // 校验长度是否为正数
        if (length < 0) {
            throw new CodecException(String.format("节点长度必须大于0: %s = %d", nodePath, length));
        }

        // 根据值类型校验长度的合理性
        validateLengthByValueType(length, node.getValueType(), nodePath);

        // 校验长度是否超过最大值
        if (length > 65536) { // 64KB
            throw new CodecException(String.format("节点长度过大: %s = %d 位 (最大支持65536位)", nodePath, length));
        }
    }

    /**
     * 根据值类型校验长度合理性
     */
    private void validateLengthByValueType(int length, ValueType valueType, String nodePath) throws CodecException {
        switch (valueType) {
            case INT:
            case UINT:
                if (length > 64) {
                    throw new CodecException(String.format("整数类型长度不能超过64位: %s = %d 位", nodePath, length));
                }
                break;
            case FLOAT:
                // 浮点数类型长度校验
                if (length != 32 && length != 64) {
                    throw new CodecException(String.format("浮点数类型长度必须是32位或64位: %s = %d 位", nodePath, length));
                }
                break;
            case STRING:
                // 字符串类型长度校验
                if (length % 8 != 0) {
                    throw new CodecException(String.format("字符串类型长度必须是8的倍数: %s = %d 位", nodePath, length));
                }
                break;
            default:
                // 其他类型暂不校验
                break;
        }
    }

    /**
     * 校验节点范围
     */
    private void validateNodeRangeValid(Node node, String nodePath) throws CodecException {
        String range = node.getRange();

        if (range != null && !range.trim().isEmpty()) {
            log.debug("[校验] 校验节点范围: {} = {}", node.getName(), range);

            // 校验范围格式
            validateRangeFormat(range, nodePath);
        }
    }

    /**
     * 校验范围格式
     */
    private void validateRangeFormat(String range, String nodePath) throws CodecException {
        if (RangeValidator.isValidRange(range)) {
            throw new CodecException(String.format("范围格式错误，应为[min,max]格式: %s = %s," +
                    "或者(min,max),或者[min,max] || (min,max) ||[value]", nodePath, range));
        }
    }

    /**
     * 校验节点枚举值
     */
    private void validateNodeEnum(Node node) throws CodecException {
        EnumValidator.validateEnum(node.getValue(), node);
    }

    /**
     * 校验节点表达式
     */
    private void validateNodeExpressions(Node node, String nodePath) throws CodecException {
        // 校验正向表达式
        String fwdExpr = node.getFwdExpr();
        if (fwdExpr != null && !fwdExpr.trim().isEmpty()) {
            validateExpression(fwdExpr, nodePath + ".fwdExpr");
        }

        // 校验反向表达式
        String bwdExpr = node.getBwdExpr();
        if (bwdExpr != null && !bwdExpr.trim().isEmpty()) {
            validateExpression(bwdExpr, nodePath + ".bwdExpr");
        }
    }

    /**
     * 校验表达式
     */
    private void validateExpression(String expression, String nodePath) throws CodecException {
        // 基本语法检查
        if (expression.trim().isEmpty()) {
            throw new CodecException(String.format("表达式不能为空: %s", nodePath));
        }

        AviatorEvaluator.compile(expression);

    }

    /**
     * 校验值格式,每个格式都支持十六进制
     */
    private void validateValueFormat(Object value, ValueType valueType, String nodePath) throws CodecException {
        if (value == null) {
            return;
        }

        String strValue = value.toString();

        switch (valueType) {
            case INT:
                validateIntFormat(strValue, nodePath);
                break;
            case UINT:
                validateUintFormat(strValue, nodePath);
                break;
            case FLOAT:
                validateFloatFormat(strValue, nodePath);
                break;
            case HEX:
                validateHexFormat(strValue, nodePath);
                break;
            case BIT:
                validateBitFormat(strValue, nodePath);
                break;
            case STRING:
                validateStringFormat(strValue, nodePath);
                break;
            case TIME:
                validateTimeFormat(strValue, nodePath);
            default:
                // 其他类型暂不校验
                break;
        }
    }

    private void validateTimeFormat(String strValue, String nodePath) throws CodecException {
        if (HexHelper.isHexString(strValue)) {
            return;
        }
        try {
            LocalDateTime.parse(strValue, TimeUtil.TIME_FORMATTER_YYYY_MM_DD_HH_MM_SS);
        } catch (Exception e) {
            try {
                LocalDateTime.parse(strValue, DateTimeFormatter.ofPattern(TimeUtil.PATTERN_YYYY_MM_DD_HH_MM_SS_SSS));
            } catch (Exception e2) {
                throw new CodecException(String.format("时间格式错误，应为yyyy-MM-dd HH:mm:ss或yyyy-MM-dd HH:mm:ss.SSS 格式 : %s = %s", nodePath, strValue));
            }
        }
    }

    /**
     * 校验整数格式
     */
    private void validateIntFormat(String value, String nodePath) throws CodecException {
        if (HexHelper.isHexString(value)) {
            try {
                value = HexHelper.normalizeHexString(value);
                Long.parseLong(value, 16);
                return;
            } catch (NumberFormatException e) {
                throw new CodecException(String.format("整数格式错误: %s = %s", nodePath, value));
            }
        }

        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                throw new CodecException(String.format("整数格式错误: %s = %s", nodePath, value));
            }
        }
    }

    /**
     * 校验无符号整数格式
     */
    private void validateUintFormat(String value, String nodePath) throws CodecException {
        if (HexHelper.isHexString(value)) {
            try {
                value = HexHelper.normalizeHexString(value);
                if (Long.parseLong(value, 16) < 0) {
                    throw new CodecException(String.format("无符号整数不能为负数: %s = %s", nodePath, value));
                }
                return;
            } catch (NumberFormatException e) {
                throw new CodecException(String.format("整数格式错误: %s = %s", nodePath, value));
            }
        }
        try {
            if (Long.parseLong(value) < 0) {
                throw new CodecException(String.format("无符号整数不能为负数: %s = %s", nodePath, value));
            }
        } catch (NumberFormatException e) {
            try {
                if (Double.parseDouble(value) < 0) {
                    throw new CodecException(String.format("无符号整数不能为负数: %s = %s", nodePath, value));
                }
            } catch (NumberFormatException e2) {
                throw new CodecException(String.format("整数格式错误: %s = %s", nodePath, value));
            }
        }
    }

    /**
     * 校验浮点数格式
     */
    private void validateFloatFormat(String value, String nodePath) throws CodecException {
        if (HexHelper.isHexString(value) && !HexHelper.isHexFloat(value)) {
            throw new CodecException(String.format("十六进制浮点数格式错误: %s = %s", nodePath, value));
        }
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new CodecException(String.format("浮点数格式错误: %s = %s", nodePath, value));
        }
    }

    /**
     * 校验十六进制格式
     */
    private void validateHexFormat(String value, String nodePath) throws CodecException {
        if (!HexHelper.isHexString(value)) {
            throw new CodecException(String.format("十六进制格式错误: %s = %s", nodePath, value));
        }
    }

    /**
     * 校验二进制格式
     */
    private void validateBitFormat(String value, String nodePath) throws CodecException {
        if (HexHelper.isHexString(value)) {
            return;
        }
        String cleanValue = value.startsWith("0b") ? value.substring(2) : value;

        if (!cleanValue.matches("[01]+")) {
            throw new CodecException(String.format("二进制格式错误: %s = %s", nodePath, value));
        }
    }

    /**
     * 校验字符串格式
     */
    private void validateStringFormat(String value, String nodePath) throws CodecException {
        if (HexHelper.isHexString(value)) {
            return;
        }
        // 字符串格式校验相对简单，主要是检查是否为null
        if (value == null) {
            throw new CodecException(String.format("字符串值不能为null: %s", nodePath));
        }
    }

    /**
     * 解析长整数值
     */
    private long parseLong(String value) {
        if (value.startsWith("0x")) {
            return Long.parseLong(value.substring(2), 16);
        } else if (value.startsWith("0b")) {
            return Long.parseLong(value.substring(2), 2);
        } else {
            return Long.parseLong(value);
        }
    }

    /**
     * 解析十六进制值
     */
    private long parseHex(String value) {
        String cleanValue = value.startsWith("0x") ? value.substring(2) : value;
        return Long.parseLong(cleanValue, 16);
    }

    /**
     * 解析二进制值
     */
    private long parseBinary(String value) {
        String cleanValue = value.startsWith("0b") ? value.substring(2) : value;
        return Long.parseLong(cleanValue, 2);
    }
}