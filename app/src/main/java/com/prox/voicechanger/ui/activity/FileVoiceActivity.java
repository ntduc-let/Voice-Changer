package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prox.voicechanger.adapter.FileVoiceAdapter;
import com.prox.voicechanger.databinding.ActivityFileVoiceBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileVoiceActivity extends AppCompatActivity {
    private ActivityFileVoiceBinding binding;
    private FileVoiceAdapter adapter;
    private FileVoiceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "FileVoiceActivity: onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityFileVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getFileVoices().observe(this, fileVoices -> {
            if (fileVoices.size()==0){
                binding.txtNoItem.setVisibility(View.VISIBLE);
            }else{
                binding.txtNoItem.setVisibility(View.GONE);
            }
            adapter.setFileVoices(fileVoices);
        });

        init();

        binding.btnBack3.setOnClickListener(view -> onBackPressed());

        binding.btnDeleteAll.setOnClickListener(view -> {
            for (FileVoice fileVoice : adapter.getFileVoices()){
                if (FileUtils.deleteFile(this, fileVoice.getPath())){
                    model.delete(fileVoice);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "FileVoiceActivity: onDestroy");
        adapter = null;
        model = null;
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void init(){
        Log.d(TAG, "FileVoiceActivity: init");
        adapter = new FileVoiceAdapter();
        binding.recyclerViewFileVoice.setAdapter(adapter);
        binding.recyclerViewFileVoice.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerViewFileVoice.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerHorizontal = new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL);
        binding.recyclerViewFileVoice.addItemDecoration(dividerHorizontal);
    }
}