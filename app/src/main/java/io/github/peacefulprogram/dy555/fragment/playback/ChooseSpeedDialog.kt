package io.github.peacefulprogram.dy555.fragment.playback

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.peacefulprogram.dy555.R
import io.github.peacefulprogram.dy555.ext.dpToPx
import io.github.peacefulprogram.dy555.ext.getColorWithAlpha

/**
 * 倍速选择对话框
 */
class ChooseSpeedDialog(
    private val currentSpeed: Float,
    private val onSpeedSelected: (Float) -> Unit
) : DialogFragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            adapter = SpeedAdapter()
        }
        return recyclerView
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.attributes?.run {
            height = WindowManager.LayoutParams.MATCH_PARENT
            width = 60.dpToPx.toInt()
            gravity = Gravity.END
            dialog!!.window!!.attributes = this
        }
        dialog?.window?.attributes?.dimAmount = 0f
        dialog!!.window!!.setBackgroundDrawable(
            requireContext().getColorWithAlpha(
                R.color.zinc900,
                0.3f
            ).toDrawable()
        )
        dialog!!.window!!.setDimAmount(0f)
        dialog!!.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        dialog!!.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                dismiss()
                true
            } else {
                false
            }
        }
    }

    private val speedOptions = listOf(
        SpeedOption("0.5x", 0.5f),
        SpeedOption("0.75x", 0.75f),
        SpeedOption("1.0x", 1.0f),
        SpeedOption("1.25x", 1.25f),
        SpeedOption("1.5x", 1.5f),
        SpeedOption("2.0x", 2.0f)
    )

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
                dismiss()
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
