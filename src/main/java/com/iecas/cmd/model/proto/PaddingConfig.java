package com.iecas.cmd.model.proto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 填充配置类
 * 用于描述节点的填充规则，支持固定长度填充和动态填充
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PaddingConfig {

    /**
     * 填充类型枚举
     * 定义了不同的填充策略，用于在协议解析和构建中处理数据对齐和长度要求
     */
    public enum PaddingType {
        /**
         * 固定长度填充 - 填充到指定的固定长度
         *
         * <p>说明：无论当前数据长度是多少，都填充到指定的固定长度。如果当前数据已经超过目标长度，则不进行填充。</p>
         *
         * <p>使用场景：</p>
         * <ul>
         *   <li>字段需要固定长度，如身份证号、手机号等</li>
         *   <li>协议报文要求特定字段必须是固定字节数</li>
         *   <li>数据库字段有固定长度要求</li>
         * </ul>
         *
         * <p>配置示例：</p>
         * <pre>{@code
         * // 将字段填充到32字节
         * PaddingConfig config = new PaddingConfig();
         * config.setPaddingType(PaddingType.FIXED_LENGTH);
         * config.setTargetLength(256);  // 32字节 = 256位
         * config.setPaddingValue("0x00");
         *
         * // 示例：原始数据"Hello"(5字节) -> 填充后"Hello" + 27个0x00字节 = 32字节
         * </pre>
         */
        FIXED_LENGTH,

        /**
         * 对齐填充 - 填充到指定字节边界对齐
         *
         * <p>说明：将数据长度调整到指定字节边界的倍数。常用于内存对齐优化和协议要求。</p>
         *
         * <p>使用场景：</p>
         * <ul>
         *   <li>内存对齐优化，如4字节、8字节边界对齐</li>
         *   <li>网络协议要求数据按特定边界对齐</li>
         *   <li>硬件要求数据按特定字节数对齐</li>
         * </ul>
         *
         * <p>配置示例：</p>
         * <pre>{@code
         * // 按8字节边界对齐
         * PaddingConfig config = new PaddingConfig();
         * config.setPaddingType(PaddingType.ALIGNMENT);
         * config.setTargetLength(64);  // 8字节边界 = 64位
         * config.setPaddingValue("0x00");
         *
         * // 示例：
         * // 原始数据10字节 -> 填充到16字节（下一个8字节边界）
         * // 原始数据16字节 -> 不填充（已经对齐）
         * // 原始数据18字节 -> 填充到24字节（下一个8字节边界）
         * }</pre>
         */
        ALIGNMENT,

        /**
         * 动态填充 - 根据表达式计算填充长度
         *
         * <p>说明：通过执行用户定义的表达式来动态计算需要填充的长度。表达式可以引用其他节点的值和长度。</p>
         *
         * <p>使用场景：</p>
         * <ul>
         *   <li>填充长度依赖于其他字段的值</li>
         *   <li>复杂的对齐计算逻辑</li>
         *   <li>根据协议版本或类型动态调整填充</li>
         *   <li>条件性填充，某些情况下填充不同长度</li>
         * </ul>
         *
         * <p>配置示例：</p>
         * <pre>{@code
         * // 根据header长度动态计算填充
         * PaddingConfig config = new PaddingConfig();
         * config.setPaddingType(PaddingType.DYNAMIC);
         * config.setLengthExpression("header.totalLength - currentLength");
         * config.setPaddingValue("0x00");
         *
         * // 更复杂的示例：按条件填充
         * config.setLengthExpression("protocolVersion == 1 ? 8 : 16");
         *
         * // 对齐计算示例
         * config.setLengthExpression("((currentLength + 7) / 8) * 8 - currentLength");
         * }</pre>
         */
        DYNAMIC,

        /**
         * 补齐填充 - 填充剩余空间到容器总长度
         *
         * <p>说明：这是最常用的填充模式。当容器有固定总长度时，填充节点会自动计算并填充剩余空间，
         * 使得容器的总长度等于所有子节点长度之和加上填充长度。</p>
         *
         * <p>计算公式：填充长度 = 容器固定长度 - 所有其他子节点长度之和</p>
         *
         * <p>使用场景：</p>
         * <ul>
         *   <li>协议报文有固定总长度，需要填充到指定大小</li>
         *   <li>数据包需要固定大小，剩余空间用填充数据补齐</li>
         *   <li>文件格式要求固定块大小</li>
         *   <li>网络传输要求固定帧长度</li>
         * </ul>
         *
         * <p>配置示例：</p>
         * <pre>{@code
         * // 容器总长度1024字节，自动填充剩余空间
         * PaddingConfig config = new PaddingConfig();
         * config.setPaddingType(PaddingType.FILL_CONTAINER);
         * config.setContainerFixedLength(8192);  // 1024字节 = 8192位
         * config.setPaddingValue("0xFF");
         * config.setAutoCalculateContainerLength(false);
         *
         * // 示例：
         * // 容器固定长度：1024字节
         * // 子节点A：100字节，子节点B：200字节，子节点C：300字节
         * // 填充长度：1024 - 100 - 200 - 300 = 424字节
         *
         * // 从容器节点自动获取长度
         * config.setContainerNode("#body");
         * config.setAutoCalculateContainerLength(true);
         * }</pre>
         */
        FILL_CONTAINER;
    }

    /**
     * 填充类型
     */
    @XmlAttribute
    private PaddingType paddingType = PaddingType.FIXED_LENGTH;

    /**
     * 目标长度（位数）
     * 对于FIXED_LENGTH类型，表示填充后的总长度
     * 对于ALIGNMENT类型，表示对齐的字节数
     */
    @XmlAttribute
    private int targetLength;

    /**
     * 填充值（十六进制字符串）
     * 例如："0xAA", "0x00", "0xFF"
     */
    @XmlAttribute
    private String paddingValue = "0x00";

    /**
     * 填充值重复模式
     * true: 重复填充值直到达到目标长度
     * false: 只填充一次指定的值
     */
    @XmlAttribute
    private boolean repeatPattern = true;

    /**
     * 最小填充长度（位数）
     * 即使计算出的填充长度小于此值，也要填充到最小长度
     */
    @XmlAttribute
    private int minPaddingLength = 0;

    /**
     * 最大填充长度（位数）
     * 填充长度不能超过此值
     */
    @XmlAttribute
    private int maxPaddingLength = Integer.MAX_VALUE;

    /**
     * 填充长度计算表达式
     * 用于DYNAMIC类型，支持AviatorScript语法
     * 可以引用其他节点的长度和值
     * 例如："targetLength - currentLength", "align(currentLength, 8) - currentLength"
     */
    @XmlAttribute
    private String lengthExpression;

    /**
     * 参考容器节点
     * 用于FILL_REMAINING类型，指定要填充到哪个容器的总长度
     * 例如："#body", "parent", "protocol"
     */
    @XmlAttribute
    private String containerNode;

    /**
     * 容器固定长度（位数）
     * 用于FILL_CONTAINER类型，指定容器的固定总长度
     * 容器长度 = 所有子节点长度之和 + 填充长度
     */
    @XmlAttribute
    private int containerFixedLength;

    /**
     * 是否自动计算容器长度
     * true: 从容器节点的length属性获取固定长度
     * false: 使用containerFixedLength指定的长度
     */
    @XmlAttribute
    private boolean autoCalculateContainerLength = true;

    /**
     * 填充节点在容器中的位置
     * START: 填充在容器开始位置
     * END: 填充在容器结束位置（默认）
     * MIDDLE: 填充在指定位置
     */
    @XmlAttribute
    private PaddingPosition paddingPosition = PaddingPosition.END;

    /**
     * 填充位置枚举
     */
    public enum PaddingPosition {
        START,   // 开始位置
        END,     // 结束位置
        MIDDLE   // 中间位置
    }

    /**
     * 是否启用填充
     * 可以通过条件表达式动态控制是否进行填充
     */
    @XmlAttribute
    private boolean enabled = true;

    /**
     * 填充启用条件表达式
     * 当表达式结果为true时才进行填充
     * 例如："remainingLength > 0", "needPadding == true"
     */
    @XmlAttribute
    private String enableCondition;

    /**
     * 填充描述信息
     */
    @XmlAttribute
    private String description;

    /**
     * 默认构造函数
     */
    public PaddingConfig() {
    }

    /**
     * 固定长度填充构造函数
     *
     * @param targetLength 目标长度（位数）
     * @param paddingValue 填充值
     */
    public PaddingConfig(int targetLength, String paddingValue) {
        this.paddingType = PaddingType.FIXED_LENGTH;
        this.targetLength = targetLength;
        this.paddingValue = paddingValue;
    }

    /**
     * 动态填充构造函数
     *
     * @param lengthExpression 长度计算表达式
     * @param paddingValue     填充值
     */
    public PaddingConfig(String lengthExpression, String paddingValue) {
        this.paddingType = PaddingType.DYNAMIC;
        this.lengthExpression = lengthExpression;
        this.paddingValue = paddingValue;
    }

    /**
     * 创建容器填充配置（自动计算容器长度）
     *
     * @param containerNode 容器节点引用
     * @param paddingValue  填充值
     * @return 容器填充配置
     */
    public static PaddingConfig createContainerPadding(String containerNode, String paddingValue) {
        PaddingConfig config = new PaddingConfig();
        config.paddingType = PaddingType.FILL_CONTAINER;
        config.containerNode = containerNode;
        config.paddingValue = paddingValue;
        config.autoCalculateContainerLength = true;
        return config;
    }

    /**
     * 创建容器填充配置（指定固定长度）
     *
     * @param containerFixedLength 容器固定长度
     * @param paddingValue         填充值
     * @return 容器填充配置
     */
    public static PaddingConfig createContainerPaddingWithFixedLength(int containerFixedLength, String paddingValue) {
        PaddingConfig config = new PaddingConfig();
        config.paddingType = PaddingType.FILL_CONTAINER;
        config.containerFixedLength = containerFixedLength;
        config.paddingValue = paddingValue;
        config.autoCalculateContainerLength = false;
        return config;
    }

    /**
     * 验证填充配置的有效性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        switch (paddingType) {
            case FIXED_LENGTH:
            case ALIGNMENT:
                return targetLength > 0;
            case DYNAMIC:
                return lengthExpression != null && !lengthExpression.trim().isEmpty();
            case FILL_CONTAINER:
                if (autoCalculateContainerLength) {
                    return containerNode != null && !containerNode.trim().isEmpty();
                } else {
                    return containerFixedLength > 0;
                }
            default:
                return false;
        }
    }

    /**
     * 获取填充值的字节数组
     *
     * @return 填充值的字节数组
     */
    public byte[] getPaddingBytes() {
        if (paddingValue == null || paddingValue.trim().isEmpty()) {
            return new byte[]{0x00};
        }

        String value = paddingValue.trim();
        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }

        try {
            // 处理单字节值
            if (value.length() <= 2) {
                return new byte[]{(byte) Integer.parseInt(value, 16)};
            }

            // 处理多字节值
            if (value.length() % 2 != 0) {
                value = "0" + value; // 补齐到偶数位
            }

            byte[] bytes = new byte[value.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
            }
            return bytes;

        } catch (NumberFormatException e) {
            // 如果解析失败，返回默认值
            return new byte[]{0x00};
        }
    }

    @Override
    public String toString() {
        return String.format("PaddingConfig{type=%s, targetLength=%d, value='%s', enabled=%s}",
                paddingType, targetLength, paddingValue, enabled);
    }
} 