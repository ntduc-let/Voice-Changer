package com.prox.voicechanger.ui.activity

import android.Manifest
import com.prox.voicechanger.utils.FFMPEGUtils.effects
import com.prox.voicechanger.utils.NumberUtils.formatAsTime
import com.prox.voicechanger.utils.FileUtils.Companion.getTempEffectFilePath
import com.prox.voicechanger.utils.PermissionUtils.checkPermission
import com.prox.voicechanger.utils.FileUtils.Companion.getName
import com.prox.voicechanger.utils.FileUtils.Companion.recordingFileName
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDConvertRecording
import com.prox.voicechanger.utils.FileUtils.Companion.getTempRecording2FilePath
import com.prox.voicechanger.utils.FFMPEGUtils.executeFFMPEG
import com.prox.voicechanger.utils.PermissionUtils.openDialogAccessAllFile
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDAddEffect
import com.prox.voicechanger.utils.FFMPEGUtils.getCMDCustomEffect
import com.prox.voicechanger.utils.FileUtils.Companion.getTempCustomFilePath
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import com.prox.voicechanger.media.Player
import com.prox.voicechanger.adapter.EffectAdapter
import com.prox.voicechanger.viewmodel.FileVoiceViewModel
import com.prox.voicechanger.VoiceChangerApp
import android.widget.RadioGroup
import com.prox.voicechanger.R
import android.widget.CompoundButton
import com.google.android.material.slider.Slider
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.widget.Toast
import com.prox.voicechanger.ui.dialog.NameDialog
import android.view.WindowManager
import com.prox.voicechanger.interfaces.EffectListener
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.prox.voicechanger.databinding.ActivityChangeVoiceBinding
import com.prox.voicechanger.databinding.DialogNameBinding
import com.prox.voicechanger.model.Effect
import com.prox.voicechanger.utils.PermissionUtils.REQUEST_PERMISSION
import space.siy.waveformview.WaveFormData
import space.siy.waveformview.WaveFormView
import java.io.File
import java.lang.Exception

