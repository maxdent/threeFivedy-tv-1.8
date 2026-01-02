package io.github.peacefulprogram.dy555.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.peacefulprogram.dy555.room.VideoEpisodeHistory
import io.github.peacefulprogram.dy555.room.entity.VideoHistory

@Dao
interface VideoHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveVideo(video: VideoHistory)

    @Query("update video_history set epId = :episodeId where id = :videoId")
    suspend fun updateLatestPlayedEpisode(videoId: String, episodeId: String)

    // TODO: 临时注释掉数据库视图查询，避免编译问题
    // @Query("SELECT * FROM VideoEpisodeHistory")
    // fun queryAllHistory(): PagingSource<Int, VideoEpisodeHistory>


    @Query("delete from video_history where id = :id")
    suspend fun deleteVideo(id: String)

    @Query("delete from episode_history where videoId = :videoId")
    suspend fun deleteEpisodeHistoryOfVideo(videoId: String)

    @Query("delete from video_history")
    suspend fun deleteAllVideoHistory()


    @Query("delete from episode_history")
    suspend fun deleteAllEpisodeHistory()

    @Transaction
    suspend fun deleteHistoryById(id: String) {
        deleteVideo(id)
        deleteEpisodeHistoryOfVideo(id)
    }

    @Transaction
    suspend fun deleteAllHistory() {
        deleteAllVideoHistory()
        deleteAllEpisodeHistory()
    }


}