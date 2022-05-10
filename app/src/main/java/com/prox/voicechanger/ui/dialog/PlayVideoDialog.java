package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogPlayVideoBinding;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.NumberUtils;

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

        binding.seekTime2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    player.seekTo(i);
                    binding.txtCurrentTime2.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (player.isPlaying()) {
                    handler.removeCallbacks(updateTime);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.isPlaying()) {
                    handler.post(updateTime);
                }
            }
        });

        binding.btnPauseOrResume2.setOnClickListener(view -> {
            if (player.isPlaying()) {
                pauseVideo();
            } else {
                resumeVideo();
            }
        });

        binding.btnClose2.setOnClickListener(view -> {
            stop();
            player.stop();
            player.release();
            cancel();
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
