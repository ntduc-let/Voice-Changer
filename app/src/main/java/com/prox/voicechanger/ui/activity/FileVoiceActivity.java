package com.prox.voicechanger.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prox.voicechanger.adapter.FileVoiceAdapter;
import com.prox.voicechanger.databinding.ActivityFileVoiceBinding;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileVoiceActivity extends AppCompatActivity {
    private ActivityFileVoiceBinding binding;
    private FileVoiceAdapter adapter;
    private FileVoiceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileVoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getFileVoices().observe(this, fileVoices -> adapter.setFileVoices(fileVoices));

        init();

        binding.btnBack3.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void init(){
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