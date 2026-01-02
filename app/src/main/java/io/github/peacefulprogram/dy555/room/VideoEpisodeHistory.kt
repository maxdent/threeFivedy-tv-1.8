package io.github.peacefulprogram.dy555.room

/**
 * 视频播放历史数据类（不依赖数据库视图）
 */
data class VideoEpisodeHistory(
    val videoId: String,
    val epId: String,
    val title: String,
    val pic: String,
    val epName: String,
    val progress: Long,
    val duration: Long,
    val timestamp: Long
)