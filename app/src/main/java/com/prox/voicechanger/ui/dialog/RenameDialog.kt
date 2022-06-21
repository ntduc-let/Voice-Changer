package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogRenameBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

public class RenameDialog extends CustomDialog{

    public RenameDialog(@NonNull Context context, DialogRenameBinding binding, FileVoiceViewModel model, FileVoice fileVoice) {
        super(context, binding.getRoot());
        Log.d(TAG, "RenameDialog: create");
        setCancelable(false);

        binding.edtName.setText(FileUtils.getName(fileVoice.getPath()));
        binding.icEffect.setImageResource(fileVoice.getSrc());

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "RenameDialog: Cancel");
            cancel();
        });

        binding.btnSave.setOnClickListener(view -> {
            Log.d(TAG, "RenameDialog: Save");
            String newPath = FileUtils.renameFile(context, fileVoice.getPath(), binding.edtName.getText().toString().trim());
            if (newPath!=null){
                fileVoice.setPath(newPath);
                fileVoice.setName(FileUtils.getName(newPath));
                model.update(fileVoice);
                cancel();
            }
        });
    }
}
