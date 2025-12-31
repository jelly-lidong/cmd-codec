package com.iecas.cmd.test;

import com.iecas.cmd.annotation.ProtocolBody;
import com.iecas.cmd.annotation.ProtocolDefinition;
import com.iecas.cmd.annotation.ProtocolHeader;
import com.iecas.cmd.annotation.ProtocolNode;
import com.iecas.cmd.annotation.ProtocolTail;
import com.iecas.cmd.model.enums.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Slf4j
@ProtocolDefinition(id = "simpleNestedProto", name = "简单嵌套协议")
public class SimpleNestedProto {

    @ProtocolHeader(id = "h1", name = "头部", order = 1)
    private H1 h1;
    @ProtocolBody(id = "b1", name = "主体", order = 2)
    private B1 b1;
    @ProtocolTail(id = "t1", name = "尾部", order = 3)
    private T1 t1;


    // 测试各种类型输入
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class H1 {

        //使用位域定义头部字段
        @ProtocolNode(id = "h1Field1", name = "H1字段1", length = 1, valueType = ValueType.BIT, value = "0b1", order = 1)
        private String h1Field1;

        @ProtocolNode(id = "h1Field2", name = "H1字段2", length = 4, valueType = ValueType.BIT, value = "0b1001", order = 2)
        private String h1Field2;
        @ProtocolNode(id = "h1Field3", name = "H1字段3", length = 3, valueType = ValueType.BIT, value = "0b111", order = 3)
        private String h1Field3;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class B1 {
        @ProtocolHeader(id = "h2", name = "B1头部", order = 1)
        private H2 h2;
        @ProtocolBody(id = "b2", name = "B1主体", order = 2)
        private B2 b2;
        @ProtocolTail(id = "t2", name = "B1尾部", order = 3)
        private T2 t2;

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class H2 {

            @ProtocolNode(id = "h2Field1", name = "H2字段1", length = 1, valueType = ValueType.BIT, value = "0b1", order = 1)
            private String h2Field1;

            @ProtocolNode(id = "h2Field2", name = "H2字段2", length = 4, valueType = ValueType.BIT, value = "0b1001", order = 2)
            private String h2Field2;

            @ProtocolNode(id = "h2Field3", name = "H2字段3", length = 3, valueType = ValueType.BIT, value = "0b111", order = 3)
            private String h2Field3;

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class B2 {

            @ProtocolHeader(id = "h3", name = "B2头部", order = 1)
            private H3 h3;

            @ProtocolBody(id = "b3", name = "B2主体", order = 2)
            private B3 b3;

            @ProtocolTail(id = "t3", name = "B2尾部", order = 3)
            private T3 t3;


            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @Data
            public static class H3 {

                @ProtocolNode(id = "h3Field1", name = "H3字段1", length = 1, valueType = ValueType.BIT, value = "0b1", order = 1)
                private String h3Field1;

                @ProtocolNode(id = "h3Field2", name = "H3字段2", length = 4, valueType = ValueType.BIT, value = "0b1001", order = 2)
                private String h3Field2;

                @ProtocolNode(id = "h3Field3", name = "H3字段3", length = 3, valueType = ValueType.BIT, value = "0b111", order = 3)
                private String h3Field3;


            }

            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @Data
            public static class B3 {

                @ProtocolNode(id = "b3Field1", name = "B3字段1", length = 1, valueType = ValueType.BIT, value = "0b1", order = 1)
                private String b3Field1;

                @ProtocolNode(id = "b3Field2", name = "B3字段2", length = 4, valueType = ValueType.BIT, value = "0b1001", order = 2)
                private String b3Field2;

                @ProtocolNode(id = "b3Field3", name = "B3字段3", length = 3, valueType = ValueType.BIT, value = "0b111", order = 3)
                private String b3Field3;


            }

            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @Data
            public static class T3 {

                @ProtocolNode(id = "t3Field1", name = "T3字段1", length = 16, valueType = ValueType.HEX, value = "0xCC", order = 1)
                private String t3Field1;

            }

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class T2 {

            @ProtocolNode(id = "t2Field1", name = "T2字段1", length = 16, valueType = ValueType.HEX, value = "0xBB", order = 1)
            private String t2Field1;
        }

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class T1 {
        @ProtocolNode(id = "t1Field1", name = "T1字段1", length = 16, valueType = ValueType.HEX, value = "0xAA", order = 1)
        private String t1Field1;
    }
}
