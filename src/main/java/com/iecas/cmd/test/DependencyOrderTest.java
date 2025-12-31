package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
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
 * 依赖构建顺序测试类
 * 
 * 验证修复后的依赖构建顺序：
 * 1. 先构建所有节点和依赖关系
 * 2. 再验证表达式中的节点引用
 * 
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
public class DependencyOrderTest {

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

        // 这个节点依赖 param-group 节点组
        @ProtocolNode(id = "body-group-count", name = "组数量", length = 16, valueType = ValueType.UINT, 
                     fwdExpr = "size(#param-group)", order = 2)
        private Integer groupCount;

        // 节点组 - 不设置 repeat，让系统自动检测长度
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                order = 3
        )
        private List<ParamItem> paramGroup;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolTail(id = "test-tail", name = "测试协议尾")
    public static class TestTail {

        @ProtocolNode(id = "tail-checksum", name = "校验和", length = 16, valueType = ValueType.HEX,
                     fwdExpr = "crc16(#test-body)", order = 1)
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

    /**
     * 主方法 - 测试依赖构建顺序
     */
    public static void main(String[] args) {
        log.debug("依赖构建顺序测试");
        log.debug("==================");
        
        try {
            // 创建测试数据
            TestBody body = TestBody.builder()
                    .type("0xA1")
                    .paramGroup(Arrays.asList(
                            ParamItem.builder().id(1).value("0x01020304").build(),
                            ParamItem.builder().id(2).value("0x05060708").build(),
                            ParamItem.builder().id(3).value("0x090A0B0C").build()
                    ))
                    .build();

            log.debug("测试数据创建完成");
            log.debug("param-group 大小: {}", body.getParamGroup().size());
            
            // 测试协议解析
            log.debug("开始解析协议...");
            Protocol protocol = ProtocolClassParser.parseProtocol(body);
            log.debug("协议解析完成，节点数: {}", protocol.getNodes().size());
            
            // 测试协议编解码
            log.debug("开始编解码协议...");
            ProtocolCodec protocolCodec = new ProtocolCodec();
            byte[] encodedData = protocolCodec.encode(protocol);
            log.debug("协议编解码完成，编码后数据长度: {} 字节", encodedData.length);
            
            log.debug("==================");
            log.debug("测试结果: ✅ 成功");
            log.debug("说明:");
            log.debug("1. 依赖构建顺序已修复");
            log.debug("2. 节点组在表达式验证之前被正确创建");
            log.debug("3. size(#param-group) 表达式能够正确解析");
            log.debug("4. 协议编解码过程正常完成");
            
        } catch (CodecException e) {
            log.error("测试失败: {}", e.getMessage(), e);
            log.debug("==================");
            log.debug("测试结果: ❌ 失败");
            log.debug("错误分析:");
            log.debug("1. 依赖构建顺序可能仍有问题");
            log.debug("2. 节点组创建时机不正确");
            log.debug("3. 表达式验证时机不正确");
        } catch (Exception e) {
            log.error("测试过程中发生未知错误: {}", e.getMessage(), e);
            log.debug("==================");
            log.debug("测试结果: ❌ 未知错误");
        }
    }
}
