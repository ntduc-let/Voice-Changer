package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.ui.activity.FileVoiceActivity.PATH_VIDEO;
import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;

import com.prox.voicechanger.databinding.ActivityPlayVideoBinding;
import com.prox.voicechanger.utils.FileUtils;

public class PlayVideoActivity extends AppCompatActivity {
    private ActivityPlayVideoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "PlayVideoActivity onCreate");
        binding = ActivityPlayVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        binding.btnClose2.setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "PlayVideoActivity onDestroy");
        binding = null;
        super.onDestroy();
    }

    private void init(){
        Log.d(TAG, "PlayVideoActivity init");
        Intent intent = getIntent();
        if (intent == null){
            Log.d(TAG, "PlayVideoActivity start intent null");
            finish();
            return;
        }
        String pathVideo = intent.getStringExtra(PATH_VIDEO);
        binding.txtNameVideo.setText(FileUtils.getName(pathVideo));
        binding.videoView.setVideoPath(pathVideo);

        MediaController mediaController = new MediaController(this);
        binding.videoView.setMediaController(mediaController);
        mediaController.setAnchorView(binding.videoView);
    }
}