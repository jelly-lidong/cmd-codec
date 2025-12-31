package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolDefinition;
import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@ProtocolDefinition(name = "简单协议")
@AllArgsConstructor
public class SingleUintProto {

    @ProtocolNode(id = "validData", name = "数据", valueType = ValueType.UINT, length = 32, order = 1)
    private String data;

    @ProtocolNode(id = "bit", name = "二进制", valueType = ValueType.BIT, length = 2, order = 2)
    private String bit;

    public static void main(String[] args) throws CodecException {
        SingleUintProto proto = SingleUintProto.builder().data("0x11").bit("0b11").build();
        Protocol protocol = ProtocolClassParser.parseProtocol(proto);
        ProtocolCodec codec = new ProtocolCodec();
        byte[] encode = codec.encode(protocol);
        List<Node> decode = codec.decode(encode, protocol);
    }
}
