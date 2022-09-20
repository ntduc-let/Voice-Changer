package com.prox.voicechanger.ui.change_voice.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import com.google.android.material.slider.Slider
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.R
import com.prox.voicechanger.databinding.FragmentCustomEffectBinding
import com.prox.voicechanger.ui.change_voice.adapter.EffectAdapter
import com.prox.voicechanger.utils.FFMPEGUtils
import com.prox.voicechanger.utils.FileUtils
import com.prox.voicechanger.utils.FirebaseUtils

class CustomEffectFragment : Fragment() {
    private lateinit var binding: FragmentCustomEffectBinding
    private var hzSelect: String = "500"

    private var callback: Callback? = null

    fun newInstance(): CustomEffectFragment {
        val args = Bundle()
        val fragment = CustomEffectFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomEffectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        initView()
        initEvent()
    }

    private fun initView() {
        binding.btnResetBasic.isEnabled = false
        binding.btnResetEqualizer.isEnabled = false
        binding.btnResetReverb.isEnabled = false
        setEnableCustom(false)
    }

    fun isCustom(): Boolean{
        return binding.btnResetBasic.isEnabled || binding.btnResetEqualizer.isEnabled || binding.btnResetReverb.isEnabled
    }

    private fun initEvent() {
        actionCustomBasic()
        actionCustomEqualizer()
        actionCustomReverb()
    }

    fun setEnableCustom(isEnable: Boolean) {
        binding.switchBasic.isEnabled = isEnable
        binding.switchEqualizer.isEnabled = isEnable
        binding.switchReverb.isEnabled = isEnable
//        binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(isEnable);
        binding.layoutBasic.seekTempoPitch.isEnabled = isEnable
        binding.layoutBasic.seekTempoRate.isEnabled = isEnable
        binding.layoutBasic.seekPanning.isEnabled = isEnable
        binding.layoutEqualizer.radio500.isEnabled = isEnable
        binding.layoutEqualizer.seekBandwidth.isEnabled = isEnable
        binding.layoutEqualizer.seekGain.isEnabled = isEnable
        binding.layoutReverb.seekInGain.isEnabled = isEnable
        binding.layoutReverb.seekOutGain.isEnabled = isEnable
        binding.layoutReverb.seekDelay.isEnabled = isEnable
        binding.layoutReverb.seekDecay.isEnabled = isEnable
        binding.layoutEqualizer.radGroupHz.isEnabled = isEnable
        binding.layoutEqualizer.radio500.isEnabled = isEnable
        binding.layoutEqualizer.radio1000.isEnabled = isEnable
        binding.layoutEqualizer.radio2000.isEnabled = isEnable
        binding.layoutEqualizer.radio3000.isEnabled = isEnable
        binding.layoutEqualizer.radio4000.isEnabled = isEnable
        binding.layoutEqualizer.radio5000.isEnabled = isEnable
        binding.layoutEqualizer.radio6000.isEnabled = isEnable
        binding.layoutEqualizer.radio7000.isEnabled = isEnable
        binding.layoutEqualizer.radio8000.isEnabled = isEnable
    }

