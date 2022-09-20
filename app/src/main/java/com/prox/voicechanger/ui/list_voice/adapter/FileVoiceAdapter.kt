package com.prox.voicechanger.ui.list_voice.adapter

import android.annotation.SuppressLint
import com.prox.voicechanger.model.FileVoice
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.ItemFileVoiceBinding
import com.prox.voicechanger.utils.NumberUtils
import java.io.File
import java.io.IOException

class FileVoiceAdapter(
    val context: Context,
    private var fileVoices: List<FileVoice> = listOf()
) : RecyclerView.Adapter<FileVoiceAdapter.FileVoiceViewHolder>() {

    private var holderSelect: FileVoiceViewHolder? = null
    private var player: MediaPlayer? = null
    private var path: String = ""
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            if (player != null) {
                holderSelect!!.binding.itemPlayMedia.txtCurrentTime2.text =
                    NumberUtils.formatAsTime(
                        player!!.currentPosition.toLong()
                    )
                holderSelect!!.binding.itemPlayMedia.seekTime.progress = player!!.currentPosition
                handler.post(this)
            }
        }
    }


    private var onOptionListener: ((FileVoice) -> Unit)? = null

    fun setOnOptionListener(listener: (FileVoice) -> Unit) {
        onOptionListener = listener
    }

    private var onNotExistsListener: ((FileVoice) -> Unit)? = null

    fun setOnNotExistsListener(listener: (FileVoice) -> Unit) {
        onNotExistsListener = listener
    }

    inner class FileVoiceViewHolder(binding: ItemFileVoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemFileVoiceBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVoiceViewHolder {
        val binding =
            ItemFileVoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileVoiceViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FileVoiceViewHolder, position: Int) {
        val fileVoice = fileVoices[position]
        holder.binding.imgFile.setImageResource(fileVoice.src)
        holder.binding.txtNameFile.text = fileVoice.name
        holder.binding.txtSize.text =
            NumberUtils.formatAsTime(fileVoice.duration) + " | " + NumberUtils.formatAsSize(
                fileVoice.size
            )
        holder.binding.txtDate.text = NumberUtils.formatAsDate(fileVoice.date)
        holder.binding.btnOption.setOnClickListener {
            release()
            isPlaying = false
            onOptionListener?.let {
                it(fileVoice)
            }
        }
        holder.binding.btnPlayOrPause.setOnClickListener {
            if (holderSelect == null) {
                holderSelect = holder

                player = MediaPlayer()
                path = fileVoice.path
                if (!File(path).exists()) {
                    onNotExistsListener?.let {
                        it(fileVoice)
                    }
                } else {
                    startMediaPlayer(fileVoice.path)
                    isPlaying = true
                }
            } else if (holderSelect == holder) {
                if (player == null) {
                    player = MediaPlayer()
                    path = fileVoice.path
                    if (!File(path).exists()) {
                        onNotExistsListener?.let {
                            it(fileVoice)
                        }
                    } else {
                        startMediaPlayer(fileVoice.path)
                        isPlaying = true
                    }
                } else if (player!!.isPlaying) {
                    pauseMediaPlayer()
                    isPlaying = false
                } else {
                    resumeMediaPlayer()
                    isPlaying = true
                }
            } else {
                stopMediaPlayer()
                holderSelect = holder
                if (player == null) {
                    player = MediaPlayer()
                }
                path = fileVoice.path
                if (!File(path).exists()) {
                    onNotExistsListener?.let {
                        it(fileVoice)
                    }
                } else {
                    startMediaPlayer(fileVoice.path)
                    isPlaying = true
                }
            }
        }
        holder.binding.itemPlayMedia.seekTime.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    if (player != null) {
                        player!!.seekTo(i)
                        holder.binding.itemPlayMedia.txtCurrentTime2.text =
                            NumberUtils.formatAsTime(
                                player!!.currentPosition.toLong()
                            )
                    } else {
                        holder.binding.itemPlayMedia.root.visibility = View.GONE
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (player != null) {
                    if (player!!.isPlaying) {
                        handler.removeCallbacksAndMessages(null)
                    }
                } else {
                    holder.binding.itemPlayMedia.root.visibility = View.GONE
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (player != null) {
                    if (player!!.isPlaying) {
                        handler.post(updateTime)
                    }
                } else {
                    holder.binding.itemPlayMedia.root.visibility = View.GONE
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return fileVoices.size
    }

    fun release() {
        if (player == null) {
            return
        }
        if (player!!.isPlaying) {
            stopMediaPlayer()
        } else {
            holderSelect!!.binding.itemPlayMedia.root.visibility = View.GONE
        }
        player!!.release()
        player = null
        holderSelect = null
    }

    private fun stopMediaPlayer() {
        if (player == null) {
            return
        }
        player!!.stop()
        handler.removeCallbacksAndMessages(null)
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.play)
        holderSelect!!.binding.itemPlayMedia.root.visibility = View.GONE
    }

    private fun startMediaPlayer(path: String) {
        if (player == null) {
            return
        }
        try {
            player!!.reset()
            player!!.setDataSource(path)
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player!!.isLooping = true
            player!!.prepare()
            player!!.setOnPreparedListener { mediaPlayer: MediaPlayer ->
                mediaPlayer.start()
                holderSelect!!.binding.btnPlayOrPause.setText(R.string.pause)
                holderSelect!!.binding.itemPlayMedia.root.visibility = View.VISIBLE
            }
        } catch (_: IOException) {
        }
        updateTime()
    }

    private fun updateTime() {
        if (player != null) {
            holderSelect!!.binding.itemPlayMedia.seekTime.max = player!!.duration
            holderSelect!!.binding.itemPlayMedia.txtTotalTime2.text = NumberUtils.formatAsTime(
                player!!.duration.toLong()
            )
            handler.post(updateTime)
        }
    }

    private fun pauseMediaPlayer() {
        if (player == null) {
            return
        }
        player!!.pause()
        handler.removeCallbacksAndMessages(null)
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.play)
    }

    private fun resumeMediaPlayer() {
        if (player == null) {
            return
        }
        player!!.start()
        updateTime()
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.pause)
    }

    fun resume() {
        if (!File(path).exists()) {
            isPlaying = false
            handler.removeCallbacksAndMessages(null)
            return
        }
        if (isPlaying) {
            if (player == null) {
                player = MediaPlayer()
                startMediaPlayer(path)
            } else {
                resumeMediaPlayer()
            }
        }
    }

    fun pause() {
        if (isPlaying) {
            pauseMediaPlayer()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFileVoices(fileVoices: List<FileVoice>) {
        this.fileVoices = fileVoices
        notifyDataSetChanged()
    }

    fun getFileVoices(): List<FileVoice> {
        return fileVoices
    }
}