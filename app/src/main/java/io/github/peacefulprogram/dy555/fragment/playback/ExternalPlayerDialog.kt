package io.github.peacefulprogram.dy555.fragment.playback

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.peacefulprogram.dy555.R
import io.github.peacefulprogram.dy555.player.ExternalPlayerManager
import io.github.peacefulprogram.dy555.player.VideoPlayer

/**
 * 外部播放器选择对话框
 */
class ExternalPlayerDialog(
    private val context: Context,
    private val externalPlayerManager: ExternalPlayerManager,
    private val onPlayerSelected: (VideoPlayer) -> Unit,
    private val onCancel: () -> Unit = {}
) {
    
    private var dialog: AlertDialog? = null
    private var rememberChoiceCheckBox: android.widget.CheckBox? = null
    
    /**
     * 显示播放器选择对话框
     */
    fun show() {
        val players = externalPlayerManager.getRecommendedPlayers()
        
        if (players.isEmpty()) {
            // 没有可用的播放器
            AlertDialog.Builder(context)
                .setTitle("无可用播放器")
                .setMessage("未检测到可用的外部播放器，请先安装 VLC、MX Player 等播放器。")
                .setPositiveButton("确定", null) { _, _ -> onCancel() }
                .show()
            return
        }
        
        val dialogView = createDialogView(players)
        val builder = AlertDialog.Builder(context)
            .setTitle("选择播放器")
            .setView(dialogView)
            .setNegativeButton("取消") { _, _ -> onCancel() }
            .setNeutralButton("使用内置播放器") { _, _ -> onCancel() }
        
        dialog = builder.create()
        dialog?.show()
    }
    
    /**
     * 创建对话框视图
     */
    private fun createDialogView(players: List<VideoPlayer>): View {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_external_player, null)
        val radioGroup = layout.findViewById<RadioGroup>(R.id.radio_group_players)
        val rememberChoiceLayout = layout.findViewById<View>(R.id.layout_remember_choice)
        rememberChoiceCheckBox = layout.findViewById(R.id.checkbox_remember_choice)
        
        // 设置记住选择的初始状态
        rememberChoiceCheckBox?.isChecked = externalPlayerManager.shouldRememberChoice()
        
        // 创建播放器选项
        players.forEach { player ->
            val playerView = createPlayerView(player)
            radioGroup.addView(playerView)
        }
        
        // 设置单选组选择监听
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            rememberChoiceLayout.visibility = if (checkedId != View.NO_ID) View.VISIBLE else View.GONE
        }
        
        return layout
    }
    
    /**
     * 创建播放器选项视图
     */
    private fun createPlayerView(player: VideoPlayer): View {
        val playerView = LayoutInflater.from(context).inflate(R.layout.item_player_option, null)
        val radioButton = playerView.findViewById<RadioButton>(R.id.radio_button_player)
        val nameTextView = playerView.findViewById<TextView>(R.id.text_view_player_name)
        val descTextView = playerView.findViewById<TextView>(R.id.text_view_player_description)
        
        // 设置播放器信息
        radioButton.text = player.name
        nameTextView.text = player.name
        descTextView.text = player.description
        
        // 设置播放器图标
        try {
            val icon = player.getIcon(context.packageManager, context)
            val iconView = playerView.findViewById<android.widget.ImageView>(R.id.image_view_player_icon)
            iconView.setImageDrawable(icon)
        } catch (e: Exception) {
            // 如果获取图标失败，使用默认图标
            val iconView = playerView.findViewById<android.widget.ImageView>(R.id.image_view_player_icon)
            iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_video_player))
        }
        
        return playerView
    }
    
    /**
     * 确认选择
     */
    fun confirmSelection() {
        val radioGroup = dialog?.findViewById<RadioGroup>(R.id.radio_group_players)
        val selectedId = radioGroup?.checkedRadioButtonId
        
        if (selectedId != null && selectedId != View.NO_ID) {
            val playerView = radioGroup.findViewById<View>(selectedId)
            val radioButton = playerView.findViewById<RadioButton>(R.id.radio_button_player)
            val playerName = radioButton.text.toString()
            
            // 查找选中的播放器
            val selectedPlayer = externalPlayerManager.getAvailablePlayers()
                .find { it.name == playerName }
            
            if (selectedPlayer != null) {
                // 保存用户选择
                if (rememberChoiceCheckBox?.isChecked == true) {
                    externalPlayerManager.setDefaultPlayer(selectedPlayer)
                    externalPlayerManager.setRememberChoice(true)
                } else {
                    externalPlayerManager.setRememberChoice(false)
                }
                
                // 回调处理
                onPlayerSelected(selectedPlayer)
            }
        }
        
        dialog?.dismiss()
    }
    
    /**
     * 设置确认按钮点击监听
     */
    fun setOnConfirmClickListener(listener: () -> Unit) {
        dialog?.setPositiveButton("确定", null) { _, _ -> listener() }
    }
    
    /**
     * 对话框是否正在显示
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
    
    /**
     * 对话框是否已创建
     */
    fun isCreated(): Boolean {
        return dialog != null
    }
}