package com.prox.voicechanger.ui.change_voice.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.prox.voicechanger.databinding.FragmentBasicEffectBinding
import com.prox.voicechanger.model.Effect
import com.prox.voicechanger.ui.change_voice.adapter.EffectAdapter
import com.prox.voicechanger.utils.FFMPEGUtils

class BasicEffectFragment : Fragment() {
    private lateinit var binding: FragmentBasicEffectBinding
    private lateinit var adapter: EffectAdapter

    private var callback: Callback? = null

    fun newInstance(): BasicEffectFragment {
        val args = Bundle()
        val fragment = BasicEffectFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBasicEffectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EffectAdapter(requireContext(), FFMPEGUtils.effects)
        adapter.setAddEffectListener {
            callback?.addEffect(it)
        }
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
        flexboxLayoutManager.justifyContent = JustifyContent.SPACE_AROUND
        binding.recyclerViewEffects.layoutManager = flexboxLayoutManager
        binding.recyclerViewEffects.adapter = adapter
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {
        fun addEffect(effect: Effect)
    }
}