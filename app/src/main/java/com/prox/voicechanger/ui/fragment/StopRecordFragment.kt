package com.prox.voicechanger.ui.fragment

import androidx.navigation.Navigation.findNavController
import com.prox.voicechanger.utils.NumberUtils.formatAsTime
import com.prox.voicechanger.utils.FileUtils.Companion.getTempRecordingFilePath
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.R
import com.prox.voicechanger.ui.dialog.MoreOptionDialog
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.prox.voicechanger.ui.activity.ChangeVoiceActivity
import com.prox.voicechanger.ui.dialog.NameDialog
import com.prox.voicechanger.databinding.DialogMoreOptionBinding
import com.prox.voicechanger.databinding.FragmentStopRecordBinding
import com.prox.voicechanger.media.Recorder

class StopRecordFragment : Fragment() {
    private var binding: FragmentStopRecordBinding? = null
    private var recorder: Recorder? = null
    private var isStop = false
    private val handler = Handler(Looper.getMainLooper())
    private var runnableAnimation: Runnable? = null
    private val runnableTime: Runnable = object : Runnable {
        override fun run() {
            binding!!.timelineTextView.text = formatAsTime(recorder!!.currentTime)
            binding!!.visualizer.addAmp(recorder!!.maxAmplitude, recorder!!.tickDuration)
            handler.post(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: onCreateView")
        binding = FragmentStopRecordBinding.inflate(inflater, container, false)
        init()
        recording()
        binding!!.btnBack.setOnClickListener { popBackStack() }
        binding!!.btnStop.setOnClickListener {
            stopRecord()
            val intent = Intent(requireActivity(), ChangeVoiceActivity::class.java)
            intent.action = NameDialog.RECORD_TO_CHANGE_VOICE
            intent.putExtra(
                ChangeVoiceActivity.PATH_FILE,
                getTempRecordingFilePath(requireContext())
            )
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.anim_right_left_1,
                R.anim.anim_right_left_2
            )
        }
        binding!!.btnMoreOption.setOnClickListener {
            val dialog = MoreOptionDialog(
                requireContext(),
                requireActivity(),
                DialogMoreOptionBinding.inflate(layoutInflater)
            )
            dialog.show()
        }
        return binding!!.root
    }

    override fun onStart() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: onStart")
        super.onStart()
        if (isStop) {
            popBackStack()
        }
    }

    override fun onStop() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: onStop")
        super.onStop()
        isStop = true
    }

    override fun onDestroyView() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: onDestroyView")
        super.onDestroyView()
        stopRecord()
        binding = null
        isStop = false
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: init")
        binding!!.txtMess.root.visibility = View.VISIBLE
        binding!!.txtMess.txtMess.setText(R.string.mess_stop_record)
        val translate2Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2)
        runnableAnimation = object : Runnable {
            override fun run() {
                binding!!.txtMess.root.startAnimation(translate2Animation)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnableAnimation as Runnable)
    }

    private fun recording() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: recording")
        recorder = Recorder(requireContext())
        recorder!!.start()
        handler.post(runnableTime)
    }

    private fun stopRecord() {
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: stopRecord")
        handler.removeCallbacks(runnableAnimation!!)
        handler.removeCallbacks(runnableTime)
        recorder!!.stop()
        recorder!!.release()
        binding!!.txtMess.root.visibility = View.INVISIBLE
    }

    private fun popBackStack() {
        stopRecord()
        val navController = findNavController(requireActivity(), R.id.nav_host_record_activity)
        navController.popBackStack()
        Log.d(VoiceChangerApp.TAG, "StopRecordFragment: To RecordFragment")
    }
}