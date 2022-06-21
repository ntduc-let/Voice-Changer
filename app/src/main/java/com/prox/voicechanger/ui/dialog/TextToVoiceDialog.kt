package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogTextToVoiceBinding;

public class TextToVoiceDialog extends CustomDialog{
    public static final int IMPORT_TEXT = 40;
    public static String textToSpeech;

    public TextToVoiceDialog(@NonNull Context context, Activity activity, DialogTextToVoiceBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "TextToVoiceDialog: create");
        setCancelable(false);

        binding.btnCancel.setOnClickListener(view -> {
            Log.d(TAG, "TextToVoiceDialog: Cancel");
            cancel();
        });

        binding.btnDone.setOnClickListener(view -> {
            Log.d(TAG, "TextToVoiceDialog: Done");
            String text = binding.edtTextToVoice.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(context, R.string.text_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            textToSpeech = text;
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            activity.startActivityForResult(checkIntent, IMPORT_TEXT);
            cancel();
        });
    }
}