    private fun actionCustomBasic() {
        binding.switchBasic.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                FirebaseUtils.sendEvent(requireContext(), "Layout_Effect", "Click Custom Basic")
                binding.switchBasic.setTrackResource(R.drawable.ic_track_enable)
                binding.layoutBasic.root.visibility = View.VISIBLE
                binding.btnResetBasic.visibility = View.VISIBLE
                binding.switchBasic.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding.switchBasic.setTrackResource(R.drawable.ic_track_disable)
                binding.layoutBasic.root.visibility = View.GONE
                binding.btnResetBasic.visibility = View.INVISIBLE
                binding.switchBasic.setThumbResource(R.drawable.ic_thumb)
                if (binding.btnResetBasic.isEnabled) {
                    binding.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
                    binding.btnResetBasic.isEnabled = false
                    resetCustomBasic()
                    selectCustom()
                }
            }
        }

        binding.btnResetBasic.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                requireContext().shortToast(R.string.processing_in_progress)
                return@setOnClickListener
            }

            resetCustomBasic()
            binding.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetBasic.isEnabled = false
            selectCustom()
        }

        binding.layoutBasic.seekTempoPitch.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutBasic.seekTempoRate.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutBasic.seekPanning.addOnSliderTouchListener(onSliderTouchListener)
    }

    private fun actionCustomEqualizer() {
        binding.switchEqualizer.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                FirebaseUtils.sendEvent(requireContext(), "Layout_Effect", "Click Custom Equalizer")
                binding.switchEqualizer.setTrackResource(R.drawable.ic_track_enable)
                binding.layoutEqualizer.root.visibility = View.VISIBLE
                binding.btnResetEqualizer.visibility = View.VISIBLE
                binding.switchEqualizer.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding.switchEqualizer.setTrackResource(R.drawable.ic_track_disable)
                binding.layoutEqualizer.root.visibility = View.GONE
                binding.btnResetEqualizer.visibility = View.INVISIBLE
                binding.switchEqualizer.setThumbResource(R.drawable.ic_thumb)
                if (binding.btnResetEqualizer.isEnabled) {
                    binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
                    binding.btnResetEqualizer.isEnabled = false
                    resetCustomEqualizer()
                    selectCustom()
                }
            }
        }

        binding.btnResetEqualizer.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                requireContext().shortToast(R.string.processing_in_progress)
                return@setOnClickListener
            }

            resetCustomEqualizer()
            binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetEqualizer.isEnabled = false
            selectCustom()
        }
        checkRadio()
        binding.layoutEqualizer.seekBandwidth.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutEqualizer.seekGain.addOnSliderTouchListener(onSliderTouchListener)
    }

    private fun actionCustomReverb() {
        binding.switchReverb.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            if (b) {
                FirebaseUtils.sendEvent(requireContext(), "Layout_Effect", "Click Custom Reverb")
                binding.switchReverb.setTrackResource(R.drawable.ic_track_enable)
                binding.layoutReverb.root.visibility = View.VISIBLE
                binding.btnResetReverb.visibility = View.VISIBLE
                binding.switchReverb.setThumbResource(R.drawable.ic_thumb2)
            } else {
                binding.switchReverb.setTrackResource(R.drawable.ic_track_disable)
                binding.layoutReverb.root.visibility = View.GONE
                binding.btnResetReverb.visibility = View.INVISIBLE
                binding.switchReverb.setThumbResource(R.drawable.ic_thumb)
                if (binding.btnResetReverb.isEnabled) {
                    binding.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
                    binding.btnResetReverb.isEnabled = false
                    resetCustomReverb()
                    selectCustom()
                }
            }
        }

        binding.btnResetReverb.setOnClickListener {
            if (EffectAdapter.isExecuting) {
                requireContext().shortToast(R.string.processing_in_progress)
                return@setOnClickListener
            }

            resetCustomReverb()
            binding.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetReverb.isEnabled = false
            selectCustom()
        }
        binding.layoutReverb.seekInGain.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutReverb.seekOutGain.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutReverb.seekDelay.addOnSliderTouchListener(onSliderTouchListener)
        binding.layoutReverb.seekDecay.addOnSliderTouchListener(onSliderTouchListener)
    }

    private fun checkRadio() {
        binding.layoutEqualizer.radGroupHz.setOnCheckedChangeListener(radGrpOnCheckedChangeListener)
        binding.layoutEqualizer.radio500.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio1000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio2000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio3000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio4000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio5000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio6000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio7000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
        binding.layoutEqualizer.radio8000.setOnCheckedChangeListener(radBtnOnCheckedChangeListener)
    }

    fun resetCustomEffect() {
        binding.switchBasic.isChecked = false
        binding.switchEqualizer.isChecked = false
        binding.switchReverb.isChecked = false
        binding.switchBasic.setTrackResource(R.drawable.ic_track_disable)
        binding.switchEqualizer.setTrackResource(R.drawable.ic_track_disable)
        binding.switchReverb.setTrackResource(R.drawable.ic_track_disable)
        binding.layoutBasic.root.visibility = View.GONE
        binding.layoutEqualizer.root.visibility = View.GONE
        binding.layoutReverb.root.visibility = View.GONE
        resetCustomBasic()
        resetCustomEqualizer()
        resetCustomReverb()
    }

    private fun resetCustomBasic() {
        binding.layoutBasic.seekTempoPitch.value = 16000f
        binding.layoutBasic.seekTempoRate.value = 1f
        binding.layoutBasic.seekPanning.value = 1f
    }

    private fun resetCustomEqualizer() {
        binding.layoutEqualizer.radio500.isChecked = true
        hzSelect = "500"
        binding.layoutEqualizer.seekBandwidth.value = 100f
        binding.layoutEqualizer.seekGain.value = 0f
    }

    private fun resetCustomReverb() {
        binding.layoutReverb.seekInGain.value = 1f
        binding.layoutReverb.seekOutGain.value = 1f
        binding.layoutReverb.seekDelay.value = 0f
        binding.layoutReverb.seekDecay.value = 1f
    }

    private fun enableReset() {
        if (binding.layoutBasic.seekTempoPitch.value == 16000f
            && binding.layoutBasic.seekTempoRate.value == 1f
            && binding.layoutBasic.seekPanning.value == 1f
        ) {
            binding.btnResetBasic.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetBasic.isEnabled = false
        } else {
            binding.btnResetBasic.setImageResource(R.drawable.ic_reset_enable)
            binding.btnResetBasic.isEnabled = true
        }
        if (binding.layoutEqualizer.radio500.isChecked
            && binding.layoutEqualizer.seekBandwidth.value == 100f
            && binding.layoutEqualizer.seekGain.value == 0f
        ) {
            binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetEqualizer.isEnabled = false
        } else {
            binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable)
            binding.btnResetEqualizer.isEnabled = true
        }
        if (binding.layoutReverb.seekInGain.value == 1f
            && binding.layoutReverb.seekOutGain.value == 1f
            && binding.layoutReverb.seekDelay.value == 0f
            && binding.layoutReverb.seekDecay.value == 1f
        ) {
            binding.btnResetReverb.setImageResource(R.drawable.ic_reset_disable)
            binding.btnResetReverb.isEnabled = false
        } else {
            binding.btnResetReverb.setImageResource(R.drawable.ic_reset_enable)
            binding.btnResetReverb.isEnabled = true
        }
    }

    private val onSliderTouchListener: Slider.OnSliderTouchListener =
        object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                enableReset()
                selectCustom()
            }
        }

    private val radBtnOnCheckedChangeListener =
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

    private val radGrpOnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _: RadioGroup?, _: Int ->
            if (binding.layoutEqualizer.radio500.isChecked
                && binding.layoutEqualizer.seekBandwidth.value == 100f
                && binding.layoutEqualizer.seekGain.value == 0f
            ) {
                binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable)
                binding.btnResetEqualizer.isEnabled = false
            } else {
                binding.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable)
                binding.btnResetEqualizer.isEnabled = true
            }
        }

    private fun selectCustom() {
        val hzNumber: Double = try {
            hzSelect.toDouble()
        } catch (e: Exception) {
            500.0
        }
        val cmd = FFMPEGUtils.getCMDCustomEffect(
            FileUtils.getTempEffectFilePath(requireContext()),
            FileUtils.getTempCustomFilePath(requireContext()),
            (binding.layoutBasic.seekTempoPitch.value / 16000).toDouble(),
            binding.layoutBasic.seekTempoRate.value.toDouble(),
            binding.layoutBasic.seekPanning.value.toDouble(),
            hzNumber,
            binding.layoutEqualizer.seekBandwidth.value.toDouble(),
            binding.layoutEqualizer.seekGain.value.toDouble(),
            binding.layoutReverb.seekInGain.value.toDouble(),
            binding.layoutReverb.seekOutGain.value.toDouble(),
            if (binding.layoutReverb.seekDelay.value == 0f) 1.0 else binding.layoutReverb.seekDelay.value.toDouble(),
            if (binding.layoutReverb.seekDecay.value == 0f) 0.01 else binding.layoutReverb.seekDecay.value.toDouble()
        )
        callback?.addCustom(cmd)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {
        fun addCustom(cmd: String)
    }
}