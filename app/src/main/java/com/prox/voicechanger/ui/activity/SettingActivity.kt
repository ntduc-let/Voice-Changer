package com.prox.voicechanger.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.ActivitySettingBinding
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.repository.FileVoiceRepository
import com.prox.voicechanger.ui.dialog.NameDialog
import com.prox.voicechanger.ui.dialog.OptionDialog.Companion.fileVoice
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)
    private var model: FileVoiceViewModel? = null

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var riversRef: StorageReference? = null

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)

        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.reference

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Choose authentication providers
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        binding.btnLogin.setOnClickListener {
            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }

        binding.btnLogout.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    updateUI(null)
                }
        }

        binding.btnClose.setOnClickListener { finish() }

        binding.btnStorage.setOnClickListener {
            startActivity(Intent(this, FileStorageActivity::class.java))
            overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
        }

        if (auth.currentUser != null){
            initStorage()
        }
    }

    private fun initStorage() {
        activityScope.launch {
            withContext(Dispatchers.IO) {
                FileVoiceRepository.listStorage = ArrayList()

                val docRef = db.collection(auth.currentUser!!.uid)
                docRef.get().addOnSuccessListener {
                    for (doc in it.documents) {
                        val fileStorage = FileVoice()
                        fileStorage.src = (doc.data!!["src"] as Long).toInt()
                        fileStorage.name = doc.data!!["name"] as String?
                        fileStorage.path = doc.data!!["path"] as String?
                        fileStorage.duration = doc.data!!["duration"] as Long
                        fileStorage.size = doc.data!!["size"] as Long
                        fileStorage.date = doc.data!!["date"] as Long

                        val check = model?.check(fileStorage.path)
                        if (check == null){
                            FileVoiceRepository.listStorage.add(fileStorage)
                        }
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            binding.imgAva.setImageResource(R.drawable.img_ava)
            binding.txtAccount.text = "Trạng thái: Chưa đăng nhập"
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.btnStorage.visibility = View.GONE
        } else {
            val url = currentUser.photoUrl.toString().replace("s96-c", "s400-c", true)
            Glide.with(this).load(url)
                .placeholder(R.drawable.img_ava)
                .error(R.drawable.img_ava)
                .into(binding.imgAva)
            binding.txtAccount.text = "Email: ${currentUser.email}"
            binding.btnLogin.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnStorage.visibility = View.VISIBLE

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

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val user = auth.currentUser
            updateUI(user)
        }
    }
}