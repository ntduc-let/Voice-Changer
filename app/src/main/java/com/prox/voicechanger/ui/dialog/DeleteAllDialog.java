package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogDeleteAllBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.util.List;

public class DeleteAllDialog extends CustomDialog{

    public DeleteAllDialog(@NonNull Context context, DialogDeleteAllBinding binding, FileVoiceViewModel model, List<FileVoice> fileVoices) {
        super(context, binding.getRoot());
        Log.d(TAG, "DeleteAllDialog: create");
        setCancelable(false);

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "DeleteAllDialog: Cancel");
            cancel();
        });

        binding.btnDelete.setOnClickListener(view -> {
            Log.d(TAG, "DeleteAllDialog: Delete");
            for (FileVoice fileVoice : fileVoices){
                if (FileUtils.deleteFile(context, fileVoice.getPath())){
                    model.delete(fileVoice);
                }
            }
            cancel();
        });
    }
}
