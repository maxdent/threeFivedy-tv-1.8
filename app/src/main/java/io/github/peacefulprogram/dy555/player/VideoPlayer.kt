package io.github.peacefulprogram.dy555.player

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * 视频播放器数据类
 */
data class VideoPlayer(
    val name: String,
    val packageName: String,
    val mimeType: String = "video/*",
    val supportedActions: List<PlayerAction> = emptyList(),
    val iconResId: Int? = null,
    val description: String = ""
) {
    
    /**
     * 播放器操作类型
     */
    enum class PlayerAction {
        PLAY,
        PAUSE,
        SEEK,
        RESUME,
        SET_SPEED
    }
    
    /**
     * 检查播放器是否已安装
     */
    fun isInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * 获取播放器图标
     */
    fun getIcon(packageManager: PackageManager, context: android.content.Context): Drawable? {
        return if (iconResId != null) {
            ContextCompat.getDrawable(context, iconResId)
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationIcon(packageName)
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationIcon(packageName)
                }
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to get icon for $packageName", e)
                null
            }
        }
    }
    
    /**
     * 创建播放Intent
     */
    fun createPlayIntent(
        videoUrl: String,
        title: String,
        position: Long = 0L,
        speed: Float = 1.0f
    ): android.content.Intent {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(videoUrl), mimeType)
            putExtra("title", title)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // 根据播放器类型添加特定参数
            when (packageName) {
                "org.videolan.vlc" -> {
                    // VLC 特定参数
                    putExtra("position", position)
                    if (speed != 1.0f) {
                        putExtra("speed", speed)
                    }
                }
                "com.mxtech.videoplayer.pro", "com.mxtech.videoplayer.ad" -> {
                    // MX Player 特定参数
                    putExtra("title", title)
                    putExtra("position", position)
                    putExtra("return_result", true)
                    if (speed != 1.0f) {
                        putExtra("speed", speed)
                    }
                }
                "org.xbmc.kodi" -> {
                    // Kodi 特定参数
                    putExtra("title", title)
                    putExtra("resume", position > 0)
                }
                "com.plexapp.android" -> {
                    // Plex 特定参数
                    putExtra("title", title)
                    putExtra("offset", position)
                }
            }
        }
        
        // 尝试设置特定包名
        intent.setPackage(packageName)
        
        return intent
    }
    
    /**
     * 检查Intent是否可以解析
     */
    fun canResolveIntent(packageManager: PackageManager): Boolean {
        return intent.resolveActivity(packageManager) != null
    }
    
    /**
     * 获取Intent
     */
    val intent: android.content.Intent
        get() = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setPackage(packageName)
        }
}