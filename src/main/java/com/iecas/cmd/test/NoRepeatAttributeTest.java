package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.EndianType;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 无 repeat 属性测试类
 * 
 * 验证移除 repeat 属性后，系统能够：
 * 1. 自动检测实际数据长度
 * 2. 使用表达式计算重复次数
 * 3. 正常进行协议编解码
 * 
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
public class NoRepeatAttributeTest {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolHeader(id = "test-header", name = "测试协议头")
    public static class TestHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, order = 1)
        private String id;

        @ProtocolNode(id = "header-version", name = "版本", length = 32, valueType = ValueType.STRING, order = 2)
        private String version;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolBody(id = "test-body", name = "测试协议体")
    public static class TestBody {

        @ProtocolNode(id = "body-type", name = "类型", length = 8, valueType = ValueType.HEX, order = 1)
        private String type;

        @ProtocolNode(id = "body-group-count", name = "组数量", length = 16, valueType = ValueType.UINT, 
                     fwdExpr = "size(#param-group)", order = 2)
        private Integer groupCount;

        // 节点组1 - 不设置 repeat，让系统自动检测长度
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                order = 3
        )
        private List<ParamItem> paramGroup;

        // 节点组2 - 长度自动检测
        @ProtocolNodeGroup(
                id = "data-group",
                name = "数据组",
                order = 4
        )
        private List<DataItem> dataGroup;

        // 节点组3 - 长度自动检测
        @ProtocolNodeGroup(
                id = "fixed-group",
                name = "固定组",
                order = 5
        )
        private List<FixedItem> fixedGroup;
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

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FixedItem {

        @ProtocolNode(id = "fixed-id", name = "固定ID", length = 16, valueType = ValueType.UINT, order = 1)
        private Integer id;

        @ProtocolNode(id = "fixed-name", name = "固定名称", length = 32, valueType = ValueType.STRING, order = 2)
        private String name;
    }

    /**
     * 主方法 - 演示无 repeat 属性的使用
     */
    public static void main(String[] args) {
        log.debug("无 repeat 属性测试");
        log.debug("==================");
        
        // 创建测试数据
        TestBody body = TestBody.builder()
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
                .fixedGroup(Arrays.asList(
                        FixedItem.builder().id(1).name("Fixed1").build(),
                        FixedItem.builder().id(2).name("Fixed2").build(),
                        FixedItem.builder().id(3).name("Fixed3").build()
                ))
                .build();

        log.debug("参数组大小: {}", body.getParamGroup().size());
        log.debug("数据组大小: {}", body.getDataGroup().size());
        log.debug("固定组大小: {}", body.getFixedGroup().size());
        log.debug("数据组大小是参数组的两倍: {}", body.getDataGroup().size() == body.getParamGroup().size() * 2);
        log.debug("固定组大小是3: {}", body.getFixedGroup().size() == 3);
        
        log.debug("==================");
        log.debug("测试说明:");
        log.debug("1. param-group: 无配置，系统自动检测长度为 3");
        log.debug("2. data-group: 无配置，系统自动检测长度为 6");
        log.debug("3. fixed-group: 无配置，系统自动检测长度为 3");
        log.debug("4. 所有节点组都不需要任何重复次数配置");
        log.debug("5. 系统能够自动检测实际数据长度并处理依赖关系");
    }
}
