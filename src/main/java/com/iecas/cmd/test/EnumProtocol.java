package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolDefinition;import com.iecas.cmd.annotation.ProtocolEnum;
import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.model.enums.ValueType;
import lombok.Data;

@Data
@ProtocolDefinition(id = "enum", name = "枚举协议", version = "1.0", description = "枚举协议测试")
public class EnumProtocol {

    @ProtocolNode(id = "uint", name = "无符号整型枚举", length = 8, valueType = ValueType.UINT, order = 1)
    @ProtocolEnum(values = {"0:离线", "1:在线", "2:忙碌", "3:离开"})
    private String unitType = "在线";

    @ProtocolNode(id = "int", name = "有符号整型枚举", length = 16, valueType = ValueType.INT, order = 2)
    @ProtocolEnum(values = {"0:哈哈", "1:拉拉", "2:儿子", "3:粑粑"})
    private String intType = "儿子";

    @ProtocolNode(id = "bitType", name = "二进制枚举", length = 8, valueType = ValueType.BIT, order = 3)
    @ProtocolEnum(values = {"0b00000011:哈哈", "0b00000001:拉拉", "0b00000000:儿子"})  // 注意位数要和length对应
    private String bitType = "哈哈";


    @ProtocolNode(id = "hexType", name = "十六进制枚举", length = 8, valueType = ValueType.HEX, order = 4)
    @ProtocolEnum(values = {"0x11:哈哈", "0x01:拉拉", "0x00:儿子"})
    private String hexType = "哈哈";

    @ProtocolNode(id = "floatType", name = "浮点数枚举", length = 4 * 8, valueType = ValueType.FLOAT, order = 4)
    @ProtocolEnum(values = {"0.11111:哈哈", "0.22222:拉拉", "0.3233333:儿子"})
    private String floatType = "哈哈";
}
