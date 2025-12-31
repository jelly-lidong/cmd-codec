package com.iecas.cmd.util;

import cn.hutool.core.lang.UUID;

public class IDGenerator {

    public static void main(String[] args) {
        System.out.println(createParamID("K110", "hahahahha", 1));
    }


    /**
     * 使用UUID 生成参数ID
     *
     * @param cmdSymbol 指令代号
     * @param paramName 参数名称
     * @param paramNo   参数序号
     * @return 参数ID
     */
    public static String createParamID(String cmdSymbol, String paramName, int paramNo) {
        String nameSpace = String.format("%s-%s-%d", cmdSymbol, paramName, paramNo);
        return UUID.nameUUIDFromBytes(nameSpace.getBytes()).toString().replaceAll("-", "");
    }
}
