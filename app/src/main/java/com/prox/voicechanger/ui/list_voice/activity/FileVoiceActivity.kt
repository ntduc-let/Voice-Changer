package com.prox.voicechanger.ui.list_voice.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.fileutils.getRealPath
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.VoiceChangerApp.Companion.FOLDER_APP
import com.prox.voicechanger.databinding.*
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.ui.dialog.RateDialog
import com.prox.voicechanger.ui.home.activity.RecordActivity
import com.prox.voicechanger.ui.list_voice.dialog.RenameDialog
import com.prox.voicechanger.ui.list_voice.adapter.FileVoiceAdapter
import com.prox.voicechanger.ui.list_voice.dialog.DeleteAllDialog
import com.prox.voicechanger.ui.list_voice.dialog.LoadingDialog
import com.prox.voicechanger.ui.list_voice.dialog.OptionDialog
import com.prox.voicechanger.ui.list_voice.dialog.PlayVideoDialog
import com.prox.voicechanger.utils.ConvertersUtils
import com.prox.voicechanger.utils.FFMPEGUtils
import com.prox.voicechanger.utils.FFMPEGUtils.executeFFMPEG
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDAddImage
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.utils.NetworkUtils
import com.prox.voicechanger.utils.PermissionUtils.requestWriteSetting
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.proxglobal.proxads.adsv2.ads.ProxAds
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import com.proxglobal.purchase.ProxPurchase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class FileVoiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileVoiceBinding
    private lateinit var adapter: FileVoiceAdapter
    private lateinit var model: FileVoiceViewModel
    private val myScop = CoroutineScope(Job() + Dispatchers.Main)

    private var fileVoiceSelected: FileVoice? = null
    private var pathVideo: String = ""
    private var playVideoDialog: PlayVideoDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = ViewModelProvider(this)[FileVoiceViewModel::class.java]

        model.fileVoices.observe(this) {
            if (it == null) {
                adapter.setFileVoices(listOf())
                binding.recyclerViewFileVoice.setItemViewCacheSize(0)
                return@observe
            }
            if (it.isEmpty()) {
                binding.layoutNoItem.root.visibility = View.VISIBLE
                binding.btnDeleteAll.isEnabled = false
                binding.btnDeleteAll.visibility = View.INVISIBLE
            } else {
                binding.layoutNoItem.root.visibility = View.GONE
                binding.btnDeleteAll.isEnabled = true
                binding.btnDeleteAll.visibility = View.VISIBLE
            }
            adapter.setFileVoices(it)
            binding.recyclerViewFileVoice.setItemViewCacheSize(it.size)
        }

        init()

        binding.btnBack3.setOnClickListener { onBackPressed() }
        binding.btnDeleteAll.setOnClickListener {
            adapter.release()
            val dialog = DeleteAllDialog()
            dialog.setOnDeleteAllListener {
                for (fileVoice in adapter.getFileVoices()) {
                    FileUtils.deleteFile(this, fileVoice.path)
                    model.delete(fileVoice)
                }
            }
            dialog.show(supportFragmentManager, "DeleteAllDialog")
        }
        binding.layoutNoItem.btnRecordNow.setOnClickListener { goToRecord() }
    }

    override fun onResume() {
        super.onResume()
        var isDeleted = false
        for (fileVoice in adapter.getFileVoices()) {
            if (!File(fileVoice.path).exists()) {
                model.delete(fileVoice)
                if (!isDeleted) {
                    isDeleted = true
                }
            }
        }
        if (isDeleted) {
            recreate()
        } else {
            adapter.resume()
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.pause()
        if (playVideoDialog != null) {
            playVideoDialog!!.stop()
        }
    }

    override fun onDestroy() {
        myScop.cancel()
        adapter.release()
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
        adapter = FileVoiceAdapter(this)
        adapter.setOnOptionListener {
            val dialog = OptionDialog(it)
            dialog.setOnShareListener {
                FileUtils.shareFile(this, it.path)
            }
            dialog.setOnAddImgListener {
                fileVoiceSelected = it
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                addImgLauncher.launch(intent)
            }
            dialog.setOnRingPhoneListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        if (FileUtils.setAsRingtoneOrNotification(
                                this,
                                it.path,
                                RingtoneManager.TYPE_RINGTONE
                            )
                        ) {
                            shortToast(R.string.setting_success)
                        }
                    } else {
                        shortToast(R.string.request_write_setting)
                        requestWriteSetting(this)
                    }
                } else {
                    if (FileUtils.setAsRingtoneOrNotification(
                            this,
                            it.path,
                            RingtoneManager.TYPE_RINGTONE
                        )
                    ) {
                        shortToast(R.string.setting_success)
                    }
                }
            }
            dialog.setOnRingNotiListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        if (FileUtils.setAsRingtoneOrNotification(
                                this,
                                it.path,
                                RingtoneManager.TYPE_NOTIFICATION
                            )
                        ) {
                            shortToast(R.string.setting_success)
                        }
                    } else {
                        shortToast(R.string.request_write_setting)
                        requestWriteSetting(this)
                    }
                } else {
                    if (FileUtils.setAsRingtoneOrNotification(
                            this,
                            it.path,
                            RingtoneManager.TYPE_NOTIFICATION
                        )
                    ) {
                        shortToast(R.string.setting_success)
                    }
                }
            }
            dialog.setOnRenameListener {
                val dialogRename = RenameDialog(it)
                dialogRename.setOnSaveListener { newPath ->
                    it.path = newPath
                    it.name = FileUtils.getName(newPath)
                    model.update(it)
                }
                dialogRename.show(supportFragmentManager, "RenameDialog")
            }
            dialog.setOnDeleteListener {
                FileUtils.deleteFile(this, it.path)
                model.delete(it)
            }
            dialog.show(supportFragmentManager, "OptionDialog")
        }
        adapter.setOnNotExistsListener {
            model.delete(it)
        }
        binding.recyclerViewFileVoice.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewFileVoice.layoutManager = linearLayoutManager
        val dividerHorizontal = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerViewFileVoice.addItemDecoration(dividerHorizontal)
        ProxAds.instance.showMediumNativeWithShimmerStyle19(
            this,
            BuildConfig.native_file,
            binding.adContainer,
            object : AdsCallback() {
                override fun onClosed() {
                    super.onClosed()
                    Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: Ads onClosed")
                }

                override fun onError() {
                    super.onError()
                    Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: Ads onError")
                }
            })
        if (ProxPurchase.getInstance().checkPurchased()
            || !NetworkUtils.isNetworkAvailable(this)
        ) {
            binding.adContainer.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (RateDialog.isRated(this)) {
            adapter.release()
            finish()
            overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
        } else {
            val dialog = RateDialog(
                this, DialogRateBinding.inflate(
                    layoutInflater
                )
            ) {
                adapter.release()
                finish()
                overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
            }
            dialog.show()
        }
    }

    companion object {
        const val FILE_TO_RECORD = "FILE_TO_RECORD"
    }

    private val addImgLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (fileVoiceSelected == null) {
                    return@registerForActivityResult
                }

                if (!File(fileVoiceSelected!!.path).exists()) {
                    shortToast(R.string.file_not_exist)
                    return@registerForActivityResult
                }
                if (it.data != null) {
                    val pathImage = it.data!!.data?.getRealPath(this)
                    if (pathImage == null || pathImage.isEmpty() || !File(pathImage).exists()) {
                        shortToast(R.string.file_not_exist)
                        return@registerForActivityResult
                    }
                    val dialog = LoadingDialog()
                    dialog.show(supportFragmentManager, "LoadingDialog")

                    pathVideo =
                        FileUtils.getDCIMFolderPath(FOLDER_APP) + "/" + FileUtils.videoFileName
                    val cmdConvertImage =
                        FFMPEGUtils.getCMDConvertImage(pathImage, FileUtils.getTempImagePath(this))
                    executeFFMPEG(cmdConvertImage, object : FFmpegExecuteCallback {
                        override fun onSuccess() {
                            val cmd = getCMDAddImage(
                                fileVoiceSelected!!.path,
                                FileUtils.getTempImagePath(this@FileVoiceActivity),
                                pathVideo
                            )
                            executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                                override fun onSuccess() {
                                    dialog.dismiss()
                                    myScop.launch(Dispatchers.Main) {
                                        executeAddImage(true)
                                    }
                                }

                                override fun onFailed() {
                                    dialog.dismiss()
                                    myScop.launch(Dispatchers.Main) {
                                        executeAddImage(false)
                                    }
                                }
                            })
                        }

                        override fun onFailed() {
                            dialog.dismiss()
                        }
                    })
                }
            } else if (it.resultCode == RESULT_CANCELED) {
                shortToast(R.string.canceled)
            }
        }

    private fun executeAddImage(success: Boolean) {
        if (success) {
            myScop.launch(Dispatchers.IO) {
                insertVideoToDB()
            }
            playVideoDialog = PlayVideoDialog(pathVideo)
            playVideoDialog!!.show(supportFragmentManager, "PlayVideoDialog")
            shortToast("Save: $pathVideo")
        } else {
            shortToast(R.string.video_creation_failed)
        }
    }

    private fun insertVideoToDB() {
        val fileVideo = FileVoice()
        fileVideo.src = fileVoiceSelected!!.src
        fileVideo.name = FileUtils.getName(pathVideo)
        fileVideo.path = pathVideo
        val bitmap = BitmapFactory.decodeFile(FileUtils.getTempImagePath(this))
        fileVideo.image = ConvertersUtils.fromBitmap(bitmap)
        val playerVideo = MediaPlayer()
        try {
            playerVideo.setDataSource(pathVideo)
            playerVideo.prepare()
        } catch (e: IOException) {
            Log.d(VoiceChangerApp.TAG, "insertVideoToDB: " + e.message)
            return
        }
        fileVideo.duration = playerVideo.duration.toLong()
        fileVideo.size = File(pathVideo).length()
        fileVideo.date = Date().time
        model.insertVideoBG(fileVideo)
    }
}