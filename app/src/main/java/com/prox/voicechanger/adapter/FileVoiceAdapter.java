package com.prox.voicechanger.adapter;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogOptionBinding;
import com.prox.voicechanger.databinding.ItemFileVoiceBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.OptionDialog;
import com.prox.voicechanger.utils.NumberUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.IOException;
import java.util.List;

public class FileVoiceAdapter extends RecyclerView.Adapter<FileVoiceAdapter.FileVoiceViewHolder> {
    private List<FileVoice> fileVoices;
    private final Context context;
    private final Activity activity;
    private final FileVoiceViewModel model;
    private boolean isPlaying;

    private MediaPlayer player;
    private FileVoiceViewHolder holderSelect;

    private final Handler handler = new Handler();
    private final Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            holderSelect.binding.itemPlayMedia.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
            holderSelect.binding.itemPlayMedia.seekTime.setProgress(player.getCurrentPosition());
            handler.post(this);
        }
    };

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
            release();
            OptionDialog dialog = new OptionDialog(
                    context,
                    activity,
                    DialogOptionBinding.inflate(activity.getLayoutInflater()),
                    model,
                    fileVoice);
            dialog.show();
        });
        holder.binding.btnPlayOrPause.setOnClickListener(view -> {
            if (holderSelect == null){
                holderSelect = holder;
                player = new MediaPlayer();
                startMediaPlayer(fileVoice.getPath());
                isPlaying = true;
            }else if (holderSelect.equals(holder)){
                if (player.isPlaying()){
                    pauseMediaPlayer();
                    isPlaying = false;
                }else {
                    resumeMediaPlayer();
                    isPlaying = true;
                }
            }else {
                stopMediaPlayer();
                holderSelect = holder;
                startMediaPlayer(fileVoice.getPath());
                isPlaying = true;
            }
        });
        holder.binding.itemPlayMedia.seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    player.seekTo(i);
                    holder.binding.itemPlayMedia.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (player.isPlaying()){
                    handler.removeCallbacks(updateTime);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.isPlaying()){
                    handler.post(updateTime);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileVoices == null){
            return 0;
        }
        return fileVoices.size();
    }

    public void pause(){
        if (isPlaying){
            pauseMediaPlayer();
        }
    }

    public void resume(){
        if (isPlaying){
            resumeMediaPlayer();
        }
    }

    public static class FileVoiceViewHolder extends RecyclerView.ViewHolder{
        private final ItemFileVoiceBinding binding;

        public FileVoiceViewHolder(@NonNull ItemFileVoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void startMediaPlayer(String path) {
        Log.d(TAG, "FileVoiceAdapter: startMediaPlayer "+path);
        if (player == null){
            return;
        }
        try {
            player.reset();
            player.setDataSource(path);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setLooping(true);
            player.prepare();
            player.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.start();
                holderSelect.binding.btnPlayOrPause.setText(R.string.pause);
                holderSelect.binding.itemPlayMedia.getRoot().setVisibility(View.VISIBLE);
            });
        } catch (IOException e) {
            Log.d(TAG, "FileVoiceAdapter: "+e.getMessage());
        }
        updateTime();
    }

    private void pauseMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: pauseMediaPlayer");
        if (player == null){
            return;
        }
        player.pause();
        handler.removeCallbacks(updateTime);

        holderSelect.binding.btnPlayOrPause.setText(R.string.play);
    }

    private void resumeMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: resumeMediaPlayer");
        if (player == null){
            return;
        }
        player.start();
        updateTime();

        holderSelect.binding.btnPlayOrPause.setText(R.string.pause);
    }

    private void stopMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: stopMediaPlayer");
        if (player == null){
            return;
        }
        player.stop();
        handler.removeCallbacks(updateTime);

        holderSelect.binding.btnPlayOrPause.setText(R.string.play);
        holderSelect.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
    }

    public void release() {
        Log.d(TAG, "FileVoiceAdapter: release");
        if (player == null){
            return;
        }
        if (player.isPlaying()){
            stopMediaPlayer();
        }else {
            holderSelect.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
        }

        player.release();
        player = null;
        holderSelect = null;
    }

    private void updateTime(){
        Log.d(TAG, "FileVoiceAdapter: updateTime");
        holderSelect.binding.itemPlayMedia.seekTime.setMax(player.getDuration());
        holderSelect.binding.itemPlayMedia.txtTotalTime2.setText(NumberUtils.formatAsTime(player.getDuration()));

        handler.post(updateTime);
    }
}
