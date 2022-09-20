package com.prox.voicechanger.ui.change_voice.activity

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.ntduc.contextutils.inflater
import com.ntduc.contextutils.showConfirmationDialog
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.ActivityChangeVoiceBinding
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import com.prox.voicechanger.media.Player
import com.prox.voicechanger.model.Effect
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.ui.list_voice.activity.FileVoiceActivity
import com.prox.voicechanger.ui.change_voice.adapter.EffectAdapter
import com.prox.voicechanger.ui.change_voice.dialog.NameDialog
import com.prox.voicechanger.ui.change_voice.fragment.BasicEffectFragment
import com.prox.voicechanger.ui.change_voice.fragment.CustomEffectFragment
import com.prox.voicechanger.ui.home.activity.RecordActivity
import com.prox.voicechanger.utils.*
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.proxglobal.proxads.adsv2.ads.ProxAds
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import com.proxglobal.purchase.ProxPurchase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import space.siy.waveformview.WaveFormData
import space.siy.waveformview.WaveFormView
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class ChangeVoiceActivity : AppCompatActivity(), BasicEffectFragment.Callback,
    WaveFormView.Callback, CustomEffectFragment.Callback {

    companion object {
        const val PATH_FILE = "PATH_FILE"
        const val CHANGE_TO_RECORD = "CHANGE_TO_RECORD"

        var effectSelected = FFMPEGUtils.effects[0]
    }

    private lateinit var binding: ActivityChangeVoiceBinding
    private lateinit var model: FileVoiceViewModel
    private val myScop = CoroutineScope(Job() + Dispatchers.Main)

    private var isCustom = false
    private var isCreatedCustomEffectFragment = false

    private val listFragment = listOf(
        BasicEffectFragment().newInstance(),
        CustomEffectFragment().newInstance()
    )

    private var player: Player? = null
    private var isPlaying = false
    private var isPlayingConvert = false
    private var current = 0.0
    private var nameFile = ""
    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            if (player != null) {
                current = player!!.currentPosition.toDouble() / player!!.duration
                binding.layoutPlayer.visualizer.position = player!!.currentPosition.toLong()
                binding.layoutPlayer.txtCurrentTime.text = NumberUtils.formatAsTime(
                    player!!.currentPosition.toLong()
                )
                handler.post(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeVoiceBinding.inflate(inflater)
        setContentView(binding.root)

        model = ViewModelProvider(this)[FileVoiceViewModel::class.java]

        init()
    }

    override fun onStart() {
        super.onStart()
        if (isPlaying) {
            resumePlayer()
        }
        isPlayingConvert = true

    }

    override fun onStop() {
        super.onStop()
        if (isPlaying) {
            pausePlayer()
        }
        isPlayingConvert = false
    }

    override fun onDestroy() {
        myScop.cancel()
        if (player != null) {
            if (player!!.isPlaying) {
                stopPlayer()
            }
            player!!.release()
        }
        System.gc()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (player?.isPlaying == true) {
            stopPlayer()
        }
        goToRecord()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.REQUEST_PERMISSION_READ_WRITE) {
            if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                initData()
            } else {
                showDialogGoToSetting()
            }
        }
    }

    private fun showDialogGoToSetting() {
        showConfirmationDialog(
            title = getString(R.string.app_name),
            msg = getString(R.string.dialog_request_permission),
            positiveText = getString(R.string.setting),
            negativeText = null,
            cancelable = false,
            onResponse = {
                if (it) goToSetting()
            }
        )
    }

    private fun goToSetting() {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            goToSettingLauncher.launch(intent)
        } catch (_: Exception) {
        }
    }

    private val goToSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (PermissionUtils.checkPermissionRecord(this)) {
                if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                    initData()
                } else {
                    requestPermissionReadWriteFile()
                }
            }

        }

    private fun requestPermissionReadWriteFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermissions(permissions, PermissionUtils.REQUEST_PERMISSION_READ_WRITE)
        }
    }

    private fun init() {
        if (PermissionUtils.checkPermissionReadWriteFile(this)) {
            initData()
        } else {
            requestPermissionReadWriteFile()
        }
        initView()
        initEvent()
    }

    private fun initData() {
        val intent = intent
        if (intent == null || intent.action == null) {
            goToRecord()
            return
        }

        val path = intent.getStringExtra(PATH_FILE)
        if (path == null || !File(path).exists()) {
            goToRecord()
            return
        }

        nameFile =
            if (intent.action == NameDialog.RECORD_TO_CHANGE_VOICE || intent.action == RecordActivity.IMPORT_TEXT_TO_SPEECH) {
                FileUtils.getName(FileUtils.recordingFileName)
            } else {
                FileUtils.getName(path)
            }

        val cmd =
            FFMPEGUtils.getCMDConvertRecording(path, FileUtils.getTempRecording2FilePath(this))
        myScop.launch(Dispatchers.IO) {
            FFMPEGUtils.executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                override fun onSuccess() {
                    myScop.launch(Dispatchers.Main) {
                        selectEffect(effectSelected)
                    }
                }

                override fun onFailed() {
                    myScop.launch(Dispatchers.Main) {
                        loadingPlayer(null)
                    }
                }
            })
        }
    }

    private fun loadingPlayer(isLoading: Boolean?) {
        if (isLoading == null) {
            EffectAdapter.isExecuting = false

            binding.layoutLoading.txtProcessing.setText(R.string.process_error)
            binding.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.red))
        } else if (isLoading) {
            EffectAdapter.isExecuting = true

            binding.layoutPlayer.root.visibility = View.INVISIBLE
            binding.layoutLoading.root.visibility = View.VISIBLE
            binding.layoutLoading.txtProcessing.setText(R.string.processing)
            binding.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.white30))
            binding.btnSave2.isEnabled = false
            binding.btnSave2.setTextColor(resources.getColor(R.color.white30))
            binding.btnSave2.setBackgroundResource(R.drawable.bg_button6)
        } else {
            binding.layoutPlayer.root.visibility = View.VISIBLE
            binding.layoutLoading.root.visibility = View.GONE
            binding.btnSave2.isEnabled = true
            binding.btnSave2.setTextColor(resources.getColor(R.color.white))
            binding.btnSave2.setBackgroundResource(R.drawable.bg_button1)
        }
    }

    private fun initView() {
        player = Player()
        isPlaying = true

        val fragment0 = listFragment[0]
        if (fragment0 is BasicEffectFragment) {
            fragment0.setCallback(this)
        }

        val fragment1 = listFragment[1]
        if (fragment1 is CustomEffectFragment) {
            fragment1.setCallback(this)
        }

        loadingPlayer(true)
        clickBasicEffect(true)

        if (ProxPurchase.getInstance().checkPurchased()
            || !NetworkUtils.isNetworkAvailable(this)
        ) {
            binding.bannerContainer.visibility = View.GONE
        }
        ProxAds.instance.showBanner(
            this,
            binding.bannerContainer,
            BuildConfig.banner,
            object : AdsCallback() {}
        )
    }

    private fun initEvent() {
        binding.btnBack2.setOnClickListener { onBackPressed() }

        binding.btnSave2.setOnClickListener {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Save")
            pausePlayer()
            val name = binding.layoutPlayer.txtName2.text.toString()
            val fragment = listFragment[1]
            if (isCreatedCustomEffectFragment) {
                if (fragment is CustomEffectFragment) {
                    if (fragment.isCustom()) {
                        isCustom = true
                    }
                }
            }

            val nameDialog = NameDialog().newInstance(name)
            nameDialog.setOnSaveListener {
                myScop.launch(Dispatchers.IO) {
                    val cmd: String = if (!isCustom) {
                        FFMPEGUtils.getCMDConvertRecording(
                            FileUtils.getTempEffectFilePath(this@ChangeVoiceActivity),
                            it
                        )
                    } else {
                        FFMPEGUtils.getCMDConvertRecording(
                            FileUtils.getTempCustomFilePath(this@ChangeVoiceActivity),
                            it
                        )
                    }

                    FFMPEGUtils.executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                        override fun onSuccess() {
                            myScop.launch(Dispatchers.IO) {
                                insertEffectToDB(it)
                                withContext(Dispatchers.Main) {
                                    goToFileVoice(true)
                                }
                            }
                        }

                        override fun onFailed() {
                            myScop.launch(Dispatchers.Main) {
                                goToFileVoice(false)
                            }
                        }
                    })
                }
            }
            nameDialog.show(supportFragmentManager, "NameDialog")
        }

        binding.layoutPlayer.visualizer.callback = this

        binding.layoutPlayer.btnPauseOrResume.setOnClickListener {
            isPlaying = if (isPlaying) {
                pausePlayer()
                false
            } else {
                resumePlayer()
                true
            }
        }

        binding.layoutEffect.btnEffect.setOnClickListener {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Effect")

            val fragment = listFragment[1]
            if (fragment is CustomEffectFragment) {
                if (fragment.isCustom()) {
                    isCustom = true
                }
            }

            if (isCustom) {
                if (EffectAdapter.isExecuting) {
                    shortToast(R.string.processing_in_progress)
                    return@setOnClickListener
                }
                EffectAdapter.isExecuting = true
                loadingPlayer(true)
                setNewPlayer(FileUtils.getTempEffectFilePath(this))
            }

            val fragment1 = listFragment[1]
            if (fragment1 is CustomEffectFragment) {
                fragment1.resetCustomEffect()
            }

            clickBasicEffect(true)
        }

        binding.layoutEffect.btnCustom.setOnClickListener {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Custom")
            clickBasicEffect(false)
        }
    }

    private fun insertEffectToDB(path: String) {
        val fileVoice = FileVoice()
        fileVoice.src = effectSelected.src
        fileVoice.name = FileUtils.getName(path)
        fileVoice.path = path
        val playerEffect = MediaPlayer()
        try {
            playerEffect.setDataSource(path)
            playerEffect.prepare()
        } catch (e: IOException) {
            return
        }
        fileVoice.duration = playerEffect.duration.toLong()
        fileVoice.size = File(path).length()
        fileVoice.date = Date().time
        model.insertBG(fileVoice)
    }

    private fun goToFileVoice(isSuccess: Boolean) {
        ProxAds.instance.showInterstitial(this, "interstitial", object : AdsCallback() {
            override fun onClosed() {
                super.onClosed()
                startActivity(Intent(this@ChangeVoiceActivity, FileVoiceActivity::class.java))
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
                if (isSuccess) {
                    shortToast(R.string.save_success)
                } else {
                    shortToast(R.string.save_fail)
                }
            }

            override fun onError() {
                super.onError()
                startActivity(Intent(this@ChangeVoiceActivity, FileVoiceActivity::class.java))
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
                if (isSuccess) {
                    shortToast(R.string.save_success)
                } else {
                    shortToast(R.string.save_fail)
                }
            }
        })
    }

    private fun selectEffect(effect: Effect) {
        effectSelected = effect
        stopPlayer()
        loadingPlayer(true)
        binding.layoutPlayer.txtName2.text = nameFile + "-" + effect.title
        val cmd = FFMPEGUtils.getCMDAddEffect(
            FileUtils.getTempRecording2FilePath(this),
            FileUtils.getTempEffectFilePath(this),
            effect
        )
        myScop.launch(Dispatchers.IO) {
            FFMPEGUtils.executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                override fun onSuccess() {
                    myScop.launch(Dispatchers.Main) {
                        setNewPlayer(FileUtils.getTempEffectFilePath(this@ChangeVoiceActivity))
                    }
                }

                override fun onFailed() {
                    myScop.launch(Dispatchers.Main) {
                        loadingPlayer(null)
                    }
                }
            })
        }
    }

    private fun setNewPlayer(path: String) {
        WaveFormData.Factory(path).build(object : WaveFormData.Factory.Callback {
            override fun onComplete(waveFormData: WaveFormData) {
                myScop.launch(Dispatchers.Main) {
                    binding.layoutPlayer.visualizer.data = waveFormData

                    loadingPlayer(false)
                    if (player == null) {
                        player = Player()
                    }
                    player!!.setNewPath(path)
                    startPlayer()
                    player!!.seekTo((current * player!!.duration).toLong())
                    binding.layoutPlayer.visualizer.position = player!!.currentPosition.toLong()
                    binding.layoutPlayer.txtCurrentTime.text = NumberUtils.formatAsTime(
                        player!!.currentPosition.toLong()
                    )
                    if (!isPlaying || !isPlayingConvert) {
                        pausePlayer()
                    }

                    binding.layoutLoading.progressProcessing.progress = 0f.roundToInt()
                    if (isCreatedCustomEffectFragment) {
                        val fragment = listFragment[1]
                        if (fragment is CustomEffectFragment) {
                            fragment.setEnableCustom(true)
                        }
                    }
                    EffectAdapter.isExecuting = false
                }
            }

            override fun onProgress(v: Float) {
                myScop.launch(Dispatchers.Main) {
                    binding.layoutLoading.progressProcessing.progress = (v * 10).roundToInt()
                }
            }
        })
    }

    private fun startPlayer() {
        player?.start()
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause)
        updateTime()
    }

    private fun stopPlayer() {
        player?.stop()
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume)
        handler.removeCallbacksAndMessages(null)
    }

    private fun pausePlayer() {
        player?.pause()
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume)
        handler.removeCallbacksAndMessages(null)
    }

    private fun resumePlayer() {
        player?.resume()
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause)
        updateTime()
    }

    private fun updateTime() {
        if (player != null) {
            binding.layoutPlayer.txtTotalTime.text = NumberUtils.formatAsTime(
                player!!.duration.toLong()
            )
            handler.post(updateTime)
        }
    }

    private fun goToRecord() {
        val goToRecord = Intent(this, RecordActivity::class.java)
        goToRecord.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        goToRecord.action = CHANGE_TO_RECORD
        startActivity(goToRecord)
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: To RecordActivity")
        finish()
    }

    private fun clickBasicEffect(basicEffect: Boolean) {
        if (basicEffect) {
            isCustom = false

            binding.layoutPlayer.txtName2.text = nameFile + "-" + effectSelected.title

            binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_enable)
            binding.layoutEffect.btnEffect.isEnabled = false
            binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_disable)
            binding.layoutEffect.btnCustom.isEnabled = true

            createChildFragment(listFragment[0])
            isCreatedCustomEffectFragment = false
        } else {
            binding.layoutPlayer.txtName2.text =
                binding.layoutPlayer.txtName2.text.toString() + "-Custom"

            binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_disable)
            binding.layoutEffect.btnEffect.isEnabled = true
            binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_enable)
            binding.layoutEffect.btnCustom.isEnabled = false

            createChildFragment(listFragment[1])
            isCreatedCustomEffectFragment = true

            handler.postDelayed({
                if (!EffectAdapter.isExecuting) {
                    val fragment = listFragment[1]
                    if (fragment is CustomEffectFragment) {
                        fragment.setEnableCustom(true)
                    }
                }
            }, 500)
        }
    }

    private fun createChildFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.layoutEffect.layoutFragment.id, fragment).commit()
    }

    override fun addEffect(effect: Effect) {
        selectEffect(effect)
    }

    override fun onPlayPause() {}

    override fun onSeek(pos: Long) {
        player!!.seekTo(pos)
        binding.layoutPlayer.txtCurrentTime.text = NumberUtils.formatAsTime(
            player!!.currentPosition.toLong()
        )
    }

    override fun addCustom(cmd: String) {
        if (EffectAdapter.isExecuting) {
            return
        }
        val fragment = listFragment[1]
        if (fragment is CustomEffectFragment) {
            fragment.setEnableCustom(false)
        }
        stopPlayer()
        loadingPlayer(true)

        myScop.launch(Dispatchers.IO) {
            FFMPEGUtils.executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                override fun onSuccess() {
                    myScop.launch(Dispatchers.Main) {
                        setNewPlayer(FileUtils.getTempEffectFilePath(this@ChangeVoiceActivity))
                    }
                }

                override fun onFailed() {
                    myScop.launch(Dispatchers.Main) {
                        loadingPlayer(null)
                    }
                }
            })
        }
    }
}