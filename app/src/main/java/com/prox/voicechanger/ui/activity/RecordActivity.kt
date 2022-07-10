package com.prox.voicechanger.ui.activity

import android.Manifest
import com.prox.voicechanger.utils.FileUtils.Companion.getTempTextToSpeechFilePath
import androidx.navigation.ui.NavigationUI.navigateUp
import com.prox.voicechanger.utils.PermissionUtils.checkPermission
import com.prox.voicechanger.utils.PermissionUtils.openDialogAccessAllFile
import com.prox.voicechanger.utils.FileUtils.Companion.getRealPath
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import android.speech.tts.TextToSpeech
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import android.os.Bundle
import com.prox.voicechanger.VoiceChangerApp
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import com.prox.voicechanger.R
import android.widget.Toast
import android.view.WindowManager
import androidx.navigation.fragment.NavHostFragment
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.prox.voicechanger.ui.dialog.MoreOptionDialog
import com.prox.voicechanger.ui.dialog.TextToVoiceDialog
import com.prox.voicechanger.ui.dialog.LoadingDialog
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.bumptech.glide.Glide.init
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.prox.voicechanger.databinding.ActivityRecordBinding
import com.prox.voicechanger.databinding.DialogLoading2Binding
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.ui.dialog.OptionDialog
import com.prox.voicechanger.ui.dialog.OptionDialog.Companion.fileVoice
import com.prox.voicechanger.utils.PermissionUtils.REQUEST_PERMISSION
import kotlinx.coroutines.*
import java.io.File
import java.util.*

