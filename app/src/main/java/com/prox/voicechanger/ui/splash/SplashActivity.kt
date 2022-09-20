package com.prox.voicechanger.ui.splash

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.prox.voicechanger.R
import android.graphics.drawable.AnimationDrawable
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.animation.AnimationUtils
import com.ntduc.contextutils.inflater
import com.ntduc.fileutils.getRealPath
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.ActivitySplashBinding
import com.prox.voicechanger.ui.change_voice.activity.ChangeVoiceActivity
import com.prox.voicechanger.ui.home.activity.RecordActivity
import com.proxglobal.proxads.adsv2.ads.ProxAds
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import java.io.File

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val SPLASH_TO_CHANGE_VOICE = "SPLASH_TO_CHANGE_VOICE"
        const val SPLASH_TO_RECORD = "SPLASH_TO_RECORD"
    }

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(inflater)
        setContentView(binding.root)

        initView()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val open_app = preferences.getInt("open_app", 0)
        preferences.edit().putInt("open_app", open_app + 1).apply()
    }

    override fun onDestroy() {
        System.gc()
        super.onDestroy()
    }

    private fun initView() {
        val rotate1Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate1)
        val rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2)
        val rotate3Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate3)
        val rotate4Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate4)
        binding.aniRecord.icAniRecord1.startAnimation(rotate2Animation)
        binding.aniRecord.icAniRecord2.startAnimation(rotate4Animation)
        binding.aniRecord.icAniRecord3.startAnimation(rotate1Animation)
        binding.aniRecord.icAniRecord4.startAnimation(rotate2Animation)
        binding.aniRecord.icAniRecord5.startAnimation(rotate4Animation)
        binding.aniRecord.icAniRecord6.startAnimation(rotate3Animation)
        val rocketAnimation = binding.aniRecord.icAniRecord7.background as AnimationDrawable
        rocketAnimation.start()
        ProxAds.instance.initInterstitial(
            this,
            BuildConfig.interstitial,
            null,
            "interstitial"
        )
        Handler(Looper.getMainLooper()).postDelayed({
            ProxAds.instance.showSplash(this, object : AdsCallback() {
                override fun onClosed() {
                    goToMain()
                }

                override fun onError() {
                    goToMain()
                }
            }, BuildConfig.interstitial_splash, null, 12000)
        }, 4500)
    }

    private fun goToMain() {
        if (intent == null) {
            finish()
            return
        }

        if (intent.action == Intent.ACTION_MAIN) {
            goToRecord()
            return
        }

        if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data == null) {
                goToRecord()
                return
            }

            val path = intent.data!!.getRealPath(this)
            if (path == null || path.isEmpty() || !File(path).exists()) {
                goToRecord()
                return
            }
            val goToChangeVoice = Intent(this@SplashActivity, ChangeVoiceActivity::class.java)
            goToChangeVoice.action = SPLASH_TO_CHANGE_VOICE
            goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, path)
            startActivity(goToChangeVoice)
            finish()
        }
    }

    private fun goToRecord() {
        val goToRecord = Intent(this@SplashActivity, RecordActivity::class.java)
        goToRecord.action = SPLASH_TO_RECORD
        startActivity(goToRecord)
        finish()
    }
}