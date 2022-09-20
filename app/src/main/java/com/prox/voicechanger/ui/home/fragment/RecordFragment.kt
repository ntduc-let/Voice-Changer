package com.prox.voicechanger.ui.home.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.prox.voicechanger.utils.FirebaseUtils
import com.prox.voicechanger.R
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.prox.voicechanger.BuildConfig
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.databinding.FragmentRecordBinding
import com.prox.voicechanger.ui.home.dialog.MoreOptionDialog
import com.prox.voicechanger.utils.NetworkUtils
import com.proxglobal.proxads.adsv2.ads.ProxAds
import com.proxglobal.proxads.adsv2.callback.AdsCallback
import com.proxglobal.purchase.ProxPurchase

class RecordFragment : Fragment() {
    private lateinit var binding: FragmentRecordBinding
    private lateinit var dialog: MoreOptionDialog
    private var callback: Callback? = null

    private val handler = Handler(Looper.getMainLooper())
    private var runnableAnimation: Runnable? = null

    fun newInstance(): RecordFragment {
        val args = Bundle()
        val fragment = RecordFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        System.gc()
    }

    private fun init() {
        initView()
        initEvent()
    }

    private fun initView() {
        val rotate1Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate1)
        val rotate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate2)
        val rotate3Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate3)
        val rotate4Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate4)
        binding.aniRecord.icAniRecord1.startAnimation(rotate2Animation)
        binding.aniRecord.icAniRecord2.startAnimation(rotate4Animation)
        binding.aniRecord.icAniRecord3.startAnimation(rotate1Animation)
        binding.aniRecord.icAniRecord4.startAnimation(rotate2Animation)
        binding.aniRecord.icAniRecord5.startAnimation(rotate4Animation)
        binding.aniRecord.icAniRecord6.startAnimation(rotate3Animation)
        val rocketAnimation = binding.aniRecord.icAniRecord7.background as AnimationDrawable
        rocketAnimation.start()
        val translate1Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate1)
        binding.aniRecord.icAniRecord.startAnimation(translate1Animation)
        handler.postDelayed({
            binding.btnSetting.visibility = View.VISIBLE
            binding.btnMoreOption.visibility = View.VISIBLE
            binding.txtContent.visibility = View.VISIBLE
            binding.txtContent2.visibility = View.VISIBLE
            binding.btnRecord.visibility = View.VISIBLE
            binding.txtMess.txtMess.setText(R.string.mess_start_record)
            binding.txtMess.root.visibility = View.VISIBLE
            val translate2Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2)
            runnableAnimation = object : Runnable {
                override fun run() {
                    binding.txtMess.root.startAnimation(translate2Animation)
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnableAnimation!!)
        }, 1500)

        dialog = MoreOptionDialog()

        ProxAds.instance.showMediumNativeWithShimmerStyle20(
            requireActivity(),
            BuildConfig.native_home,
            binding.adContainer,
            object : AdsCallback() {})
        if (ProxPurchase.getInstance().checkPurchased()
            || !NetworkUtils.isNetworkAvailable(requireContext())
        ) {
            binding.adContainer.visibility = View.GONE
        } else {
            val marginParams = binding.btnRecord.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.setMargins(0, 0, 0, 0)
        }
    }

    private fun initEvent() {
        binding.btnRecord.setOnClickListener {
            FirebaseUtils.sendEvent(requireContext(), "Layout_Home", "Click recoding")
            callback?.onRecord()
        }
        binding.btnSetting.setOnClickListener {
            callback?.onSetting()
        }

        binding.btnMoreOption.setOnClickListener {
            dialog.show(requireActivity().supportFragmentManager, "MoreOptionDialog")
        }

        dialog.setOnImportListener {
            callback?.onImport()
        }

        dialog.setOnTextToVoiceListener {
            callback?.onTextToVoice()
        }

        dialog.setOnFileListener {
            callback?.onFile()
        }

        dialog.setOnVideoListener {
            callback?.onVideo()
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {
        fun onRecord()
        fun onSetting()
        fun onImport()
        fun onTextToVoice()
        fun onFile()
        fun onVideo()
    }
}