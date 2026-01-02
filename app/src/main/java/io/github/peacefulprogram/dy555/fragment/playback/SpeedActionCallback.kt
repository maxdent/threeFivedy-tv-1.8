package io.github.peacefulprogram.dy555.fragment.playback

import androidx.leanback.widget.Action
import androidx.media3.common.Player

/**
 * 倍速播放Action的回调处理
 */
class SpeedActionCallback(
    private val player: Player,
    private val availableSpeeds: List<Float> = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
) : GlueActionCallback {

    var currentSpeedIndex = 2 // 默认1.0x

    override fun support(action: Action): Boolean {
        return action is SpeedAction
    }

    override fun onAction(action: Action) {
        if (action is SpeedAction) {
            setPlaybackSpeed(action.speed)
        }
    }

    /**
     * 设置播放速度
     */
    private fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        // 查找当前速度对应的索引
        val index = availableSpeeds.indexOf(speed)
        if (index >= 0) {
            currentSpeedIndex = index
        }
    }

    /**
     * 切换到下一个速度
     */
    fun cycleToNextSpeed() {
        val nextIndex = (currentSpeedIndex + 1) % availableSpeeds.size
        currentSpeedIndex = nextIndex
        val nextSpeed = availableSpeeds[nextIndex]
        player.setPlaybackSpeed(nextSpeed)
    }

    /**
     * 切换到上一个速度
     */
    fun cycleToPreviousSpeed() {
        val prevIndex = if (currentSpeedIndex == 0) {
            availableSpeeds.size - 1
        } else {
            currentSpeedIndex - 1
        }
        currentSpeedIndex = prevIndex
        val prevSpeed = availableSpeeds[prevIndex]
        player.setPlaybackSpeed(prevSpeed)
    }

    /**
     * 获取当前速度
     */
    fun getCurrentSpeed(): Float {
        return availableSpeeds[currentSpeedIndex]
    }

    /**
     * 获取当前速度标签
     */
    fun getCurrentSpeedLabel(): String {
        val speed = getCurrentSpeed()
        return when (speed) {
            1.0f -> "1.0x"
            else -> "${speed}x"
        }
    }
}