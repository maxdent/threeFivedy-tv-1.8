package io.github.peacefulprogram.dy555.fragment.playback

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.peacefulprogram.dy555.R

/**
 * 倍速选择对话框
 */
class ChooseSpeedDialog(
    private val context: Context,
    private val currentSpeed: Float,
    private val onSpeedSelected: (Float) -> Unit
) {
    private val speedOptions = listOf(
        SpeedOption("0.5x", 0.5f),
        SpeedOption("0.75x", 0.75f),
        SpeedOption("1.0x", 1.0f),
        SpeedOption("1.25x", 1.25f),
        SpeedOption("1.5x", 1.5f),
        SpeedOption("2.0x", 2.0f)
    )

    private var alertDialog: AlertDialog? = null

    fun show() {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adapter = SpeedAdapter()
        }

        alertDialog = AlertDialog.Builder(context)
            .setTitle("选择播放倍速")
            .setView(recyclerView)
            .setNegativeButton("取消", null)
            .create()

        alertDialog?.show()
    }

    private inner class SpeedAdapter : RecyclerView.Adapter<SpeedViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return SpeedViewHolder(view)
        }

        override fun onBindViewHolder(holder: SpeedViewHolder, position: Int) {
            val option = speedOptions[position]
            holder.bind(option)
        }

        override fun getItemCount(): Int = speedOptions.size
    }

    private inner class SpeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(option: SpeedOption) {
            itemView.setOnClickListener {
                onSpeedSelected(option.speed)
                alertDialog?.dismiss()
            }
            itemView.setBackgroundResource(if (option.speed == currentSpeed) R.drawable.speed_selected_bg else android.R.color.transparent)
            (itemView as? android.widget.TextView)?.text = option.label
        }
    }

    private data class SpeedOption(
        val label: String,
        val speed: Float
    )
}
