package io.github.peacefulprogram.dy555.room

import androidx.room.DatabaseView

/**
 * 视频播放历史数据类（依赖数据库视图）
 */
@DatabaseView(
    """
    SELECT
        vh.id as videoId,
        eh.id as epId,
        vh.title,
        vh.pic,
        eh.name as epName,
        eh.progress,
        eh.duration,
        eh.timestamp
    FROM video_history vh
    JOIN episode_history eh ON vh.epId = eh.id
    """
)
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