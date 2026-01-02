package io.github.peacefulprogram.dy555.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.peacefulprogram.dy555.room.dao.VideoHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayHistoryViewModel(
    private val videoHistoryDao: VideoHistoryDao
) : ViewModel() {
    fun deleteHistoryByVideoId(videoId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            videoHistoryDao.deleteHistoryById(videoId)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.Default) {
            videoHistoryDao.deleteAllVideoHistory()
        }
    }


    // TODO: 临时注释掉Pager，避免数据库视图编译问题
    // val pager = Pager(
    //     config = PagingConfig(20)
    // ) {
    //     videoHistoryDao.queryAllHistory()
    // }
    //     .flow

}