package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.activity.ChangeVoiceActivity.PATH_FILE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.ActivitySplashBinding;
import com.prox.voicechanger.utils.FileUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static final String SPLASH_TO_CHANGE_VOICE = "SPLASH_TO_CHANGE_VOICE";

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "SplashActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "SplashActivity: onDestroy");
        binding = null;
        super.onDestroy();
    }

    private void init(){
        Log.d(TAG, "SplashActivity: init");
        Animation rotate1Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate1);
        Animation rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2);
        Animation rotate3Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate3);
        Animation rotate4Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate4);
        binding.aniRecord.icAniRecord1.startAnimation(rotate2Animation);
        binding.aniRecord.icAniRecord2.startAnimation(rotate4Animation);
        binding.aniRecord.icAniRecord3.startAnimation(rotate1Animation);
        binding.aniRecord.icAniRecord4.startAnimation(rotate2Animation);
        binding.aniRecord.icAniRecord5.startAnimation(rotate4Animation);
        binding.aniRecord.icAniRecord6.startAnimation(rotate3Animation);

        AnimationDrawable rocketAnimation = (AnimationDrawable) binding.aniRecord.icAniRecord7.getBackground();
        rocketAnimation.start();

        new Handler().postDelayed(this::goToMain, 4500);
    }

    private void goToMain() {
        Intent intent = getIntent();
        if (intent==null){
            Log.d(TAG, "SplashActivity: Intent null");
            Intent goToRecord = new Intent(SplashActivity.this, RecordActivity.class);
            startActivity(goToRecord);
            Log.d(TAG, "SplashActivity: To RecordActivity");
            finish();
        }else if (intent.getAction()==null){
            Log.d(TAG, "SplashActivity: Action null");
            Intent goToRecord = new Intent(SplashActivity.this, RecordActivity.class);
            startActivity(goToRecord);
            Log.d(TAG, "SplashActivity: To RecordActivity");
            finish();
        }else if (intent.getAction().equals(Intent.ACTION_MAIN)){
            Log.d(TAG, "SplashActivity: Intent.ACTION_MAIN");
            Intent goToRecord = new Intent(SplashActivity.this, RecordActivity.class);
            startActivity(goToRecord);
            Log.d(TAG, "SplashActivity: To RecordActivity");
            finish();
        }else if (intent.getAction().equals(Intent.ACTION_VIEW)){
            Log.d(TAG, "SplashActivity: Intent.ACTION_VIEW");
            Uri data = getIntent().getData();
            String filePath = FileUtils.getFilePathForN(this, data);
            Log.d(TAG, "SplashActivity: filePath "+filePath);

            Intent goToChangeVoice = new Intent(SplashActivity.this, ChangeVoiceActivity.class);
            goToChangeVoice.setAction(SPLASH_TO_CHANGE_VOICE);
            goToChangeVoice.putExtra(PATH_FILE, filePath);
            startActivity(goToChangeVoice);
            Log.d(TAG, "SplashActivity: To ChangeVoiceActivity");
            finish();
        }
    }
}