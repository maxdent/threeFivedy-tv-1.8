package io.github.peacefulprogram.dy555.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "dy555_preferences"
    private const val KEY_M3U8_API_SERVER = "m3u8_extract_api_server"
    private const val DEFAULT_M3U8_API_SERVER = "http://127.0.0.1:8080"
    
    // 播放器偏好设置
    private const val KEY_DEFAULT_PLAYER = "default_player"
    private const val KEY_REMEMBER_CHOICE = "remember_player_choice"
    private const val DEFAULT_DEFAULT_PLAYER = ""
    private const val DEFAULT_REMEMBER_CHOICE = true

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveM3u8ApiServer(context: Context, serverUrl: String) {
        getPreferences(context).edit().putString(KEY_M3U8_API_SERVER, serverUrl).apply()
    }

    fun getM3u8ApiServer(context: Context): String {
        return getPreferences(context).getString(KEY_M3U8_API_SERVER, DEFAULT_M3U8_API_SERVER)
            ?: DEFAULT_M3U8_API_SERVER
    }

    // 保存网站地址
    fun saveBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("base_url", url)
            .apply()
    }

    // 获取网站地址
    fun getBaseUrl(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("base_url", null)
    }
    
    // ========== 播放器偏好设置 ==========
    
    /**
     * 保存默认播放器
     */
    fun setDefaultPlayer(context: Context, packageName: String) {
        getPreferences(context).edit()
            .putString(KEY_DEFAULT_PLAYER, packageName)
            .apply()
    }
    
    /**
     * 获取默认播放器
     */
    fun getDefaultPlayer(context: Context): String {
        return getPreferences(context).getString(KEY_DEFAULT_PLAYER, DEFAULT_DEFAULT_PLAYER)
            ?: DEFAULT_DEFAULT_PLAYER
    }
    
    /**
     * 保存是否记住选择
     */
    fun setRememberChoice(context: Context, remember: Boolean) {
        getPreferences(context).edit()
            .putBoolean(KEY_REMEMBER_CHOICE, remember)
            .apply()
    }
    
    /**
     * 获取是否记住选择
     */
    fun shouldRememberChoice(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_REMEMBER_CHOICE, DEFAULT_REMEMBER_CHOICE)
    }
}