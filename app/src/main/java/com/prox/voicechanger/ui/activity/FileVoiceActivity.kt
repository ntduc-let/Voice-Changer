package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.FOLDER_APP;
import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.dialog.OptionDialog.SELECT_IMAGE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prox.voicechanger.R;
import com.prox.voicechanger.adapter.FileVoiceAdapter;
import com.prox.voicechanger.databinding.ActivityFileVoiceBinding;
import com.prox.voicechanger.databinding.DialogDeleteAllBinding;
import com.prox.voicechanger.databinding.DialogLoading2Binding;
import com.prox.voicechanger.databinding.DialogPlayVideoBinding;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.DeleteAllDialog;
import com.prox.voicechanger.ui.dialog.LoadingDialog;
import com.prox.voicechanger.ui.dialog.OptionDialog;
import com.prox.voicechanger.ui.dialog.PlayVideoDialog;
import com.prox.voicechanger.utils.ConvertersUtils;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileVoiceActivity extends AppCompatActivity {
    public static final String FILE_TO_RECORD = "FILE_TO_RECORD";

    private ActivityFileVoiceBinding binding;
    private FileVoiceAdapter adapter;
    private FileVoiceViewModel model;
    private String pathVideo;
    private PlayVideoDialog playVideoDialog;

    private final Runnable insertVideoToDB = () -> {
        FileVoice fileVideo = new FileVoice();
        fileVideo.setSrc(OptionDialog.fileVoice.getSrc());
        fileVideo.setName(FileUtils.getName(pathVideo));
        fileVideo.setPath(pathVideo);

        Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getTempImagePath(this));
        fileVideo.setImage(ConvertersUtils.fromBitmap(bitmap));

        MediaPlayer playerVideo = new MediaPlayer();
        try {
            playerVideo.setDataSource(pathVideo);
            playerVideo.prepare();
        } catch (IOException e) {
            Log.d(TAG, "insertVideoToDB: " + e.getMessage());
            return;
        }
        fileVideo.setDuration(playerVideo.getDuration());
        fileVideo.setSize(new File(pathVideo).length());
        fileVideo.setDate(new Date().getTime());
        model.insertVideoBG(fileVideo);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "FileVoiceActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityFileVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getFileVoices().observe(this, fileVoices -> {
            if (fileVoices == null) {
                fileVoices = new ArrayList<>();
            }
            if (fileVoices.size() == 0) {
                binding.layoutNoItem.getRoot().setVisibility(View.VISIBLE);
                binding.btnDeleteAll.setEnabled(false);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white30));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button6);
            } else {
                binding.layoutNoItem.getRoot().setVisibility(View.GONE);
                binding.btnDeleteAll.setEnabled(true);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button1);
            }
            adapter.setFileVoices(fileVoices);
            binding.recyclerViewFileVoice.setItemViewCacheSize(fileVoices.size());
        });
        model.isExecuteAddImage().observe(this, execute -> {
            if (execute) {
                new Handler().post(insertVideoToDB);

                playVideoDialog = new PlayVideoDialog(
                        FileVoiceActivity.this,
                        DialogPlayVideoBinding.inflate(getLayoutInflater()),
                        pathVideo
                );
                playVideoDialog.show();
                Toast.makeText(this, "Save: " + pathVideo, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.video_creation_failed, Toast.LENGTH_SHORT).show();
            }
        });

        init();

        binding.btnBack3.setOnClickListener(view -> onBackPressed());

        binding.btnDeleteAll.setOnClickListener(view -> {
            adapter.release();
            DeleteAllDialog dialog = new DeleteAllDialog(this,
                    DialogDeleteAllBinding.inflate(getLayoutInflater()),
                    model,
                    adapter.getFileVoices());
            dialog.show();
        });

        binding.layoutNoItem.btnRecordNow.setOnClickListener(view -> goToRecord());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "FileVoiceActivity: onResume");
        super.onResume();

        boolean isDeleted = false;
        for (FileVoice fileVoice : adapter.getFileVoices()) {
            if (!(new File(fileVoice.getPath()).exists())) {
                model.delete(fileVoice);
                if (!isDeleted) {
                    isDeleted = true;
                }
            }
        }
        if (isDeleted) {
            recreate();
        } else {
            adapter.resume();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "FileVoiceActivity: onStop");
        super.onStop();
        adapter.pause();
        if (playVideoDialog != null) {
            playVideoDialog.stop();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "FileVoiceActivity: onDestroy");
        adapter.release();
        adapter = null;
        model = null;
        binding = null;
        super.onDestroy();
    }

    private void goToRecord() {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setAction(FILE_TO_RECORD);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2);
        finish();
    }

    private void init() {
        Log.d(TAG, "FileVoiceActivity: init");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.background_app));

        adapter = new FileVoiceAdapter(this, this, model);
        binding.recyclerViewFileVoice.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerViewFileVoice.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerHorizontal = new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL);
        binding.recyclerViewFileVoice.addItemDecoration(dividerHorizontal);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (!(new File(OptionDialog.fileVoice.getPath()).exists())) {
                    Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data == null) {
                    Log.d(TAG, "FileVoiceActivity: data null");
                    Toast.makeText(this, R.string.process_error, Toast.LENGTH_SHORT).show();
                } else {
                    String pathImage = FileUtils.getRealPath(this, data.getData());
                    if (pathImage.isEmpty()) {
                        Log.d(TAG, "FileVoiceActivity: pathImage isEmpty");
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
                    } else if (!(new File(pathImage).exists())) {
                        Log.d(TAG, "FileVoiceActivity: pathImage not exists");
                        Toast.makeText(this, R.string.file_not_exist, Toast.LENGTH_SHORT).show();
                    } else {
                        LoadingDialog dialog = new LoadingDialog(
                                this,
                                DialogLoading2Binding.inflate(getLayoutInflater())
                        );
                        dialog.show();

                        pathVideo = FileUtils.getDCIMFolderPath(FOLDER_APP) + "/" + FileUtils.getVideoFileName();
                        String cmdConvertImage = FFMPEGUtils.getCMDConvertImage(pathImage, FileUtils.getTempImagePath(this));
                        FFMPEGUtils.executeFFMPEG(cmdConvertImage, new FFmpegExecuteCallback() {
                            @Override
                            public void onSuccess() {
                                String cmd = FFMPEGUtils.getCMDAddImage(OptionDialog.fileVoice.getPath(), FileUtils.getTempImagePath(FileVoiceActivity.this), pathVideo);
                                FFMPEGUtils.executeFFMPEG(cmd, new FFmpegExecuteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        dialog.cancel();
                                        model.setExecuteAddImage(true);
                                    }

                                    @Override
                                    public void onFailed() {
                                        dialog.cancel();
                                        model.setExecuteAddImage(false);
                                    }
                                });
                            }

                            @Override
                            public void onFailed() {
                                dialog.cancel();
                            }
                        });
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "FileVoiceActivity: onBackPressed");
        adapter.release();
        finish();
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2);
    }
}