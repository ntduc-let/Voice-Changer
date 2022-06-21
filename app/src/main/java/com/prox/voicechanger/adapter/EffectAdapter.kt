package com.prox.voicechanger.adapter

import com.prox.voicechanger.interfaces.EffectListener
import androidx.recyclerview.widget.RecyclerView
import com.prox.voicechanger.adapter.EffectAdapter.EffectViewHolder
import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.LayoutInflater
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import com.prox.voicechanger.R
import android.graphics.drawable.AnimationDrawable
import android.content.Context
import android.view.View
import android.widget.ImageView
import com.prox.voicechanger.databinding.ItemEffectBinding
import com.prox.voicechanger.model.Effect
import java.util.ArrayList

class EffectAdapter(private val context: Context, private val effectListener: EffectListener) :
    RecyclerView.Adapter<EffectViewHolder>() {
    private var effects: ArrayList<Effect>? = null
    private var imgSelect: ImageView? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setEffects(effects: ArrayList<Effect>?) {
        this.effects = effects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val binding = ItemEffectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EffectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        val button = holder.binding.btnAddEffect as MaterialButton
        button.text = effects!![position].title
        button.setIconResource(effects!![position].src)
        if (position == 0) {
            holder.binding.imgSelect.visibility = View.VISIBLE
            imgSelect = holder.binding.imgSelect
        } else {
            holder.binding.imgSelect.visibility = View.GONE
        }
        button.setOnClickListener {
            if (isExecuting) {
                Toast.makeText(context, R.string.processing_in_progress, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isExecuting = true
            imgSelect!!.visibility = View.GONE
            holder.binding.imgSelect.visibility = View.VISIBLE
            imgSelect = holder.binding.imgSelect
            effectListener.addEffectListener(effects!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (effects == null) {
            0
        } else effects!!.size
    }

    class EffectViewHolder(val binding: ItemEffectBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        init {
            val rocketAnimation = binding.imgSelect.background as AnimationDrawable
            rocketAnimation.start()
        }
    }

    companion object {
        @JvmField
        var isExecuting = false
    }
}