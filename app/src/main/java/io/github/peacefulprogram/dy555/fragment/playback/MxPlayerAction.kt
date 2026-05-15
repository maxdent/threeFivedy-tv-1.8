package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Action
import io.github.peacefulprogram.dy555.R

class MxPlayerAction(context: Context) : Action(20) {
    init {
        icon = ContextCompat.getDrawable(context, R.drawable.ic_mx_player)
    }
}