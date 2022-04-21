package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.ActivitySplashBinding;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
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
            Log.d(TAG, "SplashActivity: start Intent null");
            finish();
        }else if (intent.getAction().equals(Intent.ACTION_MAIN)){
            Log.d(TAG, "SplashActivity: start Intent.ACTION_MAIN");
            Intent goToRecord = new Intent(SplashActivity.this, RecordActivity.class);
            startActivity(goToRecord);
            finish();
        }

    }
}