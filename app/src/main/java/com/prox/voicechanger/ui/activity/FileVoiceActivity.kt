package com.prox.voicechanger.ui.activity

import com.prox.voicechanger.utils.FileUtils.Companion.getName
import com.prox.voicechanger.utils.FileUtils.Companion.getTempImagePath
import com.prox.voicechanger.utils.ConvertersUtils.fromBitmap
import com.prox.voicechanger.utils.FileUtils.Companion.getRealPath
import com.prox.voicechanger.utils.FileUtils.Companion.getDCIMFolderPath
import com.prox.voicechanger.utils.FileUtils.Companion.videoFileName
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDConvertImage
import com.prox.voicechanger.utils.FFMPEGUtils.executeFFMPEG
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDAddImage
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import com.prox.voicechanger.adapter.FileVoiceAdapter
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.ui.dialog.PlayVideoDialog
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.ui.dialog.OptionDialog
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import com.prox.voicechanger.VoiceChangerApp
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.prox.voicechanger.R
import android.widget.Toast
import com.prox.voicechanger.ui.dialog.DeleteAllDialog
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.prox.voicechanger.databinding.ActivityFileVoiceBinding
import com.prox.voicechanger.databinding.DialogDeleteAllBinding
import com.prox.voicechanger.databinding.DialogLoading2Binding
import com.prox.voicechanger.databinding.DialogPlayVideoBinding
import com.prox.voicechanger.ui.dialog.LoadingDialog
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class FileVoiceActivity : AppCompatActivity() {
    private var binding: ActivityFileVoiceBinding? = null
    private var adapter: FileVoiceAdapter? = null

    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)
    private var model: FileVoiceViewModel? = null

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var riversRef: StorageReference? = null

    private var pathVideo: String? = null
    private var playVideoDialog: PlayVideoDialog? = null
    private val insertVideoToDB = Runnable label@{
        val fileVideo = FileVoice()
        fileVideo.src = OptionDialog.fileVoice!!.src
        fileVideo.name = getName(pathVideo!!)
        fileVideo.path = pathVideo
        val bitmap = BitmapFactory.decodeFile(getTempImagePath(this))
        fileVideo.image = fromBitmap(bitmap)
        val playerVideo = MediaPlayer()
        try {
            playerVideo.setDataSource(pathVideo)
            playerVideo.prepare()
        } catch (e: IOException) {
            Log.d(VoiceChangerApp.TAG, "insertVideoToDB: " + e.message)
            return@label
        }
        fileVideo.duration = playerVideo.duration.toLong()
        fileVideo.size = File(pathVideo!!).length()
        fileVideo.date = Date().time
        model!!.insertVideoBG(fileVideo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityFileVoiceBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)

        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.reference

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        model!!.getFileVoices().observe(this) { fileVoices: List<FileVoice?>? ->
            if (fileVoices == null) {
                adapter!!.setFileVoices(ArrayList())
                return@observe
            }
            if (fileVoices.isEmpty()) {
                binding!!.layoutNoItem.root.visibility = View.VISIBLE
                binding!!.btnDeleteAll.isEnabled = false
                binding!!.btnDeleteAll.setTextColor(resources.getColor(R.color.white30))
                binding!!.btnDeleteAll.setBackgroundResource(R.drawable.bg_button6)
            } else {
                binding!!.layoutNoItem.root.visibility = View.GONE
                binding!!.btnDeleteAll.isEnabled = true
                binding!!.btnDeleteAll.setTextColor(resources.getColor(R.color.white))
                binding!!.btnDeleteAll.setBackgroundResource(R.drawable.bg_button1)
            }
            adapter!!.setFileVoices(fileVoices)
            binding!!.recyclerViewFileVoice.setItemViewCacheSize(fileVoices.size)
        }
        model!!.isExecuteAddImage().observe(this) { execute: Boolean ->
            if (execute) {
                Handler(Looper.getMainLooper()).post(insertVideoToDB)
                playVideoDialog = PlayVideoDialog(
                    this@FileVoiceActivity,
                    DialogPlayVideoBinding.inflate(layoutInflater),
                    pathVideo
                )
                playVideoDialog!!.show()
                Toast.makeText(this, "Save: $pathVideo", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.video_creation_failed, Toast.LENGTH_SHORT).show()
            }
        }
        init()
        binding!!.btnBack3.setOnClickListener { onBackPressed() }
        binding!!.btnDeleteAll.setOnClickListener {
            adapter!!.release()
            val dialog = DeleteAllDialog(
                this,
                DialogDeleteAllBinding.inflate(layoutInflater),
                model!!,
                adapter!!.getFileVoices()
            )
            dialog.show()
        }
        binding!!.layoutNoItem.btnRecordNow.setOnClickListener { goToRecord() }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
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

    override fun onResume() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onResume")
        super.onResume()
        var isDeleted = false
        for (fileVoice in adapter!!.getFileVoices()) {
            if (!File(fileVoice!!.path).exists()) {
                model!!.delete(fileVoice)
                if (!isDeleted) {
                    isDeleted = true
                }
            }
        }
        if (isDeleted) {
            recreate()
        } else {
            adapter!!.resume()
        }
    }

    override fun onStop() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onStop")
        super.onStop()
        adapter!!.pause()
        if (playVideoDialog != null) {
            playVideoDialog!!.stop()
        }
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onDestroy")
        adapter!!.release()
        adapter = null
        model = null
        binding = null
        super.onDestroy()
    }

    private fun goToRecord() {
        val intent = Intent(this, RecordActivity::class.java)
        intent.action = FILE_TO_RECORD
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
        finish()
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        adapter = FileVoiceAdapter(this, this, model!!)
        binding!!.recyclerViewFileVoice.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.recyclerViewFileVoice.layoutManager = linearLayoutManager
        val dividerHorizontal = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding!!.recyclerViewFileVoice.addItemDecoration(dividerHorizontal)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OptionDialog.SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (!File(OptionDialog.fileVoice!!.path).exists()) {
                    Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    return
                }
                if (data == null) {
                    Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: data null")
                    Toast.makeText(this, R.string.process_error, Toast.LENGTH_SHORT).show()
                } else {
                    val pathImage = getRealPath(this, data.data)
                    if (pathImage!!.isEmpty()) {
                        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: pathImage isEmpty")
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    } else if (!File(pathImage).exists()) {
                        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: pathImage not exists")
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    } else {
                        val dialog = LoadingDialog(
                            this,
                            DialogLoading2Binding.inflate(
                                layoutInflater
                            )
                        )
                        dialog.show()
                        pathVideo =
                            getDCIMFolderPath(VoiceChangerApp.FOLDER_APP) + "/" + videoFileName
                        val cmdConvertImage = getCMDConvertImage(pathImage, getTempImagePath(this))
                        executeFFMPEG(cmdConvertImage, object : FFmpegExecuteCallback {
                            override fun onSuccess() {
                                val cmd = getCMDAddImage(
                                    OptionDialog.fileVoice!!.path,
                                    getTempImagePath(this@FileVoiceActivity),
                                    pathVideo!!
                                )
                                executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                                    override fun onSuccess() {
                                        dialog.cancel()
                                        model!!.setExecuteAddImage(true)
                                    }

                                    override fun onFailed() {
                                        dialog.cancel()
                                        model!!.setExecuteAddImage(false)
                                    }
                                })
                            }

                            override fun onFailed() {
                                dialog.cancel()
                            }
                        })
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onBackPressed")
        adapter!!.release()
        finish()
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
    }

    companion object {
        const val FILE_TO_RECORD = "FILE_TO_RECORD"
    }
}