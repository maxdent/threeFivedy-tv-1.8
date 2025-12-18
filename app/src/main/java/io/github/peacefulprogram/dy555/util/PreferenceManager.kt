package io.github.peacefulprogram.dy555.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "dy555_preferences"
    private const val KEY_M3U8_API_SERVER = "m3u8_extract_api_server"
    private const val DEFAULT_M3U8_API_SERVER = "http://192.168.100.109:8000"

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
}
