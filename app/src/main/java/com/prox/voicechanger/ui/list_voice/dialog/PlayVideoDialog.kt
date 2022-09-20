package com.prox.voicechanger.ui.list_voice.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.DialogPlayVideoBinding
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.utils.NumberUtils
import java.io.File

class PlayVideoDialog(val path: String) : DialogFragment() {
    private lateinit var binding: DialogPlayVideoBinding
    private var player: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            try {
                binding.txtCurrentTime2.text = NumberUtils.formatAsTime(
                    player!!.currentPosition.toLong()
                )
                binding.seekTime2.progress = player!!.currentPosition
                handler.post(this)
            } catch (e: Exception) {
                Log.d(VoiceChangerApp.TAG, "updateTime error " + e.message)
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogPlayVideoBinding.inflate(layoutInflater)

        binding.txtNameVideo.text = FileUtils.getName(path)
        binding.videoView.setVideoPath(path)
        binding.videoView.setOnPreparedListener { player ->
            this.player = player
            this.player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            this.player!!.isLooping = true
        }

        binding.videoView.setOnErrorListener { _, _, _ ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.dialog_video_error)
                .setTitle(R.string.app_name)
            builder.setPositiveButton(R.string.ok) { _, _ -> dismiss() }
            val dialogRequest = builder.create()
            dialogRequest.show()
            true
        }

        binding.seekTime2.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    if (player != null && File(path).exists()) {
                        player!!.seekTo(i)
                        binding.txtCurrentTime2.text =
                            NumberUtils.formatAsTime(player!!.currentPosition.toLong())
                    } else {
                        dismiss()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (player != null && File(path).exists()) {
                    if (player!!.isPlaying) {
                        handler.removeCallbacksAndMessages(null)
                    }
                } else {
                    dismiss()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (player != null && File(path).exists()) {
                    if (player!!.isPlaying) {
                        handler.post(updateTime)
                    }
                } else {
                    dismiss()
                }
            }
        })

        binding.btnPauseOrResume2.setOnClickListener { view ->
            if (player != null && File(path).exists()) {
                if (player!!.isPlaying) {
                    pauseVideo()
                } else {
                    resumeVideo()
                }
            } else {
                dismiss()
            }
        }

        binding.btnBackVideo.setOnClickListener { view ->
            stop()
            if (player != null && File(path).exists()) {
                player!!.stop()
                player!!.release()
            }
            dismiss()
        }

        binding.btnShareVideo.setOnClickListener { view ->
            if (player != null && File(path).exists()) {
                if (player!!.isPlaying) {
                    pauseVideo()
                }
                FileUtils.shareFile(requireContext(), path)
            } else {
                dismiss()
            }
        }

        builder.setView(binding.root)
        val d: Dialog = builder.create()
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return d
    }

    override fun onStart() {
        super.onStart()
        val mDialog = dialog
        if (mDialog != null) {
            mDialog.setCanceledOnTouchOutside(false)
            if (mDialog.window != null) {
                mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window!!.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                val layoutParams = mDialog.window!!.attributes
                layoutParams.gravity = Gravity.CENTER
                layoutParams.windowAnimations = R.style.CustomDialogAnimation
                mDialog.window!!.attributes = layoutParams
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isAdded) {
            return
        }
        try {
            super.show(manager, tag)
        } catch (_: Exception) {
        }
    }

    override fun dismiss() {
        if (isAdded) {
            try {
                super.dismissAllowingStateLoss()
            } catch (_: Exception) {
            }
        }
    }

    private fun pauseVideo() {
        if (player == null){
            return
        }
        player!!.pause()
        handler.removeCallbacksAndMessages(null)
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume)
    }

    private fun resumeVideo() {
        if (player == null){
            return
        }
        player!!.start()
        handler.post(updateTime)
        binding.txtTotalTime2.text = NumberUtils.formatAsTime(player!!.duration.toLong())
        binding.seekTime2.max = player!!.duration
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_pause)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume)
    }
}