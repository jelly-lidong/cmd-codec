package com.iecas.cmd.encode;

import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Node;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import java.util.List;
import org.junit.Test;

public class ExpressionProtocolTest {


    @Test
    public void testExpressionProtocol() throws Exception {
        ExpressionProtocol expressionProtocol = new ExpressionProtocol();

        Protocol protocol      = ProtocolClassParser.parseProtocol(expressionProtocol);
        ProtocolCodec protocolCodec = new ProtocolCodec();
        byte[]        encode        = protocolCodec.encode(protocol);
        List<Node>    bytes         = protocolCodec.decode(encode, protocol);
    }

    public static class ExpressionProtocol {

        //输入的是2.3，乘以1000后变成2300，转成16进制是0x08FC，反过来除以1000变成2.3
        @ProtocolNode(id = "a", name = "浮点数计算", length = 4 * 8, valueType = ValueType.INT, fwdExpr = "value * 1000", bwdExpr = "double(value) / 1000 ", order = 1)
        private String a = "2.3";  //000008FC


    }

}
