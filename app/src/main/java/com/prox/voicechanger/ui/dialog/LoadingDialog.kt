package com.prox.voicechanger.ui.dialog

import android.content.Context
import com.prox.voicechanger.VoiceChangerApp
import android.util.Log
import com.prox.voicechanger.databinding.DialogLoading2Binding

class LoadingDialog(context: Context, binding: DialogLoading2Binding) :
    CustomDialog(context, binding.root) {
    init {
        Log.d(VoiceChangerApp.TAG, "LoadingDialog: create")
        setCancelable(false)
    }
}