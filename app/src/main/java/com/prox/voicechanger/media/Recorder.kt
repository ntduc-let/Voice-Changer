package com.prox.voicechanger.media

import android.content.Context
import android.media.*
import com.prox.voicechanger.utils.FileUtils.Companion.getTempRecordingFilePath
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.interfaces.RecorderListener
import android.util.Log
import java.io.IOException

class Recorder(context: Context?) : RecorderListener {
    private var recorder: MediaRecorder?
    var path: String?
        private set
    private var startTime: Long
    private var isRecording: Boolean
    override fun start() {
        if (recorder == null) {
            Log.d(VoiceChangerApp.TAG, "Recorder: start null")
            return
        }
        if (!isRecording) {
            try {
                recorder!!.prepare()
                recorder!!.start()
                startTime = System.currentTimeMillis()
                isRecording = true
                Log.d(VoiceChangerApp.TAG, "Recorder: start")
            } catch (e: IOException) {
                Log.d(VoiceChangerApp.TAG, "Recorder: error start - " + e.message)
            }
        }
    }

    override fun stop() {
        if (recorder == null) {
            Log.d(VoiceChangerApp.TAG, "Recorder: stop null")
            return
        }
        if (isRecording) {
            recorder!!.stop()
            recorder!!.release()
            startTime = 0
            isRecording = false
            Log.d(VoiceChangerApp.TAG, "Recorder: stop")
        }
    }

    override fun release() {
        if (recorder == null) {
            Log.d(VoiceChangerApp.TAG, "Recorder: release null")
            return
        }
        if (isRecording) {
            recorder!!.stop()
        }
        recorder!!.release()
        recorder = null
        path = null
        startTime = 0
        isRecording = false
        Log.d(VoiceChangerApp.TAG, "Recorder: release")
    }

    val currentTime: Long
        get() = System.currentTimeMillis() - startTime
    val tickDuration: Int
        get() = (BUFFER_SIZE.toDouble() * 500 / BYTE_RATE).toInt()
    val maxAmplitude: Int
        get() = if (recorder != null) {
            recorder!!.maxAmplitude
        } else 0

    companion object {
        private const val SAMPLING_RATE_IN_HZ = 8000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_COUNT = 1
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BIT_PER_SAMPLE = 16
        private const val BYTE_RATE =
            (BIT_PER_SAMPLE * SAMPLING_RATE_IN_HZ * CHANNEL_COUNT / 8).toLong()
        private val BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT)
    }

    init {
        path = getTempRecordingFilePath(context!!)
        Log.d(VoiceChangerApp.TAG, "Recorder: create $path")
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setOutputFile(path)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        startTime = 0
        isRecording = false
    }
}