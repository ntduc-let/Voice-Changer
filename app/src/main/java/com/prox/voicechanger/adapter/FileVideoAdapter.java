package com.prox.voicechanger.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.voicechanger.databinding.DialogOptionVideoBinding;
import com.prox.voicechanger.databinding.DialogPlayVideoBinding;
import com.prox.voicechanger.databinding.ItemFileVideoBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.OptionVideoDialog;
import com.prox.voicechanger.ui.dialog.PlayVideoDialog;
import com.prox.voicechanger.utils.NumberUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileVideoAdapter extends RecyclerView.Adapter<FileVideoAdapter.FileVideoViewHolder> {
    private List<FileVoice> fileVideos;
    private final Context context;
    private final Activity activity;
    private final FileVoiceViewModel model;

    public FileVideoAdapter(Context context, Activity activity, FileVoiceViewModel model) {
        this.context = context;
        this.activity = activity;
        this.model = model;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFileVideos(List<FileVoice> fileVideos) {
        if (fileVideos != null) {
            this.fileVideos = fileVideos;
        }else {
            this.fileVideos = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public List<FileVoice> getFileVideos() {
        if (fileVideos == null) {
            return new ArrayList<>();
        } else {
            return fileVideos;
        }
    }

    @NonNull
    @Override
    public FileVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileVideoBinding binding = ItemFileVideoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FileVideoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileVideoViewHolder holder, int position) {
        FileVoice fileVideo = fileVideos.get(position);
        if (new File(fileVideo.getImageVideo()).exists()){
            holder.binding.imgFile.setImageBitmap(BitmapFactory.decodeFile(fileVideo.getImageVideo()));
        }
        holder.binding.txtNameFile.setText(fileVideo.getName());
        holder.binding.txtSize.setText(NumberUtils.formatAsTime(fileVideo.getDuration()) + " | " + NumberUtils.formatAsSize(fileVideo.getSize()));
        holder.binding.txtDate.setText(NumberUtils.formatAsDate(fileVideo.getDate()));
        holder.binding.btnOption.setOnClickListener(view -> {
            OptionVideoDialog dialog = new OptionVideoDialog(
                    context,
                    activity,
                    DialogOptionVideoBinding.inflate(activity.getLayoutInflater()),
                    model,
                    fileVideo);
            dialog.show();
        });
        holder.binding.btnPlay.setOnClickListener(view -> {
            PlayVideoDialog dialog = new PlayVideoDialog(
                    context,
                    DialogPlayVideoBinding.inflate(activity.getLayoutInflater()),
                    fileVideo.getPath());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        if (fileVideos == null) {
            return 0;
        }
        return fileVideos.size();
    }

    public static class FileVideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemFileVideoBinding binding;

        public FileVideoViewHolder(@NonNull ItemFileVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
