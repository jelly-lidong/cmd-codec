package com.iecas.cmd.aviator.between;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.iecas.cmd.annotation.BetweenFunction;
import com.iecas.cmd.aviator.node.NodeLookup;
import com.iecas.cmd.util.ByteUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 计算两个节点之间数据的长度
 *
 * <p>使用示例：</p>
 * <pre>
 * lengthBetween('start_node_id', 'end_node_id')  // 计算从start_node_id节点到end_node_id节点之间数据的长度
 * </pre>
 */
@BetweenFunction
@Slf4j
public class LengthBetweenFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "lengthBetween";
    }

    @SneakyThrows
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject sArg, AviatorObject eArg) {
        String sId = FunctionUtils.getStringValue(sArg, env);
        String eId = FunctionUtils.getStringValue(eArg, env);

        log.debug("[lengthBetween] 起始节点={}, 结束节点={}", sId, eId);

        byte[] betweenBytes = NodeLookup.getBetweenBytes(env, sId, eId);
        log.debug("[lengthBetween] 长度计算: 计算范围字节数据={}", ByteUtil.bytesToHexString(betweenBytes));
        return AviatorLong.valueOf(betweenBytes.length);
    }
}
