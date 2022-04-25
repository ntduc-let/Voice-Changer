package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogLoadingBinding;

public class LoadingDialog extends CustomDialog{

    public LoadingDialog(@NonNull Context context,DialogLoadingBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "LoadingDialog: create");
        setCancelable(false);
    }
}
