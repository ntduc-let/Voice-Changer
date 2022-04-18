package com.prox.voicechanger.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogNameBinding;
import com.prox.voicechanger.recorder.Recorder;
import com.prox.voicechanger.ui.activity.ChangeVoiceActivity;
import com.prox.voicechanger.utils.FileUtils;

public class NameDialog extends CustomDialog{
    public static final String RECORD_TO_CHANGE_VOICE = "RECORD_TO_CHANGE_VOICE";
    private static final String PATH_FILE = "PATH_FILE";

    public NameDialog(@NonNull Context context, Activity activity, DialogNameBinding binding, Recorder recorder) {
        super(context, binding.getRoot());
        setCancelable(false);

        binding.edtName.setText(recorder.getName());

        binding.btnCancel.setOnClickListener(view -> {
            if (FileUtils.deleteFile(context, recorder.getPath())){
                NavController navController= Navigation.findNavController(activity, R.id.nav_host_fragment);
                navController.popBackStack();
            }
            cancel();
        });

        binding.btnSave.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ChangeVoiceActivity.class);
            intent.setAction(RECORD_TO_CHANGE_VOICE);
            if (recorder.getName().equals(binding.edtName.getText().toString().trim())){
                intent.putExtra(PATH_FILE, recorder.getPath());
            }else {
                String newPath = FileUtils.renameFile(context, recorder.getPath(), binding.edtName.getText().toString());
                if (newPath != null){
                    intent.putExtra(PATH_FILE, newPath);
                }else {
                    Toast.makeText(context, R.string.save_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            activity.startActivity(intent);
            cancel();
        });
    }
}
