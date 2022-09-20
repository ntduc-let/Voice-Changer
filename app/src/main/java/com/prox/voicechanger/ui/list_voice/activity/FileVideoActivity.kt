package com.prox.voicechanger.ui.list_voice.activity

import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import com.prox.voicechanger.ui.list_voice.adapter.FileVideoAdapter
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prox.voicechanger.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.databinding.ActivityFileVideoBinding
import com.prox.voicechanger.databinding.DialogRateBinding
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import com.proxglobal.purchase.ProxPurchase
import com.prox.voicechanger.utils.NetworkUtils
import com.prox.voicechanger.ui.dialog.RateDialog
import com.prox.voicechanger.ui.list_voice.dialog.*
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.utils.PermissionUtils
import com.proxglobal.proxads.adsv2.ads.ProxAds
import java.io.File
import java.util.ArrayList

@AndroidEntryPoint
class FileVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileVideoBinding
    private lateinit var adapter: FileVideoAdapter
    private lateinit var model: FileVoiceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = ViewModelProvider(this)[FileVoiceViewModel::class.java]

        model.fileVideos.observe(this) {
            if (it == null) {
                adapter.setFileVideos(listOf())
                return@observe
            }
            if (it.isEmpty()) {
                binding.layoutNoItem.visibility = View.VISIBLE
                binding.btnDeleteAll.isEnabled = false
                binding.btnDeleteAll.visibility = View.INVISIBLE
            } else {
                binding.layoutNoItem.visibility = View.GONE
                binding.btnDeleteAll.isEnabled = true
                binding.btnDeleteAll.visibility = View.VISIBLE
            }
            adapter.setFileVideos(it)
        }

        init()

        binding.btnBack3.setOnClickListener { onBackPressed() }
        binding.btnDeleteAll.setOnClickListener {
            val dialog = DeleteAllDialog()
            dialog.setOnDeleteAllListener {
                for (fileVideo in adapter.getFileVideos()) {
                    FileUtils.deleteFile(this, fileVideo.path)
                    model.delete(fileVideo)
                }
            }
            dialog.show(supportFragmentManager, "DeleteAllDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        for (fileVoice in adapter.getFileVideos()) {
            if (!File(fileVoice.path).exists()) {
                model.delete(fileVoice)
            }
        }
    }

    override fun onDestroy() {
        System.gc()
        super.onDestroy()
    }

    private fun init() {
        adapter = FileVideoAdapter(this)
        adapter.setOnOptionListener {
            val dialog = OptionVideoDialog(it)
            dialog.setOnShareListener {
                FileUtils.shareFile(this, it.path)
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
        adapter.setOnPlayListener {
            val playVideoDialog = PlayVideoDialog(it.path)
            playVideoDialog.show(supportFragmentManager, "PlayVideoDialog")
        }
        binding.recyclerViewFileVideo.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewFileVideo.layoutManager = linearLayoutManager
        val dividerHorizontal = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerViewFileVideo.addItemDecoration(dividerHorizontal)
        ProxAds.instance.showMediumNativeWithShimmerStyle19(
            this,
            BuildConfig.native_file,
            binding.adContainer,
            object : AdsCallback() {})
        if (ProxPurchase.getInstance().checkPurchased()
            || !NetworkUtils.isNetworkAvailable(this)
        ) {
            binding.adContainer.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (RateDialog.isRated(this)) {
            finish()
            overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
        } else {
            val dialog = RateDialog(
                this, DialogRateBinding.inflate(
                    layoutInflater
                )
            ) {
        finish()
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
            }
            dialog.show()
        }
    }
}