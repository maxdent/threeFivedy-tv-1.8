package io.github.peacefulprogram.dy555.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.graphics.drawable.toDrawable
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.Action
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import io.github.peacefulprogram.dy555.Constants
import io.github.peacefulprogram.dy555.ext.dpToPx
import io.github.peacefulprogram.dy555.ext.secondsToDuration
import io.github.peacefulprogram.dy555.ext.showLongToast
import io.github.peacefulprogram.dy555.ext.showShortToast
import io.github.peacefulprogram.dy555.fragment.playback.ChooseEpisodeDialog
import io.github.peacefulprogram.dy555.fragment.playback.GlueActionCallback
import io.github.peacefulprogram.dy555.fragment.playback.PlayListAction
import io.github.peacefulprogram.dy555.fragment.playback.ProgressTransportControlGlue
import io.github.peacefulprogram.dy555.fragment.playback.ReplayAction
import io.github.peacefulprogram.dy555.http.Resource
import io.github.peacefulprogram.dy555.viewmodel.PlaybackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VideoPlaybackFragment(
    private val viewModel: PlaybackViewModel
) : VideoSupportFragment() {

    private var exoplayer: ExoPlayer? = null

    private var glue: ProgressTransportControlGlue<LeanbackPlayerAdapter>? = null

    private var resumeFrom = -1L

    private var backPressed = false

    // 加载界面相关
    private var loadingContainer: ViewGroup? = null
    private var loadingProgressBar: View? = null
    private var loadingText: TextView? = null
    private var loadingDotsAnimator: ValueAnimator? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = Color.BLACK.toDrawable()

        // 创建加载界面
        createLoadingView(view)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playbackEpisode.collectLatest { playbackEpisode ->
                    when (playbackEpisode) {
                        Resource.Loading -> {
                            // 换集时暂停并显示自定义加载界面
                            exoplayer?.pause()
                            showLoadingView()
                            // 显示"正在获取视频链接"提示
                            glue?.subtitle = "正在获取视频链接..."
                        }

                        is Resource.Success -> {
                            progressBarManager.hide()
                            hideLoadingView()
                            glue?.subtitle = playbackEpisode.data.name
                            exoplayer?.run {
                                setMediaItem(MediaItem.fromUri(playbackEpisode.data.url))
                                prepare()
                                if (resumeFrom > 0) {
                                    seekTo(resumeFrom)
                                    resumeFrom = -1
                                } else if (playbackEpisode.data.lastPlayPosition > 0) {
                                    // 距离结束小于10秒,当作播放结束
                                    if (playbackEpisode.data.videoDuration > 0 && playbackEpisode.data.videoDuration - playbackEpisode.data.lastPlayPosition < 10_000) {
                                        requireContext().showShortToast("上次已播放完,将从头开始播放")
                                    } else {
                                        val seekTo = playbackEpisode.data.lastPlayPosition
                                        exoplayer?.seekTo(seekTo)
                                        requireContext().showShortToast("已定位到上次播放位置:${(seekTo / 1000).secondsToDuration()}")
                                    }
                                }
                                play()
                            }
                        }

                        is Resource.Error -> {
                            progressBarManager.hide()
                            hideLoadingView()
                            requireContext().showLongToast(playbackEpisode.message)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23) {
            buildPlayer()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) {
            buildPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            resumeFrom = exoplayer!!.currentPosition
            destroyPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            resumeFrom = exoplayer!!.currentPosition
            destroyPlayer();
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildPlayer() {
        val factory = DefaultHttpDataSource.Factory().apply {
            setUserAgent(Constants.USER_AGENT)
            setDefaultRequestProperties(mapOf("referer" to Constants.BASE_URL))
            // 优化网络请求配置，提升 m3u8 兼容性
            setConnectTimeoutMs(20000)
            setReadTimeoutMs(60000)
        }
        val mediaSourceFactory = DefaultMediaSourceFactory(factory)

        // 创建 ExoPlayer 并优化性能配置，提升 m3u8 播放兼容性
        exoplayer = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                    prepareGlue(this)
                    // Don't auto-play until video URL is loaded
                    playWhenReady = false
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == ExoPlayer.STATE_ENDED) {
                                viewModel.playNextEpisodeIfExists()
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) {
                                viewModel.startSaveHistory()
                            } else {
                                viewModel.saveHistory()
                                viewModel.stopSaveHistory()
                            }
                        }
                    })
                    addListener(object : Player.Listener {
                        private var lastSeekPosition = 0L
                        private var isUserSeeking = false

                        override fun onEvents(player: Player, events: Player.Events) {
                            // 监听进度变化，检测拖动完成
                            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                                val newPosition = player.currentPosition
                                if (kotlin.math.abs(newPosition - lastSeekPosition) > 1000) {
                                    isUserSeeking = true
                                } else if (isUserSeeking) {
                                    isUserSeeking = false
                                    // 拖动进度条后自动播放
                                    play()
                                }
                                lastSeekPosition = newPosition
                            }
                        }
                    })
                }

    }


    private fun destroyPlayer() {
        exoplayer?.let {
            // Pause the player to notify listeners before it is released.
            it.pause()
            it.release()
            exoplayer = null
        }
    }

    @OptIn(UnstableApi::class)
    private fun prepareGlue(localExoplayer: ExoPlayer) {
        glue = ProgressTransportControlGlue(context = requireContext(),
            playerAdapter = LeanbackPlayerAdapter(
                requireContext(), localExoplayer, 200
            ),
            onCreatePrimaryAction = {
                it.add(PlayListAction(requireContext()))
                it.add(ReplayAction(requireContext()))
            },
            updateProgress = {
                viewModel.currentPlayPosition = localExoplayer.currentPosition
                viewModel.videoDuration = localExoplayer.duration
            }).apply {
            host = VideoSupportFragmentGlueHost(this@VideoPlaybackFragment)
            title = viewModel.videoTitle
            // Enable seek manually since PlaybackTransportControlGlue.getSeekProvider() is null,
            // so that PlayerAdapter.seekTo(long) will be called during user seeking.
            isSeekEnabled = true
            isControlsOverlayAutoHideEnabled = true
            addActionCallback(replayActionCallback)
            addActionCallback(changePlayVideoActionCallback)
            setKeyEventInterceptor { onKeyEvent(it) }
        }
    }


    private val replayActionCallback = object : GlueActionCallback {
        override fun support(action: Action): Boolean = action is ReplayAction

        override fun onAction(action: Action) {
            exoplayer?.seekTo(0L)
            exoplayer?.play()
            hideControlsOverlay(true)
        }

    }

    private val changePlayVideoActionCallback = object : GlueActionCallback {
        override fun support(action: Action): Boolean = action is PlayListAction

        override fun onAction(action: Action) {
            openPlayListDialogAndChoose()
        }

    }


    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
            if (isControlsOverlayVisible) {
                return false
            }
            if (exoplayer?.isPlaying != true) {
                backPressed = false
                return false
            }
            if (backPressed) {
                return false
            }
            backPressed = true
            Toast.makeText(requireContext(), "再按一次退出播放", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                delay(2000)
                backPressed = false
            }
            return true
        }
        if (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER && !isControlsOverlayVisible) {
            if (exoplayer?.isPlaying == true) {
                exoplayer?.pause()
            } else {
                exoplayer?.play()
            }
            return true
        }

        if (keyEvent.keyCode == KeyEvent.KEYCODE_MENU) {
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                openPlayListDialogAndChoose()
            }
            return true
        }
        return false
    }

    private fun openPlayListDialogAndChoose() {
        val fragmentManager = requireActivity().supportFragmentManager
        val current = viewModel.episode
        val defaultSelectIndex = viewModel.playlist.indexOfFirst { it.id == current.id }
        ChooseEpisodeDialog(dataList = viewModel.playlist,
            defaultSelectIndex = defaultSelectIndex,
            viewWidth = 60.dpToPx.toInt(),
            getText = { _, item -> item.name }) { _, ep ->
            viewModel.changeEpisode(ep)
        }.apply {
            showNow(fragmentManager, "")
        }
    }

    /**
     * 创建自定义加载界面
     */
    private fun createLoadingView(container: View) {
        val context = requireContext()

        // 创建主容器
        val mainContainer = FrameLayout(context).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.BLACK)
            visibility = View.GONE
        }

        // 创建内容容器
        val contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(80, 80, 80, 80)
        }

        // 创建进度条容器
        val progressContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }

        // 创建进度条 - 使用 Android 原生 ProgressBar
        val progressBar = android.widget.ProgressBar(context, null, android.R.attr.progressBarStyleLarge).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            // 设置为不确定模式（转圈动画）
            isIndeterminate = true
        }

        // 创建文字容器
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 40, 0, 0)
        }

        // 创建加载文字
        val loadingText = TextView(context).apply {
            text = "正在获取视频链接"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
        }

        // 组装界面
        progressContainer.addView(progressBar)
        textContainer.addView(loadingText)
        contentContainer.addView(progressContainer)
        contentContainer.addView(textContainer)
        mainContainer.addView(contentContainer)

        // 添加到主容器
        (container as? ViewGroup)?.addView(mainContainer)

        // 保存引用
        loadingContainer = mainContainer
        loadingProgressBar = progressBar
        this.loadingText = loadingText

        // 创建进度条旋转动画
        createProgressBarAnimation(progressBar)
    }

    /**
     * 创建进度条动画（ProgressBar 自带动画，无需额外设置）
     */
    private fun createProgressBarAnimation(progressBar: View) {
        // ProgressBar 自带 indeterminate 动画，无需额外创建
        // 这里可以保留方法以兼容代码结构
    }

    /**
     * 显示加载界面
     */
    private fun showLoadingView() {
        loadingContainer?.let { container ->
            container.visibility = View.VISIBLE
            // ProgressBar 自带动画，无需手动启动
            // 开始文字动画
            startTextAnimation()
        }
    }

    /**
     * 隐藏加载界面
     */
    private fun hideLoadingView() {
        loadingContainer?.let { container ->
            container.visibility = View.GONE
            // ProgressBar 自带动画，会自动停止
            stopTextAnimation()
        }
    }

    /**
     * 开始文字动画（添加省略号效果）
     */
    private fun startTextAnimation() {
        lifecycleScope.launch {
            var dotCount = 0
            while (loadingContainer?.visibility == View.VISIBLE) {
                loadingText?.text = "正在获取视频链接" + ".".repeat(dotCount)
                dotCount = (dotCount + 1) % 4
                delay(500)
            }
        }
    }

    /**
     * 停止文字动画
     */
    private fun stopTextAnimation() {
        // 这个方法会在循环条件不满足时自动停止
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ProgressBar 会自动清理，无需手动停止动画
        loadingDotsAnimator = null
    }

}