@AndroidEntryPoint
class RecordActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private var appBarConfiguration: AppBarConfiguration? = null
    private var mTts: TextToSpeech? = null
    private var model: FileVoiceViewModel? = null
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var riversRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(VoiceChangerApp.TAG, "RecordActivity: onCreate")
        super.onCreate(savedInstanceState)
        val binding = ActivityRecordBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)
        model!!.isExecuteText().observe(this) { isExecute: Boolean ->
            if (isExecute) {
                val goToChangeVoice = Intent(this@RecordActivity, ChangeVoiceActivity::class.java)
                goToChangeVoice.action = IMPORT_TEXT_TO_SPEECH
                goToChangeVoice.putExtra(
                    ChangeVoiceActivity.PATH_FILE,
                    getTempTextToSpeechFilePath(this@RecordActivity)
                )
                startActivity(goToChangeVoice)
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
                Log.d(VoiceChangerApp.TAG, "RecordActivity: To ChangeVoiceActivity")
            } else {
                Toast.makeText(this@RecordActivity, R.string.process_error, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        init()

        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.reference

        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            activityScope.launch {
                val listFileVoice = model!!.getFileVoices().value

                for (fileVoice in listFileVoice!!) {
                    withContext(Dispatchers.IO) {
                        val docRef =
                            db.collection(auth.currentUser!!.uid).document(fileVoice!!.name)
                        docRef.get().addOnSuccessListener { document ->
                            if (document.data == null) {
                                val data = hashMapOf(
                                    "id" to fileVoice.id,
                                    "src" to fileVoice.src,
                                    "name" to fileVoice.name,
                                    "path" to fileVoice.path,
                                    "duration" to fileVoice.duration,
                                    "size" to fileVoice.size,
                                    "date" to fileVoice.date
                                )
                                db.collection(auth.currentUser!!.uid)
                                    .document(fileVoice.name)
                                    .set(data)

                                val file = Uri.fromFile(File(fileVoice.path))

                                riversRef =
                                    storageRef!!.child("${auth.currentUser!!.uid}/${fileVoice.name}")
                                riversRef!!.putFile(file)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "RecordActivity: onDestroy")
        appBarConfiguration = null
        navController = null
        if (mTts != null) {
            mTts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController == null || appBarConfiguration == null) {
            super.onSupportNavigateUp()
        } else {
            (navigateUp(navController!!, appBarConfiguration!!)
                    || super.onSupportNavigateUp())
        }
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "RecordActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_record_activity) as NavHostFragment?
        navController = if (navHostFragment != null) {
            navHostFragment.navController
        } else {
            Log.d(VoiceChangerApp.TAG, "RecordActivity: navHostFragment null")
            recreate()
            return
        }
        appBarConfiguration = AppBarConfiguration.Builder(navController!!.graph).build()
        checkPermission(this, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
//                if (navController == null){
//                    Log.d(TAG, "RecordActivity: navController null");
//                }else {
//                    navController.navigate(R.id.action_recordFragment_to_stopRecordFragment);
//                    Log.d(TAG, "RecordActivity: To StopRecordFragment");
//                }
            } else {
                openDialogAccessAllFile(this)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION) {
            val record = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (record == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED) {
//                if (navController == null){
//                    Log.d(TAG, "RecordActivity: navController null");
//                }else {
//                    navController.navigate(R.id.action_recordFragment_to_stopRecordFragment);
//                    Log.d(TAG, "RecordActivity: To StopRecordFragment");
//                }
            } else {
                openDialogAccessAllFile(this)
            }
        } else if (requestCode == MoreOptionDialog.SELECT_AUDIO) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Log.d(VoiceChangerApp.TAG, "RecordActivity: data null")
                } else {
                    val filePath = getRealPath(this, data.data)
                    if (filePath!!.isEmpty()) {
                        Log.d(VoiceChangerApp.TAG, "RecordActivity: filePath isEmpty")
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    } else if (!File(filePath).exists()) {
                        Log.d(VoiceChangerApp.TAG, "RecordActivity: filePath not exists")
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(VoiceChangerApp.TAG, "RecordActivity: filePath $filePath")
                        val goToChangeVoice = Intent(this, ChangeVoiceActivity::class.java)
                        goToChangeVoice.action = IMPORT_TO_CHANGE_VOICE
                        goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, filePath)
                        startActivity(goToChangeVoice)
                        overridePendingTransition(
                            R.anim.anim_right_left_1,
                            R.anim.anim_right_left_2
                        )
                        Log.d(VoiceChangerApp.TAG, "RecordActivity: To ChangeVoiceActivity")
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == TextToVoiceDialog.IMPORT_TEXT) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                val dialog = LoadingDialog(
                    this,
                    DialogLoading2Binding.inflate(layoutInflater)
                )
                dialog.show()
                mTts = TextToSpeech(this, OnInitListener label@{ status: Int ->
                    if (status == TextToSpeech.SUCCESS) {
                        mTts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(s: String) {
                                Log.d(VoiceChangerApp.TAG, "onStart")
                            }

                            override fun onDone(s: String) {
                                Log.d(VoiceChangerApp.TAG, "onDone")
                                model!!.setExecuteText(true)
                                dialog.cancel()
                                Log.d(VoiceChangerApp.TAG, "RecordActivity: To ChangeVoiceActivity")
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(s: String) {
                                Log.d(VoiceChangerApp.TAG, "onError")
                                model!!.setExecuteText(false)
                                dialog.cancel()
                            }
                        })
                        mTts!!.language = Locale(TextToVoiceDialog.code_language!!)
                        if (TextToVoiceDialog.textToSpeech!!.isEmpty()) {
                            Toast.makeText(this, R.string.process_error, Toast.LENGTH_SHORT).show()
                            return@label
                        }
                        val params = HashMap<String, String?>()
                        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] =
                            TextToVoiceDialog.textToSpeech
                        Log.d(
                            VoiceChangerApp.TAG,
                            "textToSpeech: " + TextToVoiceDialog.textToSpeech
                        )
                        mTts!!.synthesizeToFile(
                            TextToVoiceDialog.textToSpeech,
                            params,
                            getTempTextToSpeechFilePath(this)
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
    }

    companion object {
        const val IMPORT_TO_CHANGE_VOICE = "IMPORT_TO_CHANGE_VOICE"
        const val IMPORT_TEXT_TO_SPEECH = "IMPORT_TEXT_TO_SPEECH"
    }
}