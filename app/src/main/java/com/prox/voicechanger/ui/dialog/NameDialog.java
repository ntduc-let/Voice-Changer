package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.FOLDER_APP;
import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogNameBinding;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.model.Effect;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.activity.FileVoiceActivity;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class NameDialog extends CustomDialog {
    public static final String RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE";

    public NameDialog(@NonNull Context context,
                      Activity activity,
                      DialogNameBinding binding,
                      FileVoiceViewModel model,
                      String name,
                      boolean isCustom,
                      Effect effectSelected) {
        super(context, binding.getRoot());
        Log.d(TAG, "NameDialog: create");
        setCancelable(false);

        binding.edtName.setText(name);

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "NameDialog: Cancel");
            cancel();
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

            String cmd;
            if (!isCustom) {
                cmd = FFMPEGUtils.getCMDConvertRecording(FileUtils.getTempEffectFilePath(context), path);
            } else {
                cmd = FFMPEGUtils.getCMDConvertRecording(FileUtils.getTempCustomFilePath(context), path);
            }

            FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
                @Override
                public void onSuccess() {
                    insertEffectToDB(model, effectSelected, path);
                    activity.startActivity(new Intent(activity, FileVoiceActivity.class));
                    activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                    Log.d(TAG, "NameDialog: To FileVoiceActivity");
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

    private void insertEffectToDB(FileVoiceViewModel model, Effect effect, String path) {
        FileVoice fileVoice = new FileVoice();
        if (effect == null) {
            Log.d(TAG, "ChangeVoiceActivity: effect null");
        } else {
            fileVoice.setSrc(effect.getSrc());
            fileVoice.setName(FileUtils.getName(path));
            fileVoice.setPath(path);

            MediaPlayer playerEffect = new MediaPlayer();
            try {
                playerEffect.setDataSource(path);
                playerEffect.prepare();
            } catch (IOException e) {
                Log.d(TAG, "insertEffectToDB: " + e.getMessage());
                return;
            }
            fileVoice.setDuration(playerEffect.getDuration());
            fileVoice.setSize(new File(path).length());
            fileVoice.setDate(new Date().getTime());
            model.insertBG(fileVoice);
        }
    }
}
