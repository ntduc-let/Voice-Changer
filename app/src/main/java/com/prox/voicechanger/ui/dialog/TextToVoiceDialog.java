package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogTextToVoiceBinding;

public class TextToVoiceDialog extends CustomDialog{

    public TextToVoiceDialog(@NonNull Context context, Activity activity, DialogTextToVoiceBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "TextToVoiceDialog: create");
        setCancelable(false);

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "TextToVoiceDialog: Cancel");
            cancel();
        });

        binding.btnDone.setOnClickListener(view -> {
            Log.d(TAG, "TextToVoiceDialog: Done");

        });
    }
}
