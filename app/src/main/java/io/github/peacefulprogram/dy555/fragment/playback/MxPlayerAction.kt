package io.github.peacefulprogram.dy555.fragment.playback

import androidx.leanback.widget.Action
import io.github.peacefulprogram.dy555.R

/**
 * MX Player 操作按钮
 */
class MxPlayerAction : Action {
    
    constructor(id: Long) : super(id)
    
    constructor(id: Long, iconResId: Int) : super(id, iconResId)
    
    override fun getIconResourceId(): Int {
        return R.drawable.ic_mx_player
    }
    
    override fun getLabel(): CharSequence {
        return "MX Player"
    }
}