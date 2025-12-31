package com.iecas.cmd.aviator.between;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.googlecode.aviator.runtime.type.AviatorString;
import com.iecas.cmd.annotation.BetweenFunction;
import com.iecas.cmd.aviator.node.NodeLookup;
import com.iecas.cmd.util.ByteUtil;
import com.iecas.cmd.util.VerifyUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 计算两个节点之间数据的CRC16值
 *
 * <p>使用CRC-16-CCITT标准算法 (多项式: X^16+X^12+X^5+1)</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * crc16Between('version', 'data_field')  // 计算从version节点到data_field节点之间数据的CRC16
 * </pre>
 */
@BetweenFunction
@Slf4j
public class Crc16BetweenFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "crc16Between";
    }

    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject sArg, AviatorObject eArg) {
        String sId = FunctionUtils.getStringValue(sArg, env);
        String eId = FunctionUtils.getStringValue(eArg, env);

        log.debug("[crc16Between] CRC16计算: 起始节点={}, 结束节点={}", sId, eId);

        byte[] betweenBytes = NodeLookup.getBetweenBytes(env, sId, eId);
        log.debug("[crc16Between] CRC16计算: 计算范围字节数据={}", ByteUtil.bytesToHexString(betweenBytes));
        String crc16 = VerifyUtils.calCRC16(betweenBytes);
        return new AviatorString(crc16);
    }
}
