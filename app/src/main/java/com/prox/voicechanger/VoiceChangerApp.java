package com.prox.voicechanger;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class VoiceChangerApp extends Application {
    public static final String TAG = "ntduc";
    public static final String FOLDER_APP = "Voice Changer";

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
