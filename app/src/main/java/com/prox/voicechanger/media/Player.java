package com.prox.voicechanger.media;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.media.MediaPlayer;
import android.util.Log;

import com.prox.voicechanger.interfaces.PlayerListener;

import java.io.IOException;

public class Player implements PlayerListener {
    private MediaPlayer player;

    public Player() {
        player = new MediaPlayer();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public void seekTo(long i){
        player.seekTo((int) i);
    }

    @Override
    public void start(String path) {
        Log.d(TAG, "Player: start " + path);
        if (player == null){
            Log.d(TAG, "Player: start null");
            return;
        }
        if (!player.isPlaying()){
            try {
                player.reset();
                player.setDataSource(path);
                player.setLooping(true);
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.d(TAG, "Player: start error " + e.getMessage());
            }
        }
    }

    @Override
    public void pause() {
        if (player == null){
            Log.d(TAG, "Player: pause null");
            return;
        }

        if (player.isPlaying()){
            player.pause();
        }
    }

    @Override
    public void resume() {
        if (player == null){
            Log.d(TAG, "Player: resume null");
            return;
        }

        if (!player.isPlaying()){
            player.start();
        }
    }

    @Override
    public void stop() {
        if (player == null){
            Log.d(TAG, "Player: stop null");
            return;
        }

        if (player.isPlaying()){
            player.stop();
        }
    }

    @Override
    public void release() {
        if (player == null){
            Log.d(TAG, "Player: release null");
            return;
        }
        if (player.isPlaying()){
            player.stop();
        }
        player.release();
        player = null;
        Log.d(TAG, "Recorder: release");
    }
}
