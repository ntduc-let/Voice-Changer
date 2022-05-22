package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.dialog.MoreOptionDialog.SELECT_AUDIO;
import static com.prox.voicechanger.ui.dialog.TextToVoiceDialog.IMPORT_TEXT;
import static com.prox.voicechanger.utils.PermissionUtils.REQUEST_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.prox.voicechanger.R;
import com.prox.voicechanger.VoiceChangerApp;
import com.prox.voicechanger.databinding.ActivityRecordBinding;
import com.prox.voicechanger.databinding.DialogLoading2Binding;
import com.prox.voicechanger.databinding.DialogRateBinding;
import com.prox.voicechanger.ui.dialog.LoadingDialog;
import com.prox.voicechanger.ui.dialog.RateDialog;
import com.prox.voicechanger.ui.dialog.TextToVoiceDialog;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.PermissionUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;
import com.proxglobal.proxads.adsv2.callback.AdsCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RecordActivity extends AppCompatActivity {
    public static final String IMPORT_TO_CHANGE_VOICE = "IMPORT_TO_CHANGE_VOICE";
    public static final String IMPORT_TEXT_TO_SPEECH = "IMPORT_TEXT_TO_SPEECH";
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private TextToSpeech mTts;
    private FileVoiceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "RecordActivity: onCreate");
        super.onCreate(savedInstanceState);
        ActivityRecordBinding binding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.isExecuteText().observe(this, isExecute -> {
            if (isExecute){
                VoiceChangerApp.instance.showInterstitial(RecordActivity.this, "interstitial_text", new AdsCallback() {
                    @Override
                    public void onClosed() {
                        super.onClosed();
                        Intent goToChangeVoice = new Intent(RecordActivity.this, ChangeVoiceActivity.class);
                        goToChangeVoice.setAction(IMPORT_TEXT_TO_SPEECH);
                        goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, FileUtils.getTempTextToSpeechFilePath(RecordActivity.this));
                        startActivity(goToChangeVoice);
                        overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                        Log.d(TAG, "RecordActivity: To ChangeVoiceActivity");
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        Intent goToChangeVoice = new Intent(RecordActivity.this, ChangeVoiceActivity.class);
                        goToChangeVoice.setAction(IMPORT_TEXT_TO_SPEECH);
                        goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, FileUtils.getTempTextToSpeechFilePath(RecordActivity.this));
                        startActivity(goToChangeVoice);
                        overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                        Log.d(TAG, "RecordActivity: To ChangeVoiceActivity");
                    }
                });
            }else {
                Toast.makeText(RecordActivity.this, R.string.process_error, Toast.LENGTH_SHORT).show();
            }
        });

        init();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "RecordActivity: onDestroy");
        appBarConfiguration = null;
        navController = null;
        if (mTts != null) {
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController == null || appBarConfiguration == null) {
            return super.onSupportNavigateUp();
        } else {
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
    }

    private void init() {
        Log.d(TAG, "RecordActivity: init");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.background_app));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_record_activity);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            Log.d(TAG, "RecordActivity: navHostFragment null");
            recreate();
            return;
        }

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        PermissionUtils.checkPermission(this, this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int open_app = preferences.getInt("open_app", 1);
        if (open_app >= 2){
            RateDialog dialog = new RateDialog(this, DialogRateBinding.inflate(getLayoutInflater()), () -> {});
            dialog.show();
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
//                if (navController == null){
//                    Log.d(TAG, "RecordActivity: navController null");
//                }else {
//                    navController.navigate(R.id.action_recordFragment_to_stopRecordFragment);
//                    Log.d(TAG, "RecordActivity: To StopRecordFragment");
//                }
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
//                if (navController == null){
//                    Log.d(TAG, "RecordActivity: navController null");
//                }else {
//                    navController.navigate(R.id.action_recordFragment_to_stopRecordFragment);
//                    Log.d(TAG, "RecordActivity: To StopRecordFragment");
//                }
            } else {
                PermissionUtils.openDialogAccessAllFile(this);
            }
        } else if (requestCode == SELECT_AUDIO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Log.d(TAG, "RecordActivity: data null");
                } else {
                    String filePath = FileUtils.getRealPath(this, data.getData());
                    if (filePath.isEmpty()) {
                        Log.d(TAG, "RecordActivity: filePath isEmpty");
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
                    } else if (!(new File(filePath).exists())) {
                        Log.d(TAG, "RecordActivity: filePath not exists");
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "RecordActivity: filePath " + filePath);

                        Intent goToChangeVoice = new Intent(this, ChangeVoiceActivity.class);
                        goToChangeVoice.setAction(IMPORT_TO_CHANGE_VOICE);
                        goToChangeVoice.putExtra(ChangeVoiceActivity.PATH_FILE, filePath);
                        startActivity(goToChangeVoice);
                        overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
                        Log.d(TAG, "RecordActivity: To ChangeVoiceActivity");
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == IMPORT_TEXT) {
            VoiceChangerApp.instance.showInterstitial(RecordActivity.this, "interstitial_text", new AdsCallback() {
                @Override
                public void onClosed() {
                    super.onClosed();

                }

                @Override
                public void onError() {
                    super.onError();

                }
            });
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                LoadingDialog dialog = new LoadingDialog(
                        this,
                        DialogLoading2Binding.inflate(getLayoutInflater())
                );
                dialog.show();

                mTts = new TextToSpeech(this, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {
                                Log.d(TAG, "onStart");
                            }

                            @Override
                            public void onDone(String s) {
                                Log.d(TAG, "onDone");
                                model.setExecuteText(true);
                                dialog.cancel();
                                Log.d(TAG, "RecordActivity: To ChangeVoiceActivity");
                            }

                            @Override
                            public void onError(String s) {
                                Log.d(TAG, "onError");
                                model.setExecuteText(false);
                                dialog.cancel();
                            }
                        });

                        mTts.setLanguage(Locale.US);

                        if (TextToVoiceDialog.textToSpeech.isEmpty()) {
                            Toast.makeText(this, R.string.process_error, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        HashMap<String, String> params = new HashMap<>();
                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TextToVoiceDialog.textToSpeech);

                        Log.d(TAG, "textToSpeech: " + TextToVoiceDialog.textToSpeech);
                        mTts.synthesizeToFile(TextToVoiceDialog.textToSpeech, params, FileUtils.getTempTextToSpeechFilePath(this));
                    }
                });
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
                overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
            }
        }
    }
}