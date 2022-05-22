package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.activity.ChangeVoiceActivity.PATH_FILE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.prox.voicechanger.BuildConfig;
import com.prox.voicechanger.R;
import com.prox.voicechanger.VoiceChangerApp;
import com.prox.voicechanger.databinding.ActivitySplashBinding;
import com.prox.voicechanger.utils.FileUtils;
import com.proxglobal.proxads.adsv2.callback.AdsCallback;

import java.io.File;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static final String SPLASH_TO_CHANGE_VOICE = "SPLASH_TO_CHANGE_VOICE";
    public static final String SPLASH_TO_RECORD = "SPLASH_TO_RECORD";

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "SplashActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        init();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int open_app = preferences.getInt("open_app", 0);
        preferences.edit().putInt("open_app", open_app + 1).apply();
        Log.d(TAG, "Open app: "+open_app);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "SplashActivity: onDestroy");
        binding = null;
        super.onDestroy();
    }

    private void init(){
        Log.d(TAG, "SplashActivity: init");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.background_app));

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

        VoiceChangerApp.instance.initInterstitial(this, BuildConfig.interstitial_home, null, "interstitial_home");
        VoiceChangerApp.instance.initInterstitial(this, BuildConfig.interstitial_save, null, "interstitial_save");
        VoiceChangerApp.instance.initInterstitial(this, BuildConfig.interstitial_text, null, "interstitial_text");


        new Handler().postDelayed(() -> VoiceChangerApp.instance.showSplash(this, new AdsCallback() {
            @Override
            public void onClosed() {
                Log.d(TAG, "SplashActivity Ads onClosed");
                goToMain();
            }

            @Override
            public void onError() {
                Log.d(TAG, "SplashActivity Ads onError");
                goToMain();
            }
        }, BuildConfig.interstitial_splash, null, 12000), 4500);
    }

    private void goToMain() {
        Intent intent = getIntent();
        if (intent==null){
            Log.d(TAG, "SplashActivity: Intent null");
            goToRecord();
        }else if (intent.getAction()==null){
            Log.d(TAG, "SplashActivity: Action null");
            goToRecord();
        }else if (intent.getAction().equals(Intent.ACTION_MAIN)){
            Log.d(TAG, "SplashActivity: Intent.ACTION_MAIN");
            goToRecord();
        }else if (intent.getAction().equals(Intent.ACTION_VIEW)){
            Log.d(TAG, "SplashActivity: Intent.ACTION_VIEW");
            Uri data = getIntent().getData();
            if (data == null){
                Log.d(TAG, "SplashActivity: data null");
                goToRecord();
            }
            String filePath = FileUtils.getRealPath(this, data);
            if (filePath.isEmpty()){
                Log.d(TAG, "SplashActivity: filePath isEmpty");
                goToRecord();
            }else if(!(new File(filePath).exists())){
                Log.d(TAG, "SplashActivity: filePath not exists");
                goToRecord();
            }else{
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

    private void goToRecord(){
        Intent goToRecord = new Intent(SplashActivity.this, RecordActivity.class);
        goToRecord.setAction(SPLASH_TO_RECORD);
        startActivity(goToRecord);
        Log.d(TAG, "SplashActivity: To RecordActivity");
        finish();
    }
}