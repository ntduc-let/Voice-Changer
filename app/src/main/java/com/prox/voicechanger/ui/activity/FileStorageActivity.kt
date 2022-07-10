package com.prox.voicechanger.ui.activity

import android.net.Uri
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.VoiceChangerApp
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.prox.voicechanger.R
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide.init
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.prox.voicechanger.adapter.FileStorageAdapter
import com.prox.voicechanger.databinding.*
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.repository.FileVoiceRepository
import com.prox.voicechanger.ui.dialog.OptionDialog.Companion.fileVoice
import kotlinx.coroutines.*
import java.io.File

@AndroidEntryPoint
class FileStorageActivity : AppCompatActivity() {
    private var binding: ActivityFileStorageBinding? = null
    private var adapter: FileStorageAdapter? = null
    private var model: FileVoiceViewModel? = null

    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var riversRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityFileStorageBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)

        init()

        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.reference

        auth = FirebaseAuth.getInstance()

        binding!!.btnBack4.setOnClickListener { onBackPressed() }
        adapter!!.setClickDownloadListener {
            activityScope.launch {
                withContext(Dispatchers.IO) {
                    model!!.insertBG(it)
                    val islandRef = storageRef!!.child("${auth.currentUser!!.uid}/${it.name}")
                    val localFile = File(it.path)

                    islandRef.getFile(localFile).addOnSuccessListener { _ ->
                        FileVoiceRepository.listStorage.remove(it)
                        adapter!!.notifyDataSetChanged()
                        Toast.makeText(this@FileStorageActivity, "Successful download", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{
                        Toast.makeText(this@FileStorageActivity, "Failed download", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        adapter!!.setClickDeleteListener {
            model!!.delete(it)

            activityScope.launch {
                withContext(Dispatchers.IO) {
                    db.collection(auth.currentUser!!.uid).document(it.name).delete().addOnSuccessListener { _ ->
                        Log.d("aaaaaaaaaaaaa", "addOnSuccessListener")
                    }.addOnFailureListener {
                        Log.d("aaaaaaaaaaaaa", "addOnFailureListener")
                    }

                    val islandRef = storageRef!!.child("${auth.currentUser!!.uid}/${it.name}")
                    islandRef.delete().addOnSuccessListener { _ ->
                        FileVoiceRepository.listStorage.remove(it)
                        adapter!!.notifyDataSetChanged()
                        Toast.makeText(this@FileStorageActivity, "Successful delete", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@FileStorageActivity, "Failed delete", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onDestroy")
        adapter = null
        model = null
        binding = null
        super.onDestroy()
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        adapter = FileStorageAdapter(this, FileVoiceRepository.listStorage)
        binding!!.recyclerViewFileStorage.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.recyclerViewFileStorage.layoutManager = linearLayoutManager
        val dividerHorizontal = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding!!.recyclerViewFileStorage.addItemDecoration(dividerHorizontal)
    }

    override fun onBackPressed() {
        Log.d(VoiceChangerApp.TAG, "FileVoiceActivity: onBackPressed")
        finish()
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2)
    }
}