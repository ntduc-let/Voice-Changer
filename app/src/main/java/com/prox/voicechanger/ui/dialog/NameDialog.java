package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogNameBinding;
import com.prox.voicechanger.ui.activity.ChangeVoiceActivity;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;

import java.io.File;

public class NameDialog extends CustomDialog{
    public static final String RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE";
    public static final String PATH_FILE = "PATH_FILE";

    public NameDialog(@NonNull Context context, Activity activity, DialogNameBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "NameDialog: create");
        setCancelable(false);

        binding.edtName.setText(FileUtils.getName(FileUtils.getRecordingFileName()));

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "NameDialog: Cancel");
            NavController navController= Navigation.findNavController(activity, R.id.nav_host_record_activity);
            navController.popBackStack();
            cancel();
        });

        binding.btnSave.setOnClickListener(view -> {
            Log.d(TAG, "NameDialog: Save");

            String path = FileUtils.getDownloadFolderPath("VoiceChanger")+"/"+binding.edtName.getText().toString().trim()+".mp3";
            File file = new File(path);
            if (file.exists()){
                Toast.makeText(context, R.string.name_exits, Toast.LENGTH_SHORT).show();
            }else {
                binding.txtNoti.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
                binding.btnSave.setBackgroundResource(R.drawable.bg_button_enable30);
                binding.btnSave.setTextColor(context.getResources().getColor(R.color.white30));

                new Handler().post(() -> {
                    String cmd = FFMPEGUtils.getCMDConvertRecording(FileUtils.getTempRecordingFilePath(context), file.getPath());
                    if (FFMPEGUtils.executeFFMPEG(cmd)){
                        Intent intent = new Intent(activity, ChangeVoiceActivity.class);
                        intent.setAction(RECORD_TO_CHANGE_VOICE);
                        intent.putExtra(PATH_FILE, file.getPath());
                        activity.startActivity(intent);
                        cancel();
                    }else {
                        binding.txtNoti.setText(R.string.create_error);
                        binding.txtNoti.setTextColor(context.getResources().getColor(R.color.red));
                    }
                });
            }
        });
    }
}
