package com.iecas.cmd.test;

import cn.hutool.core.util.ClassUtil;
import com.iecas.cmd.codec.ProtocolCodec;
import com.iecas.cmd.exception.CodecException;
import com.iecas.cmd.model.proto.Protocol;
import com.iecas.cmd.parser.ProtocolClassParser;
import com.iecas.cmd.test.SimpleNestedProto.B1;
import com.iecas.cmd.test.SimpleNestedProto.H1;
import com.iecas.cmd.test.SimpleNestedProto.T1;

import java.util.Set;

public class MainTest {

    public static void main(String[] args) throws CodecException {
        Set<Class<?>> classes = ClassUtil.scanPackage("com.iecas.cmd.aviator");
        for (Class<?> aClass : classes) {
            System.out.println(aClass.getSimpleName());
        }

        // Example usage of MyProto
//        SimpleNestedProto simpleNestedProto = SimpleNestedProto.builder()
//            .h1(H1.builder()
//                .h1Field1("0b1")
//                .h1Field2("0b1001")
//                .h1Field3("0b111")
//                .build())
//            .b1(B1.builder()
//                .h2(B1.H2.builder()
//                    .h2Field1("0b1")
//                    .h2Field2("0b1001")
//                    .h2Field3("0b111")
//                    .build())
//                .b2(B1.B2.builder()
//                    .h3(B1.B2.H3.builder()
//                        .h3Field1("0b1")
//                        .h3Field2("0b1001")
//                        .h3Field3("0b111")
//                        .build())
//                    .b3(B1.B2.B3.builder()
//                        .b3Field1("0b1")
//                        .b3Field2("0b1001")
//                        .b3Field3("0b111")
//                        .build())
//                    .t3(B1.B2.T3.builder()
//                        .t3Field1("0xCC")
//                        .build())
//                    .build())
//                .t2(B1.T2.builder()
//                    .t2Field1("0xBB")
//                    .build())
//                .build())
//            .t1(T1.builder()
//                .t1Field1("0xAA")
//                .build())
//            .build();
//        Protocol protocol = ProtocolClassParser.parseProtocol(simpleNestedProto);
//
//        ProtocolCodec protocolCodec = new ProtocolCodec();
//        byte[] encode = protocolCodec.encode(protocol);

    }

}
