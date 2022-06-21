package com.prox.voicechanger.media

import com.prox.voicechanger.interfaces.PlayerListener
import android.media.MediaPlayer
import com.prox.voicechanger.VoiceChangerApp
import android.media.AudioManager
import android.util.Log
import java.io.IOException

class Player : PlayerListener {
    private var player: MediaPlayer?
    val isPlaying: Boolean
        get() = player!!.isPlaying
    val currentPosition: Int
        get() = player!!.currentPosition
    val duration: Int
        get() = player!!.duration

    fun seekTo(i: Long) {
        player!!.seekTo(i.toInt())
    }

    override fun setNewPath(path: String?) {
        Log.d(VoiceChangerApp.TAG, "Player: setNewPath $path")
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        try {
            player!!.reset()
            player!!.setDataSource(path)
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player!!.isLooping = true
            player!!.prepare()
        } catch (e: IOException) {
            Log.d(VoiceChangerApp.TAG, "Player: setNewPath error " + e.message)
        }
    }

    override fun start() {
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        player!!.start()
    }

    override fun pause() {
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        if (player!!.isPlaying) {
            player!!.pause()
        }
    }

    override fun resume() {
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        if (!player!!.isPlaying) {
            player!!.start()
        }
    }

    override fun stop() {
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        if (player!!.isPlaying) {
            player!!.stop()
        }
    }

    override fun release() {
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "Player: player null")
            return
        }
        if (player!!.isPlaying) {
            player!!.stop()
        }
        player!!.release()
        player = null
        Log.d(VoiceChangerApp.TAG, "Player: release")
    }

    init {
        player = MediaPlayer()
    }
}