#!/bin/bash

# 禾启吊舱控制器构建脚本

echo "=========================================="
echo "禾启吊舱控制器 - 构建脚本"
echo "=========================================="

# 检查Gradle
if [ ! -f "./gradlew" ]; then
    echo "正在生成Gradle Wrapper..."
    gradle wrapper --gradle-version 8.0
fi

# 清理构建
echo ""
echo "清理旧构建..."
./gradlew clean

# 构建Debug版本
echo ""
echo "构建Debug版本..."
./gradlew assembleDebug

# 检查构建结果
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "构建成功!"
    echo "=========================================="
    echo "APK位置: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "安装到设备:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "=========================================="
    echo "构建失败!"
    echo "=========================================="
    exit 1
fi
