package com.prox.voicechanger.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.voicechanger.databinding.DialogOptionBinding;
import com.prox.voicechanger.databinding.ItemFileVoiceBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.OptionDialog;
import com.prox.voicechanger.utils.NumberUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.util.List;

public class FileVoiceAdapter extends RecyclerView.Adapter<FileVoiceAdapter.FileVoiceViewHolder> {
    private List<FileVoice> fileVoices;
    private final Context context;
    private final Activity activity;
    private final FileVoiceViewModel model;

    public FileVoiceAdapter(Context context, Activity activity, FileVoiceViewModel model){
        this.context = context;
        this.activity = activity;
        this.model = model;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFileVoices(List<FileVoice> fileVoices){
        this.fileVoices = fileVoices;
        notifyDataSetChanged();
    }

    public List<FileVoice> getFileVoices(){
        return fileVoices;
    }

    @NonNull
    @Override
    public FileVoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileVoiceBinding binding = ItemFileVoiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FileVoiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileVoiceViewHolder holder, int position) {
        FileVoice fileVoice = fileVoices.get(position);
        holder.binding.imgFile.setImageResource(fileVoice.getSrc());
        holder.binding.txtNameFile.setText(fileVoice.getName());
        holder.binding.txtSize.setText(NumberUtils.formatAsTime(fileVoice.getDuration())+" | "+fileVoice.getSize()/1024 + "kB");
        holder.binding.txtDate.setText(NumberUtils.formatAsDate(fileVoice.getDate()));
        holder.binding.btnOption.setOnClickListener(view -> {
            OptionDialog dialog = new OptionDialog(
                    context,
                    activity,
                    DialogOptionBinding.inflate(activity.getLayoutInflater()),
                    model,
                    fileVoice);
            dialog.show();
        });
        holder.binding.btnPlay.setOnClickListener(view -> {

        });
    }

    @Override
    public int getItemCount() {
        if (fileVoices == null){
            return 0;
        }
        return fileVoices.size();
    }

    public static class FileVoiceViewHolder extends RecyclerView.ViewHolder{
        private final ItemFileVoiceBinding binding;

        public FileVoiceViewHolder(@NonNull ItemFileVoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
