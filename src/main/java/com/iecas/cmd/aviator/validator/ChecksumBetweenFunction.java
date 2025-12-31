package com.iecas.cmd.aviator.validator;

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
 * 累加和校验函数（1字节）
 *
 * <p>与 {@link CRC16Function} 一致的风格，提供一个参数（默认UTF-8）和两个参数（指定编码）两个版本。</p>
 * <p>返回按字节累加并取低8位后的结果（0x00~0xFF）。</p>
 * <p>
 * 使用示例：
 * <pre>
 * checksum("Hello World")
 * checksum("数据", "GBK")
 * </pre>
 */
@Slf4j
@BetweenFunction
public class ChecksumBetweenFunction extends AbstractFunction {

    public ChecksumBetweenFunction() {
        super();
    }

    @Override
    public String getName() {
        return "checksumBetween";
    }

    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject sArg, AviatorObject eArg) {
        String sId = FunctionUtils.getStringValue(sArg, env);
        String eId = FunctionUtils.getStringValue(eArg, env);
        if (sId == null || eId.trim().isEmpty()) {
            throw new IllegalArgumentException("起始节点或结束节点ID不能为空");
        }
        log.debug("[checksumBetween] 起始节点 = {}, 结束节点 = {}", sId, eId);

        byte[] betweenBytes = NodeLookup.getBetweenBytes(env, sId, eId);
        log.debug("[checksumBetween] 计算范围字节数据={}", ByteUtil.bytesToHexString(betweenBytes));
        String crc16 = VerifyUtils.checkSum(betweenBytes);
        return new AviatorString(crc16);
    }
}