@AndroidEntryPoint
class ChangeVoiceActivity : AppCompatActivity() {
    private var binding: ActivityChangeVoiceBinding? = null
    private var player: Player? = null
    private var isPlaying = false
    private var current = 0.0
    private var effectAdapter: EffectAdapter? = null
    private var hzSelect: String? = "500"
    private var nameFile: String? = null
    private var effectSelected: Effect? = effects[0]
    private var model: FileVoiceViewModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateTime: Runnable = object : Runnable {
        override fun run() {
            if (player == null) {
                Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: player null")
            } else {
                current = player!!.currentPosition.toDouble() / player!!.duration
                binding!!.layoutPlayer.visualizer.position = player!!.currentPosition.toLong()
                binding!!.layoutPlayer.txtCurrentTime.text =
                    formatAsTime(player!!.currentPosition.toLong())
                handler.post(this)
            }
        }
    }
    private val radioGroupOnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _: RadioGroup?, _: Int ->
            if (binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked
                && binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.value == 100f && binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.value == 0f
            ) {
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = false
            } else {
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable)
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = true
            }
        }
    private val radioButtonOnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b) {
                compoundButton.setTextColor(resources.getColor(R.color.black))
                hzSelect =
                    compoundButton.text.toString().substring(0, compoundButton.text.length - 2)
                selectCustom()
            } else {
                compoundButton.setTextColor(resources.getColor(R.color.black30))
            }
        }
    private val onSliderTouchListener: Slider.OnSliderTouchListener =
        object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                enableReset()
                selectCustom()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityChangeVoiceBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        model = ViewModelProvider(this).get(FileVoiceViewModel::class.java)
        model!!.getPathPlayer().observe(this) { path: String? ->
            if (path != null) {
                setNewPlayer(path)
            } else {
                binding!!.layoutLoading.txtProcessing.setText(R.string.process_error)
                binding!!.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.red))
                EffectAdapter.isExecuting = false
            }
        }
        model!!.isExecuteConvertRecording().observe(this) { execute: Boolean ->
            if (execute) {
                selectEffect(effectSelected)
            } else {
                binding!!.layoutLoading.txtProcessing.setText(R.string.process_error)
                binding!!.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.red))
                EffectAdapter.isExecuting = false
            }
        }
        model!!.isExecuteSave().observe(this) { execute: Boolean ->
            startActivity(Intent(this@ChangeVoiceActivity, FileVoiceActivity::class.java))
            overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2)
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: To FileVoiceActivity")
            if (execute) {
                Toast.makeText(this@ChangeVoiceActivity, R.string.save_success, Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@ChangeVoiceActivity, R.string.save_fail, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        model!!.getLoading().observe(this) { loading: Float? ->
            binding!!.layoutLoading.progressProcessing.progress = Math.round(
                loading!!
            )
        }
        model!!.isExecuteCustom().observe(this) { execute: Boolean ->
            if (execute) {
                setEnableCustom(false)
            } else {
                setEnableCustom(true)
            }
        }
        init()
        binding!!.btnBack2.setOnClickListener { onBackPressed() }
        binding!!.btnSave2.setOnClickListener {
            pausePlayer()
            val name = binding!!.layoutPlayer.txtName2.text.toString()
            val isCustom = binding!!.layoutEffect.btnEffect.isEnabled
            val dialog = NameDialog(
                this,
                DialogNameBinding.inflate(layoutInflater),
                model!!,
                name,
                isCustom,
                effectSelected
            )
            dialog.show()
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: Show NameDialog")
        }
        binding!!.layoutPlayer.visualizer.callback = object : WaveFormView.Callback {
            override fun onPlayPause() {}
            override fun onSeek(pos: Long) {
                player!!.seekTo(pos)
                binding!!.layoutPlayer.txtCurrentTime.text =
                    formatAsTime(player!!.currentPosition.toLong())
            }
        }
        binding!!.layoutPlayer.btnPauseOrResume.setOnClickListener {
            isPlaying = if (isPlaying) {
                pausePlayer()
                false
            } else {
                resumePlayer()
                true
            }
        }
        binding!!.layoutEffect.btnEffect.setOnClickListener {
            if (!binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled
                && !binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled
                && !binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled
            ) {
            } else {
                if (EffectAdapter.isExecuting) {
                    Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                EffectAdapter.isExecuting = true
                setNewPlayer(getTempEffectFilePath(this))
            }
            initClickBtnEffect()
            resetCustomEffect()
        }
        binding!!.layoutEffect.btnCustom.setOnClickListener { view: View? -> initClickBtnCustom() }
        actionCustomEffect()
    }

    override fun onStart() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: onStart")
        super.onStart()
        if (isPlaying) {
            resumePlayer()
        }
    }

    override fun onStop() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: onStop")
        super.onStop()
        if (isPlaying) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: onDestroy")
        if (player != null) {
            if (player!!.isPlaying) {
                stopPlayer()
            }
            player!!.release()
        }
        player = null
        effectAdapter = null
        hzSelect = null
        nameFile = null
        effectSelected = null
        isPlaying = false
        current = 0.0
        model = null
        binding = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: onBackPressed")
        if (player != null) {
            if (player!!.isPlaying) {
                stopPlayer()
            }
        }
        goToRecord()
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: init")
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_app)
        player = Player()
        isPlaying = true
        binding!!.layoutEffect.btnEffect.isEnabled = false
        binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled = false
        binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = false
        binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled = false
        effectAdapter =
            EffectAdapter(this, object : EffectListener {
                override fun addEffectListener(effect: Effect?) {
                    selectEffect(effect)
                }
            })
        val flexboxLayoutManager = FlexboxLayoutManager(this)
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
        flexboxLayoutManager.justifyContent = JustifyContent.SPACE_AROUND
        binding!!.layoutEffect.recyclerViewEffects.layoutManager = flexboxLayoutManager
        binding!!.layoutEffect.recyclerViewEffects.adapter = effectAdapter
        effectAdapter!!.setEffects(effects)
        if (checkPermission(this, this)) {
            actionIntent()
        }
    }

    private fun actionIntent() {
        val intent = intent
        if (intent == null) {
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: start intent null")
            goToRecord()
        } else if (intent.action == null) {
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: start action null")
            goToRecord()
        } else {
            val path = intent.getStringExtra(PATH_FILE)
            if (path == null) {
                Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: path null")
                goToRecord()
                return
            } else if (!File(path).exists()) {
                Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: file not exist")
                goToRecord()
                return
            }
            nameFile =
                if (intent.action == NameDialog.RECORD_TO_CHANGE_VOICE || intent.action == RecordActivity.IMPORT_TEXT_TO_SPEECH) {
                    getName(recordingFileName)
                } else {
                    getName(path)
                }
            EffectAdapter.isExecuting = true
            model!!.setExecuteCustom(true)
            binding!!.layoutPlayer.root.visibility = View.INVISIBLE
            binding!!.layoutLoading.root.visibility = View.VISIBLE
            binding!!.layoutLoading.txtProcessing.setText(R.string.processing)
            binding!!.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.white30))
            binding!!.btnSave2.isEnabled = false
            binding!!.btnSave2.setTextColor(resources.getColor(R.color.white30))
            binding!!.btnSave2.setBackgroundResource(R.drawable.bg_button6)
            val cmd = getCMDConvertRecording(path, getTempRecording2FilePath(this))
            executeFFMPEG(cmd, object : FFmpegExecuteCallback {
                override fun onSuccess() {
                    model!!.setExecuteConvertRecording(true)
                }

                override fun onFailed() {
                    model!!.setExecuteConvertRecording(false)
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                actionIntent()
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
                actionIntent()
            } else {
                openDialogAccessAllFile(this)
            }
        }
    }

    private fun initClickBtnCustom() {
        binding!!.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_disable)
        binding!!.layoutEffect.btnEffect.isEnabled = true
        binding!!.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_enable)
        binding!!.layoutEffect.btnCustom.isEnabled = false
        binding!!.layoutEffect.recyclerViewEffects.visibility = View.GONE
        binding!!.layoutEffect.layoutCustom.root.visibility = View.VISIBLE
        binding!!.layoutPlayer.txtName2.text =
            binding!!.layoutPlayer.txtName2.text.toString() + "-Custom"
    }

    private fun initClickBtnEffect() {
        binding!!.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_enable)
        binding!!.layoutEffect.btnEffect.isEnabled = false
        binding!!.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_disable)
        binding!!.layoutEffect.btnCustom.isEnabled = true
        binding!!.layoutEffect.recyclerViewEffects.visibility = View.VISIBLE
        binding!!.layoutEffect.layoutCustom.root.visibility = View.GONE
        binding!!.layoutPlayer.txtName2.text = nameFile + "-" + effectSelected!!.title
    }

    private fun selectEffect(effect: Effect?) {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: selectEffect " + effect!!.title)
        model!!.setExecuteCustom(true)
        effectSelected = effect
        stopPlayer()
        binding!!.layoutPlayer.root.visibility = View.INVISIBLE
        binding!!.layoutLoading.root.visibility = View.VISIBLE
        binding!!.layoutLoading.txtProcessing.setText(R.string.processing)
        binding!!.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.white30))
        binding!!.btnSave2.isEnabled = false
        binding!!.btnSave2.setTextColor(resources.getColor(R.color.white30))
        binding!!.btnSave2.setBackgroundResource(R.drawable.bg_button6)
        binding!!.layoutPlayer.txtName2.text = nameFile + "-" + effect.title
        val cmd =
            getCMDAddEffect(getTempRecording2FilePath(this), getTempEffectFilePath(this), effect)
        executeFFMPEG(cmd, object : FFmpegExecuteCallback {
            override fun onSuccess() {
                model!!.setPathPlayer(getTempEffectFilePath(this@ChangeVoiceActivity))
            }

            override fun onFailed() {
                model!!.setPathPlayer(null)
            }
        })
    }

    private fun selectCustom() {
        if (EffectAdapter.isExecuting) {
            return
        }
        EffectAdapter.isExecuting = true
        model!!.setExecuteCustom(true)
        stopPlayer()
        binding!!.layoutPlayer.root.visibility = View.INVISIBLE
        binding!!.layoutLoading.root.visibility = View.VISIBLE
        binding!!.layoutLoading.txtProcessing.setText(R.string.processing)
        binding!!.layoutLoading.txtProcessing.setTextColor(resources.getColor(R.color.white30))
        if (hzSelect == null) {
            hzSelect = "500"
        }
        val hzNumber: Double = try {
            hzSelect!!.toDouble()
        } catch (e: Exception) {
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: hzNumber " + e.message)
            500.0
        }
        val cmd = getCMDCustomEffect(
            getTempEffectFilePath(this),
            getTempCustomFilePath(this), (
                    binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.value / 16000).toDouble(),
            binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.value.toDouble(),
            binding!!.layoutEffect.layoutCustom.layoutBasic.seekPanning.value.toDouble(),
            hzNumber,
            binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.value.toDouble(),
            binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.value.toDouble(),
            binding!!.layoutEffect.layoutCustom.layoutReverb.seekInGain.value.toDouble(),
            binding!!.layoutEffect.layoutCustom.layoutReverb.seekOutGain.value.toDouble(),
            (if (binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.value == 0f) 1 else binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.value.toDouble()) as Double,
            (if (binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.value == 0f) 0.01 else binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.value) as Double
        )
        executeFFMPEG(cmd, object : FFmpegExecuteCallback {
            override fun onSuccess() {
                model!!.setPathPlayer(getTempCustomFilePath(this@ChangeVoiceActivity))
            }

            override fun onFailed() {
                model!!.setPathPlayer(null)
            }
        })
    }

    private fun setNewPlayer(path: String) {
        Log.d(VoiceChangerApp.TAG, "setNewPlayer: $path")
        WaveFormData.Factory(path).build(object : WaveFormData.Factory.Callback {
            override fun onComplete(waveFormData: WaveFormData) {
                if (binding != null) {
                    binding!!.layoutPlayer.visualizer.data = waveFormData
                    binding!!.layoutPlayer.root.visibility = View.VISIBLE
                    binding!!.layoutLoading.root.visibility = View.GONE
                    binding!!.btnSave2.isEnabled = true
                    binding!!.btnSave2.setTextColor(resources.getColor(R.color.white))
                    binding!!.btnSave2.setBackgroundResource(R.drawable.bg_button1)
                    if (player == null) {
                        player = Player()
                    }
                    player!!.setNewPath(path)
                    startPlayer()
                    player!!.seekTo((current * player!!.duration).toLong())
                    binding!!.layoutPlayer.visualizer.position = player!!.currentPosition.toLong()
                    binding!!.layoutPlayer.txtCurrentTime.text =
                        formatAsTime(player!!.currentPosition.toLong())
                    if (!isPlaying) {
                        pausePlayer()
                    }
                    if (model != null) {
                        model!!.setLoading(0f)
                        model!!.setExecuteCustom(false)
                    }
                    EffectAdapter.isExecuting = false
                }
            }

            override fun onProgress(v: Float) {
                if (model != null) {
                    model!!.setLoading(v * 10)
                }
            }
        })
    }

    private fun startPlayer() {
        player!!.start()
        binding!!.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause)
        updateTime()
    }

    private fun stopPlayer() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: stopPlayer")
        player!!.stop()
        binding!!.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume)
        handler.removeCallbacks(updateTime)
    }

    private fun pausePlayer() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: pausePlayer")
        player!!.pause()
        binding!!.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume)
        handler.removeCallbacks(updateTime)
    }

    private fun resumePlayer() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: resumePlayer")
        player!!.resume()
        binding!!.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause)
        updateTime()
    }

    private fun updateTime() {
        Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: updateTime")
        if (player == null) {
            Log.d(VoiceChangerApp.TAG, "ChangeVoiceActivity: player null")
        } else {
            binding!!.layoutPlayer.txtTotalTime.text = formatAsTime(player!!.duration.toLong())
            handler.post(updateTime)
        }
    }

    private fun actionCustomEffect() {
        actionCustomBasic()
        actionCustomEqualizer()
        actionCustomReverb()
    }

    private fun actionCustomBasic() {
        binding!!.layoutEffect.layoutCustom.switchBasic.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                binding!!.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_enable)
                binding!!.layoutEffect.layoutCustom.layoutBasic.root.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.btnResetBasic.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.switchBasic.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding!!.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_disable)
                binding!!.layoutEffect.layoutCustom.layoutBasic.root.visibility = View.GONE
                binding!!.layoutEffect.layoutCustom.btnResetBasic.visibility = View.INVISIBLE
                binding!!.layoutEffect.layoutCustom.switchBasic.setThumbResource(R.drawable.ic_thumb)
                if (binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled) {
                    binding!!.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
                    binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled = false
                    resetCustomBasic()
                    selectCustom()
                }
            }
        }
        binding!!.layoutEffect.layoutCustom.btnResetBasic.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resetCustomBasic()
            binding!!.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled = false
            selectCustom()
        }
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekPanning.addOnSliderTouchListener(
            onSliderTouchListener
        )
    }

    private fun actionCustomEqualizer() {
        binding!!.layoutEffect.layoutCustom.switchEqualizer.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                binding!!.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_enable)
                binding!!.layoutEffect.layoutCustom.layoutEqualizer.root.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.switchEqualizer.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding!!.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_disable)
                binding!!.layoutEffect.layoutCustom.layoutEqualizer.root.visibility = View.GONE
                binding!!.layoutEffect.layoutCustom.btnResetEqualizer.visibility = View.INVISIBLE
                binding!!.layoutEffect.layoutCustom.switchEqualizer.setThumbResource(R.drawable.ic_thumb)
                if (binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled) {
                    binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
                    binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = false
                    resetCustomEqualizer()
                    selectCustom()
                }
            }
        }
        binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resetCustomEqualizer()
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = false
            selectCustom()
        }
        checkRadio()
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.addOnSliderTouchListener(
            onSliderTouchListener
        )
    }

    private fun actionCustomReverb() {
        binding!!.layoutEffect.layoutCustom.switchReverb.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                binding!!.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_enable)
                binding!!.layoutEffect.layoutCustom.layoutReverb.root.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.btnResetReverb.visibility = View.VISIBLE
                binding!!.layoutEffect.layoutCustom.switchReverb.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding!!.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_disable)
                binding!!.layoutEffect.layoutCustom.layoutReverb.root.visibility = View.GONE
                binding!!.layoutEffect.layoutCustom.btnResetReverb.visibility = View.INVISIBLE
                binding!!.layoutEffect.layoutCustom.switchReverb.setThumbResource(R.drawable.ic_thumb)
                if (binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled) {
                    binding!!.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
                    binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled = false
                    resetCustomReverb()
                    selectCustom()
                }
            }
        }
        binding!!.layoutEffect.layoutCustom.btnResetReverb.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resetCustomReverb()
            binding!!.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled = false
            selectCustom()
        }
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekInGain.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekOutGain.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.addOnSliderTouchListener(
            onSliderTouchListener
        )
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.addOnSliderTouchListener(
            onSliderTouchListener
        )
    }

    private fun checkRadio() {
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radGroupHz.setOnCheckedChangeListener(
            radioGroupOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio1000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio2000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio3000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio4000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio5000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio6000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio7000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio8000.setOnCheckedChangeListener(
            radioButtonOnCheckedChangeListener
        )
    }

    private fun resetCustomEffect() {
        binding!!.layoutEffect.layoutCustom.switchBasic.isChecked = false
        binding!!.layoutEffect.layoutCustom.switchEqualizer.isChecked = false
        binding!!.layoutEffect.layoutCustom.switchReverb.isChecked = false
        binding!!.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_disable)
        binding!!.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_disable)
        binding!!.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_disable)
        binding!!.layoutEffect.layoutCustom.layoutBasic.root.visibility = View.GONE
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.root.visibility = View.GONE
        binding!!.layoutEffect.layoutCustom.layoutReverb.root.visibility = View.GONE
        resetCustomBasic()
        resetCustomEqualizer()
        resetCustomReverb()
    }

    private fun resetCustomBasic() {
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.value = 16000f
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.value = 1f
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekPanning.value = 1f
    }

    private fun resetCustomEqualizer() {
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked = true
        hzSelect = "500"
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.value = 100f
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.value = 0f
    }

    private fun resetCustomReverb() {
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekInGain.value = 1f
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekOutGain.value = 1f
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.value = 0f
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.value = 1f
    }

    private fun enableReset() {
        if (binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.value == 16000f && binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.value == 1f && binding!!.layoutEffect.layoutCustom.layoutBasic.seekPanning.value == 1f) {
            binding!!.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled = false
        } else {
            binding!!.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_enable)
            binding!!.layoutEffect.layoutCustom.btnResetBasic.isEnabled = true
        }
        if (binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked
            && binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.value == 100f && binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.value == 0f
        ) {
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = false
        } else {
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable)
            binding!!.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled = true
        }
        if (binding!!.layoutEffect.layoutCustom.layoutReverb.seekInGain.value == 1f && binding!!.layoutEffect.layoutCustom.layoutReverb.seekOutGain.value == 1f && binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.value == 0f && binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.value == 1f) {
            binding!!.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
            binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled = false
        } else {
            binding!!.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_enable)
            binding!!.layoutEffect.layoutCustom.btnResetReverb.isEnabled = true
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

    private fun setEnableCustom(isEnable: Boolean) {
        binding!!.layoutEffect.layoutCustom.switchBasic.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.switchEqualizer.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.switchReverb.isEnabled = isEnable
        //        binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(isEnable);
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutBasic.seekPanning.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.seekGain.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekInGain.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekOutGain.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDelay.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutReverb.seekDecay.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radGroupHz.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio500.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio1000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio2000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio3000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio4000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio5000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio6000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio7000.isEnabled = isEnable
        binding!!.layoutEffect.layoutCustom.layoutEqualizer.radio8000.isEnabled = isEnable
    }

    companion object {
        const val PATH_FILE = "PATH_FILE"
        const val CHANGE_TO_RECORD = "CHANGE_TO_RECORD"
    }
}