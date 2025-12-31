package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.EndianType;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Arrays;

/**
 * Size函数使用示例
 * 
 * 展示如何在协议定义中使用 size() 函数来动态获取节点组的大小，
 * 而不是通过硬编码的 repeat 属性。
 * 
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
public class SizeFunctionExample {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolHeader(id = "example-header", name = "示例协议头")
    public static class ExampleHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, order = 1)
        private String id;

        @ProtocolNode(id = "header-version", name = "版本", length = 32, valueType = ValueType.STRING, order = 2)
        private String version;

        @ProtocolNode(id = "header-timestamp", name = "时间戳", length = 64, valueType = ValueType.UINT, order = 3)
        private Long timestamp;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolBody(id = "example-body", name = "示例协议体")
    public static class ExampleBody {

        @ProtocolNode(id = "body-type", name = "类型", length = 8, valueType = ValueType.HEX, order = 1)
        private String type;

        @ProtocolNode(id = "body-group-count", name = "组数量", length = 16, valueType = ValueType.UINT, 
                     fwdExpr = "size(#param-group)", order = 2)
        private Integer groupCount;

        // 节点组 - 不设置 repeat，让系统自动检测长度
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                resolveStrategy = GroupResolveStrategy.FLATTEN,
                order = 3
        )
        private List<ParamItem> paramGroup;

        // 另一个节点组 - 长度自动检测
        @ProtocolNodeGroup(
                id = "data-group",
                name = "数据组",
                resolveStrategy = GroupResolveStrategy.FLATTEN,
                order = 4
        )
        private List<DataItem> dataGroup;

        // 校验和 - 基于参数组大小计算
        @ProtocolNode(id = "body-checksum", name = "校验和", length = 16, valueType = ValueType.HEX,
                     fwdExpr = "crc16(#param-group)", order = 5)
        private String checksum;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolTail(id = "example-tail", name = "示例协议尾")
    public static class ExampleTail {

        @ProtocolNode(id = "tail-length", name = "数据长度", length = 32, valueType = ValueType.UINT,
                     fwdExpr = "length(#example-body)", order = 1)
        private Long dataLength;

        @ProtocolNode(id = "tail-valid", name = "有效性", length = 8, valueType = ValueType.BIT,
                     fwdExpr = "size(#param-group) > 0", order = 2)
        private Boolean isValid;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParamItem {

        @ProtocolNode(id = "param-id", name = "参数ID", length = 16, valueType = ValueType.UINT, 
                     endian = EndianType.BIG, order = 1)
        private Integer id;

        @ProtocolNode(id = "param-value", name = "参数值", length = 32, valueType = ValueType.HEX, order = 2)
        private String value;

        @ProtocolNode(id = "param-length", name = "参数长度", length = 16, valueType = ValueType.UINT,
                     fwdExpr = "length(param-value)", order = 3)
        private Integer length;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataItem {

        @ProtocolNode(id = "data-index", name = "数据索引", length = 16, valueType = ValueType.UINT, order = 1)
        private Integer index;

        @ProtocolNode(id = "data-content", name = "数据内容", length = 64, valueType = ValueType.STRING, order = 2)
        private String content;
    }

    /**
     * 主方法 - 演示如何使用
     */
    public static void main(String[] args) {
        log.debug("Size函数使用示例");
        log.debug("==================");
        
        // 创建示例数据
        ExampleBody body = ExampleBody.builder()
                .type("0xA1")
                .paramGroup(Arrays.asList(
                        ParamItem.builder().id(1).value("0x01020304").build(),
                        ParamItem.builder().id(2).value("0x05060708").build(),
                        ParamItem.builder().id(3).value("0x090A0B0C").build()
                ))
                .dataGroup(Arrays.asList(
                        DataItem.builder().index(1).content("Data1").build(),
                        DataItem.builder().index(2).content("Data2").build(),
                        DataItem.builder().index(3).content("Data3").build(),
                        DataItem.builder().index(4).content("Data4").build(),
                        DataItem.builder().index(5).content("Data5").build(),
                        DataItem.builder().index(6).content("Data6").build()
                ))
                .build();

        log.debug("参数组大小: {}", body.getParamGroup().size());
        log.debug("数据组大小: {}", body.getDataGroup().size());
        log.debug("数据组大小是参数组的两倍: {}", body.getDataGroup().size() == body.getParamGroup().size() * 2);
        
        log.debug("==================");
        log.debug("示例说明:");
        log.debug("1. body-group-count 使用 size(#param-group) 动态获取参数组大小");
        log.debug("2. data-group 长度由系统自动检测，无需配置");
        log.debug("3. tail-valid 使用 size(#param-group) > 0 判断参数组是否为空");
        log.debug("4. 所有节点组都不需要任何重复次数配置");
    }
}
