package com.prox.voicechanger

import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp
class VoiceChangerApp : Application() {
    companion object {
        const val TAG = "ntduc"
        const val FOLDER_APP = "Voice Changer"
    }
}