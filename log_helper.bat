@echo off
echo 查看 HttpDataRepository 的日志...
echo 请确保设备已连接并启用 USB 调试
adb logcat -s "HttpDataRepository" *:S
pause