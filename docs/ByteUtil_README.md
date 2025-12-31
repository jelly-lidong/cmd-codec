# ByteUtil 字节操作工具类

## 概述

`ByteUtil` 是一个功能强大的Java字节操作工具类，提供了全面的字节数组与各种数据类型之间的转换功能。该工具类专为协议编解码、网络通信、数据序列化等场景设计，支持多种数据格式转换和字节序处理。

## 核心特性

### 1. 有符号整数转换
- **功能**: 支持有符号整数与字节数组的双向转换
- **字节序**: 支持大端序和小端序
- **范围**: 支持1-8字节长度的整数

```java
// 有符号整数转字节数组
byte[] bytes = ByteUtil.signedIntToBytes(0x1234, 2);  // [0x12, 0x34]
byte[] littleEndian = ByteUtil.signedIntToBytes(0x1234, 2, false);  // [0x34, 0x12]

// 字节数组转有符号整数
long value = ByteUtil.bytesToSignedInt(new byte[]{0x12, 0x34});  // 4660
long valueLE = ByteUtil.bytesToSignedInt(new byte[]{0x34, 0x12}, false);  // 4660
```

### 2. 无符号整数转换
- **功能**: 支持无符号整数与字节数组的双向转换
- **范围检查**: 自动验证值是否在有效范围内
- **溢出保护**: 防止数值溢出

```java
// 无符号整数转字节数组
byte[] bytes = ByteUtil.unsignedIntToBytes(255, 1);  // [0xFF]
byte[] bytes2 = ByteUtil.unsignedIntToBytes(65535, 2);  // [0xFF, 0xFF]

// 字节数组转无符号整数
long value = ByteUtil.bytesToUnsignedInt(new byte[]{(byte)0xFF});  // 255
```

### 3. 二进制字符串转换
- **功能**: 字节数组与二进制字符串的双向转换
- **自动补齐**: 不足8位的二进制字符串自动补齐
- **格式验证**: 严格验证二进制字符串格式

```java
// 字节数组转二进制字符串
String binary = ByteUtil.bytesToBinaryString(new byte[]{(byte)0xAB});  // "10101011"

// 二进制字符串转字节数组
byte[] bytes = ByteUtil.binaryStringToBytes("10101011");  // [0xAB]
byte[] padded = ByteUtil.binaryStringToBytes("101");  // [0x05] (自动补齐)
```

### 4. 十六进制字符串转换
- **功能**: 字节数组与十六进制字符串的双向转换
- **格式支持**: 支持带/不带"0x"前缀
- **大小写**: 支持大小写字母
- **奇数长度**: 自动处理奇数长度的十六进制字符串

```java
// 字节数组转十六进制字符串
String hex = ByteUtil.bytesToHexString(new byte[]{(byte)0xAB, (byte)0xCD});  // "0xABCD"
String hexUpper = ByteUtil.bytesToHexString(bytes, true, true);  // "0xABCD"
String hexLower = ByteUtil.bytesToHexString(bytes, false, false);  // "abcd"

// 十六进制字符串转字节数组
byte[] bytes1 = ByteUtil.hexStringToBytes("0xABCD");  // [0xAB, 0xCD]
byte[] bytes2 = ByteUtil.hexStringToBytes("ABCD");    // [0xAB, 0xCD]
byte[] bytes3 = ByteUtil.hexStringToBytes("ABC");     // [0x0A, 0xBC] (自动补齐)
```

### 5. 浮点数转换
- **功能**: 支持float和double与字节数组的双向转换
- **字节序**: 支持大端序和小端序
- **特殊值**: 正确处理无穷大、负无穷大、NaN等特殊值
- **精度保持**: 保证转换过程中的精度不丢失

```java
// float转换
float value = 3.14159f;
byte[] floatBytes = ByteUtil.floatToBytes(value);  // IEEE 754格式
byte[] floatBE = ByteUtil.floatToBytes(value, true);   // 大端序
byte[] floatLE = ByteUtil.floatToBytes(value, false);  // 小端序
float restored = ByteUtil.bytesToFloat(floatBytes);

// double转换
double dValue = 3.141592653589793;
byte[] doubleBytes = ByteUtil.doubleToBytes(dValue);
double dRestored = ByteUtil.bytesToDouble(doubleBytes);

// 特殊值处理
byte[] infBytes = ByteUtil.floatToBytes(Float.POSITIVE_INFINITY);
byte[] nanBytes = ByteUtil.floatToBytes(Float.NaN);
```

