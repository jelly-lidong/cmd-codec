package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.EndianType;
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

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ProtocolDefinition(name = "简单协议")
public class SimpleProtocolTest {

    @ProtocolHeader(id = "simple-protocol-header", name = "协议头", order = 1)
    private SimpleProtocolHeader header;

    @ProtocolBody(id = "simple-protocol-body", name = "协议体", order = 2)
    private SimpleProtocolBody body;

    @ProtocolTail(id = "simple-protocol-tail", name = "协议尾", order = 3)
    private SimpleProtocolTail tail;

    public static void main(String[] args) throws CodecException {

        SimpleProtocolTest simpleProtocolTest = SimpleProtocolTest.builder()
                .header(SimpleProtocolHeader.builder()
                        .id("0x1122")
                        .version("v1.0")
                        .build())
                .body(SimpleProtocolBody.builder()
                        .type("0xA1")
                        .group(Arrays.asList(
                                ParamGroupItem.builder().id(1).value("0x01").build(),
                                ParamGroupItem.builder().id(2).value("0x02").build()
                        ))
                        .build())
                .tail(SimpleProtocolTail.builder()
                        .build())
                .build();

        log.debug("开始解析协议...");
        Protocol protocol = ProtocolClassParser.parseProtocol(simpleProtocolTest);
        ProtocolCodec protocolCodec = new ProtocolCodec();
        byte[] encodedData = protocolCodec.encode(protocol);

    }



    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolHeader(id = "simple-protocol-header", name = "协议头")
    public static class SimpleProtocolHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, endian = EndianType.BIG, order = 1)
        private String id;

        @ProtocolNode(id = "header-version", name = "版本", length = 32, valueType = ValueType.STRING, order = 2)
        private String version;

        @ProtocolNode(id = "header-length", name = "数据域长度", length = 64, valueType = ValueType.UINT, fwdExpr = "length(#simple-protocol-body)", order = 3)
        private Long timestamp;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolBody(id = "simple-protocol-body", name = "协议体")
    public static class SimpleProtocolBody {

        @ProtocolNode(id = "body-type", name = "类型", length = 18, valueType = ValueType.HEX, order = 1)
        private String type;

        @ProtocolNode(id = "body-group-num", name = "组数量", length = 8, valueType = ValueType.INT, fwdExpr = "size(#param-group)", order = 2)
        private Integer groupNum;

        // 节点组 - 扁平化策略
        @ProtocolNodeGroup(
                id = "param-group",
                name = "参数组",
                resolveStrategy = GroupResolveStrategy.FLATTEN,
                order = 3
        )
        private List<ParamGroupItem> group;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolTail(id = "simple-protocol-tail", name = "协议尾")
    public static class SimpleProtocolTail {

        @ProtocolNode(id = "tail-checksum", name = "校验和", length = 16, valueType = ValueType.HEX, fwdExpr = "crc16(#simple-protocol-body)", order = 1)
        private String checksum;

    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParamGroupItem {

        @ProtocolNode(id = "param-id", name = "参数ID", length = 16, valueType = ValueType.UINT, endian = EndianType.BIG, order = 1)
        private Integer id;

        @ProtocolNode(id = "param-value", name = "参数值", length = 8, valueType = ValueType.HEX, order = 2)
        private String value;
    }
}
