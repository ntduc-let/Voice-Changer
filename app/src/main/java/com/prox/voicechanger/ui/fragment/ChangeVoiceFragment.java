package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.ui.dialog.NameDialog.PATH_FILE;
import static com.prox.voicechanger.ui.dialog.NameDialog.RECORD_TO_CHANGE_VOICE;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.prox.voicechanger.R;
import com.prox.voicechanger.adapter.EffectAdapter;
import com.prox.voicechanger.databinding.FragmentChangeVoiceBinding;
import com.prox.voicechanger.utils.FFMPEGUtils;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.NumberUtils;

import java.io.IOException;

public class ChangeVoiceFragment extends Fragment {
    private FragmentChangeVoiceBinding binding;
    private MediaPlayer player;
    private String pathRecording;
    private String pathFFMPEG;

    private Handler handler;
    private Runnable updateTime;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangeVoiceBinding.inflate(inflater, container, false);

        init();

        binding.btnBack2.setOnClickListener(view -> {
            requireActivity().onBackPressed();
            if (pathFFMPEG!=null){
                FileUtils.deleteFile(requireContext(), pathFFMPEG);
                pathFFMPEG = null;
            }
        });

        binding.layoutEffect.btnEffect.setOnClickListener(view -> {
            initClickBtnEffect();
        });

        binding.layoutEffect.btnCustom.setOnClickListener(view -> {
            initClickBtnCustom();
            if (pathFFMPEG!=null){
                FileUtils.deleteFile(requireContext(), pathFFMPEG);
                pathFFMPEG = null;
            }
        });

        return binding.getRoot();
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

    private void init() {
        Intent intent = requireActivity().getIntent();
        if (intent == null) {
            return;
        }
        if (intent.getAction().equals(RECORD_TO_CHANGE_VOICE)) {
            pathRecording = intent.getStringExtra(PATH_FILE);
            try {
                player = new MediaPlayer();
                player.setDataSource(pathRecording);
                player.setLooping(true);
                player.prepare();
                player.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.start();
                    binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_pause);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateTime();
        }

        EffectAdapter effectAdapter = new EffectAdapter(effect -> {
            player.stop();
            handler.removeCallbacks(updateTime);
            binding.layoutPlayer.btnPauseOrResume.setImageResource(R.drawable.ic_resume);
            if (pathFFMPEG!=null){
                FileUtils.deleteFile(requireContext(), pathFFMPEG);
                pathFFMPEG = null;
            }
            String path;
            if (effect.getTitle().equals(FFMPEGUtils.Original)) {
                path = pathRecording;
            }else {
                FFMPEGUtils.playEffect(pathRecording, effect);
                pathFFMPEG = FFMPEGUtils.getPathFFMPEG();
                if (pathFFMPEG==null) {
                    Toast.makeText(requireContext(), R.string.add_effect_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                path = pathFFMPEG;
            }
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
                e.printStackTrace();
            }
            updateTime();
        });
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getContext());
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);

        binding.layoutEffect.recyclerViewEffects.setLayoutManager(flexboxLayoutManager);
        binding.layoutEffect.recyclerViewEffects.setAdapter(effectAdapter);
        effectAdapter.setEffects(FFMPEGUtils.getEffects());

        initClickBtnEffect();
    }

    private void updateTime(){
        binding.layoutPlayer.txtTotalTime.setText(NumberUtils.formatAsTime(player.getDuration()));
        handler = new Handler();
        updateTime = new Runnable() {
            @Override
            public void run() {
                binding.layoutPlayer.txtCurrentTime.setText(NumberUtils.formatAsTime(player.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTime);
    }
}