package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prox.voicechanger.R;
import com.prox.voicechanger.adapter.FileVideoAdapter;
import com.prox.voicechanger.databinding.ActivityFileVideoBinding;
import com.prox.voicechanger.databinding.DialogDeleteAllBinding;
import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.ui.dialog.DeleteAllDialog;
import com.prox.voicechanger.viewmodel.FileVoiceViewModel;

import java.io.File;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileVideoActivity extends AppCompatActivity {
    private ActivityFileVideoBinding binding;
    private FileVideoAdapter adapter;
    private FileVoiceViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ViewModelProvider(this).get(FileVoiceViewModel.class);
        model.getFileVideos().observe(this, fileVideos -> {
            if (fileVideos == null){
                fileVideos = new ArrayList<>();
            }
            if (fileVideos.size()==0){
                binding.layoutNoItem.setVisibility(View.VISIBLE);
                binding.btnDeleteAll.setEnabled(false);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white30));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button6);
            }else{
                binding.layoutNoItem.setVisibility(View.GONE);
                binding.btnDeleteAll.setEnabled(true);
                binding.btnDeleteAll.setTextColor(getResources().getColor(R.color.white));
                binding.btnDeleteAll.setBackgroundResource(R.drawable.bg_button1);
            }
            adapter.setFileVideos(fileVideos);
        });

        init();

        binding.btnBack3.setOnClickListener(view -> onBackPressed());

        binding.btnDeleteAll.setOnClickListener(view -> {
            DeleteAllDialog dialog = new DeleteAllDialog(this,
                    DialogDeleteAllBinding.inflate(getLayoutInflater()),
                    model,
                    adapter.getFileVideos());
            dialog.show();
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "FileVideoActivity: onResume");
        super.onResume();

        for (FileVoice fileVoice : adapter.getFileVideos()){
            if (!(new File(fileVoice.getPath()).exists())){
                model.delete(fileVoice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "FileVideoActivity: onDestroy");
        adapter = null;
        model = null;
        binding = null;
        super.onDestroy();
    }

    private void init(){
        Log.d(TAG, "FileVideoActivity: init");
        adapter = new FileVideoAdapter( this, this, model);
        binding.recyclerViewFileVideo.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerViewFileVideo.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerHorizontal = new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL);
        binding.recyclerViewFileVideo.addItemDecoration(dividerHorizontal);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "FileVideoActivity: onBackPressed");
        finish();
        overridePendingTransition(R.anim.anim_left_right_1, R.anim.anim_left_right_2);
    }
}