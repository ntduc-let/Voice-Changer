package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogOptionBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;

public class OptionDialog extends CustomDialog{

    public OptionDialog(@NonNull Context context, DialogOptionBinding binding, FileVoice fileVoice) {
        super(context, binding.getRoot());
        Log.d(TAG, "OptionDialog: create");
        setCancelable(true);

        binding.btnShare.setOnClickListener(view -> {
            FileUtils.shareFile(context, fileVoice.getPath());
            cancel();
        });
    }
}
