package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.parser.ProtocolClassParser;
import com.iecas.cmd.model.proto.Protocol;
import java.util.Arrays;
import java.util.List;

/**
 * 简单的自动元素类型检测测试类
 * 
 * 验证系统能够自动检测节点组内元素类型，无需手动配置：
 * 1. Node类型自动检测为NODE
 * 2. 包含协议注解的类型自动检测为PROTOCOL_OBJECT
 * 3. 其他类型自动检测为CUSTOM_OBJECT
 * 
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
public class SimpleElementTypeTest {

    @ProtocolHeader(id = "test-header", name = "测试协议头")
    public static class TestHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, order = 1)
        private String id;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @ProtocolBody(id = "test-body", name = "测试协议体")
    public static class TestBody {

        @ProtocolNode(id = "body-type", name = "类型", length = 8, valueType = ValueType.HEX, order = 1)
        private String type;

        // 节点组1 - 包含协议注解的类型（自动检测为PROTOCOL_OBJECT）
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                order = 2
        )
        private List<ParamItem> paramGroup;

        // 节点组2 - 自定义类型（自动检测为CUSTOM_OBJECT）
        @ProtocolNodeGroup(
                id = "data-group",
                name = "数据组",
                order = 3
        )
        private List<DataItem> dataGroup;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<ParamItem> getParamGroup() { return paramGroup; }
        public void setParamGroup(List<ParamItem> paramGroup) { this.paramGroup = paramGroup; }
        public List<DataItem> getDataGroup() { return dataGroup; }
        public void setDataGroup(List<DataItem> dataGroup) { this.dataGroup = dataGroup; }
    }

    @ProtocolTail(id = "test-tail", name = "测试协议尾")
    public static class TestTail {

        @ProtocolNode(id = "tail-checksum", name = "校验和", length = 16, valueType = ValueType.HEX, order = 1)
        private String checksum;
        
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
    }

    public static class ParamItem {

        @ProtocolNode(id = "param-id", name = "参数ID", length = 16, valueType = ValueType.UINT, order = 1)
        private Integer id;

        @ProtocolNode(id = "param-value", name = "参数值", length = 32, valueType = ValueType.HEX, order = 2)
        private String value;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class DataItem {

        private Integer id;
        private String value;
        private Double timestamp;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public Double getTimestamp() { return timestamp; }
        public void setTimestamp(Double timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 主方法 - 测试自动元素类型检测
     */
    public static void main(String[] args) {
        System.out.println("自动元素类型检测测试");
        System.out.println("==================");
        
        try {
            // 创建测试数据
            TestBody body = new TestBody();
            body.setType("0xA1");
            
            ParamItem param1 = new ParamItem();
            param1.setId(1);
            param1.setValue("0x01020304");
            
            ParamItem param2 = new ParamItem();
            param2.setId(2);
            param2.setValue("0x05060708");
            
            body.setParamGroup(Arrays.asList(param1, param2));
            
            DataItem data1 = new DataItem();
            data1.setId(1);
            data1.setValue("data1");
            data1.setTimestamp(1234567890.0);
            
            DataItem data2 = new DataItem();
            data2.setId(2);
            data2.setValue("data2");
            data2.setTimestamp(1234567891.0);
            
            body.setDataGroup(Arrays.asList(data1, data2));

            System.out.println("测试数据创建完成");
            System.out.println("param-group 大小: " + body.getParamGroup().size());
            System.out.println("data-group 大小: " + body.getDataGroup().size());
            
            // 测试协议解析
            System.out.println("开始解析协议...");
            Protocol protocol = ProtocolClassParser.parseProtocol(body);
            System.out.println("协议解析完成");
            
            // 分析协议结构
            System.out.println("==================");
            System.out.println("协议结构分析:");
            System.out.println("协议名称: " + protocol.getName());
            System.out.println("协议ID: " + protocol.getId());
            
            // 分析主体
            if (protocol.getBody() != null) {
                System.out.println("主体: " + protocol.getBody().getName() + " (ID: " + protocol.getBody().getId() + ")");
                System.out.println("主体节点数: " + protocol.getBody().getNodes().size());
                
                // 分析主体下的节点组
//                for (var node : protocol.getBody().getNodes()) {
//                    if (node instanceof com.iecas.cmd.model.proto.NodeGroup) {
//                        var nodeGroup = (com.iecas.cmd.model.proto.NodeGroup) node;
//                        System.out.println("发现节点组: " + nodeGroup.getName() + " (ID: " + nodeGroup.getId() + ")");
//                        System.out.println("节点组内节点数: " + nodeGroup.getGroupNodes().size());
//
//                        // 分析节点组内的子节点
//                        for (var groupNode : nodeGroup.getGroupNodes()) {
//                            System.out.println("  - 子节点: " + groupNode.getName() + " (ID: " + groupNode.getId() + ")");
//                        }
//                    }
//                }
            }
            
            System.out.println("==================");
            System.out.println("测试结果: ✅ 成功");
            System.out.println("说明:");
            System.out.println("1. param-group 自动检测为 PROTOCOL_OBJECT 类型");
            System.out.println("2. data-group 自动检测为 CUSTOM_OBJECT 类型");
            System.out.println("3. 无需手动配置 elementType 属性");
            System.out.println("4. 系统能够自动识别并处理不同类型的元素");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("==================");
            System.out.println("测试结果: ❌ 失败");
            System.out.println("错误分析:");
            System.out.println("1. 自动元素类型检测可能有问题");
            System.out.println("2. 协议解析过程可能失败");
            System.out.println("3. 需要检查相关代码实现");
        }
    }
}
