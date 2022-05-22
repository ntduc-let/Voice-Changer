package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.activity.RecordActivity.IMPORT_TEXT_TO_SPEECH;
import static com.prox.voicechanger.ui.dialog.NameDialog.RECORD_TO_CHANGE_VOICE;
import static com.prox.voicechanger.utils.PermissionUtils.REQUEST_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.Slider;
import com.prox.voicechanger.BuildConfig;
import com.prox.voicechanger.R;
import com.prox.voicechanger.VoiceChangerApp;
import com.prox.voicechanger.adapter.EffectAdapter;
import com.prox.voicechanger.databinding.ActivityChangeVoiceBinding;
import com.prox.voicechanger.databinding.DialogNameBinding;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.media.Player;
import com.prox.voicechanger.model.Effect;
import com.prox.voicechanger.ui.dialog.NameDialog;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.FirebaseUtils;
import com.prox.voicechanger.utils.NetworkUtils;
import com.prox.voicechanger.utils.NumberUtils;
import com.prox.voicechanger.utils.PermissionUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;
import com.proxglobal.proxads.adsv2.callback.AdsCallback;
import com.proxglobal.purchase.ProxPurchase;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;
import space.siy.waveformview.WaveFormData;
import space.siy.waveformview.WaveFormView;

@AndroidEntryPoint
public class ChangeVoiceActivity extends AppCompatActivity {
    public static final String PATH_FILE = "PATH_FILE";

    private ActivityChangeVoiceBinding binding;
    private Player player;
    private boolean isPlaying;
    private double current;
    private EffectAdapter effectAdapter;
    private String hzSelect = "500";
    private String nameFile;
    private Effect effectSelected = FFMPEGUtils.getEffects().get(0);
    private FileVoiceViewModel model;

