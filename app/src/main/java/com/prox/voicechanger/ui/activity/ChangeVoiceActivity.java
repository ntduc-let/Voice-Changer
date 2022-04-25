package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.dialog.NameDialog.PATH_FILE;
import static com.prox.voicechanger.ui.dialog.NameDialog.RECORD_TO_CHANGE_VOICE;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.prox.voicechanger.R;
import com.prox.voicechanger.adapter.EffectAdapter;
import com.prox.voicechanger.databinding.ActivityChangeVoiceBinding;
import com.prox.voicechanger.databinding.DialogLoadingBinding;
import com.prox.voicechanger.model.Effect;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.LoadingDialog;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.NumberUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChangeVoiceActivity extends AppCompatActivity {
    private ActivityChangeVoiceBinding binding;
    private MediaPlayer player;
    private EffectAdapter effectAdapter;
    private String pathRecording;
    private String pathFFMPEG;
    private Effect effectSelected;
    private FileVoiceViewModel model;

    private Handler handler = new Handler();
    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            binding.layoutPlayer.txtCurrentTime.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
            handler.postDelayed(this, 1000);
        }
    };
    private Runnable selectEffect = new Runnable() {
        @Override
        public void run() {
            String cmd = FFMPEGUtils.getCMDAddEffect(pathRecording, FileUtils.getTempEffectFilePath(ChangeVoiceActivity.this), effectSelected);
            if (FFMPEGUtils.executeFFMPEG(cmd)){
                binding.layoutPlayer.getRoot().setVisibility(View.VISIBLE);
                binding.layoutLoading.getRoot().setVisibility(View.GONE);
                binding.layoutLoading.txtProcessing.setText(R.string.processing);
                binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.white30));
                binding.btnSave2.setEnabled(true);
                binding.btnSave2.setTextColor(getResources().getColor(R.color.white));
                binding.btnSave2.setBackgroundResource(R.drawable.bg_button1);
                startMediaPlayer(FileUtils.getTempEffectFilePath(ChangeVoiceActivity.this));
            }else {
                binding.layoutLoading.txtProcessing.setText(R.string.process_error);
                binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.red));
                binding.btnSave2.setEnabled(false);
                binding.btnSave2.setTextColor(getResources().getColor(R.color.white30));
                binding.btnSave2.setBackgroundResource(R.drawable.bg_button6);
            }
            handler.removeCallbacks(selectEffect);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ChangeVoiceActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityChangeVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);

        init();

        binding.btnBack2.setOnClickListener(view -> onBackPressed());

        binding.btnSave2.setOnClickListener(view -> {
            LoadingDialog dialog = new LoadingDialog(
                    this,
                    DialogLoadingBinding.inflate(getLayoutInflater())
            );
            dialog.show();

            if (pathFFMPEG != null){
                new Handler().post(() -> {
                    String cmd = FFMPEGUtils.getCMDConvertRecording(FileUtils.getTempEffectFilePath(this), pathFFMPEG);
                    if (FFMPEGUtils.executeFFMPEG(cmd)){
                        insertEffectToDB();
                        startActivity(new Intent(this, FileVoiceActivity.class));
                        finish();
                        dialog.cancel();
                    }
                });
            }else {
                startActivity(new Intent(this, FileVoiceActivity.class));
                finish();
                dialog.cancel();
            }
        });

        binding.layoutPlayer.btnPauseOrResume.setOnClickListener(view -> {
            if (player.isPlaying()){
                pauseMediaPlayer();
            }else {
                resumeMediaPlayer();
            }
        });

        binding.layoutEffect.btnEffect.setOnClickListener(view -> {
            initClickBtnEffect();
            startMediaPlayer(pathRecording);
        });

        binding.layoutEffect.btnCustom.setOnClickListener(view -> {
            initClickBtnCustom();
            effectAdapter.resetEffects();
            startMediaPlayer(pathRecording);
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "ChangeVoiceActivity: onDestroy");
        stopMediaPlayer();
        player.release();
        updateTime = null;
        selectEffect = null;
        handler = null;
        player = null;
        effectAdapter = null;
        pathRecording = null;
        pathFFMPEG = null;
        model = null;
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "ChangeVoiceActivity: onBackPressed");
        stopMediaPlayer();

        Intent intent = new Intent(this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void init() {
        Log.d(TAG, "ChangeVoiceActivity: init");
        Intent intent = getIntent();
        if (intent == null) {
            Log.d(TAG, "ChangeVoiceActivity: start intent null");
            return;
        }
        if (intent.getAction().equals(RECORD_TO_CHANGE_VOICE)) {
            pathRecording = intent.getStringExtra(PATH_FILE);
            binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathRecording));

            player = new MediaPlayer();
            startMediaPlayer(pathRecording);

            insertOriginToDB();
        }

        effectAdapter = new EffectAdapter(this::selectEffect);

        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        binding.layoutEffect.recyclerViewEffects.setLayoutManager(flexboxLayoutManager);
        binding.layoutEffect.recyclerViewEffects.setAdapter(effectAdapter);
        effectAdapter.setEffects(FFMPEGUtils.getEffects());
    }

    private void initClickBtnCustom() {
        binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_disable);
        binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_enable);
        binding.layoutEffect.recyclerViewEffects.setVisibility(View.GONE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.VISIBLE);
    }

    private void initClickBtnEffect() {
        binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_enable);
        binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_disable);
        binding.layoutEffect.recyclerViewEffects.setVisibility(View.VISIBLE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.GONE);
    }

    private void selectEffect(Effect effect) {
        Log.d(TAG, "ChangeVoiceActivity: selectEffect "+effect.getTitle());
        effectSelected = effect;
        stopMediaPlayer();

        if (effect.getTitle().equals(FFMPEGUtils.Original)) {
            binding.layoutPlayer.getRoot().setVisibility(View.VISIBLE);
            binding.layoutLoading.getRoot().setVisibility(View.GONE);
            binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathRecording));
            binding.btnSave2.setEnabled(true);
            binding.btnSave2.setTextColor(getResources().getColor(R.color.white));
            binding.btnSave2.setBackgroundResource(R.drawable.bg_button1);
            startMediaPlayer(pathRecording);
            pathFFMPEG = null;
        }else {
            binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);
            binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            File file;
            String name;
            int i = 1;
            do{
                name = FileUtils.getName(pathRecording)+effect.getTitle()+i+"."+FileUtils.getType(pathRecording);
                i++;
                file = new File(FileUtils.getDownloadFolderPath("VoiceChanger") + "/" + name);
            }while (file.exists());
            pathFFMPEG = file.getPath();
            binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathFFMPEG));
            handler.post(selectEffect);
        }
    }

    private void startMediaPlayer(String path) {
        Log.d(TAG, "ChangeVoiceActivity: startMediaPlayer "+path);
        try {
            player.reset();
            player.setDataSource(path);
            player.setLooping(true);
            player.prepare();
            player.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.start();
                binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause);
            });
        } catch (IOException e) {
            Log.d(TAG, "startMediaPlayer: "+e.getMessage());
        }
        updateTime();
    }

    private void stopMediaPlayer() {
        Log.d(TAG, "ChangeVoiceActivity: stopMediaPlayer");
        player.stop();
        handler.removeCallbacks(updateTime);

        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume);
    }

    private void pauseMediaPlayer() {
        Log.d(TAG, "ChangeVoiceActivity: pauseMediaPlayer");
        player.pause();
        handler.removeCallbacks(updateTime);

        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume);
    }

    private void resumeMediaPlayer() {
        Log.d(TAG, "ChangeVoiceActivity: resumeMediaPlayer");
        player.start();
        updateTime();

        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause);
    }

    private void updateTime(){
        Log.d(TAG, "ChangeVoiceActivity: updateTime");
        binding.layoutPlayer.txtTotalTime.setText(NumberUtils.formatAsTime(player.getDuration()));
        handler.post(updateTime);
    }

    private void insertOriginToDB() {
        FileVoice fileVoice = new FileVoice();
        fileVoice.setSrc(R.drawable.ic_original);
        fileVoice.setName(FileUtils.getName(pathRecording));
        fileVoice.setPath(pathRecording);
        fileVoice.setDuration(player.getDuration());
        fileVoice.setSize(new File(pathRecording).length());
        fileVoice.setDate(new Date().getTime());
        model.insert(fileVoice);
    }

    private void insertEffectToDB() {
        FileVoice fileVoice = new FileVoice();
        fileVoice.setSrc(effectSelected.getSrc());
        fileVoice.setName(FileUtils.getName(pathFFMPEG));
        fileVoice.setPath(pathFFMPEG);

        MediaPlayer playerEffect = new MediaPlayer();
        try {
            playerEffect.setDataSource(pathFFMPEG);
            playerEffect.prepare();
        } catch (IOException e) {
            Log.d(TAG, "insertEffectToDB: "+e.getMessage());
            return;
        }
        fileVoice.setDuration(playerEffect.getDuration());
        fileVoice.setSize(new File(pathFFMPEG).length());
        fileVoice.setDate(new Date().getTime());
        model.insert(fileVoice);
    }
}