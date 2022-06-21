package com.prox.voicechanger.ui.activity

import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import com.prox.voicechanger.adapter.FileVideoAdapter
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.R
import com.prox.voicechanger.ui.dialog.DeleteAllDialog
import com.prox.voicechanger.VoiceChangerApp
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.prox.voicechanger.databinding.ActivityFileVideoBinding
import com.prox.voicechanger.databinding.DialogDeleteAllBinding
import java.io.File
import java.util.ArrayList

@AndroidEntryPoint
class FileVideoActivity : AppCompatActivity() {
    private var binding: ActivityFileVideoBinding? = null
    private var adapter: FileVideoAdapter? = null
    private var model: FileVoiceViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileVideoBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)
        model!!.getFileVideos().observe(this) { fileVideos: List<FileVoice?>? ->
            if (fileVideos == null) {
                adapter!!.setFileVideos(ArrayList())
                return@observe
            }
            if (fileVideos.isEmpty()) {
                binding!!.layoutNoItem.visibility = View.VISIBLE
                binding!!.btnDeleteAll.isEnabled = false
                binding!!.btnDeleteAll.setTextColor(resources.getColor(R.color.white30))
                binding!!.btnDeleteAll.setBackgroundResource(R.drawable.bg_button6)
            } else {
                binding!!.layoutNoItem.visibility = View.GONE
                binding!!.btnDeleteAll.isEnabled = true
                binding!!.btnDeleteAll.setTextColor(resources.getColor(R.color.white))
                binding!!.btnDeleteAll.setBackgroundResource(R.drawable.bg_button1)
            }
            adapter!!.setFileVideos(fileVideos)
        }
        init()
        binding!!.btnBack3.setOnClickListener { onBackPressed() }
        binding!!.btnDeleteAll.setOnClickListener {
            val dialog = DeleteAllDialog(
                this,
                DialogDeleteAllBinding.inflate(layoutInflater),
                model!!,
                adapter!!.getFileVideos()
            )
            dialog.show()
        }
    }

    override fun onResume() {
        Log.d(VoiceChangerApp.TAG, "FileVideoActivity: onResume")
        super.onResume()
        for (fileVoice in adapter!!.getFileVideos()) {
            if (!File(fileVoice!!.path).exists()) {
                model!!.delete(fileVoice)
            }
        }
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "FileVideoActivity: onDestroy")
        adapter = null
        model = null
        binding = null
        super.onDestroy()
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "FileVideoActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        adapter = FileVideoAdapter(this, this, model!!)
        binding!!.recyclerViewFileVideo.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.recyclerViewFileVideo.layoutManager = linearLayoutManager
        val dividerHorizontal = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding!!.recyclerViewFileVideo.addItemDecoration(dividerHorizontal)
    }

    override fun onBackPressed() {
        Log.d(VoiceChangerApp.TAG, "FileVideoActivity: onBackPressed")
        finish()
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
    }
}