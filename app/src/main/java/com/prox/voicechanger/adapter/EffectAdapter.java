package com.prox.voicechanger.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.prox.voicechanger.databinding.ItemEffectBinding;
import com.prox.voicechanger.interfaces.EffectListener;
import com.prox.voicechanger.model.Effect;

import java.util.ArrayList;

public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.EffectViewHolder> {
    private ArrayList<Effect> effects;
    private ImageView imgSelect;
    private final EffectListener effectListener;

    public EffectAdapter(EffectListener effectListener){
        this.effectListener = effectListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEffects(ArrayList<Effect> effects){
        this.effects = effects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEffectBinding binding = ItemEffectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EffectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EffectViewHolder holder, int position) {
        MaterialButton button = (MaterialButton) holder.binding.btnAddEffect;
        button.setText(effects.get(position).getTitle());
        button.setIconResource(effects.get(position).getSrc());

        if (position==0){
            holder.binding.imgSelect.setVisibility(View.VISIBLE);
            imgSelect = holder.binding.imgSelect;
        }else{
            holder.binding.imgSelect.setVisibility(View.GONE);
        }

        button.setOnClickListener(view -> {
            imgSelect.setVisibility(View.GONE);
            holder.binding.imgSelect.setVisibility(View.VISIBLE);
            imgSelect = holder.binding.imgSelect;

            effectListener.addEffectListener(effects.get(position));
        });
    }

    @Override
    public int getItemCount() {
        if (effects == null){
            return 0;
        }
        return effects.size();
    }

    public static class EffectViewHolder extends RecyclerView.ViewHolder{
        private final ItemEffectBinding binding;

        public EffectViewHolder(ItemEffectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            AnimationDrawable rocketAnimation = (AnimationDrawable) binding.imgSelect.getBackground();
            rocketAnimation.start();
        }
    }
}
