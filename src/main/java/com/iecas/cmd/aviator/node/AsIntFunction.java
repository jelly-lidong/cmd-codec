package com.iecas.cmd.aviator.node;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

import com.iecas.cmd.model.proto.INode;

import java.util.Map;

/**
 * asInt(nodeId, signed, endian, bitOffset, bitLen)
 * signed: 0/1；endian: "BIG"/"LITTLE"（仅在bitLen按字节对齐时生效）
 */
public class AsIntFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "asInt";
    }

    private static long extractUnsignedBits(byte[] bytes, int bitOffset, int bitLen) {
        if (bytes == null || bitLen <= 0) return 0L;
        long value = 0L;
        for (int i = 0; i < bitLen; i++) {
            int abs = bitOffset + i;
            int byteIndex = abs / 8;
            if (byteIndex >= bytes.length) break;
            int bitInByte = 7 - (abs % 8);
            int bit = (bytes[byteIndex] >> bitInByte) & 1;
            value = (value << 1) | bit;
        }
        return value;
    }

    private static long parseWithEndian(byte[] bytes, int bitOffset, int bitLen, boolean littleEndian) {
        if (bitLen % 8 != 0 || bitLen <= 0) {
            return extractUnsignedBits(bytes, bitOffset, bitLen);
        }
        int byteCount = bitLen / 8;
        int startByte = bitOffset / 8;
        int startBitInByte = bitOffset % 8;
        if (startBitInByte != 0) {
            // 不按字节对齐时退化为逐位提取
            return extractUnsignedBits(bytes, bitOffset, bitLen);
        }
        if (bytes == null || startByte + byteCount > bytes.length) return 0L;
        long value = 0L;
        if (littleEndian) {
            for (int i = byteCount - 1; i >= 0; i--) {
                value = (value << 8) | (bytes[startByte + i] & 0xFFL);
            }
        } else {
            for (int i = 0; i < byteCount; i++) {
                value = (value << 8) | (bytes[startByte + i] & 0xFFL);
            }
        }
        return value;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject nodeArg, AviatorObject signedArg, AviatorObject endianArg, AviatorObject bitOffArg, AviatorObject bitLenArg) {
        String nodeId = FunctionUtils.getStringValue(nodeArg, env);
        int signed = FunctionUtils.getNumberValue(signedArg, env).intValue();
        String endian = FunctionUtils.getStringValue(endianArg, env);
        int bitOffset = FunctionUtils.getNumberValue(bitOffArg, env).intValue();
        int bitLen = FunctionUtils.getNumberValue(bitLenArg, env).intValue();
        INode node = NodeLookup.findNode(env, nodeId);
        byte[] data = node != null ? node.getData() : null;
        boolean little = "LITTLE".equalsIgnoreCase(endian);
        long unsigned = parseWithEndian(data, bitOffset, bitLen, little);
        if (signed != 0 && bitLen > 0 && bitLen < 64) {
            long signBit = 1L << (bitLen - 1);
            if ((unsigned & signBit) != 0) {
                long mask = (1L << bitLen) - 1;
                long signedVal = (unsigned ^ mask) + 1; // two's complement
                return AviatorLong.valueOf(-signedVal);
            }
        }
        return AviatorLong.valueOf(unsigned);
    }
}