    private final Handler handler = new Handler();
    private final Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            if (player == null) {
                Log.d(TAG, "ChangeVoiceActivity: player null");
            } else {
                current = (double) player.getCurrentPosition() / player.getDuration();
                binding.layoutPlayer.visualizer.setPosition(player.getCurrentPosition());
                binding.layoutPlayer.txtCurrentTime.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                handler.post(this);
            }
        }
    };
    private final RadioGroup.OnCheckedChangeListener RadioGroupOnCheckedChangeListener = (radioGroup, i) -> {
        if (binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked()
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue() == 100
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue() == 0) {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
        } else {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(true);
        }
    };
    private final CompoundButton.OnCheckedChangeListener RadioButtonOnCheckedChangeListener = (compoundButton, b) -> {
        if (b) {
            compoundButton.setTextColor(getResources().getColor(R.color.black));
            hzSelect = compoundButton.getText().toString().substring(0, compoundButton.getText().length() - 2);
            selectCustom();
        } else {
            compoundButton.setTextColor(getResources().getColor(R.color.black30));
        }
    };
    private final Slider.OnSliderTouchListener onSliderTouchListener = new Slider.OnSliderTouchListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {

        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            enableReset();
            selectCustom();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ChangeVoiceActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityChangeVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getPathPlayer().observe(this, path -> {
            if (path != null) {
                setNewPlayer(path);
            } else {
                binding.layoutLoading.txtProcessing.setText(R.string.process_error);
                binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.red));
                EffectAdapter.isExecuting = false;
            }
        });
        model.isExecuteConvertRecording().observe(this, execute -> {
            if (execute) {
                selectEffect(effectSelected);
            } else {
                binding.layoutLoading.txtProcessing.setText(R.string.process_error);
                binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.red));
                EffectAdapter.isExecuting = false;
            }
        });
        model.isExecuteSave().observe(this, execute -> VoiceChangerApp.instance.showInterstitial(this, "interstitial_save", new AdsCallback() {
            @Override
            public void onClosed() {
                super.onClosed();
                Log.d(TAG, "ChangeVoiceActivity Ads onClosed");
                startActivity(new Intent(ChangeVoiceActivity.this, FileVoiceActivity.class));
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                Log.d(TAG, "ChangeVoiceActivity: To FileVoiceActivity");
                if (execute){
                    Toast.makeText(ChangeVoiceActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ChangeVoiceActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError() {
                super.onError();
                Log.d(TAG, "ChangeVoiceActivity Ads onError");
                startActivity(new Intent(ChangeVoiceActivity.this, FileVoiceActivity.class));
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                Log.d(TAG, "ChangeVoiceActivity: To FileVoiceActivity");
                if (execute){
                    Toast.makeText(ChangeVoiceActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ChangeVoiceActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
                }
            }
        }));
        model.getLoading().observe(this, loading ->
                binding.layoutLoading.progressProcessing.setProgress(Math.round(loading)));
        model.isExecuteCustom().observe(this, execute -> {
            if (execute){
                setEnableCustom(false);
            }else {
                setEnableCustom(true);
            }
        });
        init();

        binding.btnBack2.setOnClickListener(view -> onBackPressed());

        binding.btnSave2.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Save");
            pausePlayer();

            String name = binding.layoutPlayer.txtName2.getText().toString();
            boolean isCustom = binding.layoutEffect.btnEffect.isEnabled();

            NameDialog dialog = new NameDialog(
                    this,
                    this,
                    DialogNameBinding.inflate(getLayoutInflater()),
                    model,
                    name,
                    isCustom,
                    effectSelected
            );
            dialog.show();
            Log.d(TAG, "ChangeVoiceActivity: Show NameDialog");
        });

        binding.layoutPlayer.visualizer.setCallback(new WaveFormView.Callback() {
            @Override
            public void onPlayPause() {

            }

            @Override
            public void onSeek(long l) {
                player.seekTo(l);
                binding.layoutPlayer.txtCurrentTime.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
            }
        });

        binding.layoutPlayer.btnPauseOrResume.setOnClickListener(view -> {
            if (isPlaying) {
                pausePlayer();
                isPlaying = false;
            } else {
                resumePlayer();
                isPlaying = true;
            }
        });

        binding.layoutEffect.btnEffect.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Effect");
            if (!binding.layoutEffect.layoutCustom.btnResetBasic.isEnabled()
                    && !binding.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled()
                    && !binding.layoutEffect.layoutCustom.btnResetReverb.isEnabled()) {

            } else {
                if (EffectAdapter.isExecuting){
                    Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show();
                    return;
                }
                EffectAdapter.isExecuting = true;
                setNewPlayer(FileUtils.getTempEffectFilePath(this));
            }
            initClickBtnEffect();
            resetCustomEffect();
        });

        binding.layoutEffect.btnCustom.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Custom");
            initClickBtnCustom();
        });

        actionCustomEffect();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "ChangeVoiceActivity: onStart");
        super.onStart();
        if (isPlaying) {
            resumePlayer();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "ChangeVoiceActivity: onStop");
        super.onStop();
        if (isPlaying) {
            pausePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "ChangeVoiceActivity: onDestroy");
        if (player != null) {
            if (player.isPlaying()) {
                stopPlayer();
            }
            player.release();
        }
        player = null;
        effectAdapter = null;
        hzSelect = null;
        nameFile = null;
        effectSelected = null;
        isPlaying = false;
        current = 0;
        model = null;
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "ChangeVoiceActivity: onBackPressed");
        if (player != null) {
            if (player.isPlaying()) {
                stopPlayer();
            }
        }

        Intent intent = new Intent(this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2);
        Log.d(TAG, "ChangeVoiceActivity: To RecordActivity");
        finish();
    }

    private void init() {
        Log.d(TAG, "ChangeVoiceActivity: init");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.background_app));

        player = new Player();
        isPlaying = true;
        binding.layoutEffect.btnEffect.setEnabled(false);
        binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
        binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
        binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);

        effectAdapter = new EffectAdapter(this, this::selectEffect);

        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        binding.layoutEffect.recyclerViewEffects.setLayoutManager(flexboxLayoutManager);
        binding.layoutEffect.recyclerViewEffects.setAdapter(effectAdapter);
        effectAdapter.setEffects(FFMPEGUtils.getEffects());

        if (PermissionUtils.checkPermission(this, this)) {
            actionIntent();
        }

        if (ProxPurchase.getInstance().checkPurchased()
                || !NetworkUtils.isNetworkAvailable(this)) {
            binding.bannerContainer.setVisibility(View.GONE);
        }

        VoiceChangerApp.instance.showBanner(
                this,
                binding.bannerContainer,
                BuildConfig.banner,
                new AdsCallback() {
                    @Override
                    public void onClosed() {
                        super.onClosed();
                        Log.d(TAG, "ChangeVoiceActivity Ads onClosed");
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        Log.d(TAG, "ChangeVoiceActivity Ads onError");
                    }
                }
        );
    }

    private void actionIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.d(TAG, "ChangeVoiceActivity: start intent null");
            goToRecord();
        } else if (intent.getAction() == null) {
            Log.d(TAG, "ChangeVoiceActivity: start action null");
            goToRecord();
        } else {
            String path = intent.getStringExtra(PATH_FILE);
            if (path == null) {
                Log.d(TAG, "ChangeVoiceActivity: path null");
                goToRecord();
                return;
            } else if (!(new File(path).exists())) {
                Log.d(TAG, "ChangeVoiceActivity: file not exist");
                goToRecord();
                return;
            }

            if (intent.getAction().equals(RECORD_TO_CHANGE_VOICE)
                    || intent.getAction().equals(IMPORT_TEXT_TO_SPEECH)) {
                nameFile = FileUtils.getName(FileUtils.getRecordingFileName());
            } else {
                nameFile = FileUtils.getName(path);
            }

            EffectAdapter.isExecuting = true;
            model.setExecuteCustom(true);

            binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);

            binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            binding.layoutLoading.txtProcessing.setText(R.string.processing);
            binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.white30));

            binding.btnSave2.setEnabled(false);
            binding.btnSave2.setTextColor(getResources().getColor(R.color.white30));
            binding.btnSave2.setBackgroundResource(R.drawable.bg_button6);

            String cmd = FFMPEGUtils.getCMDConvertRecording(path, FileUtils.getTempRecording2FilePath(this));
            FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
                @Override
                public void onSuccess() {
                    model.setExecuteConvertRecording(true);
                }

                @Override
                public void onFailed() {
                    model.setExecuteConvertRecording(false);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                actionIntent();
            } else {
                PermissionUtils.openDialogAccessAllFile(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION) {
            int record = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (record == PackageManager.PERMISSION_GRANTED
                    && write == PackageManager.PERMISSION_GRANTED
                    && read == PackageManager.PERMISSION_GRANTED) {
                actionIntent();
            } else {
                PermissionUtils.openDialogAccessAllFile(this);
            }
        }
    }

    private void initClickBtnCustom() {
        binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_disable);
        binding.layoutEffect.btnEffect.setEnabled(true);

        binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_enable);
        binding.layoutEffect.btnCustom.setEnabled(false);

        binding.layoutEffect.recyclerViewEffects.setVisibility(View.GONE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.VISIBLE);

        binding.layoutPlayer.txtName2.setText(binding.layoutPlayer.txtName2.getText() + "-Custom");
    }

    private void initClickBtnEffect() {
        binding.layoutEffect.btnEffect.setBackgroundResource(R.drawable.bg_button_enable);
        binding.layoutEffect.btnEffect.setEnabled(false);

        binding.layoutEffect.btnCustom.setBackgroundResource(R.drawable.bg_button_disable);
        binding.layoutEffect.btnCustom.setEnabled(true);

        binding.layoutEffect.recyclerViewEffects.setVisibility(View.VISIBLE);
        binding.layoutEffect.layoutCustom.getRoot().setVisibility(View.GONE);

        binding.layoutPlayer.txtName2.setText(nameFile + "-" + effectSelected.getTitle());
    }

    private void selectEffect(Effect effect) {
        Log.d(TAG, "ChangeVoiceActivity: selectEffect " + effect.getTitle());
        model.setExecuteCustom(true);
        effectSelected = effect;
        stopPlayer();

        binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);

        binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        binding.layoutLoading.txtProcessing.setText(R.string.processing);
        binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.white30));

        binding.btnSave2.setEnabled(false);
        binding.btnSave2.setTextColor(getResources().getColor(R.color.white30));
        binding.btnSave2.setBackgroundResource(R.drawable.bg_button6);

        binding.layoutPlayer.txtName2.setText(nameFile + "-" + effect.getTitle());
        String cmd = FFMPEGUtils.getCMDAddEffect(FileUtils.getTempRecording2FilePath(this), FileUtils.getTempEffectFilePath(this), effect);

        FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
            @Override
            public void onSuccess() {
                model.setPathPlayer(FileUtils.getTempEffectFilePath(ChangeVoiceActivity.this));
            }

            @Override
            public void onFailed() {
                model.setPathPlayer(null);
            }
        });
    }

    private void selectCustom() {
        if (EffectAdapter.isExecuting){
            return;
        }
        EffectAdapter.isExecuting = true;
        model.setExecuteCustom(true);

        stopPlayer();

        binding.layoutPlayer.getRoot().setVisibility(View.INVISIBLE);

        binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        binding.layoutLoading.txtProcessing.setText(R.string.processing);
        binding.layoutLoading.txtProcessing.setTextColor(getResources().getColor(R.color.white30));

        if (hzSelect == null) {
            hzSelect = "500";
        }
        double hzNumber;
        try {
            hzNumber = Double.parseDouble(hzSelect);
        } catch (Exception e) {
            Log.d(TAG, "ChangeVoiceActivity: hzNumber " + e.getMessage());
            hzNumber = 500;
        }

        String cmd = FFMPEGUtils.getCMDCustomEffect(
                FileUtils.getTempEffectFilePath(this),
                FileUtils.getTempCustomFilePath(this),
                binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.getValue() / 16000,
                binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.getValue(),
                binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.getValue(),
                hzNumber,
                binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue(),
                binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue(),
                binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.getValue(),
                binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.getValue(),
                binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue() == 0 ? 1 : binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue(),
                binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue() == 0 ? 0.01 : binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue());

        FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
            @Override
            public void onSuccess() {
                model.setPathPlayer(FileUtils.getTempCustomFilePath(ChangeVoiceActivity.this));
            }

            @Override
            public void onFailed() {
                model.setPathPlayer(null);
            }
        });
    }

    private void setNewPlayer(String path) {
        Log.d(TAG, "setNewPlayer: "+ path);
        new WaveFormData.Factory(path).build(new WaveFormData.Factory.Callback() {
            @Override
            public void onComplete(@NonNull WaveFormData waveFormData) {
                if (binding != null){
                    binding.layoutPlayer.visualizer.setData(waveFormData);

                    binding.layoutPlayer.getRoot().setVisibility(View.VISIBLE);

                    binding.layoutLoading.getRoot().setVisibility(View.GONE);

                    binding.btnSave2.setEnabled(true);
                    binding.btnSave2.setTextColor(getResources().getColor(R.color.white));
                    binding.btnSave2.setBackgroundResource(R.drawable.bg_button1);
                    if (player == null) {
                        player = new Player();
                    }
                    player.setNewPath(path);
                    startPlayer();
                    player.seekTo((long) (current * player.getDuration()));
                    binding.layoutPlayer.visualizer.setPosition(player.getCurrentPosition());
                    binding.layoutPlayer.txtCurrentTime.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));

                    if (!isPlaying) {
                        pausePlayer();
                    }
                    if (model != null){
                        model.setLoading(0f);
                        model.setExecuteCustom(false);
                    }
                    EffectAdapter.isExecuting = false;
                }
            }

            @Override
            public void onProgress(float v) {
                if (model != null){
                    model.setLoading(v);
                }
            }
        });
    }

    private void startPlayer() {
        player.start();
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause);
        updateTime();
    }

    private void stopPlayer() {
        Log.d(TAG, "ChangeVoiceActivity: stopPlayer");
        player.stop();
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume);
        handler.removeCallbacks(updateTime);
    }

    private void pausePlayer() {
        Log.d(TAG, "ChangeVoiceActivity: pausePlayer");
        player.pause();
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume);
        handler.removeCallbacks(updateTime);
    }

    private void resumePlayer() {
        Log.d(TAG, "ChangeVoiceActivity: resumePlayer");
        player.resume();
        binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause);
        updateTime();
    }

    private void updateTime() {
        Log.d(TAG, "ChangeVoiceActivity: updateTime");
        if (player == null) {
            Log.d(TAG, "ChangeVoiceActivity: player null");
        } else {
            binding.layoutPlayer.txtTotalTime.setText(NumberUtils.formatAsTime(player.getDuration()));
            handler.post(updateTime);
        }
    }

    private void actionCustomEffect() {
        actionCustomBasic();
        actionCustomEqualizer();
        actionCustomReverb();
    }

    private void actionCustomBasic() {
        binding.layoutEffect.layoutCustom.switchBasic.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Custom Basic");
                binding.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutBasic.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetBasic.setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.switchBasic.setThumbResource(R.drawable.ic_thumb2);
            } else {
                binding.layoutEffect.layoutCustom.switchBasic.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutBasic.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetBasic.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.switchBasic.setThumbResource(R.drawable.ic_thumb);
                if (binding.layoutEffect.layoutCustom.btnResetBasic.isEnabled()) {
                    binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
                    binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);

                    resetCustomBasic();
                    selectCustom();
                }
            }
        });

        binding.layoutEffect.layoutCustom.btnResetBasic.setOnClickListener(view -> {
            if (EffectAdapter.isExecuting){
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show();
                return;
            }
            resetCustomBasic();
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
            selectCustom();
        });

        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.addOnSliderTouchListener(onSliderTouchListener);
    }

    private void actionCustomEqualizer() {
        binding.layoutEffect.layoutCustom.switchEqualizer.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Custom Equalizer");
                binding.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutEqualizer.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.switchEqualizer.setThumbResource(R.drawable.ic_thumb2);
            } else {
                binding.layoutEffect.layoutCustom.switchEqualizer.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutEqualizer.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetEqualizer.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.switchEqualizer.setThumbResource(R.drawable.ic_thumb);
                if (binding.layoutEffect.layoutCustom.btnResetEqualizer.isEnabled()) {
                    binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
                    binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);

                    resetCustomEqualizer();
                    selectCustom();
                }
            }
        });

        binding.layoutEffect.layoutCustom.btnResetEqualizer.setOnClickListener(view -> {
            if (EffectAdapter.isExecuting){
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show();
                return;
            }
            resetCustomEqualizer();
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
            selectCustom();
        });

        checkRadio();
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.addOnSliderTouchListener(onSliderTouchListener);
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.addOnSliderTouchListener(onSliderTouchListener);
    }

    private void actionCustomReverb() {
        binding.layoutEffect.layoutCustom.switchReverb.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                FirebaseUtils.sendEvent(this, "Layout_Effect", "Click Custom Reverb");
                binding.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_enable);
                binding.layoutEffect.layoutCustom.layoutReverb.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.btnResetReverb.setVisibility(View.VISIBLE);
                binding.layoutEffect.layoutCustom.switchReverb.setThumbResource(R.drawable.ic_thumb2);
            } else {
                binding.layoutEffect.layoutCustom.switchReverb.setTrackResource(R.drawable.ic_track_disable);
                binding.layoutEffect.layoutCustom.layoutReverb.getRoot().setVisibility(View.GONE);
                binding.layoutEffect.layoutCustom.btnResetReverb.setVisibility(View.INVISIBLE);
                binding.layoutEffect.layoutCustom.switchReverb.setThumbResource(R.drawable.ic_thumb);
                if (binding.layoutEffect.layoutCustom.btnResetReverb.isEnabled()) {
                    binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
                    binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);

                    resetCustomReverb();
                    selectCustom();
                }
            }
        });

        binding.layoutEffect.layoutCustom.btnResetReverb.setOnClickListener(view -> {
            if (EffectAdapter.isExecuting){
                Toast.makeText(this, R.string.processing_in_progress, Toast.LENGTH_SHORT).show();
                return;
            }
            resetCustomReverb();
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);
            selectCustom();
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
                && binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.getValue() == 1) {
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(false);
        } else {
            binding.layoutEffect.layoutCustom.btnResetBasic.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(true);
        }

        if (binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.isChecked()
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.getValue() == 100
                && binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.getValue() == 0) {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(false);
        } else {
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(true);
        }

        if (binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.getValue() == 1
                && binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.getValue() == 1
                && binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.getValue() == 0
                && binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.getValue() == 1) {
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_disable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(false);
        } else {
            binding.layoutEffect.layoutCustom.btnResetReverb.setImageResource(R.drawable.ic_reset_enable);
            binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(true);
        }
    }

    private void goToRecord() {
        Intent goToRecord = new Intent(this, RecordActivity.class);
        goToRecord.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goToRecord);
        Log.d(TAG, "ChangeVoiceActivity: To RecordActivity");
        finish();
    }

    private void setEnableCustom(boolean isEnable) {
        binding.layoutEffect.layoutCustom.switchBasic.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.switchEqualizer.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.switchReverb.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetBasic.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetEqualizer.setEnabled(isEnable);
//        binding.layoutEffect.layoutCustom.btnResetReverb.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoPitch.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutBasic.seekTempoRate.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutBasic.seekPanning.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekBandwidth.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.seekGain.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutReverb.seekInGain.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutReverb.seekOutGain.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDelay.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutReverb.seekDecay.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radGroupHz.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio500.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio1000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio2000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio3000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio4000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio5000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio6000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio7000.setEnabled(isEnable);
        binding.layoutEffect.layoutCustom.layoutEqualizer.radio8000.setEnabled(isEnable);
    }
}