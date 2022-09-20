package com.prox.voicechanger.ui.change_voice.adapter

import androidx.recyclerview.widget.RecyclerView
import com.prox.voicechanger.ui.change_voice.adapter.EffectAdapter.EffectViewHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.ViewGroup
import android.view.LayoutInflater
import com.prox.voicechanger.R
import android.view.View
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import com.ntduc.toastutils.shortToast
import com.prox.voicechanger.databinding.ItemEffectBinding
import com.prox.voicechanger.model.Effect
import com.prox.voicechanger.ui.change_voice.activity.ChangeVoiceActivity
import java.util.ArrayList

class EffectAdapter(
    val context: Context,
    private var effects: List<Effect> = listOf()
) : RecyclerView.Adapter<EffectViewHolder>() {

    companion object {
        var isExecuting = false
    }

    private var imgSelect: ImageView? = null

    private var addEffectListener: ((Effect) -> Unit)? = null

    fun setAddEffectListener(listener: (Effect) -> Unit) {
        addEffectListener = listener
    }

    inner class EffectViewHolder(binding: ItemEffectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemEffectBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val binding = ItemEffectBinding.inflate(LayoutInflater.from(context), parent, false)
        return EffectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        val button = holder.binding.btnAddEffect as MaterialButton

        button.text = effects[position].title
        button.setIconResource(effects[position].src)
        val rocketAnimation = holder.binding.imgSelect.background as AnimationDrawable
        rocketAnimation.start()
        if (effects[position].title == ChangeVoiceActivity.effectSelected.title) {
            holder.binding.imgSelect.visibility = View.VISIBLE
            imgSelect = holder.binding.imgSelect
        } else {
            holder.binding.imgSelect.visibility = View.GONE
        }

        button.setOnClickListener {
            if (isExecuting) {
                context.shortToast(R.string.processing_in_progress)
                return@setOnClickListener
            }
            isExecuting = true
            imgSelect!!.visibility = View.GONE
            holder.binding.imgSelect.visibility = View.VISIBLE
            imgSelect = holder.binding.imgSelect
            addEffectListener?.let {
                it(effects[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return effects.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEffects(effects: ArrayList<Effect>) {
        this.effects = effects
        notifyDataSetChanged()
    }
}