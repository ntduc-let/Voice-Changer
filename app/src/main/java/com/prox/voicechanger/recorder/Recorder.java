package com.prox.voicechanger.recorder;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.prox.voicechanger.interfaces.RecorderListener;
import com.prox.voicechanger.utils.FileUtils;

import java.io.IOException;

public class Recorder implements RecorderListener {
    private static final int SAMPLING_RATE_IN_HZ = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_COUNT = 1;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BIT_PER_SAMPLE = 16;
    private static final long BYTE_RATE = (long)(BIT_PER_SAMPLE * SAMPLING_RATE_IN_HZ * CHANNEL_COUNT / 8);
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

    private MediaRecorder recorder;
    private String path;
    private String name;
    private long startTime;
    private boolean isRecording;

    public Recorder() {
        path = FileUtils.getRecordingFilePath();
        name = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
        Log.d(TAG, "Recorder: "+path);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(path);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        startTime = 0;
        isRecording = false;
    }

    @Override
    public void start() {
        if (recorder==null){
            return;
        }
        try {
            recorder.prepare();
            recorder.start();
            startTime = System.currentTimeMillis();
            isRecording = true;
            Log.d(TAG, "Recorder: start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (recorder==null){
            return;
        }
        recorder.stop();
        recorder.release();
        startTime = 0;
        isRecording = false;
        Log.d(TAG, "Recorder: stop");
    }

    @Override
    public void release() {
        if (recorder==null){
            return;
        }
        recorder.release();
        recorder = null;
        path = null;
        name = null;
        startTime = 0;
        isRecording = false;
        Log.d(TAG, "Recorder: release");
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() - startTime;
    }

    public int getTickDuration() {
        return (int)((double)BUFFER_SIZE * 500 / (BYTE_RATE));
    }

    public int getMaxAmplitude() {
        return recorder.getMaxAmplitude();
    }

    public String getPath() {
        return path;
    }
    public String getName() {
        return name;
    }
}
