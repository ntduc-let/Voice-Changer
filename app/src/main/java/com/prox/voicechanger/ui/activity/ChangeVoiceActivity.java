package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.activity.SplashActivity.SPLASH_TO_CHANGE_VOICE;
import static com.prox.voicechanger.ui.dialog.NameDialog.PATH_FILE;
import static com.prox.voicechanger.ui.dialog.NameDialog.RECORD_TO_CHANGE_VOICE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.Slider;
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
    private String hzSelect;
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
    private Runnable selectCustom = new Runnable() {
        @Override
        public void run() {
            String cmd = FFMPEGUtils.getCMDCustomEffect(
                    pathRecording,
                    FileUtils.getTempEffectFilePath(ChangeVoiceActivity.this),
                    binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.getValue()/16000,
                    binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.getValue(),
                    binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.getValue(),
                    Double.parseDouble(hzSelect),
                    binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue(),
                    binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue(),
                    binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.getValue(),
                    binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.getValue(),
                    binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue()==0?1:binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue(),
                    binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue()==0?0.01:binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue());
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
            handler.removeCallbacks(selectCustom);
        }
    };
    private final RadioGroup.OnCheckedChangeListener RadioGroupOnCheckedChangeListener = (radioGroup, i) -> {
        stopMediaPlayer();
        binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);
        binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        if (binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked()
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue() == 100
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue() == 0){
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
        }else {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(true);
        }
        handler.post(selectCustom);
    };
    private final CompoundButton.OnCheckedChangeListener RadioButtonOnCheckedChangeListener = (compoundButton, b) -> {
        if (b){
            compoundButton.setTextColor(getResources().getColor(R.color.black));
            hzSelect = compoundButton.getText().toString().substring(0, compoundButton.getText().length()-2);
        }else{
            compoundButton.setTextColor(getResources().getColor(R.color.black30));
        }
    };
    private  final Slider.OnSliderTouchListener onSliderTouchListener = new Slider.OnSliderTouchListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {

        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            stopMediaPlayer();
            binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);
            binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            enableReset();
            handler.post(selectCustom);
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
            effectAdapter.resetEffects();
            stopMediaPlayer();
            startMediaPlayer(pathRecording);
        });

        binding.layoutEffect.btnCustom.setOnClickListener(view -> {
            initClickBtnCustom();
            stopMediaPlayer();
            binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);
            binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            handler.post(selectCustom);
            actionCustomEffect();
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "ChangeVoiceActivity: onDestroy");
        if (player != null){
            if (player.isPlaying()){
                stopMediaPlayer();
            }
            player.release();
        }

        updateTime = null;
        selectEffect = null;
        selectCustom = null;
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
        if (player != null){
            if (player.isPlaying()){
                stopMediaPlayer();
            }
        }

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
        }else if (intent.getAction().equals(SPLASH_TO_CHANGE_VOICE)){
            String path = intent.getStringExtra(PATH_FILE);
            pathRecording = FileUtils.getDownloadFolderPath("VoiceChanger") + "/" + FileUtils.getName(path)+"."+FileUtils.getType(path);
            binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathRecording));
            binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);
            binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);

            new Handler().post(() -> {
                String cmd = FFMPEGUtils.getCMDConvertRecording(path, pathRecording);
                if (FFMPEGUtils.executeFFMPEG(cmd)){
                    binding.layoutPlayer.getRoot().setVisibility(View.VISIBLE);
                    binding.layoutLoading.getRoot().setVisibility(View.GONE);
                    binding.layoutLoading.txtProcessing.setText(R.string.processing);
                    binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.white30));
                    binding.btnSave2.setEnabled(true);
                    binding.btnSave2.setTextColor(getResources().getColor(R.color.white));
                    binding.btnSave2.setBackgroundResource(R.drawable.bg_button1);
                    player = new MediaPlayer();
                    startMediaPlayer(pathRecording);
                    insertOriginToDB();
                }else {
                    binding.layoutLoading.txtProcessing.setText(R.string.process_error);
                    binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.red));
                    binding.btnSave2.setEnabled(false);
                    binding.btnSave2.setTextColor(getResources().getColor(R.color.white30));
                    binding.btnSave2.setBackgroundResource(R.drawable.bg_button6);
                }
            });
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
        binding.layoutEffect.btnEffect.setEnabled(true);
        binding.layoutEffect.btnCustom.setEnabled(false);
        binding.layoutEffect.recyclerViewEffects.setVisibility(View.GONE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.VISIBLE);
        effectSelected = FFMPEGUtils.getEffectCustom();

        File file;
        String name;
        int i = 1;
        do{
            name = FileUtils.getName(pathRecording)+"Custom"+i+"."+FileUtils.getType(pathRecording);
            i++;
            file = new File(FileUtils.getDownloadFolderPath("VoiceChanger") + "/" + name);
        }while (file.exists());
        pathFFMPEG = file.getPath();
        binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathFFMPEG));
    }

    private void initClickBtnEffect() {
        binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_enable);
        binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_disable);
        binding.layoutEffect.btnEffect.setEnabled(false);
        binding.layoutEffect.btnCustom.setEnabled(true);
        binding.layoutEffect.recyclerViewEffects.setVisibility(View.VISIBLE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.GONE);

        binding.layoutPlayer.txtName2.setText(FileUtils.getName(pathRecording));
        pathFFMPEG = null;
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

    private void actionCustomEffect() {
        resetCustomEffect();

        actionCustomBasic();
        actionCustomEqualizer();
        actionCustomReverb();
    }

    private void actionCustomBasic() {
        binding.layoutEffect.layoutCustom.switchBasic.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                binding.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutBasic.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetBasic.setVisibility(View.VISIBLE);
            }else {
                binding.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutBasic.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetBasic.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
                binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
                resetCustomBasic();
                stopMediaPlayer();
                handler.post(selectCustom);
            }
        });

        binding.layoutEffect.layoutCustom.btnResetBasic.setOnClickListener(view -> {
            resetCustomBasic();
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
            stopMediaPlayer();
            handler.post(selectCustom);
        });

        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.addOnSliderTouchListener(onSliderTouchListener);
    }

    private void actionCustomEqualizer() {
        binding.layoutEffect.layoutCustom.switchEqualizer.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                binding.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutEqualizer.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setVisibility(View.VISIBLE);
            }else {
                binding.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutEqualizer.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
                resetCustomEqualizer();
                stopMediaPlayer();
                handler.post(selectCustom);
            }
        });

        binding.layoutEffect.layoutCustom.btnResetEqualizer.setOnClickListener(view -> {
            resetCustomEqualizer();
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
            stopMediaPlayer();
            handler.post(selectCustom);
        });

        checkRadio();
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.addOnSliderTouchListener(onSliderTouchListener);
    }

    private void actionCustomReverb() {
        binding.layoutEffect.layoutCustom.switchReverb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                binding.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutReverb.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetReverb.setVisibility(View.VISIBLE);
            }else {
                binding.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutReverb.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetReverb.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
                binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);
                resetCustomReverb();
                stopMediaPlayer();
                handler.post(selectCustom);
            }
        });

        binding.layoutEffect.layoutCustom.btnResetReverb.setOnClickListener(view -> {
            resetCustomReverb();
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);
            stopMediaPlayer();
            handler.post(selectCustom);
        });

        binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.addOnSliderTouchListener(onSliderTouchListener);
    }

    private void checkRadio() {
        binding.layoutEffect.layoutCustom.layoutEqualizer.radGroupHz.setOnCheckedChangeListener(RadioGroupOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio1000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio2000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio3000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio4000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio5000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio6000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio7000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio8000.setOnCheckedChangeListener(RadioButtonOnCheckedChangeListener);
    }

    private void resetCustomEffect() {
        binding.layoutEffect.layoutCustom.switchBasic.setChecked(false);
        binding.layoutEffect.layoutCustom.switchEqualizer.setChecked(false);
        binding.layoutEffect.layoutCustom.switchReverb.setChecked(false);

        binding.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_disable);
        binding.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_disable);
        binding.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_disable);

        binding.layoutEffect.layoutCustom.layoutBasic.getRoot().setVisibility(View.GONE);
        binding.layoutEffect.layoutCustom.layoutEqualizer.getRoot().setVisibility(View.GONE);
        binding.layoutEffect.layoutCustom.layoutReverb.getRoot().setVisibility(View.GONE);
        
        resetCustomBasic();
        resetCustomEqualizer();
        resetCustomReverb();
    }

    private void resetCustomBasic() {
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.setValue(16000);
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.setValue(1);
        binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.setValue(1);
    }

    private void resetCustomEqualizer() {
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.setChecked(true);
        hzSelect = "500";
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.setValue(100);
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.setValue(0);
    }

    private void resetCustomReverb() {
        binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.setValue(1);
        binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.setValue(1);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.setValue(0);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.setValue(1);
    }

    private void enableReset() {
        if (binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.getValue() == 16000
                && binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.getValue() == 1
                && binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.getValue() == 1){
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
        }else {
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(true);
        }

        if (binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked()
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue() == 100
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue() == 0){
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
        }else {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(true);
        }

        if (binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.getValue() == 1
                && binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.getValue() == 1
                && binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue() == 0
                && binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue() == 1){
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);
        }else {
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(true);
        }
    }
}