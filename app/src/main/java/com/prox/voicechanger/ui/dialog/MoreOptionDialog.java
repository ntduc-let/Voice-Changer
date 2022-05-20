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

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogMoreOptionBinding;
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding;
import com.prox.voicechanger.ui.activity.FileVideoActivity;
import com.prox.voicechanger.ui.activity.FileVoiceActivity;
import com.prox.voicechanger.utils.FirebaseUtils;

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
        layoutParams.windowAnimations = R.style.MoreOptionDialogAnimation;
        getWindow().setAttributes(layoutParams);
        setCancelable(true);

        binding.btnImport.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(context, "Layout_Home_More", "Click Import pre-recorded sound");
            Log.d(TAG, "MoreOptionDialog: Import pre-recorded sound");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(intent, SELECT_AUDIO);
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
            cancel();
        });

        binding.btnTextToVoice.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(context, "Layout_Home_More", "Click Create voice from text");
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
            FirebaseUtils.sendEvent(context, "Layout_Home_More", "Click Recorded file");
            Log.d(TAG, "MoreOptionDialog: Recorded file");
            activity.startActivity(new Intent(activity, FileVoiceActivity.class));
            Log.d(TAG, "MoreOptionDialog: To FileVoiceActivity");
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
            cancel();
        });

        binding.btnVideo.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(context, "Layout_Home_More", "Click Video file");
            Log.d(TAG, "MoreOptionDialog: Video file");
            activity.startActivity(new Intent(activity, FileVideoActivity.class));
            Log.d(TAG, "MoreOptionDialog: To FileVideoActivity");
            activity.overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
            cancel();
        });
    }
}
