package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Action
import io.github.peacefulprogram.dy555.R

/**
 * 外部播放器操作按钮
 */
class ExternalPlayerAction(context: Context) : Action(50) {
    init {
        icon = ContextCompat.getDrawable(context, R.drawable.ic_video_player)
    }
}