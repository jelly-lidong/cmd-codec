# BitUtil 工具类使用说明

## 概述

`BitUtil` 是一个功能完整的位操作工具类，提供了丰富的位级数据处理功能，包括基本位操作、位范围操作、移位操作、循环移位、位计数、位搜索、字节数组转换、十六进制转换等功能。

## 功能特性

### 1. 基本位操作
- `setBit(long value, int position)` - 设置指定位置的位为1
- `clearBit(long value, int position)` - 清除指定位置的位为0
- `toggleBit(long value, int position)` - 切换指定位置的位
- `testBit(long value, int position)` - 测试指定位置的位是否为1

### 2. 位范围操作
- `getBits(long value, int startBit, int endBit)` - 获取指定范围的位
- `setBits(long value, int startBit, int endBit, long newValue)` - 设置指定范围的位

### 3. 移位操作
- `leftShift(long value, int positions)` - 左移位
- `rightShift(long value, int positions)` - 逻辑右移位
- `arithmeticRightShift(long value, int positions)` - 算术右移位

### 4. 循环移位
- `rotateLeft(long value, int positions, int bitLength)` - 循环左移
- `rotateRight(long value, int positions, int bitLength)` - 循环右移

### 5. 位计数
- `countOnes(long value)` - 计算1的个数
- `countZeros(long value)` - 计算0的个数
- `countLeadingZeros(long value)` - 计算前导0的个数
- `countTrailingZeros(long value)` - 计算尾随0的个数

### 6. 位搜索
- `findFirstSetBit(long value)` - 查找第一个设置的位
- `findLastSetBit(long value)` - 查找最后一个设置的位

### 7. 字节数组和位字符串转换
- `toByteArray(long value, int bitLength)` - 转换为字节数组
- `fromByteArray(byte[] bytes)` - 从字节数组转换
- `toBinaryString(long value, int bitLength)` - 转换为二进制字符串
- `fromBinaryString(String binaryString)` - 从二进制字符串转换

### 8. 字节位操作
- `setBitInByte(byte value, int position)` - 在字节中设置位
- `clearBitInByte(byte value, int position)` - 在字节中清除位
- `testBitInByte(byte value, int position)` - 在字节中测试位

### 9. **新增：位与字节数组转换**
- `bitsToByteArray(long value, int bitLength)` - 将位值转换为字节数组
- `byteArrayToBits(byte[] bytes)` - 将字节数组转换为位值

### 10. **新增：位与十六进制字符串转换**
- `bitsToHexString(long value, int bitLength)` - 将位值转换为十六进制字符串
- `hexStringToBits(String hexString)` - 将十六进制字符串转换为位值

### 11. **新增：字节数组与十六进制字符串转换**
- `byteArrayToHexString(byte[] bytes, boolean uppercase)` - 将字节数组转换为十六进制字符串
- `byteArrayToHexString(byte[] bytes)` - 将字节数组转换为十六进制字符串（默认大写）
- `hexStringToByteArray(String hexString)` - 将十六进制字符串转换为字节数组

### 12. **新增：位字符串与十六进制字符串转换**
- `bitStringToHexString(String bitString)` - 将二进制字符串转换为十六进制字符串
- `hexStringToBitString(String hexString)` - 将十六进制字符串转换为二进制字符串

## 使用示例

### 基本位操作
```java
// 设置第3位为1
long value = BitUtil.setBit(0L, 3); // 结果: 8 (1000)

// 清除第3位
value = BitUtil.clearBit(value, 3); // 结果: 0

// 切换第0位
value = BitUtil.toggleBit(5L, 0); // 结果: 4 (101 -> 100)

// 测试第2位是否为1
boolean isSet = BitUtil.testBit(5L, 2); // 结果: true
```

### 位范围操作
```java
// 获取第1-3位的值
long bits = BitUtil.getBits(0b11010110L, 1, 3); // 结果: 3 (011)

// 设置第4-6位为5
long value = BitUtil.setBits(0L, 4, 6, 5L); // 结果: 80 (01010000)
```

### **新增：位与字节数组转换**
```java
// 位值转字节数组
long value = 0x12345678L;
byte[] bytes = BitUtil.bitsToByteArray(value, 32);
// 结果: [0x12, 0x34, 0x56, 0x78]

// 字节数组转位值
byte[] bytes = {0x12, 0x34, 0x56, 0x78};
long value = BitUtil.byteArrayToBits(bytes);
// 结果: 0x12345678L
```

### **新增：位与十六进制字符串转换**
```java
// 位值转十六进制字符串
long value = 0xABCDL;
String hex = BitUtil.bitsToHexString(value, 16);
// 结果: "0xABCD"

// 十六进制字符串转位值
String hex = "0xABCD";
long value = BitUtil.hexStringToBits(hex);
// 结果: 0xABCDL

// 支持不带前缀的格式
long value2 = BitUtil.hexStringToBits("ABCD");
// 结果: 0xABCDL
```

