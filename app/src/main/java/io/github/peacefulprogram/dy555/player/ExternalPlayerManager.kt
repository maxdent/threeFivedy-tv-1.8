package io.github.peacefulprogram.dy555.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import io.github.peacefulprogram.dy555.util.PreferenceManager

/**
 * 外部播放器管理器
 */
class ExternalPlayerManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ExternalPlayerManager"
        private const val PREF_KEY_DEFAULT_PLAYER = "default_player"
        private const val PREF_KEY_REMEMBER_CHOICE = "remember_player_choice"
    }
    
    /**
     * 支持的播放器列表
     */
    private val supportedPlayers = listOf(
        VideoPlayer(
            name = "VLC",
            packageName = "org.videolan.vlc",
            mimeType = "application/x-mpegURL",
            description = "开源免费，支持格式最全"
        ),
        VideoPlayer(
            name = "MX Player Pro",
            packageName = "com.mxtech.videoplayer.pro",
            mimeType = "video/*",
            description = "功能强大，支持硬件加速"
        ),
        VideoPlayer(
            name = "MX Player Free",
            packageName = "com.mxtech.videoplayer.ad",
            mimeType = "video/*",
            description = "免费版本，功能完整"
        ),
        VideoPlayer(
            name = "Kodi",
            packageName = "org.xbmc.kodi",
            mimeType = "video/*",
            description = "高度可定制，插件丰富"
        ),
        VideoPlayer(
            name = "Plex",
            packageName = "com.plexapp.android",
            mimeType = "video/*",
            description = "界面美观，支持多设备同步"
        )
    )
    
    /**
     * 获取所有可用的播放器
     */
    fun getAvailablePlayers(): List<VideoPlayer> {
        val packageManager = context.packageManager
        return supportedPlayers.filter { player ->
            player.isInstalled(packageManager) && player.canResolveIntent(packageManager)
        }
    }
    
    /**
     * 获取默认播放器
     */
    fun getDefaultPlayer(): VideoPlayer? {
        val packageName = PreferenceManager.getDefaultPlayer(context)
        return supportedPlayers.find { it.packageName == packageName }
    }
    
    /**
     * 设置默认播放器
     */
    fun setDefaultPlayer(player: VideoPlayer?) {
        val packageName = player?.packageName ?: ""
        PreferenceManager.setDefaultPlayer(context, packageName)
    }
    
    /**
     * 检查是否记住用户选择
     */
    fun shouldRememberChoice(): Boolean {
        return PreferenceManager.shouldRememberChoice(context)
    }
    
    /**
     * 设置记住用户选择
     */
    fun setRememberChoice(remember: Boolean) {
        PreferenceManager.setRememberChoice(context, remember)
    }
    
    /**
     * 启动指定播放器
     */
    fun launchPlayer(
        player: VideoPlayer,
        videoUrl: String,
        title: String,
        position: Long = 0L,
        speed: Float = 1.0f
    ): Boolean {
        return try {
            val intent = player.createPlayIntent(videoUrl, title, position, speed)
            
            // 检查Intent是否可以解析
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                Log.e(TAG, "Cannot resolve intent for player: ${player.name}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch player: ${player.name}", e)
            false
        }
    }
    
    /**
     * 启动默认播放器
     */
    fun launchDefaultPlayer(
        videoUrl: String,
        title: String,
        position: Long = 0L,
        speed: Float = 1.0f
    ): Boolean {
        val defaultPlayer = getDefaultPlayer()
        return if (defaultPlayer != null) {
            launchPlayer(defaultPlayer, videoUrl, title, position, speed)
        } else {
            Log.w(TAG, "No default player set")
            false
        }
    }
    
    /**
     * 获取推荐的播放器
     */
    fun getRecommendedPlayers(): List<VideoPlayer> {
        val availablePlayers = getAvailablePlayers()
        val defaultPlayer = getDefaultPlayer()
        
        // 如果有默认播放器且可用，将其放在第一位
        return if (defaultPlayer != null && availablePlayers.contains(defaultPlayer)) {
            listOf(defaultPlayer) + availablePlayers.filter { it != defaultPlayer }
        } else {
            availablePlayers
        }
    }
    
    /**
     * 检查是否有可用的播放器
     */
    fun hasAvailablePlayers(): Boolean {
        return getAvailablePlayers().isNotEmpty()
    }
    
    /**
     * 获取播放器数量
     */
    fun getPlayerCount(): Int {
        return getAvailablePlayers().size
    }
    
    /**
     * 清除默认播放器设置
     */
    fun clearDefaultPlayer() {
        PreferenceManager.setDefaultPlayer(context, "")
    }
    
    /**
     * 获取播放器描述
     */
    fun getPlayerDescription(playerName: String): String {
        return supportedPlayers.find { it.name == playerName }?.description ?: ""
    }
}