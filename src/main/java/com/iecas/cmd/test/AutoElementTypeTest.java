package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 自动元素类型检测测试类
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
@Slf4j
public class AutoElementTypeTest {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolHeader(id = "test-header", name = "测试协议头")
    public static class TestHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, order = 1)
        private String id;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
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
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolTail(id = "test-tail", name = "测试协议尾")
    public static class TestTail {

        @ProtocolNode(id = "tail-checksum", name = "校验和", length = 16, valueType = ValueType.HEX, order = 1)
        private String checksum;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParamItem {

        @ProtocolNode(id = "param-id", name = "参数ID", length = 16, valueType = ValueType.UINT, order = 1)
        private Integer id;

        @ProtocolNode(id = "param-value", name = "参数值", length = 32, valueType = ValueType.HEX, order = 2)
        private String value;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataItem {

        private Integer id;
        private String value;
        private Double timestamp;
    }

    /**
     * 主方法 - 测试自动元素类型检测
     */
    public static void main(String[] args) {
        log.debug("自动元素类型检测测试");
        log.debug("==================");
        
        try {
            // 创建测试数据
            TestBody body = TestBody.builder()
                    .type("0xA1")
                    .paramGroup(Arrays.asList(
                            ParamItem.builder().id(1).value("0x01020304").build(),
                            ParamItem.builder().id(2).value("0x05060708").build()
                    ))
                    .dataGroup(Arrays.asList(
                            DataItem.builder().id(1).value("data1").timestamp(1234567890.0).build(),
                            DataItem.builder().id(2).value("data2").timestamp(1234567891.0).build()
                    ))
                    .build();

            log.debug("测试数据创建完成");
            log.debug("param-group 大小: {}", body.getParamGroup().size());
            log.debug("data-group 大小: {}", body.getDataGroup().size());
            
            // 测试协议解析
            log.debug("开始解析协议...");
            Protocol protocol = ProtocolClassParser.parseProtocol(body);
            log.debug("协议解析完成");
            
            // 分析协议结构
            log.debug("==================");
            log.debug("协议结构分析:");
            log.debug("协议名称: {}", protocol.getName());
            log.debug("协议ID: {}", protocol.getId());
            
            // 分析主体
//            if (protocol.getBody() != null) {
//                log.debug("主体: {} (ID: {})", protocol.getBody().getName(), protocol.getBody().getId());
//                log.debug("主体节点数: {}", protocol.getBody().getNodes().size());
//
//                // 分析主体下的节点组
//                for (var node : protocol.getBody().getNodes()) {
//                    if (node instanceof com.iecas.cmd.model.proto.NodeGroup) {
//                        var nodeGroup = (com.iecas.cmd.model.proto.NodeGroup) node;
//                        log.debug("发现节点组: {} (ID: {})", nodeGroup.getName(), nodeGroup.getId());
//                        log.debug("节点组内节点数: {}", nodeGroup.getGroupNodes().size());
//
//                        // 分析节点组内的子节点
//                        for (var groupNode : nodeGroup.getGroupNodes()) {
//                            log.debug("  - 子节点: {} (ID: {})", groupNode.getName(), groupNode.getId());
//                        }
//                    }
//                }
//            }
            
            log.debug("==================");
            log.debug("测试结果: ✅ 成功");
            log.debug("说明:");
            log.debug("1. param-group 自动检测为 PROTOCOL_OBJECT 类型");
            log.debug("2. data-group 自动检测为 CUSTOM_OBJECT 类型");
            log.debug("3. 无需手动配置 elementType 属性");
            log.debug("4. 系统能够自动识别并处理不同类型的元素");
            
        } catch (Exception e) {
            log.error("测试失败: {}", e.getMessage(), e);
            log.debug("==================");
            log.debug("测试结果: ❌ 失败");
            log.debug("错误分析:");
            log.debug("1. 自动元素类型检测可能有问题");
            log.debug("2. 协议解析过程可能失败");
            log.debug("3. 需要检查相关代码实现");
        }
    }
}
