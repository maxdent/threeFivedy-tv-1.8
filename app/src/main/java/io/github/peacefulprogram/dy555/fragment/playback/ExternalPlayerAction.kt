package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.leanback.widget.Action

/**
 * 外部播放器操作按钮
 */
class ExternalPlayerAction(context: Context) : Action {
    
    companion object {
        private const val ID = "external_player_action"
        private const val DESCRIPTION = "外部播放器"
    }
    
    override fun getId(): String = ID
    
    override fun getDescription(): CharSequence = DESCRIPTION
    
    override fun getIcon(): Drawable? {
        // 这里可以设置自定义图标，暂时使用默认图标
        return null
    }
    
    override fun getIconFocused(): Drawable? {
        return getIcon()
    }
    
    override fun getIconSelected(): Drawable? {
        return getIcon()
    }
    
    override fun getIconDisabled(): Drawable? {
        return getIcon()
    }
}