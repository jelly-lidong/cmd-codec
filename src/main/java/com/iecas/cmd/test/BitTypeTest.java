package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.enums.ValueType;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import com.iecas.cmd.util.ByteUtil;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class BitTypeTest {

    // 两个比特位，但只设置了第一个比特位为1，高位应该补0
//    @INode(id = "bitField1", name = "Bit Field 1", length = 2, valueType = ValueType.BIT, value = "0b1", order = 1)
//    private String bitField1;
//
//    // 五个比特位，设置了低位为101，高位应该补0
//    @INode(id = "bitField2", name = "Bit Field 2", length = 5, valueType = ValueType.BIT, value = "0b101", order = 2)
//    private String bitField2;
//
//    @INode(id = "bitField3", name = "Bit Field 3", length = 1, valueType = ValueType.BIT, value = "0b0", order = 3)
//    private String bitField3;

    @ProtocolNode(id = "bitField1", name = "Bit Field 1", length = 2, valueType = ValueType.BIT, order = 1)
    private final String bitField1 = "0b1";

    // 五个比特位，设置了低位为101，高位应该补0
    @ProtocolNode(id = "bitField2", name = "Bit Field 2", length = 5, valueType = ValueType.BIT, order = 2)
    private final String bitField2 = "0b101";

    @ProtocolNode(id = "bitField3", name = "Bit Field 3", length = 1, valueType = ValueType.BIT, order = 3)
    private final String bitField3 = "0b0";


    public static void main(String[] args) throws CodecException {
//        BitTypeTest bitTypeTest = new BitTypeTest();
        BitTypeTest bitTypeTest = BitTypeTest.builder().build();

        Protocol protocol = ProtocolClassParser.parseProtocol(bitTypeTest);

        ProtocolCodec protocolCodec = new ProtocolCodec();
        byte[]        encode        = protocolCodec.encode(protocol);

        String binaryString = ByteUtil.bytesToBinaryString(encode);
        log.debug("编码结果: {}", binaryString); // 结果应该是01001010


    }


}
