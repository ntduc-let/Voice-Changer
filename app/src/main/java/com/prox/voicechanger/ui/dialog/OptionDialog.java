package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogOptionBinding;
import com.prox.voicechanger.databinding.DialogRenameBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.PermissionUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

public class OptionDialog extends CustomDialog{

    public static final int SELECT_IMAGE = 20;
    public static FileVoice fileVoice;

    public OptionDialog(
            @NonNull Context context,
            Activity activity,
            DialogOptionBinding binding,
            FileVoiceViewModel model,
            FileVoice fileVoice) {
        super(context, binding.getRoot());
        Log.d(TAG, "OptionDialog: create");
        setCancelable(true);

        OptionDialog.fileVoice = fileVoice;

        binding.btnShare.setOnClickListener(view -> {
            FileUtils.shareFile(context, fileVoice.getPath());
            cancel();
        });

        binding.btnAddImg.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(intent, SELECT_IMAGE);
        });

        binding.btnRingPhone.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(context)){
                    if (FileUtils.setAsRingtoneOrNotification(context, fileVoice.getPath(), RingtoneManager.TYPE_RINGTONE)){
                        Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(context, R.string.request_write_setting, Toast.LENGTH_SHORT).show();
                    PermissionUtils.requestWriteSetting(context);
                }
            }else {
                if (FileUtils.setAsRingtoneOrNotification(context, fileVoice.getPath(), RingtoneManager.TYPE_RINGTONE)){
                    Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show();
                }
            }
            cancel();
        });

        binding.btnRingNoti.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(context)){
                    if(FileUtils.setAsRingtoneOrNotification(context, fileVoice.getPath(), RingtoneManager.TYPE_NOTIFICATION)){
                        Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(context, R.string.request_write_setting, Toast.LENGTH_SHORT).show();
                    PermissionUtils.requestWriteSetting(context);
                }
            }else {
                if(FileUtils.setAsRingtoneOrNotification(context, fileVoice.getPath(), RingtoneManager.TYPE_NOTIFICATION)){
                    Toast.makeText(context, R.string.setting_success, Toast.LENGTH_SHORT).show();
                }
            }
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
