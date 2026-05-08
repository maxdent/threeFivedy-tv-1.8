@echo off
echo ========================================
echo 视频URL查询日志查看器
echo ========================================
echo.
echo 正在查看 queryVideoUrl 相关日志...
echo 按 Ctrl+C 停止查看
echo.

:: 清除之前的日志，重新开始
adb logcat -c

:: 查询视频URL相关的所有日志
adb logcat -s "HttpDataRepository" | findstr /i "queryVideoUrl\|episodeId\|videoPageUrl\|API响应\|最终播放地址"

echo.
echo 查看完成！
pause