package io.github.peacefulprogram.dy555.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.github.peacefulprogram.dy555.ext.showLongToast
import io.github.peacefulprogram.dy555.ext.showShortToast

/**
 * MX Player 管理器
 * 直接调用 MX Player 播放视频
 */
class MxPlayerManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MxPlayerManager"
        private const val PACKAGE_NAME = "com.mxtech.videoplayer.ad"
        private const val FREE_PACKAGE_NAME = "com.mxplayer.pro"
    }
    
    /**
     * 检查 MX Player 是否已安装
     */
    fun isMxPlayerInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            true
        } catch (e: Exception) {
            try {
                context.packageManager.getPackageInfo(FREE_PACKAGE_NAME, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 启动 MX Player 播放视频
     * @param videoUrl 视频URL
     * @param title 视频标题
     * @param position 播放位置（毫秒）
     * @param speed 播放速度
     * @return 是否成功启动
     */
    fun launchMxPlayer(
        videoUrl: String,
        title: String,
        position: Long = 0L,
        speed: Float = 1.0f
    ): Boolean {
        if (!isMxPlayerInstalled()) {
            context.showLongToast("请先安装 MX Player")
            return false
        }
        
        return try {
            val intent = createMxPlayerIntent(videoUrl, title, position, speed)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "启动 MX Player 失败", e)
            context.showLongToast("启动 MX Player 失败")
            false
        }
    }
    
    /**
     * 创建 MX Player Intent
     */
    private fun createMxPlayerIntent(
        videoUrl: String,
        title: String,
        position: Long,
        speed: Float
    ): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        
        // 设置数据类型
        intent.setDataAndType(Uri.parse(videoUrl), "video/*")
        
        // 设置包名（优先使用 Pro 版本）
        if (isPackageInstalled(FREE_PACKAGE_NAME)) {
            intent.setPackage(FREE_PACKAGE_NAME)
        } else {
            intent.setPackage(PACKAGE_NAME)
        }
        
        // 添加启动参数
        val extras = Bundle().apply {
            // 标题
            putString("title", title)
            
            // 播放位置
            if (position > 0) {
                putLong("position", position)
            }
            
            // 播放速度
            putFloat("speed", speed)
            
            // 自动播放
            putBoolean("autoplay", true)
            
            // 循环播放
            putBoolean("loop", false)
            
            // 硬件加速
            putBoolean("hwdec", true)
            
            // 显示控制栏
            putBoolean("controls", true)
            
            // 全屏
            putBoolean("fullscreen", true)
        }
        
        intent.putExtras(extras)
        
        // 添加启动标志
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        return intent
    }
    
    /**
     * 检查指定包名是否已安装
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}