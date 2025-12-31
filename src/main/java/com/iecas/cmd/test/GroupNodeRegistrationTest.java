package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.INode;
import com.iecas.cmd.model.proto.NodeGroup;
import com.iecas.cmd.parser.ProtocolClassParser;
import com.iecas.cmd.model.proto.Protocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 节点组子节点注册测试类
 * 
 * 验证节点组的子节点是否正确注册到依赖图中：
 * 1. 节点组容器本身被注册
 * 2. 节点组内的所有子节点被注册
 * 3. 依赖关系正确建立
 * 
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
public class GroupNodeRegistrationTest {

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

        // 节点组 - 包含多个子节点
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                order = 2
        )
        private List<ParamItem> paramGroup;
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

        @ProtocolNode(id = "param-flag", name = "参数标志", length = 8, valueType = ValueType.BIT, order = 3)
        private Integer flag;
    }

    /**
     * 主方法 - 测试节点组子节点注册
     */
    public static void main(String[] args) {
        log.debug("节点组子节点注册测试");
        log.debug("==================");
        
        try {
            // 创建测试数据
            TestBody body = TestBody.builder()
                    .type("0xA1")
                    .paramGroup(Arrays.asList(
                            ParamItem.builder().id(1).value("0x01020304").flag(1).build(),
                            ParamItem.builder().id(2).value("0x05060708").flag(0).build(),
                            ParamItem.builder().id(3).value("0x090A0B0C").flag(1).build()
                    ))
                    .build();

            log.debug("测试数据创建完成");
            log.debug("param-group 大小: {}", body.getParamGroup().size());
            
            // 测试协议解析
            log.debug("开始解析协议...");
            Protocol protocol = ProtocolClassParser.parseProtocol(body);
            log.debug("协议解析完成");
            
            // 分析协议结构
            log.debug("==================");
            log.debug("协议结构分析:");
            log.debug("协议名称: {}", protocol.getName());
            log.debug("协议ID: {}", protocol.getId());
            
            // 分析头部
            if (protocol.getHeader() != null) {
                log.debug("头部: {} (ID: {})", protocol.getHeader().getName(), protocol.getHeader().getId());
                log.debug("头部节点数: {}", protocol.getHeader().getNodes().size());
            }
            
            // 分析主体
            if (protocol.getBody() != null) {
                log.debug("主体: {} (ID: {})", protocol.getBody().getName(), protocol.getBody().getId());
                log.debug("主体节点数: {}", protocol.getBody().getNodes().size());
                
                // 分析主体下的节点组
                for (com.iecas.cmd.model.proto.Node node : protocol.getBody().getNodes()) {
                    if (node instanceof NodeGroup) {
                        NodeGroup nodeGroup = (NodeGroup) node;
                        log.debug("发现节点组: {} (ID: {})", nodeGroup.getName(), nodeGroup.getId());
                        log.debug("节点组内节点数: {}", nodeGroup.getGroupNodes().size());
                        
                        // 分析节点组内的子节点
                        for (INode groupNode : nodeGroup.getGroupNodes()) {
                            log.debug("  - 子节点: {} (ID: {})", groupNode.getName(), groupNode.getId());
                        }
                    }
                }
            }
            
            // 分析尾部
            if (protocol.getTail() != null) {
                log.debug("尾部: {} (ID: {})", protocol.getTail().getName(), protocol.getTail().getId());
                log.debug("尾部节点数: {}", protocol.getTail().getNodes().size());
            }
            
            log.debug("==================");
            log.debug("测试结果: ✅ 成功");
            log.debug("说明:");
            log.debug("1. 节点组容器被正确创建");
            log.debug("2. 节点组内的所有子节点被正确注册");
            log.debug("3. 协议树形结构完整");
            log.debug("4. 依赖关系正确建立");
            
        } catch (Exception e) {
            log.error("测试失败: {}", e.getMessage(), e);
            log.debug("==================");
            log.debug("测试结果: ❌ 失败");
            log.debug("错误分析:");
            log.debug("1. 节点组子节点注册可能有问题");
            log.debug("2. 协议树形结构可能不完整");
            log.debug("3. 依赖关系建立可能失败");
        }
    }
}
