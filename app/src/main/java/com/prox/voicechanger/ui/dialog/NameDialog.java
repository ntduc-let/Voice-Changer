package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.FOLDER_APP;
import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogNameBinding;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.ui.activity.ChangeVoiceActivity;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;

import java.io.File;

public class NameDialog extends CustomDialog {
    public static final String RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE";
    public static final String NAME_FILE = "NAME_FILE";

    public NameDialog(@NonNull Context context, Activity activity, DialogNameBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "NameDialog: create");
        setCancelable(false);

        binding.edtName.setText(FileUtils.getName(FileUtils.getRecordingFileName()));

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "NameDialog: Cancel");
            NavController navController = Navigation.findNavController(activity, R.id.nav_host_record_activity);
            navController.popBackStack();
            cancel();
            Log.d(TAG, "NameDialog: To RecordFragment");
        });

        binding.btnSave.setOnClickListener(view -> {
            Log.d(TAG, "NameDialog: Save");

            if (binding.edtName.getText().toString().trim().length() == 0) {
                Toast.makeText(context, R.string.name_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            String path = FileUtils.getDownloadFolderPath(FOLDER_APP) + "/" + binding.edtName.getText().toString().trim() + ".mp3";
            File file = new File(path);
            if (file.exists()) {
                Toast.makeText(context, R.string.name_exits, Toast.LENGTH_SHORT).show();
                return;
            }

            binding.txtNoti.setVisibility(View.VISIBLE);
            binding.btnSave.setEnabled(false);
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_enable30);
            binding.btnSave.setTextColor(context.getResources().getColor(R.color.white30));
            binding.btnCancel.setEnabled(false);

            String cmd = FFMPEGUtils.getCMDConvertRecording(FileUtils.getTempRecordingFilePath(context), FileUtils.getTempRecording2FilePath(context));
            FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(activity, ChangeVoiceActivity.class);
                    intent.setAction(RECORD_TO_CHANGE_VOICE);
                    intent.putExtra(NAME_FILE, FileUtils.getName(path));
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                    cancel();
                }

                @Override
                public void onFailed() {
                    binding.txtNoti.setText(R.string.create_error);
                    binding.txtNoti.setTextColor(context.getResources().getColor(R.color.red));
                    binding.btnCancel.setEnabled(true);
                }
            });
        });
    }
}
