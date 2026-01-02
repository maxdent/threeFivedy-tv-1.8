package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import androidx.leanback.widget.Action

/**
 * 自定义倍速播放Action
 */
class SpeedAction(
    context: Context,
    val speed: Float,
    label: String
) : Action(generateId()) {

    private var _label: String = label

    companion object {
        private var idCounter = 1000L

        private fun generateId(): Long {
            return idCounter++
        }
    }

    override fun toString(): String {
        return _label
    }

    /**
     * 获取当前倍速的显示标签
     */
    fun getLabel(): String = _label

    /**
     * 更新显示标签
     */
    fun setLabel(newLabel: String) {
        _label = newLabel
    }
}