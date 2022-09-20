package com.prox.voicechanger.ui.home.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ntduc.contextutils.inflater
import com.ntduc.contextutils.showConfirmationDialog
import com.ntduc.fileutils.getRealPath
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.ActivityRecordBinding
import com.prox.voicechanger.databinding.DialogRateBinding
import com.prox.voicechanger.ui.change_voice.activity.ChangeVoiceActivity
import com.prox.voicechanger.ui.dialog.RateDialog
import com.prox.voicechanger.ui.list_voice.activity.FileVideoActivity
import com.prox.voicechanger.ui.list_voice.activity.FileVoiceActivity
import com.prox.voicechanger.ui.home.dialog.TextToVoiceDialog
import com.prox.voicechanger.ui.home.fragment.RecordFragment
import com.prox.voicechanger.ui.home.fragment.SettingFragment
import com.prox.voicechanger.ui.home.fragment.StopRecordFragment
import com.prox.voicechanger.ui.list_voice.dialog.LoadingDialog
import com.prox.voicechanger.ui.splash.SplashActivity
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.utils.NetworkUtils
import com.prox.voicechanger.utils.PermissionUtils
import com.proxglobal.proxads.adsv2.ads.ProxAds
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import com.proxglobal.purchase.ProxPurchase
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import kotlin.collections.HashMap

