package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.parser.ProtocolClassParser;
import lombok.Data;

import java.util.List;

@Data
public class SimpleTest {

    @ProtocolNode(id = "1", name = "参数1", length = 2 * 8,
            fwdExpr = "value * 1000", bwdExpr = "double(value) / 1000",
            valueType = ValueType.INT, order = 1)
    private String val;

    @ProtocolNode(id = "2", name = "参数2", length = 2 * 8,
            fwdExpr = "value * 1000", bwdExpr = "double(value) / 1000",
            valueType = ValueType.INT, order = 1)
    private String val2;

    public static void main(String[] args) throws CodecException {

        SimpleTest simpleTest = new SimpleTest();
        simpleTest.setVal("2.3");
        simpleTest.setVal2("2");

        Protocol protocol = ProtocolClassParser.parseProtocol(simpleTest);
        ProtocolCodec protocolCodec = new ProtocolCodec();
        byte[] encode = protocolCodec.encode(protocol);

        List<Node> decode = protocolCodec.decode(encode, protocol);



    }
}
