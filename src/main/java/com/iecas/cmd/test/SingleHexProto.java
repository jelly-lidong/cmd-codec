package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class SingleHexProto {

    @ProtocolNode(id = "validData", name = "十六进制字符串", valueType = ValueType.HEX,  order = 1)
    private String data;
}
