package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.FOLDER_APP;
import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.dialog.OptionDialog.SELECT_IMAGE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.prox.voicechanger.databinding.DialogLoadingBinding;
import com.prox.voicechanger.databinding.DialogPlayVideoBinding;
import com.prox.voicechanger.interfaces.FFmpegExecuteCallback;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.DeleteAllDialog;
import com.prox.voicechanger.ui.dialog.LoadingDialog;
import com.prox.voicechanger.ui.dialog.OptionDialog;
import com.prox.voicechanger.ui.dialog.PlayVideoDialog;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileVoiceActivity extends AppCompatActivity {
    public static final String PATH_VIDEO = "PATH_VIDEO";
    private ActivityFileVoiceBinding binding;
    private FileVoiceAdapter adapter;
    private FileVoiceViewModel model;
    private String pathVideo;
    private PlayVideoDialog playVideoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "FileVoiceActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityFileVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getFileVoices().observe(this, fileVoices -> {
            if (fileVoices == null || fileVoices.size()==0){
                binding.layoutNoItem.getRoot().setVisibility(View.VISIBLE);
                binding.btnDeleteAll.setEnabled(false);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white30));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button6);
            }else{
                binding.layoutNoItem.getRoot().setVisibility(View.GONE);
                binding.btnDeleteAll.setEnabled(true);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button1);
            }
            adapter.setFileVoices(fileVoices);
            if (fileVoices != null){
                binding.recyclerViewFileVoice.setItemViewCacheSize(fileVoices.size());
            }
        });
        model.isExecuteAddImage().observe(this, execute -> {
            if (execute){
                playVideoDialog = new PlayVideoDialog(
                        FileVoiceActivity.this,
                        DialogPlayVideoBinding.inflate(getLayoutInflater()),
                        pathVideo
                );
                playVideoDialog.show();
                Toast.makeText(this, "Save: "+pathVideo, Toast.LENGTH_SHORT).show();
            }else {
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
    protected void onStart() {
        Log.d(TAG, "FileVoiceActivity: onStart");
        super.onStart();
        if (adapter.getFileVoices() == null){
            return;
        }
        for (FileVoice fileVoice : adapter.getFileVoices()){
            if (!(new File(fileVoice.getPath()).exists())){
                model.delete(fileVoice);
            }
        }
        adapter.resume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "FileVoiceActivity: onStop");
        super.onStop();
        adapter.pause();
        if (playVideoDialog!=null){
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void init(){
        Log.d(TAG, "FileVoiceActivity: init");
        adapter = new FileVoiceAdapter( this, this, model);
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
                if (data != null) {
                    LoadingDialog dialog = new LoadingDialog(
                            this,
                            DialogLoadingBinding.inflate(getLayoutInflater())
                    );
                    dialog.show();

                    String pathImage = FileUtils.getFilePathForN(this, data.getData());
                    pathVideo = FileUtils.getDCIMFolderPath(FOLDER_APP) + "/"+FileUtils.getVideoFileName();
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

                        }
                    });
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "FileVoiceActivity: onBackPressed");
        adapter.release();
        finish();
    }
}