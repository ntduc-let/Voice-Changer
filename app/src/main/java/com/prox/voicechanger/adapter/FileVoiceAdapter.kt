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
import com.prox.voicechanger.databinding.DialogOptionBinding
import com.prox.voicechanger.databinding.ItemFileVoiceBinding
import com.prox.voicechanger.model.FileVoice
import java.io.File
import java.io.IOException
import java.util.ArrayList

class FileVoiceAdapter(
    private val context: Context,
    private val activity: Activity,
    private val model: FileVoiceViewModel
) : RecyclerView.Adapter<FileVoiceViewHolder>() {
    private var fileVoices: List<FileVoice?>? = null
    private var isPlaying = false
    private var path: String? = null
    private var player: MediaPlayer? = null
    private var holderSelect: FileVoiceViewHolder? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            if (player == null) {
                Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: player null")
            } else {
                holderSelect!!.binding.itemPlayMedia.txtCurrentTime2.text = formatAsTime(
                    player!!.currentPosition.toLong()
                )
                holderSelect!!.binding.itemPlayMedia.seekTime.progress = player!!.currentPosition
                handler.post(this)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFileVoices(fileVoices: List<FileVoice?>?) {
        if (fileVoices != null) {
            this.fileVoices = fileVoices
        } else {
            this.fileVoices = ArrayList()
        }
        notifyDataSetChanged()
    }

    fun getFileVoices(): List<FileVoice?> {
        return fileVoices ?: ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileVoiceViewHolder {
        val binding =
            ItemFileVoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileVoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileVoiceViewHolder, position: Int) {
        val fileVoice = fileVoices!![position]
        holder.binding.imgFile.setImageResource(fileVoice!!.src)
        holder.binding.txtNameFile.text = fileVoice.name
        holder.binding.txtSize.text =
            formatAsTime(fileVoice.duration) + " | " + formatAsSize(fileVoice.size)
        holder.binding.txtDate.text = formatAsDate(fileVoice.date)
        holder.binding.btnOption.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            release()
            val dialog = OptionDialog(
                context,
                activity,
                DialogOptionBinding.inflate(activity.layoutInflater),
                model,
                fileVoice
            )
            dialog.show()
        }
        holder.binding.btnPlayOrPause.setOnClickListener {
            if (holderSelect == null) {
                holderSelect = holder
                player = MediaPlayer()
                path = fileVoice.path
                if (!File(path!!).exists()) {
                    model.delete(fileVoice)
                } else {
                    startMediaPlayer(fileVoice.path!!)
                    isPlaying = true
                }
            } else if (holderSelect == holder) {
                if (player == null) {
                    player = MediaPlayer()
                    path = fileVoice.path
                    if (!File(path!!).exists()) {
                        model.delete(fileVoice)
                    } else {
                        startMediaPlayer(fileVoice.path!!)
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
                if (!File(path!!).exists()) {
                    model.delete(fileVoice)
                } else {
                    startMediaPlayer(fileVoice.path!!)
                    isPlaying = true
                }
            }
        }
        holder.binding.itemPlayMedia.seekTime.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    if (player != null) {
                        player!!.seekTo(i)
                        holder.binding.itemPlayMedia.txtCurrentTime2.text = formatAsTime(
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
        return if (fileVoices == null) {
            0
        } else fileVoices!!.size
    }

    fun pause() {
        if (isPlaying) {
            pauseMediaPlayer()
        }
    }

    fun resume() {
        if (path == null || !File(path!!).exists()) {
            isPlaying = false
            handler.removeCallbacksAndMessages(null)
            return
        }
//        if (isPlaying) {
//            if (player == null) {
//                player = MediaPlayer()
//                startMediaPlayer(path!!)
//            } else {
//                resumeMediaPlayer()
//            }
//        }
    }

    class FileVoiceViewHolder(val binding: ItemFileVoiceBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    private fun startMediaPlayer(path: String) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: startMediaPlayer $path")
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
        } catch (e: IOException) {
            Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: " + e.message)
        }
        updateTime()
    }

    private fun pauseMediaPlayer() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: pauseMediaPlayer")
        if (player == null) {
            return
        }
        player!!.pause()
        handler.removeCallbacksAndMessages(null)
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.play)
    }

    private fun resumeMediaPlayer() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: resumeMediaPlayer")
        if (player == null) {
            return
        }
        player!!.start()
        updateTime()
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.pause)
    }

    private fun stopMediaPlayer() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: stopMediaPlayer")
        if (player == null) {
            return
        }
        player!!.stop()
        handler.removeCallbacksAndMessages(null)
        holderSelect!!.binding.btnPlayOrPause.setText(R.string.play)
        holderSelect!!.binding.itemPlayMedia.root.visibility = View.GONE
    }

    fun release() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: release")
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

    private fun updateTime() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceAdapter: updateTime")
        if (player != null) {
            holderSelect!!.binding.itemPlayMedia.seekTime.max = player!!.duration
            holderSelect!!.binding.itemPlayMedia.txtTotalTime2.text = formatAsTime(
                player!!.duration.toLong()
            )
            handler.post(updateTime)
        }
    }
}