### 6. 大整数转换
- **功能**: 支持BigInteger与字节数组的双向转换
- **任意精度**: 支持任意精度的大整数
- **符号处理**: 正确处理正数、负数和零

```java
// 大整数转换
BigInteger bigInt = new BigInteger("12345678901234567890");
byte[] bigIntBytes = ByteUtil.bigIntegerToBytes(bigInt, 16);
BigInteger restored = ByteUtil.bytesToBigInteger(bigIntBytes);

// 负大整数
BigInteger negative = new BigInteger("-12345");
byte[] negBytes = ByteUtil.bigIntegerToBytes(negative, 4);
BigInteger negRestored = ByteUtil.bytesToBigInteger(negBytes);
```

### 7. 字节数组工具方法
- **反转**: 字节数组反转
- **连接**: 多个字节数组连接
- **提取**: 子数组提取
- **比较**: 字节数组比较
- **格式化**: 可读格式输出

```java
// 字节数组操作
byte[] original = {0x12, 0x34, 0x56, 0x78};
byte[] reversed = ByteUtil.reverseBytes(original);  // [0x78, 0x56, 0x34, 0x12]

byte[] array1 = {0x12, 0x34};
byte[] array2 = {0x56, 0x78};
byte[] concatenated = ByteUtil.concatBytes(array1, array2);  // [0x12, 0x34, 0x56, 0x78]

byte[] sub = ByteUtil.subBytes(original, 1, 2);  // [0x34, 0x56]

boolean equal = ByteUtil.equals(array1, new byte[]{0x12, 0x34});  // true

String readable = ByteUtil.toString(original);  // "[0x12, 0x34, 0x56, 0x78]"
```

## 使用示例

### 协议数据包构建
```java
// 构建网络协议包
byte version = 0x01;
byte type = 0x02;
int length = 1024;
String data = "Hello, World!";

// 构建协议头
byte[] protocolHeader = ByteUtil.concatBytes(
    new byte[]{version},
    new byte[]{type},
    ByteUtil.unsignedIntToBytes(length, 2)
);

// 添加数据
byte[] packet = ByteUtil.concatBytes(protocolHeader, data.getBytes());
String packetHex = ByteUtil.bytesToHexString(packet);
```

### 数据格式转换链
```java
// 复杂的数据转换链
long originalValue = 0x12345678L;

// 整数 -> 字节数组 -> 十六进制 -> 二进制 -> 字节数组 -> 整数
byte[] bytes = ByteUtil.signedIntToBytes(originalValue, 4);
String hex = ByteUtil.bytesToHexString(bytes);
byte[] hexBytes = ByteUtil.hexStringToBytes(hex);
String binary = ByteUtil.bytesToBinaryString(hexBytes);
byte[] binaryBytes = ByteUtil.binaryStringToBytes(binary);
long finalValue = ByteUtil.bytesToSignedInt(binaryBytes);

// 验证转换正确性
assert originalValue == finalValue;
```

### 网络字节序处理
```java
// 网络通信中的字节序转换
float networkFloat = 3.14159f;
double networkDouble = 2.718281828;

// 转为网络字节序（大端序）
byte[] networkFloatBytes = ByteUtil.floatToBytes(networkFloat, true);
byte[] networkDoubleBytes = ByteUtil.doubleToBytes(networkDouble, true);

// 从网络字节序恢复
float restoredFloat = ByteUtil.bytesToFloat(networkFloatBytes, true);
double restoredDouble = ByteUtil.bytesToDouble(networkDoubleBytes, true);
```

## 错误处理

### 参数验证
- **空值检查**: 所有方法都进行空值检查
- **范围验证**: 验证参数是否在有效范围内
- **格式检查**: 验证字符串格式的正确性

### 异常类型
- `IllegalArgumentException`: 参数无效时抛出
- `NumberFormatException`: 数字格式错误时抛出

```java
// 异常处理示例
try {
    // 无效的字节长度
    ByteUtil.signedIntToBytes(0, 0);  // 抛出IllegalArgumentException
} catch (IllegalArgumentException e) {
    log.error("参数错误: " + e.getMessage());
}

try {
    // 无效的十六进制字符串
    ByteUtil.hexStringToBytes("XYZ");  // 抛出IllegalArgumentException
} catch (IllegalArgumentException e) {
    log.error("格式错误: " + e.getMessage());
}
```

