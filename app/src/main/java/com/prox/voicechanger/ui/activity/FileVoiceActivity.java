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

import com.prox.voicechanger.R;
import com.prox.voicechanger.adapter.FileVoiceAdapter;
import com.prox.voicechanger.databinding.ActivityFileVoiceBinding;
import com.prox.voicechanger.databinding.DialogDeleteAllBinding;
import com.prox.voicechanger.ui.dialog.DeleteAllDialog;
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
        });

        init();

        binding.btnBack3.setOnClickListener(view -> onBackPressed());

        binding.btnDeleteAll.setOnClickListener(view -> {
            DeleteAllDialog dialog = new DeleteAllDialog(this,
                    DialogDeleteAllBinding.inflate(getLayoutInflater()),
                    model,
                    adapter.getFileVoices());
            dialog.show();
        });

        binding.layoutNoItem.btnRecordNow.setOnClickListener(view -> onBackPressed());
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
        adapter = new FileVoiceAdapter( this, this, model);
        binding.recyclerViewFileVoice.setAdapter(adapter);
        binding.recyclerViewFileVoice.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerViewFileVoice.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerHorizontal = new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL);
        binding.recyclerViewFileVoice.addItemDecoration(dividerHorizontal);
    }
}