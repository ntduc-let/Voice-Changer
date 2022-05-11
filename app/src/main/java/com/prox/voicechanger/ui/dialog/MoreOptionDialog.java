package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.prox.voicechanger.databinding.DialogMoreOptionBinding;
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding;
import com.prox.voicechanger.ui.activity.FileVoiceActivity;

public class MoreOptionDialog extends Dialog {
    public static final int SELECT_AUDIO = 30;

    public MoreOptionDialog(@NonNull Context context, Activity activity, DialogMoreOptionBinding binding) {
        super(context);
        Log.d(TAG, "MoreOptionDialog: create");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());

        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP|Gravity.END;
        layoutParams.y = 32;
        layoutParams.x = 16;
        getWindow().setAttributes(layoutParams);
        setCancelable(true);

        binding.btnImport.setOnClickListener(view -> {
            Log.d(TAG, "MoreOptionDialog: Import pre-recorded sound");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(intent, SELECT_AUDIO);
            cancel();
        });

        binding.btnTextToVoice.setOnClickListener(view -> {
            Log.d(TAG, "MoreOptionDialog: Create voice from text");
            TextToVoiceDialog dialog = new TextToVoiceDialog(
                    context,
                    activity,
                    DialogTextToVoiceBinding.inflate(getLayoutInflater())
            );
            dialog.show();
            cancel();
        });

        binding.btnFile.setOnClickListener(view -> {
            Log.d(TAG, "MoreOptionDialog: Recorded file");
            activity.startActivity(new Intent(activity, FileVoiceActivity.class));
            Log.d(TAG, "MoreOptionDialog: To FileVoiceActivity");
            cancel();
        });
    }
}