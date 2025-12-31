#!/bin/bash

# TCP连接测试运行脚本

echo "🚀 启动TCP连接测试..."
echo "====================================="

# 设置Java类路径
CLASSPATH="target/classes:target/dependency/*"

# 编译项目（如果需要）
echo "📦 编译项目..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "✅ 编译成功"
echo ""

# 运行简单测试
echo "🧪 运行简单TCP测试..."
java -cp "$CLASSPATH" com.iecas.cmd.network.tcp.SimpleTcpTest

if [ $? -eq 0 ]; then
    echo "✅ 简单测试完成"
else
    echo "❌ 简单测试失败"
fi

echo ""

# 询问是否运行完整测试
read -p "是否运行完整连接测试？(y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧪 运行完整TCP连接测试..."
    java -cp "$CLASSPATH" com.iecas.cmd.network.tcp.TcpConnectionTest
fi

echo "====================================="
echo "🎉 所有测试完成"