### **新增：字节数组与十六进制字符串转换**
```java
// 字节数组转十六进制字符串
byte[] bytes = {(byte)0xAB, (byte)0xCD, (byte)0xEF};
String hex = BitUtil.byteArrayToHexString(bytes);
// 结果: "0xABCDEF"

// 指定大小写
String hexLower = BitUtil.byteArrayToHexString(bytes, false);
// 结果: "0xabcdef"

// 十六进制字符串转字节数组
String hex = "0xABCDEF";
byte[] bytes = BitUtil.hexStringToByteArray(hex);
// 结果: [(byte)0xAB, (byte)0xCD, (byte)0xEF]

// 支持奇数长度（自动补零）
byte[] bytes2 = BitUtil.hexStringToByteArray("0xABC");
// 结果: [(byte)0x0A, (byte)0xBC]
```

### **新增：位字符串与十六进制字符串转换**
```java
// 位字符串转十六进制字符串
String bitString = "10101011";
String hex = BitUtil.bitStringToHexString(bitString);
// 结果: "0xAB"

// 十六进制字符串转位字符串
String hex = "0xAB";
String bitString = BitUtil.hexStringToBitString(hex);
// 结果: "10101011"

// 支持不对齐的位字符串
String hex2 = BitUtil.bitStringToHexString("101");
// 结果: "0x5"
```

### 移位操作
```java
// 左移3位
long value = BitUtil.leftShift(1L, 3); // 结果: 8

// 逻辑右移2位
value = BitUtil.rightShift(20L, 2); // 结果: 5

// 算术右移（保持符号位）
value = BitUtil.arithmeticRightShift(-8L, 2); // 结果: -2
```

### 循环移位
```java
// 在8位范围内循环左移4位
long value = BitUtil.rotateLeft(0b00001011L, 4, 8);
// 结果: 0b10110000L

// 在8位范围内循环右移3位
value = BitUtil.rotateRight(0b00001011L, 3, 8);
// 结果: 0b01100001L
```

### 位计数
```java
// 计算1的个数
int ones = BitUtil.countOnes(7L); // 结果: 3 (111)

// 计算前导0的个数
int leadingZeros = BitUtil.countLeadingZeros(8L); // 结果: 60

// 计算尾随0的个数
int trailingZeros = BitUtil.countTrailingZeros(8L); // 结果: 3
```

### 位搜索
```java
// 查找第一个设置的位
int firstBit = BitUtil.findFirstSetBit(12L); // 结果: 2 (1100)

// 查找最后一个设置的位
int lastBit = BitUtil.findLastSetBit(12L); // 结果: 3
```

### 字节数组和位字符串转换
```java
// 转换为字节数组
byte[] bytes = BitUtil.toByteArray(0x1234L, 16);
// 结果: [0x12, 0x34]

// 从字节数组转换
long value = BitUtil.fromByteArray(new byte[]{0x12, 0x34});
// 结果: 0x1234L

// 转换为二进制字符串
String binary = BitUtil.toBinaryString(5L, 8);
// 结果: "00000101"

// 从二进制字符串转换
long value = BitUtil.fromBinaryString("101");
// 结果: 5L
```

### **综合转换示例**
```java
// 复杂的往返转换
long original = 0x12AB34CDL;

// 位值 -> 字节数组 -> 十六进制 -> 字节数组 -> 位值
byte[] bytes1 = BitUtil.bitsToByteArray(original, 32);
String hex = BitUtil.byteArrayToHexString(bytes1);
byte[] bytes2 = BitUtil.hexStringToByteArray(hex);
long result = BitUtil.byteArrayToBits(bytes2);

// 验证：original == result (true)
```

## 错误处理

所有方法都包含适当的参数验证：
- 位置参数必须在有效范围内（0-63）
- 位长度必须为正数且不超过64
- 字节数组长度不能超过8字节（对于转换为long的操作）
- 十六进制字符串必须包含有效的十六进制字符
- 二进制字符串必须只包含0和1字符

无效参数会抛出 `IllegalArgumentException` 异常。

## 性能特点

- 所有操作都基于高效的位运算
- 避免不必要的对象创建
- 内存使用效率高
- 适合高频调用场景

## 测试覆盖

工具类包含全面的测试用例，覆盖：
- 所有方法的基本功能
- 边界条件测试
- 异常情况处理
- **新增功能的往返转换验证**
- **各种格式的兼容性测试**

## 应用场景

- 协议编码和解码
- 位掩码操作
- 数据压缩
- 加密算法
- 网络数据包处理
- **二进制数据格式转换**
- **调试和数据可视化**
- **协议分析工具**

## 版本信息

- **当前版本**: 2.0
- **Java版本要求**: Java 8+
- **依赖**: 无外部依赖
- **更新内容**: 
  - 新增位与字节数组转换功能
  - 新增位与十六进制字符串转换功能
  - 新增字节数组与十六进制字符串转换功能
  - 新增位字符串与十六进制字符串转换功能
  - 增强了数据格式转换的完整性
  - 提供了更丰富的调试和分析工具

## 重要说明

1. **位序**: 所有位操作都使用小端序（LSB为第0位）
2. **线程安全**: 所有方法都是静态的，线程安全
3. **性能**: 基于原生位运算，性能优异
4. **内存**: 避免不必要的对象创建，内存效率高
5. **兼容性**: 支持Java 8及以上版本
6. **格式支持**: 
   - 十六进制字符串支持"0x"前缀（可选）
   - 支持大小写十六进制字符
   - 自动处理奇数长度的十六进制字符串
   - 支持空字节数组和空字符串的处理