## 性能特性

### 高效算法
- **位运算**: 大量使用位运算提高性能
- **内存优化**: 避免不必要的对象创建
- **缓存友好**: 算法设计考虑CPU缓存效率

### 性能基准
- **整数转换**: 纳秒级别的转换速度
- **字符串转换**: 微秒级别的转换速度
- **大数据处理**: 线性时间复杂度

### 内存使用
- **零拷贝**: 尽可能避免数据拷贝
- **按需分配**: 根据实际需要分配内存
- **垃圾回收友好**: 减少临时对象创建

## 测试覆盖

### 测试范围
- **功能测试**: 覆盖所有公共方法
- **边界测试**: 测试边界条件和极值
- **异常测试**: 验证异常处理的正确性
- **往返测试**: 验证转换的可逆性

### 测试用例
- 39个测试用例，100%通过率
- 覆盖正常情况、边界情况和异常情况
- 包含性能基准测试

## 应用场景

### 1. 协议编解码
- 网络协议数据包的编码和解码
- 二进制协议的字段解析
- 自定义协议的实现

### 2. 数据序列化
- 对象的二进制序列化
- 跨平台数据交换
- 数据持久化存储

### 3. 网络通信
- 网络字节序转换
- 数据传输格式转换
- 协议适配

### 4. 文件处理
- 二进制文件读写
- 文件格式转换
- 数据压缩和解压

### 5. 加密解密
- 密钥和数据的格式转换
- 加密算法的输入输出处理
- 数字签名的数据处理

## 版本信息

- **当前版本**: 1.0.0
- **Java版本**: 兼容Java 8及以上版本
- **依赖**: 无外部依赖，仅使用Java标准库

## 重要说明

### 字节序
- **默认字节序**: 大端序（网络字节序）
- **字节序选择**: 根据应用场景选择合适的字节序
- **一致性**: 编码和解码必须使用相同的字节序

### 线程安全
- **无状态设计**: 所有方法都是静态方法，无共享状态
- **线程安全**: 可以在多线程环境中安全使用
- **并发性能**: 支持高并发访问

### 精度和范围
- **整数范围**: 支持1-8字节的整数，最大支持64位
- **浮点精度**: 严格遵循IEEE 754标准
- **大整数**: 支持任意精度的大整数运算

### 兼容性
- **平台无关**: 纯Java实现，跨平台兼容
- **版本兼容**: 向后兼容，API稳定
- **标准遵循**: 遵循Java编码规范和最佳实践

## 最佳实践

### 1. 字节序选择
```java
// 网络通信使用大端序
byte[] networkData = ByteUtil.signedIntToBytes(value, 4, true);

// 本地存储可以使用小端序（x86架构）
byte[] localData = ByteUtil.signedIntToBytes(value, 4, false);
```

### 2. 异常处理
```java
// 总是进行异常处理
try {
    byte[] result = ByteUtil.hexStringToBytes(userInput);
    // 处理结果
} catch (IllegalArgumentException e) {
    // 处理格式错误
    logger.error("Invalid hex string: " + userInput, e);
}
```

### 3. 性能优化
```java
// 对于频繁转换，考虑缓存结果
private static final Map<String, byte[]> hexCache = new ConcurrentHashMap<>();

public byte[] cachedHexToBytes(String hex) {
    return hexCache.computeIfAbsent(hex, ByteUtil::hexStringToBytes);
}
```

### 4. 数据验证
```java
// 在转换前验证数据
public void processProtocolData(byte[] data) {
    if (data == null || data.length < 4) {
        throw new IllegalArgumentException("Invalid protocolDefinition data");
    }
    
    // 安全地进行转换
    int protocolHeader = (int) ByteUtil.bytesToUnsignedInt(ByteUtil.subBytes(data, 0, 4));
    // 处理协议数据
}
```

---

**ByteUtil** 是一个经过充分测试、高性能、易于使用的字节操作工具类，为Java开发者提供了完整的字节数据处理解决方案。无论是简单的数据类型转换还是复杂的协议处理，ByteUtil都能提供可靠、高效的支持。 