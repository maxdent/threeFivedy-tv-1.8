package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import androidx.leanback.widget.Action
import io.github.peacefulprogram.dy555.R

/**
 * MX Player 操作按钮
 */
class MxPlayerAction(context: Context) : Action(context.resources.getIdentifier("mx_player_action", "id", context.packageName)) {
    
    override fun getIconResourceId(): Int {
        return R.drawable.ic_mx_player
    }
    
    override fun getLabel(): CharSequence {
        return "MX Player"
    }
}