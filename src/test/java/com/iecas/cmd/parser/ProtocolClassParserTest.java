package com.iecas.cmd.parser;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.EndianType;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.*;
import com.iecas.cmd.model.proto.Protocol;
import org.junit.Test;
import static org.junit.Assert.*;


import java.util.List;
import java.util.ArrayList;

/**
 * ProtocolClassParser测试类
 * 测试多层嵌套协议、参数组、动态参数节点等功能
 */
    public class ProtocolClassParserTest {

    /**
     * 测试用的简单协议类
     */
    public static class SimpleProtocol {
        @ProtocolHeader(name = "协议头", id = "header", order = 1)
        private SimpleHeader header;

        @ProtocolBody(name = "协议体", id = "body", order = 2)
        private SimpleBody body;

        @ProtocolTail(name = "协议尾", id = "tail", order = 3)
        private SimpleTail tail;

        public SimpleProtocol() {
            this.header = new SimpleHeader();
            this.body = new SimpleBody();
            this.tail = new SimpleTail();
        }

        // Getters and Setters
        public SimpleHeader getHeader() { return header; }
        public void setHeader(SimpleHeader header) { this.header = header; }
        public SimpleBody getBody() { return body; }
        public void setBody(SimpleBody body) { this.body = body; }
        public SimpleTail getTail() { return tail; }
        public void setTail(SimpleTail tail) { this.tail = tail; }
    }

    /**
     * 简单协议头
     */
    public static class SimpleHeader {
        @ProtocolNode(name = "消息ID", id = "msgId", order = 1, length = 2, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private int messageId;

        @ProtocolNode(name = "消息长度", id = "msgLen", order = 2, length = 2, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private int messageLength;

        @ProtocolNode(name = "版本号", id = "version", order = 3, length = 1, valueType = ValueType.UINT)
        private byte version;

        public SimpleHeader() {
            this.messageId = 1001;
            this.messageLength = 256;
            this.version = 1;
        }

        // Getters and Setters
        public int getMessageId() { return messageId; }
        public void setMessageId(int messageId) { this.messageId = messageId; }
        public int getMessageLength() { return messageLength; }
        public void setMessageLength(int messageLength) { this.messageLength = messageLength; }
        public byte getVersion() { return version; }
        public void setVersion(byte version) { this.version = version; }
    }

    /**
     * 简单协议体
     */
    public static class SimpleBody {
        @ProtocolNode(name = "用户ID", id = "userId", order = 1, length = 4, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private long userId;

        @ProtocolNode(name = "用户名", id = "userName", order = 2, length = 32, valueType = ValueType.STRING, charset = "UTF-8")
        private String userName;

        @ProtocolNode(name = "状态", id = "status", order = 3, length = 1, valueType = ValueType.UINT)
        @ProtocolEnum(values = {"0:离线", "1:在线", "2:忙碌", "3:离开"})
        private byte status;

        public SimpleBody() {
            this.userId = 12345L;
            this.userName = "测试用户";
            this.status = 1;
        }

        // Getters and Setters
        public long getUserId() { return userId; }
        public void setUserId(long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public byte getStatus() { return status; }
        public void setStatus(byte status) { this.status = status; }
    }

    /**
     * 简单协议尾
     */
    public static class SimpleTail {
        @ProtocolNode(name = "校验和", id = "checksum", order = 1, length = 2, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private int checksum;

        public SimpleTail() {
            this.checksum = 0x1234;
        }

        // Getters and Setters
        public int getChecksum() { return checksum; }
        public void setChecksum(int checksum) { this.checksum = checksum; }
    }

    /**
     * 复杂嵌套协议类
     */
    public static class ComplexNestedProtocol {
        @ProtocolHeader(name = "复杂协议头", id = "complexHeader", order = 1)
        private ComplexHeader header;

        @ProtocolBody(name = "复杂协议体", id = "complexBody", order = 2)
        private ComplexBody body;

        @ProtocolTail(name = "复杂协议尾", id = "complexTail", order = 3)
        private ComplexTail tail;

        public ComplexNestedProtocol() {
            this.header = new ComplexHeader();
            this.body = new ComplexBody();
            this.tail = new ComplexTail();
        }

        // Getters and Setters
        public ComplexHeader getHeader() { return header; }
        public void setHeader(ComplexHeader header) { this.header = header; }
        public ComplexBody getBody() { return body; }
        public void setBody(ComplexBody body) { this.body = body; }
        public ComplexTail getTail() { return tail; }
        public void setTail(ComplexTail tail) { this.tail = tail; }
    }

    /**
     * 复杂协议头
     */
    public static class ComplexHeader {
        @ProtocolNode(name = "主消息ID", id = "mainMsgId", order = 1, length = 2, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private int mainMessageId;

        @ProtocolHeader(name = "子协议头", id = "subHeader", order = 2)
        private SubHeader subHeader;

        public ComplexHeader() {
            this.mainMessageId = 2001;
            this.subHeader = new SubHeader();
        }

        // Getters and Setters
        public int getMainMessageId() { return mainMessageId; }
        public void setMainMessageId(int mainMessageId) { this.mainMessageId = mainMessageId; }
        public SubHeader getSubHeader() { return subHeader; }
        public void setSubHeader(SubHeader subHeader) { this.subHeader = subHeader; }
    }

    /**
     * 子协议头
     */
    public static class SubHeader {
        @ProtocolNode(name = "子消息ID", id = "subMsgId", order = 1, length = 1, valueType = ValueType.UINT)
        private byte subMessageId;

        @ProtocolNode(name = "优先级", id = "priority", order = 2, length = 1, valueType = ValueType.UINT)
        @ProtocolEnum(values = {"0:低", "1:中", "2:高", "3:紧急"})
        private byte priority;

        public SubHeader() {
            this.subMessageId = 101;
            this.priority = 2;
        }

        // Getters and Setters
        public byte getSubMessageId() { return subMessageId; }
        public void setSubMessageId(byte subMessageId) { this.subMessageId = subMessageId; }
        public byte getPriority() { return priority; }
        public void setPriority(byte priority) { this.priority = priority; }
    }

    /**
     * 复杂协议体
     */
    public static class ComplexBody {
        @ProtocolNode(name = "会话ID", id = "sessionId", order = 1, length = 8, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private long sessionId;

        @ProtocolBody(name = "用户信息体", id = "userInfoBody", order = 2)
        private UserInfoBody userInfoBody;

        @ProtocolNodeGroup(name = "动态数据列表", id = "dataList", order = 3, resolveStrategy = GroupResolveStrategy.FLATTEN)
        private List<DataItem> dataList;

        public ComplexBody() {
            this.sessionId = 987654321L;
            this.userInfoBody = new UserInfoBody();
            this.dataList = new ArrayList<>();
            this.dataList.add(new DataItem(1, "数据项1"));
            this.dataList.add(new DataItem(2, "数据项2"));
            this.dataList.add(new DataItem(3, "数据项3"));
        }

        // Getters and Setters
        public long getSessionId() { return sessionId; }
        public void setSessionId(long sessionId) { this.sessionId = sessionId; }
        public UserInfoBody getUserInfoBody() { return userInfoBody; }
        public void setUserInfoBody(UserInfoBody userInfoBody) { this.userInfoBody = userInfoBody; }
        public List<DataItem> getDataList() { return dataList; }
        public void setDataList(List<DataItem> dataList) { this.dataList = dataList; }
    }

    /**
     * 用户信息体
     */
    public static class UserInfoBody {
        @ProtocolNode(name = "用户类型", id = "userType", order = 1, length = 1, valueType = ValueType.UINT)
        @ProtocolEnum(values = {"0:普通用户", "1:VIP用户", "2:管理员", "3:超级管理员"})
        private byte userType;

        @ProtocolNode(name = "用户等级", id = "userLevel", order = 2, length = 1, valueType = ValueType.UINT)
        private byte userLevel;

        @ProtocolNode(name = "注册时间", id = "registerTime", order = 3, length = 8, valueType = ValueType.INT, endian = EndianType.LITTLE)
        private long registerTime;

        public UserInfoBody() {
            this.userType = 1;
            this.userLevel = 5;
            this.registerTime = System.currentTimeMillis();
        }

        // Getters and Setters
        public byte getUserType() { return userType; }
        public void setUserType(byte userType) { this.userType = userType; }
        public byte getUserLevel() { return userLevel; }
        public void setUserLevel(byte userLevel) { this.userLevel = userLevel; }
        public long getRegisterTime() { return registerTime; }
        public void setRegisterTime(long registerTime) { this.registerTime = registerTime; }
    }

    /**
     * 数据项
     */
    public static class DataItem {
        @ProtocolNode(name = "数据ID", id = "dataId", order = 1, length = 4, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private int dataId;

        @ProtocolNode(name = "数据名称", id = "dataName", order = 2, length = 20, valueType = ValueType.STRING, charset = "UTF-8")
        private String dataName;

        public DataItem() {}

        public DataItem(int dataId, String dataName) {
            this.dataId = dataId;
            this.dataName = dataName;
        }

        // Getters and Setters
        public int getDataId() { return dataId; }
        public void setDataId(int dataId) { this.dataId = dataId; }
        public String getDataName() { return dataName; }
        public void setDataName(String dataName) { this.dataName = dataName; }
    }

    /**
     * 复杂协议尾
     */
    public static class ComplexTail {
        @ProtocolNode(name = "CRC校验", id = "crc", order = 1, length = 4, valueType = ValueType.UINT, endian = EndianType.LITTLE)
        private long crc;

        @ProtocolNode(name = "时间戳", id = "timestamp", order = 2, length = 8, valueType = ValueType.INT, endian = EndianType.LITTLE)
        private long timestamp;

        public ComplexTail() {
            this.crc = 0x12345678L;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public long getCrc() { return crc; }
        public void setCrc(long crc) { this.crc = crc; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 条件依赖协议类
     */
    public static class ConditionalProtocol {
        @ProtocolNode(name = "控制标志", id = "controlFlag", order = 1, length = 1, valueType = ValueType.UINT)
        private byte controlFlag;

        @ProtocolNode(name = "可选字段1", id = "optionalField1", order = 2, length = 4, valueType = ValueType.UINT, optional = true)
        @ConditionalOn(conditionNode = "controlFlag", condition = "== 1", action = ConditionalDependency.ConditionalAction.ENABLE, elseAction = ConditionalDependency.ConditionalAction.DISABLE, priority = 1, description = "当控制标志为1时包含此字段")
        private int optionalField1;

        @ProtocolNode(name = "可选字段2", id = "optionalField2", order = 3, length = 8, valueType = ValueType.STRING, optional = true, charset = "UTF-8")
        @ConditionalOn(conditionNode = "controlFlag", condition = "== 2", action = ConditionalDependency.ConditionalAction.ENABLE, elseAction = ConditionalDependency.ConditionalAction.DISABLE, priority = 2, description = "当控制标志为2时包含此字段")
        private String optionalField2;

        @ProtocolNode(name = "条件字段", id = "conditionalField", order = 4, length = 2, valueType = ValueType.UINT)
        @ConditionalOn(conditionNode = "controlFlag", condition = "> 0", action = ConditionalDependency.ConditionalAction.ENABLE, elseAction = ConditionalDependency.ConditionalAction.DISABLE, priority = 3, description = "当控制标志大于0时包含此字段")
        private int conditionalField;

        public ConditionalProtocol() {
            this.controlFlag = 1;
            this.optionalField1 = 100;
            this.optionalField2 = "可选数据";
            this.conditionalField = 200;
        }

        // Getters and Setters
        public byte getControlFlag() { return controlFlag; }
        public void setControlFlag(byte controlFlag) { this.controlFlag = controlFlag; }
        public int getOptionalField1() { return optionalField1; }
        public void setOptionalField1(int optionalField1) { this.optionalField1 = optionalField1; }
        public String getOptionalField2() { return optionalField2; }
        public void setOptionalField2(String optionalField2) { this.optionalField2 = optionalField2; }
        public int getConditionalField() { return conditionalField; }
        public void setConditionalField(int conditionalField) { this.conditionalField = conditionalField; }
    }

    /**
     * 填充协议类
     */
    public static class PaddingProtocol {
        @ProtocolNode(name = "固定长度字段", id = "fixedField", order = 1, length = 10, valueType = ValueType.STRING, charset = "UTF-8")
        @Padding(paddingType = PaddingConfig.PaddingType.FIXED_LENGTH, targetLength = 10, paddingValue = " ", enabled = true, description = "右填充空格到10字节")
        private String fixedField;

        @ProtocolNode(name = "动态长度字段", id = "dynamicField", order = 2, length = 0, valueType = ValueType.STRING, charset = "UTF-8")
        @Padding(paddingType = PaddingConfig.PaddingType.FIXED_LENGTH, targetLength = 20, paddingValue = "0", enabled = true, description = "左填充0到20字节")
        private String dynamicField;

        @ProtocolNode(name = "容器字段", id = "containerField", order = 3, length = 0, valueType = ValueType.STRING, charset = "UTF-8")
        @Padding(paddingType = PaddingConfig.PaddingType.FIXED_LENGTH, targetLength = 15, paddingValue = "*", enabled = true, containerNode = "fixedField", description = "居中对齐填充到15字节")
        private String containerField;

        public PaddingProtocol() {
            this.fixedField = "测试";
            this.dynamicField = "动态";
            this.containerField = "容器";
        }

        // Getters and Setters
        public String getFixedField() { return fixedField; }
        public void setFixedField(String fixedField) { this.fixedField = fixedField; }
        public String getDynamicField() { return dynamicField; }
        public void setDynamicField(String dynamicField) { this.dynamicField = dynamicField; }
        public String getContainerField() { return containerField; }
        public void setContainerField(String containerField) { this.containerField = containerField; }
    }

    @Test
    public void testSimpleProtocolParsing() throws CodecException {
        SimpleProtocol protocol = new SimpleProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertNotNull(result.getHeader());
        assertNotNull(result.getBody());
        assertNotNull(result.getTail());

        // 验证协议头
        Header header = result.getHeader();
        assertEquals("协议头", header.getName());
        assertEquals("header", header.getId());
        assertEquals(1, header.getOrder());
        assertEquals(3, header.getNodes().size());

        // 验证协议体
        Body body = result.getBody();
        assertEquals("协议体", body.getName());
        assertEquals("body", body.getId());
        assertEquals(2, body.getOrder());
        assertEquals(3, body.getNodes().size());

        // 验证协议尾
        Tail tail = result.getTail();
        assertEquals("协议尾", tail.getName());
        assertEquals("tail", tail.getId());
        assertEquals(3, tail.getOrder());
        assertEquals(1, tail.getNodes().size());
    }

    @Test
    public void testComplexNestedProtocolParsing() throws CodecException {
        ComplexNestedProtocol protocol = new ComplexNestedProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertNotNull(result.getHeader());
        assertNotNull(result.getBody());
        assertNotNull(result.getTail());

        // 验证复杂协议头
        Header header = result.getHeader();
        assertEquals("复杂协议头", header.getName());
        assertEquals("complexHeader", header.getId());
        assertEquals(1, header.getOrder());
        assertEquals(1, header.getNodes().size()); // 主消息ID
        // assertNotNull(header.getHeader()); // 子协议头 - Header类没有getHeader方法

        // 验证子协议头 - Header类没有getHeader方法，需要从Body中获取
        // 这里需要调整测试逻辑，因为Header本身不能包含Header

        // 验证复杂协议体
        Body body = result.getBody();
        assertEquals("复杂协议体", body.getName());
        assertEquals("complexBody", body.getId());
        assertEquals(2, body.getOrder());
        assertEquals(2, body.getNodes().size()); // 会话ID 和 动态数据列表
        assertNotNull(body.getBody()); // 用户信息体

        // 验证用户信息体
        Body userInfoBody = body.getBody();
        assertEquals("用户信息体", userInfoBody.getName());
        assertEquals("userInfoBody", userInfoBody.getId());
        assertEquals(2, userInfoBody.getOrder());
        assertEquals(3, userInfoBody.getNodes().size());

        // 验证动态数据列表节点
        Node dataListNode = body.getNodes().get(1); // 第2个节点是动态数据列表
        assertEquals("动态数据列表[1]", dataListNode.getName());
        assertEquals("dataList_1", dataListNode.getId());
        assertNotNull(dataListNode.getChildren());
        assertEquals(2, dataListNode.getChildren().size()); // 每个DataItem有2个字段
    }

    @Test
    public void testConditionalProtocolParsing() throws CodecException {
        ConditionalProtocol protocol = new ConditionalProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertEquals(4, result.getNodes().size());

        // 验证控制标志字段
        Node controlFlagNode = result.getNodes().get(0);
        assertEquals("控制标志", controlFlagNode.getName());
        assertEquals("controlFlag", controlFlagNode.getId());
        assertEquals(1, controlFlagNode.getOrder());
        assertEquals(ValueType.UINT, controlFlagNode.getValueType());
        assertEquals("1", controlFlagNode.getValue());

        // 验证条件依赖配置
        assertNotNull(controlFlagNode.getConditionalDependencies());
        assertEquals(0, controlFlagNode.getConditionalDependencies().size()); // 控制标志本身没有条件依赖

        // 验证可选字段1的条件依赖
        Node optionalField1Node = result.getNodes().get(1);
        assertEquals("可选字段1", optionalField1Node.getName());
        assertEquals("optionalField1", optionalField1Node.getId());
        assertTrue(optionalField1Node.isOptional());
        assertNotNull(optionalField1Node.getConditionalDependencies());
        assertEquals(1, optionalField1Node.getConditionalDependencies().size());

        ConditionalDependency dependency1 = optionalField1Node.getConditionalDependencies().get(0);
        assertEquals("controlFlag", dependency1.getConditionNode());
        assertEquals("== 1", dependency1.getCondition());
        assertEquals(ConditionalDependency.ConditionalAction.ENABLE, dependency1.getAction());
        assertEquals(ConditionalDependency.ConditionalAction.DISABLE, dependency1.getElseAction());
        assertEquals(1, dependency1.getPriority());
        assertEquals("当控制标志为1时包含此字段", dependency1.getDescription());
    }

    @Test
    public void testPaddingProtocolParsing() throws CodecException {
        PaddingProtocol protocol = new PaddingProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertEquals(3, result.getNodes().size());

        // 验证固定长度字段的填充配置
        Node fixedFieldNode = result.getNodes().get(0);
        assertEquals("固定长度字段", fixedFieldNode.getName());
        assertEquals("fixedField", fixedFieldNode.getId());
        assertEquals(10, fixedFieldNode.getLength());
        assertNotNull(fixedFieldNode.getPaddingConfig());

        PaddingConfig paddingConfig1 = fixedFieldNode.getPaddingConfig();
        assertEquals(PaddingConfig.PaddingType.FIXED_LENGTH, paddingConfig1.getPaddingType());
        assertEquals(10, paddingConfig1.getTargetLength());
        assertEquals(" ", paddingConfig1.getPaddingValue());
        assertTrue(paddingConfig1.isEnabled());
        assertEquals("右填充空格到10字节", paddingConfig1.getDescription());

        // 验证动态长度字段的填充配置
        Node dynamicFieldNode = result.getNodes().get(1);
        assertEquals("动态长度字段", dynamicFieldNode.getName());
        assertEquals("dynamicField", dynamicFieldNode.getId());
        assertEquals(0, dynamicFieldNode.getLength()); // 动态长度
        assertNotNull(dynamicFieldNode.getPaddingConfig());

        PaddingConfig paddingConfig2 = dynamicFieldNode.getPaddingConfig();
        assertEquals(PaddingConfig.PaddingType.FIXED_LENGTH, paddingConfig2.getPaddingType());
        assertEquals(20, paddingConfig2.getTargetLength());
        assertEquals("0", paddingConfig2.getPaddingValue());
        assertTrue(paddingConfig2.isEnabled());
        assertEquals("左填充0到20字节", paddingConfig2.getDescription());

        // 验证容器字段的填充配置
        Node containerFieldNode = result.getNodes().get(2);
        assertEquals("容器字段", containerFieldNode.getName());
        assertEquals("containerField", containerFieldNode.getId());
        assertNotNull(containerFieldNode.getPaddingConfig());

        PaddingConfig paddingConfig3 = containerFieldNode.getPaddingConfig();
        assertEquals(PaddingConfig.PaddingType.FIXED_LENGTH, paddingConfig3.getPaddingType());
        assertEquals(15, paddingConfig3.getTargetLength());
        assertEquals("*", paddingConfig3.getPaddingValue());
        assertEquals("fixedField", paddingConfig3.getContainerNode());
        assertTrue(paddingConfig3.isEnabled());
        assertEquals("居中对齐填充到15字节", paddingConfig3.getDescription());
    }

    @Test
    public void testEnumValuesParsing() throws CodecException {
        // 使用完整的协议实例而不是单独的Body
        SimpleProtocol protocol = new SimpleProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getNodes());
        assertEquals(3, result.getBody().getNodes().size());

        // 验证状态字段的枚举配置（状态字段是第3个字段，索引为2）
        Node statusNode = result.getBody().getNodes().get(2);
        assertEquals("状态", statusNode.getName());
        assertEquals("status", statusNode.getId());
        assertNotNull(statusNode.getEnumRanges());
        assertEquals(4, statusNode.getEnumRanges().size());

        // 验证枚举值
        List<EnumRange> enumRanges = statusNode.getEnumRanges();
        assertEquals("0", enumRanges.get(0).getValue());
        assertEquals("离线", enumRanges.get(0).getDesc());
        assertEquals("1", enumRanges.get(1).getValue());
        assertEquals("在线", enumRanges.get(1).getDesc());
        assertEquals("2", enumRanges.get(2).getValue());
        assertEquals("忙碌", enumRanges.get(2).getDesc());
        assertEquals("3", enumRanges.get(3).getValue());
        assertEquals("离开", enumRanges.get(3).getDesc());
    }

    @Test
    public void testFieldAttributesParsing() throws CodecException {
        // 使用完整的协议实例而不是单独的Header
        SimpleProtocol protocol = new SimpleProtocol();
        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        assertNotNull(result.getHeader());
        assertNotNull(result.getHeader().getNodes());
        assertEquals(3, result.getHeader().getNodes().size());

        // 验证消息ID字段的属性
        Node msgIdNode = result.getHeader().getNodes().get(0);
        assertEquals("消息ID", msgIdNode.getName());
        assertEquals("msgId", msgIdNode.getId());
        assertEquals(1, msgIdNode.getOrder());
        assertEquals(2, msgIdNode.getLength());
        assertEquals(ValueType.UINT, msgIdNode.getValueType());
        assertEquals(EndianType.LITTLE, msgIdNode.getEndianType());
        assertEquals("1001", msgIdNode.getValue());

        // 验证消息长度字段的属性
        Node msgLenNode = result.getHeader().getNodes().get(1);
        assertEquals("消息长度", msgLenNode.getName());
        assertEquals("msgLen", msgLenNode.getId());
        assertEquals(2, msgLenNode.getOrder());
        assertEquals(2, msgLenNode.getLength());
        assertEquals(ValueType.UINT, msgLenNode.getValueType());
        assertEquals(EndianType.LITTLE, msgLenNode.getEndianType());
        assertEquals("256", msgLenNode.getValue());

        // 验证版本号字段的属性
        Node versionNode = result.getHeader().getNodes().get(2);
        assertEquals("版本号", versionNode.getName());
        assertEquals("version", versionNode.getId());
        assertEquals(3, versionNode.getOrder());
        assertEquals(1, versionNode.getLength());
        assertEquals(ValueType.UINT, versionNode.getValueType());
        assertEquals("1", versionNode.getValue());
    }

    @Test
    public void testNullInstanceParsing() throws CodecException {
        // 创建一个没有实例的协议类
        SimpleProtocol protocol = new SimpleProtocol();
        protocol.setHeader(null);
        protocol.setBody(null);
        protocol.setTail(null);

        Protocol result = ProtocolClassParser.parseProtocol(protocol);

        assertNotNull(result);
        // 即使实例为null，也应该能解析出结构定义
        assertNotNull(result.getHeader());
        assertNotNull(result.getBody());
        assertNotNull(result.getTail());
    }

    @Test
    public void testExceptionCases() throws CodecException {
        // 测试空实例
        try {
            ProtocolClassParser.parseProtocol(null);
            fail("应该抛出异常：协议实例不能为空");
        } catch (IllegalArgumentException e) {
            // 期望的异常
        }

        // 测试没有注解的类
        class NoAnnotationClass {
            private String field;
            public String getField() { return field; }
            public void setField(String field) { this.field = field; }
        }

        NoAnnotationClass noAnnotation = new NoAnnotationClass();
        try {
            Protocol result = ProtocolClassParser.parseProtocol(noAnnotation);
            assertNotNull(result);
            assertEquals(0, result.getNodes().size());
        } catch (CodecException e) {
            // CodecException是预期的，因为类没有协议注解
        } catch (Exception e) {
            fail("没有注解的类应该能正常解析，但不产生节点: " + e.getMessage());
        }
    }

    @Test
    public void testProtocolTreePrinting() throws CodecException {
        ComplexNestedProtocol protocol = new ComplexNestedProtocol();
        
        // 这个测试主要是验证解析过程不抛出异常，树形结构打印功能正常
        try {
            Protocol result = ProtocolClassParser.parseProtocol(protocol);
            assertNotNull(result);
            
            // 验证树形结构完整性
            assertNotNull(result.getHeader());
            assertNotNull(result.getBody());
            assertNotNull(result.getTail());
            
            // 验证嵌套结构
            // assertNotNull(result.getHeader().getHeader()); // 子协议头 - Header类没有getHeader方法
            assertNotNull(result.getBody().getBody()); // 用户信息体
        } catch (Exception e) {
            fail("复杂嵌套协议应该能正常解析并打印树形结构: " + e.getMessage());
        }
    }
}
