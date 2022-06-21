package com.prox.voicechanger.adapter

import com.prox.voicechanger.utils.ConvertersUtils.toBitmap
import com.prox.voicechanger.utils.NumberUtils.formatAsTime
import com.prox.voicechanger.utils.NumberUtils.formatAsSize
import com.prox.voicechanger.utils.NumberUtils.formatAsDate
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.LayoutInflater
import android.app.Activity
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.adapter.FileVideoAdapter.FileVideoViewHolder
import com.prox.voicechanger.ui.dialog.OptionVideoDialog
import com.prox.voicechanger.ui.dialog.PlayVideoDialog
import android.view.View
import com.prox.voicechanger.databinding.DialogOptionVideoBinding
import com.prox.voicechanger.databinding.DialogPlayVideoBinding
import com.prox.voicechanger.databinding.ItemFileVideoBinding
import com.prox.voicechanger.model.FileVoice
import java.util.ArrayList

class FileVideoAdapter(
    private val context: Context,
    private val activity: Activity,
    private val model: FileVoiceViewModel
) : RecyclerView.Adapter<FileVideoViewHolder>() {
    private var fileVideos: List<FileVoice?>? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setFileVideos(fileVideos: List<FileVoice?>?) {
        if (fileVideos != null) {
            this.fileVideos = fileVideos
        } else {
            this.fileVideos = ArrayList()
        }
        notifyDataSetChanged()
    }

    fun getFileVideos(): List<FileVoice?> {
        return fileVideos ?: ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVideoViewHolder {
        val binding =
            ItemFileVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileVideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileVideoViewHolder, position: Int) {
        val fileVideo = fileVideos!![position]
        holder.binding.imgFile.setImageBitmap(toBitmap(fileVideo!!.image))
        holder.binding.txtNameFile.text = fileVideo.name
        holder.binding.txtSize.text =
            formatAsTime(fileVideo.duration) + " | " + formatAsSize(fileVideo.size)
        holder.binding.txtDate.text = formatAsDate(fileVideo.date)
        holder.binding.btnOption.setOnClickListener {
            val dialog = OptionVideoDialog(
                context,
                activity,
                DialogOptionVideoBinding.inflate(activity.layoutInflater),
                model,
                fileVideo
            )
            dialog.show()
        }
        holder.binding.btnPlay.setOnClickListener {
            val dialog = PlayVideoDialog(
                context,
                DialogPlayVideoBinding.inflate(activity.layoutInflater),
                fileVideo.path
            )
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return if (fileVideos == null) {
            0
        } else fileVideos!!.size
    }

    class FileVideoViewHolder(val binding: ItemFileVideoBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}