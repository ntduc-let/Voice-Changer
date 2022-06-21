package com.prox.voicechanger.ui.activity

import com.prox.voicechanger.utils.FileUtils.Companion.getRealPath
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.prox.voicechanger.VoiceChangerApp
import android.view.WindowManager
import com.prox.voicechanger.R
import android.graphics.drawable.AnimationDrawable
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import com.prox.voicechanger.databinding.ActivitySplashBinding
import java.io.File

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(VoiceChangerApp.TAG, "SplashActivity: onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        init()
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "SplashActivity: onDestroy")
        binding = null
        super.onDestroy()
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "SplashActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        val rotate1Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate1)
        val rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2)
        val rotate3Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate3)
        val rotate4Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate4)
        binding!!.aniRecord.icAniRecord1.startAnimation(rotate2Animation)
        binding!!.aniRecord.icAniRecord2.startAnimation(rotate4Animation)
        binding!!.aniRecord.icAniRecord3.startAnimation(rotate1Animation)
        binding!!.aniRecord.icAniRecord4.startAnimation(rotate2Animation)
        binding!!.aniRecord.icAniRecord5.startAnimation(rotate4Animation)
        binding!!.aniRecord.icAniRecord6.startAnimation(rotate3Animation)
        val rocketAnimation = binding!!.aniRecord.icAniRecord7.background as AnimationDrawable
        rocketAnimation.start()
        Handler(Looper.getMainLooper()).postDelayed({ goToMain() }, 4500)
    }

    private fun goToMain() {
        val intent = intent
        if (intent == null) {
            Log.d(VoiceChangerApp.TAG, "SplashActivity: Intent null")
            goToRecord()
        } else if (intent.action == null) {
            Log.d(VoiceChangerApp.TAG, "SplashActivity: Action null")
            goToRecord()
        } else if (intent.action == Intent.ACTION_MAIN) {
            Log.d(VoiceChangerApp.TAG, "SplashActivity: Intent.ACTION_MAIN")
            goToRecord()
        } else if (intent.action == Intent.ACTION_VIEW) {
            Log.d(VoiceChangerApp.TAG, "SplashActivity: Intent.ACTION_VIEW")
            val data = getIntent().data
            if (data == null) {
                Log.d(VoiceChangerApp.TAG, "SplashActivity: data null")
                goToRecord()
            }
            val filePath = getRealPath(this, data)
            if (filePath!!.isEmpty()) {
                Log.d(VoiceChangerApp.TAG, "SplashActivity: filePath isEmpty")
                goToRecord()
            } else if (!File(filePath).exists()) {
                Log.d(VoiceChangerApp.TAG, "SplashActivity: filePath not exists")
                goToRecord()
            } else {
                Log.d(VoiceChangerApp.TAG, "SplashActivity: filePath $filePath")
                val goToChangeVoice = Intent(this@SplashActivity, ChangeVoiceActivity::class.java)
                goToChangeVoice.action = SPLASH_TO_CHANGE_VOICE
                goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, filePath)
                startActivity(goToChangeVoice)
                Log.d(VoiceChangerApp.TAG, "SplashActivity: To ChangeVoiceActivity")
                finish()
            }
        }
    }

    private fun goToRecord() {
        val goToRecord = Intent(this@SplashActivity, RecordActivity::class.java)
        goToRecord.action = SPLASH_TO_RECORD
        startActivity(goToRecord)
        Log.d(VoiceChangerApp.TAG, "SplashActivity: To RecordActivity")
        finish()
    }

    companion object {
        const val SPLASH_TO_CHANGE_VOICE = "SPLASH_TO_CHANGE_VOICE"
        const val SPLASH_TO_RECORD = "SPLASH_TO_RECORD"
    }
}