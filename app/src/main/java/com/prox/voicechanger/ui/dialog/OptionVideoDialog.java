package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogOptionVideoBinding;
import com.prox.voicechanger.databinding.DialogRenameBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

public class OptionVideoDialog extends CustomDialog{

    public OptionVideoDialog(
            @NonNull Context context,
            Activity activity,
            DialogOptionVideoBinding binding,
            FileVoiceViewModel model,
            FileVoice fileVoice) {
        super(context, binding.getRoot());
        Log.d(TAG, "OptionVideoDialog: create");
        setCancelable(true);

        binding.btnShare.setOnClickListener(view -> {
            Log.d(TAG, "OptionVideoDialog: Share");
            FileUtils.shareFile(context, fileVoice.getPath());
            cancel();
        });

        binding.btnRename.setOnClickListener(view -> {
            Log.d(TAG, "OptionVideoDialog: Rename");
            RenameDialog dialog = new RenameDialog(
                    context,
                    DialogRenameBinding.inflate(activity.getLayoutInflater()),
                    model,
                    fileVoice);
            dialog.show();
            cancel();
        });

        binding.btnDeleteItem.setOnClickListener(view -> {
            Log.d(TAG, "OptionVideoDialog: Delete");
            FileUtils.deleteFile(context, fileVoice.getPath());
            model.delete(fileVoice);
            cancel();
        });
    }
}
