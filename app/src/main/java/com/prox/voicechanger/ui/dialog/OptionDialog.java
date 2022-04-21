package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogOptionBinding;
import com.prox.voicechanger.databinding.DialogRenameBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

public class OptionDialog extends CustomDialog{

    public OptionDialog(
            @NonNull Context context,
            Activity activity,
            DialogOptionBinding binding,
            FileVoiceViewModel model,
            FileVoice fileVoice) {

        super(context, binding.getRoot());
        Log.d(TAG, "OptionDialog: create");
        setCancelable(true);

        binding.btnShare.setOnClickListener(view -> {
            FileUtils.shareFile(context, fileVoice.getPath());
            cancel();
        });

        binding.btnRename.setOnClickListener(view -> {
            RenameDialog dialog = new RenameDialog(
                    context,
                    DialogRenameBinding.inflate(activity.getLayoutInflater()),
                    model,
                    fileVoice);
            dialog.show();
            cancel();
        });

        binding.btnDeleteItem.setOnClickListener(view -> {
            FileUtils.deleteFile(context, fileVoice.getPath());
            model.delete(fileVoice);
            cancel();
        });
    }
}
