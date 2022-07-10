package com.prox.voicechanger.adapter

import com.prox.voicechanger.utils.NumberUtils.formatAsTime
import com.prox.voicechanger.utils.NumberUtils.formatAsSize
import com.prox.voicechanger.utils.NumberUtils.formatAsDate
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.LayoutInflater
import com.prox.voicechanger.R
import android.app.Activity
import android.content.Context
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.adapter.FileVoiceAdapter.FileVoiceViewHolder
import android.media.MediaPlayer
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.ui.dialog.OptionDialog
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.prox.voicechanger.databinding.DialogOptionBinding
import com.prox.voicechanger.databinding.ItemFileStorageBinding
import com.prox.voicechanger.databinding.ItemFileVoiceBinding
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.ui.dialog.OptionDialog.Companion.fileVoice
import io.grpc.internal.SharedResourceHolder.release
import java.io.File
import java.io.IOException
import java.util.*

class FileStorageAdapter(
    private val context: Context,
    private var listStorage: List<FileVoice>
) : RecyclerView.Adapter<FileStorageAdapter.StorageViewHolder>() {

    companion object {
        const val VOICE = 5
        const val VIDEO = 10
    }

    private var clickDownloadListener: ((FileVoice) -> Unit)? = null

    fun setClickDownloadListener(listener: ((FileVoice) -> Unit)) {
        clickDownloadListener = listener
    }

    private var clickDeleteListener: ((FileVoice) -> Unit)? = null

    fun setClickDeleteListener(listener: ((FileVoice) -> Unit)) {
        clickDeleteListener = listener
    }

    inner class StorageViewHolder(binding: ItemFileStorageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemFileStorageBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileStorageAdapter.StorageViewHolder {
        val binding = ItemFileStorageBinding.inflate(LayoutInflater.from(context), parent, false)
        return StorageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileStorageAdapter.StorageViewHolder, position: Int) {
        val item = listStorage[position]

        holder.binding.imgFile.setImageResource(item.src)
        holder.binding.txtNameFile.text = item.name
        holder.binding.txtSize.text =
            formatAsTime(item.duration) + " | " + formatAsSize(item.size)
        holder.binding.txtDate.text = formatAsDate(item.date)
        holder.binding.btnDownload.setOnClickListener {
            clickDownloadListener?.let {
                it(item)
            }
        }
        holder.binding.btnDelete.setOnClickListener {
            clickDeleteListener?.let {
                it(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return listStorage.size
    }
}