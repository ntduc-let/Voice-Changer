package com.prox.voicechanger.ui.fragment

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.prox.voicechanger.R
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.DialogMoreOptionBinding
import com.prox.voicechanger.databinding.FragmentRecordBinding
import com.prox.voicechanger.ui.activity.SettingActivity
import com.prox.voicechanger.ui.dialog.MoreOptionDialog
import com.prox.voicechanger.utils.PermissionUtils.checkPermission

class RecordFragment : Fragment() {
    private var binding: FragmentRecordBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private var runnableAnimation: Runnable? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(VoiceChangerApp.TAG, "RecordFragment: onCreateView")
        binding = FragmentRecordBinding.inflate(inflater, container, false)
        init()
        binding!!.btnRecord.setOnClickListener {
            if (checkPermission(requireContext(), requireActivity())) {
                findNavController(binding!!.root).navigate(R.id.action_recordFragment_to_stopRecordFragment)
                Log.d(VoiceChangerApp.TAG, "RecordFragment: To StopRecordFragment")
            }
        }

        binding!!.btnMore.setOnClickListener {
            val goToSetting = Intent(requireActivity(), SettingActivity::class.java)
            startActivity(goToSetting)
        }

        binding!!.btnMoreOption.setOnClickListener {
            if (checkPermission(requireContext(), requireActivity())) {
                val dialog = MoreOptionDialog(
                    requireContext(),
                    requireActivity(),
                    DialogMoreOptionBinding.inflate(layoutInflater)
                )
                dialog.show()
            }
        }
        return binding!!.root
    }

    override fun onDestroyView() {
        Log.d(VoiceChangerApp.TAG, "RecordFragment: onDestroyView")
        super.onDestroyView()
        handler.removeCallbacks(runnableAnimation!!)
        binding = null
    }

    private fun init() {
        Log.d(VoiceChangerApp.TAG, "RecordFragment: init")
        val rotate1Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate1)
        val rotate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate2)
        val rotate3Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate3)
        val rotate4Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate4)
        binding!!.aniRecord.icAniRecord1.startAnimation(rotate2Animation)
        binding!!.aniRecord.icAniRecord2.startAnimation(rotate4Animation)
        binding!!.aniRecord.icAniRecord3.startAnimation(rotate1Animation)
        binding!!.aniRecord.icAniRecord4.startAnimation(rotate2Animation)
        binding!!.aniRecord.icAniRecord5.startAnimation(rotate4Animation)
        binding!!.aniRecord.icAniRecord6.startAnimation(rotate3Animation)
        val rocketAnimation = binding!!.aniRecord.icAniRecord7.background as AnimationDrawable
        rocketAnimation.start()
        val translate1Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate1)
        binding!!.aniRecord.icAniRecord.startAnimation(translate1Animation)
        handler.postDelayed({
            binding!!.btnMore.visibility = View.VISIBLE
            binding!!.btnMoreOption.visibility = View.VISIBLE
            binding!!.txtContent.visibility = View.VISIBLE
            binding!!.txtContent2.visibility = View.VISIBLE
            binding!!.btnRecord.visibility = View.VISIBLE
            binding!!.txtMess.txtMess.setText(R.string.mess_start_record)
            binding!!.txtMess.root.visibility = View.VISIBLE
            val translate2Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2)
            runnableAnimation = object : Runnable {
                override fun run() {
                    binding!!.txtMess.root.startAnimation(translate2Animation)
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnableAnimation as Runnable)
        }, 1500)
    }
}