@AndroidEntryPoint
class RecordActivity : AppCompatActivity(), RecordFragment.Callback, StopRecordFragment.Callback,
    SettingFragment.Callback {

    companion object {
        const val IMPORT_TO_CHANGE_VOICE = "IMPORT_TO_CHANGE_VOICE"
        const val IMPORT_TEXT_TO_SPEECH = "IMPORT_TEXT_TO_SPEECH"

        private const val CLICK_RECORD = 1
        private const val CLICK_SETTING = 2
        private const val CLICK_IMPORT = 3
        private const val CLICK_TEXT_TO_VOICE = 4
        private const val CLICK_FILE = 5
        private const val CLICK_VIDEO = 6
    }

    private lateinit var binding: ActivityRecordBinding
    private lateinit var textToVoiceDialog: TextToVoiceDialog
    private lateinit var loadingDialog: LoadingDialog

    private val listFragment = listOf(
        RecordFragment().newInstance(),
        StopRecordFragment().newInstance(),
        SettingFragment().newInstance()
    )

    private var mTts: TextToSpeech? = null
    private var action: Int = 0
    private var textTTS: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(inflater)
        setContentView(binding.root)

        initView()
    }

    override fun onDestroy() {
        mTts?.shutdown()
        System.gc()
        super.onDestroy()
    }

    private fun initView() {
        textToVoiceDialog = TextToVoiceDialog()
        textToVoiceDialog.setOnDoneListener {
            textTTS = it
            try{
                val checkIntent = Intent()
                checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                checkTTSLauncher.launch(checkIntent)
            }catch (e: Exception) {
                shortToast(R.string.process_error)
            }

        }

        loadingDialog = LoadingDialog()

        val fragment0 = listFragment[0]
        if (fragment0 is RecordFragment) {
            fragment0.setCallback(this)
        }
        val fragment1 = listFragment[1]
        if (fragment1 is StopRecordFragment) {
            fragment1.setCallback(this)
        }
        val fragment2 = listFragment[2]
        if (fragment2 is SettingFragment) {
            fragment2.setCallback(this)
        }
        createChildFragment(listFragment[0])

        if (intent?.action == SplashActivity.SPLASH_TO_RECORD) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val open_app = preferences.getInt("open_app", 1)
            if (open_app >= 2) {
                val dialog = RateDialog(
                    this, DialogRateBinding.inflate(
                        layoutInflater
                    )
                ) {}
                dialog.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.REQUEST_PERMISSION_RECORD) {
            if (PermissionUtils.checkPermissionRecord(this)) {
                if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                    if (action == CLICK_RECORD) {
                        ProxAds.instance.showInterstitial(
                            this,
                            "interstitial",
                            object : AdsCallback() {
                                override fun onClosed() {
                                    super.onClosed()
                                    createChildFragment(listFragment[1])
                                }

                                override fun onError() {
                                    super.onError()
                                    createChildFragment(listFragment[1])
                                }
                            })
                    }
                } else {
                    requestPermissionReadWriteFile()
                }
            } else {
                showDialogGoToSetting(PermissionUtils.REQUEST_PERMISSION_RECORD)
            }
        } else if (requestCode == PermissionUtils.REQUEST_PERMISSION_READ_WRITE) {
            if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                when (action) {
                    CLICK_RECORD -> {
                        ProxAds.instance.showInterstitial(
                            this,
                            "interstitial",
                            object : AdsCallback() {
                                override fun onClosed() {
                                    super.onClosed()
                                    createChildFragment(listFragment[1])
                                }

                                override fun onError() {
                                    super.onError()
                                    createChildFragment(listFragment[1])
                                }
                            })
                    }
                    CLICK_IMPORT -> {
                        selectImportAudio()
                    }
                    CLICK_TEXT_TO_VOICE -> {
                        textToVoiceDialog.show(supportFragmentManager, "TextToVoiceDialog")
                    }
                }
            } else {
                showDialogGoToSetting(PermissionUtils.REQUEST_PERMISSION_READ_WRITE)
            }
        }
    }

    override fun onRecord() {
        action = CLICK_RECORD
        if (PermissionUtils.checkPermissionRecord(this)) {
            if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                ProxAds.instance.showInterstitial(
                    this,
                    "interstitial",
                    object : AdsCallback() {
                        override fun onClosed() {
                            super.onClosed()
                            createChildFragment(listFragment[1])
                        }

                        override fun onError() {
                            super.onError()
                            createChildFragment(listFragment[1])
                        }
                    })
            } else {
                requestPermissionReadWriteFile()
            }
        } else {
            requestPermissionRecord()
        }
    }

    override fun onSetting() {
        action = CLICK_SETTING
        createChildFragment(listFragment[2])
    }

    override fun onImport() {
        action = CLICK_IMPORT
        if (PermissionUtils.checkPermissionReadWriteFile(this)) {
            selectImportAudio()
        } else {
            requestPermissionReadWriteFile()
        }
    }

    override fun onTextToVoice() {
        action = CLICK_TEXT_TO_VOICE
        if (PermissionUtils.checkPermissionReadWriteFile(this)) {
            textToVoiceDialog.show(supportFragmentManager, "TextToVoiceDialog")
        } else {
            requestPermissionReadWriteFile()
        }
    }

    override fun onFile() {
        action = CLICK_FILE
        startActivity(Intent(this, FileVoiceActivity::class.java))
        overridePendingTransition(
            R.anim.anim_right_left_1,
            R.anim.anim_right_left_2
        )
    }

    override fun onVideo() {
        action = CLICK_VIDEO
        startActivity(Intent(this, FileVideoActivity::class.java))
        overridePendingTransition(
            R.anim.anim_right_left_1,
            R.anim.anim_right_left_2
        )
    }

    override fun onClose() {
        action = 0
        createChildFragment(listFragment[0])
    }

    private fun createChildFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.layoutFragment.id, fragment).commit()
    }

    private fun requestPermissionRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            requestPermissions(permissions, PermissionUtils.REQUEST_PERMISSION_RECORD)
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

    private fun showDialogGoToSetting(type: Int) {
        showConfirmationDialog(
            title = getString(R.string.app_name),
            msg =
            if (type == PermissionUtils.REQUEST_PERMISSION_RECORD)
                getString(R.string.dialog_request_permission_record)
            else
                getString(R.string.dialog_request_permission),
            positiveText = getString(R.string.setting),
            negativeText = getString(R.string.cancel),
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

    private fun selectImportAudio() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        importLauncher.launch(intent)
        overridePendingTransition(
            R.anim.anim_right_left_1,
            R.anim.anim_right_left_2
        )
    }

    private val goToSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (action == CLICK_RECORD) {
                if (PermissionUtils.checkPermissionRecord(this)) {
                    if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                        createChildFragment(listFragment[1])
                    } else {
                        requestPermissionReadWriteFile()
                    }
                }
            } else if (action == CLICK_IMPORT) {
                if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                    selectImportAudio()
                } else {
                    requestPermissionReadWriteFile()
                }
            } else if (action == CLICK_TEXT_TO_VOICE) {
                if (PermissionUtils.checkPermissionReadWriteFile(this)) {
                    textToVoiceDialog.show(supportFragmentManager, "TextToVoiceDialog")
                } else {
                    requestPermissionReadWriteFile()
                }
            }

        }

    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (it.data == null) return@registerForActivityResult

                val path = it.data!!.data?.getRealPath(this)
                if (path == null
                    || path.isEmpty()
                    || !File(path).exists()
                ) {
                    shortToast(R.string.file_not_exist)
                    return@registerForActivityResult
                }

                goToChangeVoice(IMPORT_TO_CHANGE_VOICE, path)
            } else if (it.resultCode == RESULT_CANCELED) {
                shortToast(R.string.canceled)
            }
        }

    private val checkTTSLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ProxAds.instance.showInterstitial(
                this@RecordActivity,
                "interstitial",
                object : AdsCallback() {
                    override fun onClosed() {
                        super.onClosed()
                    }

                    override fun onError() {
                        super.onError()
                    }
                })
            if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                if (ProxPurchase.getInstance().checkPurchased()
                    || !NetworkUtils.isNetworkAvailable(this)
                ) {
                    loadingDialog.show(supportFragmentManager, "LoadingDialog")
                }
                mTts = TextToSpeech(this, TextToSpeech.OnInitListener { status: Int ->
                    if (status == TextToSpeech.SUCCESS) {
                        mTts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}

                            override fun onDone(s: String) {
                                doneTTSConvert()
                                loadingDialog.dismiss()
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(s: String) {
                                shortToast(R.string.process_error)
                                loadingDialog.dismiss()
                            }
                        })
                        mTts!!.language = Locale.US
                        if (textTTS.isEmpty()) {
                            shortToast(R.string.process_error)
                            return@OnInitListener
                        }
                        val params = HashMap<String, String>()
                        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = textTTS
                        mTts!!.synthesizeToFile(
                            textTTS,
                            params,
                            FileUtils.getTempTextToSpeechFilePath(this)
                        )
                    }
                })
            } else {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
            }
        }

    private fun doneTTSConvert() {
        ProxAds.instance.showInterstitial(
            this@RecordActivity,
            "interstitial",
            object : AdsCallback() {
                override fun onClosed() {
                    super.onClosed()
                    goToChangeVoice(
                        IMPORT_TEXT_TO_SPEECH,
                        FileUtils.getTempTextToSpeechFilePath(this@RecordActivity)
                    )
                }

                override fun onError() {
                    super.onError()
                    goToChangeVoice(
                        IMPORT_TEXT_TO_SPEECH,
                        FileUtils.getTempTextToSpeechFilePath(this@RecordActivity)
                    )
                }
            })
    }

    private fun goToChangeVoice(action: String, path: String) {
        val intent = Intent(this, ChangeVoiceActivity::class.java)
        intent.action = action
        intent.putExtra(ChangeVoiceActivity.PATH_FILE, path)
        startActivity(intent)
        overridePendingTransition(
            R.anim.anim_right_left_1,
            R.anim.anim_right_left_2
        )
    }
}