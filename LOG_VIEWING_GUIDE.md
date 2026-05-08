# Android 日志查看指南

## 概述
本指南介绍如何在 APK 安装后查看应用中的日志输出，特别是 `HttpDataRepository` 中的调试日志。

## 方法一：使用 Android Studio Logcat

### 步骤 1: 连接设备
1. 确保手机已开启 USB 调试模式
2. 使用 USB 线连接手机和电脑
3. 在手机上允许 USB 调试授权

### 步骤 2: 打开 Logcat
1. 打开 Android Studio
2. 点击底部工具栏的 "Logcat" 标签页
3. 确保设备选择器中显示您的设备

### 步骤 3: 过滤日志
在 Logcat 窗口顶部的过滤器中输入：
```
HttpDataRepository
```

### 步骤 4: 设置日志级别
点击日志级别下拉菜单，选择 "Debug" 或 "Verbose" 以查看所有调试信息。

## 方法二：使用命令行

### 基本命令
```bash
# 查看所有日志
adb logcat

# 查看 HttpDataRepository 标签的日志
adb logcat -s "HttpDataRepository"

# 查看 DEBUG 级别及以上的日志
adb logcat *:D

# 查看 VERBOSE 级别的日志
adb logcat *:V

# 实时查看日志（推荐）
adb logcat -c && adb logcat -s "HttpDataRepository"
```

### 高级过滤
```bash
# 查看特定级别的日志
adb logcat *:D | findstr "HttpDataRepository"

# 过滤出包含特定关键词的日志
adb logcat | findstr "queryVideoUrl"

# 保存日志到文件
adb logcat -s "HttpDataRepository" > logs.txt
```

## 方法三：使用批处理脚本

### 运行日志查看脚本
1. 双击 `log_helper.bat` 文件
2. 脚本会自动过滤并显示 `HttpDataRepository` 的日志
3. 按 Ctrl+C 停止查看

### 自定义脚本
您可以修改 `log_helper.bat` 文件来过滤特定的日志内容：

```batch
@echo off
echo 查询视频URL的日志...
adb logcat -s "HttpDataRepository" | findstr "queryVideoUrl"
pause
```

## 方法四：在设备上直接查看（需要 root）

### 使用 ADB Shell
```bash
# 进入设备 shell
adb shell

# 查看日志
logcat -s "HttpDataRepository"
```

## 常见问题解决

### 问题 1: 设备未连接
```bash
# 检查设备连接状态
adb devices

# 如果没有设备，重启 ADB 服务器
adb kill-server
adb start-server
```

### 问题 2: 日志不显示
1. 确保应用正在运行
2. 检查 USB 调试是否已启用
3. 尝试清除日志缓存：`adb logcat -c`

### 问题 3: 日志太多难以阅读
```bash
# 清除日志后重新开始
adb logcat -c

# 只显示最近的日志
adb logcat -t 1
```

## 日志格式说明

您看到的日志格式通常如下：
```
I/HttpDataRepository: queryVideoUrl - episodeId: 12345
I/HttpDataRepository: queryVideoUrl - 视频页面URL: https://www.5dy0.top/vodplay/12345.html
```

- `I`: 表示 INFO 级别
- `HttpDataRepository`: 日志标签
- 后面是实际的日志内容

## 调试技巧

### 1. 筛选关键信息
```bash
# 只查看包含特定关键词的日志
adb logcat | findstr "episodeId\|videoPageUrl"
```

### 2. 实时监控
```bash
# 使用 tail -f 风格的实时监控
adb logcat -s "HttpDataRepository" | grep -E "(episodeId|videoPageUrl|API响应)"
```

### 3. 保存日志用于分析
```bash
# 保存到文件
adb logcat -s "HttpDataRepository" > app_logs.txt

# 使用时间戳保存
adb logcat -s "HttpDataRepository" > app_logs_$(date +%Y%m%d_%H%M%S).txt
```

## 推荐的工作流程

1. **连接设备**：确保设备已连接并启用 USB 调试
2. **启动应用**：打开您的应用并重现问题
3. **查看日志**：使用 Android Studio 或命令行查看日志
4. **分析日志**：根据日志内容分析问题原因
5. **解决问题**：根据日志信息修改代码
6. **重新测试**：重新编译安装 APK，重复测试

## 注意事项

1. **性能影响**：频繁的日志输出可能会影响应用性能
2. **安全考虑**：避免在日志中输出敏感信息
3. **日志级别**：发布版本时考虑调整日志级别以减少日志量
4. **存储空间**：长时间运行可能会产生大量日志，注意存储空间