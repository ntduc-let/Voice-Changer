package com.prox.voicechanger.ui.dialog

import com.prox.voicechanger.utils.FileUtils.Companion.getName
import com.prox.voicechanger.utils.FileUtils.Companion.shareFile
import com.prox.voicechanger.utils.NumberUtils.formatAsTime
import android.content.Context
import com.prox.voicechanger.VoiceChangerApp
import android.media.MediaPlayer
import com.prox.voicechanger.R
import android.media.AudioManager
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.prox.voicechanger.databinding.DialogPlayVideoBinding
import java.io.File
import java.lang.Exception

class PlayVideoDialog(context: Context, binding: DialogPlayVideoBinding, path: String?) :
    CustomDialog(context, binding.root) {
    private val binding: DialogPlayVideoBinding
    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            try {
                binding.txtCurrentTime2.text = formatAsTime(player!!.currentPosition.toLong())
                binding.seekTime2.progress = player!!.currentPosition
                handler.post(this)
            } catch (e: Exception) {
                Log.d(VoiceChangerApp.TAG, "updateTime error " + e.message)
                handler.removeCallbacks(this)
            }
        }
    }

    private fun pauseVideo() {
        Log.d(VoiceChangerApp.TAG, "PlayVideoDialog: pauseVideo")
        player!!.pause()
        handler.removeCallbacks(updateTime)
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume)
    }

    private fun resumeVideo() {
        Log.d(VoiceChangerApp.TAG, "PlayVideoDialog: resumeVideo")
        player!!.start()
        handler.post(updateTime)
        binding.txtTotalTime2.text = formatAsTime(player!!.duration.toLong())
        binding.seekTime2.max = player!!.duration
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_pause)
    }

    fun stop() {
        handler.removeCallbacks(updateTime)
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume)
    }

    init {
        Log.d(VoiceChangerApp.TAG, "PlayVideoDialog: create")
        setCancelable(false)
        this.binding = binding
        binding.txtNameVideo.text = getName(path!!)
        binding.videoView.setVideoPath(path)
        binding.videoView.setOnPreparedListener { player: MediaPlayer? ->
            this.player = player
            this.player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            this.player!!.isLooping = true
        }
        binding.videoView.setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.dialog_video_error)
                .setTitle(R.string.app_name)
            builder.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> cancel() }
            val dialogRequest = builder.create()
            dialogRequest.show()
            true
        }
        binding.seekTime2.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    if (player != null && File(path).exists()) {
                        player!!.seekTo(i)
                        binding.txtCurrentTime2.text = formatAsTime(player!!.currentPosition.toLong())
                    } else {
                        cancel()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (player != null && File(path).exists()) {
                    if (player!!.isPlaying) {
                        handler.removeCallbacks(updateTime)
                    }
                } else {
                    cancel()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (player != null && File(path).exists()) {
                    if (player!!.isPlaying) {
                        handler.post(updateTime)
                    }
                } else {
                    cancel()
                }
            }
        })
        binding.btnPauseOrResume2.setOnClickListener {
            if (player != null && File(path).exists()) {
                if (player!!.isPlaying) {
                    pauseVideo()
                } else {
                    resumeVideo()
                }
            } else {
                cancel()
            }
        }
        binding.btnBackVideo.setOnClickListener {
            stop()
            if (player != null && File(path).exists()) {
                player!!.stop()
                player!!.release()
            }
            cancel()
        }
        binding.btnShareVideo.setOnClickListener {
            if (player != null && File(path).exists()) {
                if (player!!.isPlaying) {
                    pauseVideo()
                }
                shareFile(context, path)
            } else {
                cancel()
            }
        }
    }
}