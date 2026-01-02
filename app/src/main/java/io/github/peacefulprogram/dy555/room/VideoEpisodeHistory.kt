package io.github.peacefulprogram.dy555.room

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "VideoEpisodeHistory",
    value = """
        select e.videoId,
               v.epId,
               v.title,
               v.pic,
               e.name epName,
               e.progress,
               e.duration
        from video_history v
        inner join episode_history e
            on v.epId = e.id
        order by e.timestamp desc
    """
)
data class VideoEpisodeHistory(
    val videoId: String,
    val epId: String,
    val title: String,
    val pic: String,
    val epName: String,
    val progress: Long,
    val duration: Long
)