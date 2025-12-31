package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolDefinition;
import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ProtocolDefinition(name = "单层协议")
public class SimpleLayerProtocol {


    @ProtocolNode(id = "protocol_id", name = "协议标识", length = 16, valueType = ValueType.HEX, value = "0x1234", order = 1)
    private String protocolId;

    @ProtocolNode(id = "checksum", name = "校验和", length = 16, valueType = ValueType.HEX, fwdExpr = "crc16Between(#version,#data_field)", order = 2)
    private String checksum;

    @ProtocolNode(id = "version", name = "协议版本", length = 8, valueType = ValueType.UINT, value = "1", order = 3)
    private Integer version;

    @ProtocolNode(id = "data_length", name = "数据长度", length = 16, valueType = ValueType.UINT, fwdExpr = "nodeLength(#data_field)", order = 4)
    private Integer dataLength;

    @ProtocolNode(id = "data_field", name = "数据域", length = 4 * 8, valueType = ValueType.HEX, order = 5)
    private final String dataField = "0xDEADBEEF";

//    @ProtocolNode(id = "checksum", name = "校验和", length = 16, valueType = ValueType.HEX, fwdExpr = "crc16Between(#version,#data_field)", order = 6)
//    private String checksum;


    public static void main(String[] args) throws CodecException {
        SimpleLayerProtocol simpleLayerProtocol = new SimpleLayerProtocol();
        Protocol protocol = ProtocolClassParser.parseProtocol(simpleLayerProtocol);
        ProtocolCodec protocolCodec = new ProtocolCodec();
        byte[] encode = protocolCodec.encode(protocol);

//        protocolCodec.decode(encode, protocol);
    }

}
