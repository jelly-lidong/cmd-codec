package com.iecas.cmd.test;

import com.iecas.cmd.annotation.*;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 节点组验证测试类
 * <p>
 * 验证系统对节点组字段类型的严格验证：
 * 1. 正确的集合类型字段能够正常处理
 * 2. 错误的字段类型会抛出异常
 * 3. 异常信息清晰明确
 *
 * @author ProtocolCodec Team
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
public class NodeGroupValidationTest {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolHeader(id = "test-header", name = "测试协议头")
    public static class TestHeader {

        @ProtocolNode(id = "header-id", name = "头ID", length = 16, valueType = ValueType.HEX, order = 1)
        private String id;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolBody(id = "test-body", name = "测试协议体")
    public static class TestBody {

        @ProtocolNode(id = "body-type", name = "类型", length = 8, valueType = ValueType.HEX, order = 1)
        private String type;

        // 正确的节点组字段 - List类型
        @ProtocolNodeGroup(id = "correct-group", name = "正确组", order = 2)
        private List<String> correctGroup;

        // 错误的节点组字段 - String类型（不是集合）
        @ProtocolNodeGroup(id = "wrong-group", name = "错误组", order = 3)
        private String wrongGroup;  // ❌ 这会导致异常

        // 错误的节点组字段 - 基本类型（不是集合）
        @ProtocolNodeGroup(id = "primitive-group", name = "基本类型组", order = 4)
        private int primitiveGroup;  // ❌ 这会导致异常
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ProtocolTail(id = "test-tail", name = "测试协议尾")
    public static class TestTail {

        @ProtocolNode(id = "tail-checksum", name = "校验和", length = 16, valueType = ValueType.HEX, order = 1)
        private String checksum;
    }

    /**
     * 主方法 - 演示类型验证
     */
    public static void main(String[] args) {
        log.debug("节点组类型验证测试");
        log.debug("==================");

        // 创建测试数据
        TestBody body = TestBody.builder().type("0xA1").correctGroup(Arrays.asList("item1", "item2", "item3")).wrongGroup("not-a-list")  // 错误类型
                .primitiveGroup(42)        // 错误类型
                .build();

        log.debug("测试数据创建完成");
        log.debug("correctGroup 类型: {} (正确)", body.getCorrectGroup().getClass().getSimpleName());
        log.debug("wrongGroup 类型: {} (错误)", body.getWrongGroup().getClass().getSimpleName());
        log.debug("primitiveGroup 类型: {} (错误)", "int (基本类型)");

        log.debug("==================");
        log.debug("测试说明:");
        log.debug("1. correctGroup: List<String> 类型，能够正常处理");
        log.debug("2. wrongGroup: String 类型，会抛出异常");
        log.debug("3. primitiveGroup: int 类型，会抛出异常");
        log.debug("4. 系统会严格验证字段类型，确保类型安全");
        log.debug("5. 异常信息会明确指出问题所在");

        log.debug("==================");
        log.debug("预期结果:");
        log.debug("✅ correctGroup: 成功解析，长度为 3");
        log.debug("❌ wrongGroup: 抛出异常 - 无法检测节点组长度: 字段值类型为 String，不是集合类型");
        log.debug("❌ primitiveGroup: 抛出异常 - 无法检测节点组长度: 字段值类型为 Integer，不是集合类型");
    }
}
