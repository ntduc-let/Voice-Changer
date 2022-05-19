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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileVoiceAdapter extends RecyclerView.Adapter<FileVoiceAdapter.FileVoiceViewHolder> {
    private List<FileVoice> fileVoices;
    private final Context context;
    private final Activity activity;
    private final FileVoiceViewModel model;
    private boolean isPlaying;
    private String path;

    private MediaPlayer player;
    private FileVoiceViewHolder holderSelect;

    private final Handler handler = new Handler();
    private final Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            if (player == null) {
                Log.d(TAG, "FileVoiceAdapter: player null");
            } else {
                holderSelect.binding.itemPlayMedia.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                holderSelect.binding.itemPlayMedia.seekTime.setProgress(player.getCurrentPosition());
                handler.post(this);
            }
        }
    };

    public FileVoiceAdapter(Context context, Activity activity, FileVoiceViewModel model) {
        this.context = context;
        this.activity = activity;
        this.model = model;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFileVoices(List<FileVoice> fileVoices) {
        if (fileVoices != null) {
            this.fileVoices = fileVoices;
        }else {
            this.fileVoices = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public List<FileVoice> getFileVoices() {
        if (fileVoices == null) {
            return new ArrayList<>();
        } else {
            return fileVoices;
        }
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
        holder.binding.txtSize.setText(NumberUtils.formatAsTime(fileVoice.getDuration()) + " | " + NumberUtils.formatAsSize(fileVoice.getSize()));
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
            if (holderSelect == null) {
                holderSelect = holder;
                player = new MediaPlayer();
                path = fileVoice.getPath();
                if (!(new File(path).exists())) {
                    model.delete(fileVoice);
                } else {
                    startMediaPlayer(fileVoice.getPath());
                    isPlaying = true;
                }
            } else if (holderSelect.equals(holder)) {
                if (player == null) {
                    player = new MediaPlayer();
                    path = fileVoice.getPath();
                    if (!(new File(path).exists())) {
                        model.delete(fileVoice);
                    } else {
                        startMediaPlayer(fileVoice.getPath());
                        isPlaying = true;
                    }
                } else if (player.isPlaying()) {
                    pauseMediaPlayer();
                    isPlaying = false;
                } else {
                    resumeMediaPlayer();
                    isPlaying = true;
                }
            } else {
                stopMediaPlayer();
                holderSelect = holder;

                if (player == null) {
                    player = new MediaPlayer();
                }
                path = fileVoice.getPath();
                if (!(new File(path).exists())) {
                    model.delete(fileVoice);
                } else {
                    startMediaPlayer(fileVoice.getPath());
                    isPlaying = true;
                }
            }
        });
        holder.binding.itemPlayMedia.seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (player != null) {
                        player.seekTo(i);
                        holder.binding.itemPlayMedia.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                    } else {
                        holder.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    if (player.isPlaying()) {
                        handler.removeCallbacks(updateTime);
                    }
                } else {
                    holder.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    if (player.isPlaying()) {
                        handler.post(updateTime);
                    }
                } else {
                    holder.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileVoices == null) {
            return 0;
        }
        return fileVoices.size();
    }

    public void pause() {
        if (isPlaying) {
            pauseMediaPlayer();
        }
    }

    public void resume() {
        if (path == null || !(new File(path).exists())) {
            isPlaying = false;
            handler.removeCallbacks(updateTime);
            return;
        }
        if (isPlaying) {
            if (player == null) {
                player = new MediaPlayer();
                startMediaPlayer(path);
            }else {
                resumeMediaPlayer();
            }
        }
    }

    public static class FileVoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemFileVoiceBinding binding;

        public FileVoiceViewHolder(@NonNull ItemFileVoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void startMediaPlayer(String path) {
        Log.d(TAG, "FileVoiceAdapter: startMediaPlayer " + path);
        if (player == null) {
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
            Log.d(TAG, "FileVoiceAdapter: " + e.getMessage());
        }
        updateTime();
    }

    private void pauseMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: pauseMediaPlayer");
        if (player == null) {
            return;
        }
        player.pause();
        handler.removeCallbacks(updateTime);

        holderSelect.binding.btnPlayOrPause.setText(R.string.play);
    }

    private void resumeMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: resumeMediaPlayer");
        if (player == null) {
            return;
        }
        player.start();
        updateTime();

        holderSelect.binding.btnPlayOrPause.setText(R.string.pause);
    }

    private void stopMediaPlayer() {
        Log.d(TAG, "FileVoiceAdapter: stopMediaPlayer");
        if (player == null) {
            return;
        }
        player.stop();
        handler.removeCallbacks(updateTime);

        holderSelect.binding.btnPlayOrPause.setText(R.string.play);
        holderSelect.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
    }

    public void release() {
        Log.d(TAG, "FileVoiceAdapter: release");
        if (player == null) {
            return;
        }
        if (player.isPlaying()) {
            stopMediaPlayer();
        } else {
            holderSelect.binding.itemPlayMedia.getRoot().setVisibility(View.GONE);
        }

        player.release();
        player = null;
        holderSelect = null;
    }

    private void updateTime() {
        Log.d(TAG, "FileVoiceAdapter: updateTime");
        if (player != null){
            holderSelect.binding.itemPlayMedia.seekTime.setMax(player.getDuration());
            holderSelect.binding.itemPlayMedia.txtTotalTime2.setText(NumberUtils.formatAsTime(player.getDuration()));

            handler.post(updateTime);
        }
    }
}
