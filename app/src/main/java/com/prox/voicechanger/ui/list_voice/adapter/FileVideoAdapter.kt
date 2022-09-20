package com.prox.voicechanger.ui.list_voice.adapter

import androidx.recyclerview.widget.RecyclerView
import com.prox.voicechanger.ui.list_voice.adapter.FileVideoAdapter.FileVideoViewHolder
import com.prox.voicechanger.model.FileVoice
import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import com.prox.voicechanger.databinding.ItemFileVideoBinding
import com.prox.voicechanger.utils.ConvertersUtils
import com.prox.voicechanger.utils.NumberUtils

class FileVideoAdapter(
    val context: Context,
    private var fileVideos: List<FileVoice> = listOf()
) : RecyclerView.Adapter<FileVideoViewHolder>() {

    private var onOptionListener: ((FileVoice) -> Unit)? = null

    fun setOnOptionListener(listener: (FileVoice) -> Unit) {
        onOptionListener = listener
    }

    private var onPlayListener: ((FileVoice) -> Unit)? = null

    fun setOnPlayListener(listener: (FileVoice) -> Unit) {
        onPlayListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFileVideos(fileVideos: List<FileVoice>) {
        this.fileVideos = fileVideos
        notifyDataSetChanged()
    }

    fun getFileVideos(): List<FileVoice> {
        return fileVideos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVideoViewHolder {
        val binding =
            ItemFileVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileVideoViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FileVideoViewHolder, position: Int) {
        val fileVideo = fileVideos[position]
        holder.binding.imgFile.setImageBitmap(ConvertersUtils.toBitmap(fileVideo.image))
        holder.binding.txtNameFile.text = fileVideo.name
        holder.binding.txtSize.text =
            NumberUtils.formatAsTime(fileVideo.duration) + " | " + NumberUtils.formatAsSize(
                fileVideo.size
            )
        holder.binding.txtDate.text = NumberUtils.formatAsDate(fileVideo.date)
        holder.binding.btnOption.setOnClickListener {
            onOptionListener?.let {
                it(fileVideo)
            }
        }
        holder.binding.btnPlay.setOnClickListener {
            onPlayListener?.let {
                it(fileVideo)
            }
        }
    }

    override fun getItemCount(): Int {
        return fileVideos.size
    }

    inner class FileVideoViewHolder(binding: ItemFileVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemFileVideoBinding

        init {
            this.binding = binding
        }
    }
}