## com.iecas.cmd.aviator 自定义函数使用说明（完整）

概述
- 所有函数在引擎初始化时按包名递归自动注册，直接在 Aviator 表达式中使用。
- 约定：字符串使用引号；HEX 为不带 0x 前缀的大写；布尔返回常用 1/0 以便参与数值表达式。

目录
1. bytes/ 字节与位操作
2. codec/ 编解码与进制转换
3. network/ IP 与 MAC 转换
4. tlv/ TLV 构造与解析
5. varint/ 变长整数
6. util/ 字符串与对齐
7. validator/ 校验函数
8. time/ 相对时间编解码
9. node/ 协议节点与结构体访问
10. 综合示例

---

### 1. bytes/ 字节与位操作

1) getBit(value, index)
- 使用场景：读取整型值的某一位标志（LSB=0）。
- 作用：返回第 index 位的比特（0/1）。
- 示例：getBit(0b1010, 1) -> 1

2) extractBits(value, offset, length)
- 使用场景：从整型中抽取位域编码的字段。
- 作用：返回位段的无符号整数（LSB=0）。
- 示例：extractBits(0xABCD, 4, 8)

3) hasFlag(value, mask)
- 使用场景：判断多标志位是否都被置位。
- 作用：value&mask==mask 时返回 1，否则 0。
- 示例：hasFlag(0x13, 0x11) -> 1

4) setFlag(value, mask, enabled)
- 使用场景：动态设置/清除位标志。
- 作用：enabled!=0 时置位，否则清零。
- 示例：setFlag(0x10, 0x02, 1) -> 0x12

5) swapEndian16(value), swapEndian32(value)
- 使用场景：大小端转换（网络序/主机序）。
- 作用：交换 16/32 位整数的字节顺序。
- 示例：swapEndian16(0x1234) -> 0x3412

6) toBytes(str[, charset])
- 使用场景：将文本载荷转换为字节。
- 作用：字符串 -> byte[]（默认 UTF-8）。
- 示例：toBytes("AB")

7) bytesConcat(b1, b2[, b3[, b4]])
- 使用场景：拼包、组帧等需要合并字节数组。
- 作用：连接多个 byte[]。
- 示例：bytesConcat(toBytes("A"), toBytes("B"))

8) bytesSlice(bytes, offset, length)
- 使用场景：按字节偏移裁剪数据段。
- 作用：返回 bytes[offset:offset+length]。
- 示例：bytesSlice(hexToBytes("01020304"), 1, 2) -> 0x0203

---

### 2. codec/ 编解码与进制转换

1) toHex(value|str[, charset]) / fromHex(hex)
- 场景：十六进制查看/传输。
- 作用：任意数值或字符串与 HEX 互转（默认 UTF-8）。
- 示例：toHex(4660)->"1234"; toHex("AB")->"4142"; fromHex("313233")->"123"

2) encode(str, charset) / decode(hex, charset)
- 场景：指定字符集互转（GBK、UTF-8 等）。
- 作用：str<->hex（带字符集）。

3) Base64 系列：base64Encode(str[, cs]), base64Decode(b64[, cs])
- 场景：文本/二进制互转。
- 作用：字符串与 Base64 文本互转。

4) HEX 与 Base64：hexToBase64(hex), base64ToHex(b64)
- 场景：二进制报文在不同文本编码之间互转。

5) BCD：toBCD(digits), fromBCD(hex)
- 场景：BCD 表示的数字字段（如时间/号码）。
- 作用：数字字符串与 BCD 的 HEX 互转。
- 示例：toBCD("123")->"0123"; fromBCD("0123")->"123"

6) HEX 处理：hexConcat(a, b), hexSlice(hex, offsetBytes, lengthBytes)
- 场景：报文 HEX 字符串的拼接/截取。

7) HEX 与 byte[]：hexToBytes(hex), bytesToHex(bytes)
- 场景：文本 HEX 与原始字节互转。

---

### 3. network/ IP 与 MAC 转换

1) ipToHex(ip) / hexToIp(hex)
- 场景：IPv4 与 4 字节字段互转。
- 示例：ipToHex("192.168.1.10")->"C0A8010A"

2) macToHex(mac) / hexToMac(hex)
- 场景：MAC 地址与 6 字节字段互转。
- 示例：macToHex("AA:BB:CC:DD:EE:FF")->"AABBCCDDEEFF"

---

### 4. tlv/ TLV 构造与解析

1) makeTLV1(tagHex, valueHex) / parseTLV1(tlvHex, field)
- 场景：Length=1 字节的 TLV 数据。
- 作用：构造/解析；field 为 "T"|"L"|"V"。

2) makeTLV2(tagHex, valueHex) / parseTLV2(tlvHex, field)
- 场景：Length=2 字节（大端）的 TLV 数据。
- 示例：makeTLV1("01","0A0B")->"01020A0B"

---

### 5. varint/ 变长整数

1) varintEncode(value) / varintDecode(hex)
- 场景：7 位分组，最高位续位标志的变长编码（如 protobuf varint）。
- 示例：varintEncode(300)->"AC02"; varintDecode("AC02")->300

