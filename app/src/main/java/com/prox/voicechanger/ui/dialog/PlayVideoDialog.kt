package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogPlayVideoBinding;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.NumberUtils;

import java.io.File;

public class PlayVideoDialog extends CustomDialog {
    private final DialogPlayVideoBinding binding;
    private MediaPlayer player;

    private final Handler handler = new Handler();
    private final Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            try {
                binding.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                binding.seekTime2.setProgress(player.getCurrentPosition());
                handler.post(this);
            } catch (Exception e) {
                Log.d(TAG, "updateTime error " + e.getMessage());
                handler.removeCallbacks(this);
            }

        }
    };


    public PlayVideoDialog(@NonNull Context context, DialogPlayVideoBinding binding, String path) {
        super(context, binding.getRoot());
        Log.d(TAG, "PlayVideoDialog: create");
        setCancelable(false);
        this.binding = binding;

        binding.txtNameVideo.setText(FileUtils.getName(path));
        binding.videoView.setVideoPath(path);
        binding.videoView.setOnPreparedListener(player -> {
            this.player = player;
            this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.player.setLooping(true);
        });

        binding.videoView.setOnErrorListener((mp, what, extra) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.dialog_video_error)
                    .setTitle(R.string.app_name);

            builder.setPositiveButton(R.string.ok, (dialog, id) -> cancel());

            AlertDialog dialogRequest = builder.create();
            dialogRequest.show();

            return true;
        });

        binding.seekTime2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (player != null && (new File(path).exists())){
                        player.seekTo(i);
                        binding.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                    }else {
                        cancel();
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (player != null && (new File(path).exists())){
                    if (player.isPlaying()) {
                        handler.removeCallbacks(updateTime);
                    }
                }else {
                    cancel();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null && (new File(path).exists())){
                    if (player.isPlaying()) {
                        handler.post(updateTime);
                    }
                }else {
                    cancel();
                }
            }
        });

        binding.btnPauseOrResume2.setOnClickListener(view -> {
            if (player != null && (new File(path).exists())){
                if (player.isPlaying()) {
                    pauseVideo();
                } else {
                    resumeVideo();
                }
            }else {
                cancel();
            }

        });

        binding.btnBackVideo.setOnClickListener(view -> {
            stop();
            if (player != null && (new File(path).exists())){
                player.stop();
                player.release();
            }
            cancel();
        });

        binding.btnShareVideo.setOnClickListener(view -> {
            if (player != null && (new File(path).exists())){
                if (player.isPlaying()) {
                    pauseVideo();
                }
                FileUtils.shareFile(context, path);
            }else {
                cancel();
            }
        });
    }

    private void pauseVideo() {
        Log.d(TAG, "PlayVideoDialog: pauseVideo");

        player.pause();
        handler.removeCallbacks(updateTime);

        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume);
    }

    private void resumeVideo() {
        Log.d(TAG, "PlayVideoDialog: resumeVideo");

        player.start();
        handler.post(updateTime);

        binding.txtTotalTime2.setText(NumberUtils.formatAsTime(player.getDuration()));
        binding.seekTime2.setMax(player.getDuration());
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_pause);
    }

    public void stop() {
        handler.removeCallbacks(updateTime);
        binding.btnPauseOrResume2.setImageResource(R.drawable.ic_resume);
    }
}