---

### 6. util/ 字符串与对齐

1) padLeft(str, len, ch) / padRight(str, len, ch)
- 场景：定长字段填充。

2) substr(str, start, len)
- 场景：字符串子串提取。

3) align(value, boundary) / alignPadding(value, boundary)
- 场景：报文字段边界对齐与补齐。
- 示例：align(13,4)->16；alignPadding(13,4)->3

4) clamp(value, min, max)
- 场景：范围裁剪（安全计算）。

---

### 7. validator/ 校验函数

1) crc16(str[, charset])
- 场景：对字符串载荷计算 CRC16。
- 说明：参见 `CRC16Function` 实现细节与注释。

2) BCHFunction（BCH 相关）
- 场景：按协议设计的 BCH 校验。
- 说明：参见源码注释与使用示例。

---

### 8. time/ 相对时间编解码

- RelativeDay/Second/Millisecond/Week... Encode/Decode 系列
- 场景：协议中常见的相对时间（天+秒、周+秒、毫秒等）字段编解码。
- 说明：各类文件内包含清晰注释与示例。

---

### 9. node/ 协议节点与结构体访问

通用：nodeId 支持 id/name/path；若涉及组/数组，请确保上下文中有完整节点树及位置信息（startBitPosition/endBitPosition）。

A) 节点值与存在性
- nodeValue(nodeId)
  - 场景：在表达式中复用节点值。
  - 返回：节点当前值（String/Number/byte[] 等）。
  - 示例：nodeValue('body.field1')
- hasNode(nodeId)
  - 场景：条件计算时判定节点是否可用。
  - 返回：1/0。
- isEmpty(nodeId)
  - 场景：校验空值/零长。
  - 返回：1/0。

B) 长度/偏移/边界
- nodeLength(nodeId)
  - 场景：按实际位范围计算字节长度（无则回退定义长度）。
- nodeBitLength(nodeId)
  - 场景：比特粒度的长度计算。
- nodeOffset(nodeId) / nodeEndOffset(nodeId)
  - 场景：按位范围映射到字节偏移（起始/结束，上取整）。
- alignNode(nodeId, boundary) / paddingForNode(nodeId, boundary)
  - 场景：以节点末尾为基准做对齐与补齐测算。

C) 结构体/数组访问
- field(structId, fieldName)
  - 场景：读取结构体子字段（优先 getChild，再按 id/name 回退）。
  - 示例：field('header','version')
- indexOfNode(groupId, nodeId)
  - 场景：查找元素在组内的次序（0 基）。
- listSize(groupId)
  - 场景：获取重复组/数组长度。
- elem(groupId, idx, fieldName)
  - 场景：读取数组第 idx 个元素的某字段值。

D) 节点范围数据
- bytesOf(nodeId) / hexOf(nodeId)
  - 场景：直接获取节点序列化后的原始字节/HEX。
- sliceByNodes(startNodeId, endNodeId)
  - 场景：以节点为边界切片一段字节。

E) 依赖/条件
- enabled(nodeId)
  - 场景：确定节点是否被条件启用。
- dependsOn(nodeId, depNodeId)
  - 场景：简单依赖判断（存在且启用）。
- when(nodeId, expr)
  - 场景：临时表达式判定启用；自动注入 value/node。
  - 示例：when('body.mode', "value == 1")

F) 校验/摘要
- crc16Of(nodeId) / checksumOf(nodeId) / xorOf(nodeId)
  - 场景：对节点字节做 CRC16/累加和/异或校验。
- crc16Between(startNodeId, endNodeId)
  - 场景：按节点边界的区间做 CRC16。
- hashOf(nodeId, algo)
  - 场景：计算 MD5/SHA-1/SHA-256 等摘要（HEX 输出）。

G) 编码/数值转换
- encodeNode(nodeId, charset) / decodeNode(nodeId, charset)
  - 场景：节点值与字节在指定字符集之间互转。
- asInt(nodeId, signed, endian, bitOffset, bitLen)
  - 场景：从节点原始字节截取任意位段解析成整数；bitLen 字节对齐时支持大小端。
  - 示例：asInt('body.flags', 0, 'BIG', 3, 5)
- asFloat(nodeId, endian)
  - 场景：按 4/8 字节解析 float/double（返回字符串）。
- asBCD(nodeId)
  - 场景：节点字节存储为 BCD 时解析为数字字符串。

---

### 10. 综合示例
```
// HEX、长度与 CRC
hexOf('body')
nodeLength('body')
crc16Of('body')

// 结构体与数组访问
field('header','version')
listSize('params')
elem('params', 0, 'id')

// 位段与标志位
extractBits(0xABCD, 4, 8)
hasFlag(0x13, 0x11)

// 位段解析为整数
asInt('payload', 1, 'LITTLE', 0, 16)

// 条件启用检查
when('body.mode', "value == 1")
```

备注
- 以上示例需在运行时上下文中存在对应节点对象；节点匹配支持 id/name/path。
- 某些高级函数（如时间、BCH）请参考相应类源码注释以获取更细的业务背景与边界条件说明。


