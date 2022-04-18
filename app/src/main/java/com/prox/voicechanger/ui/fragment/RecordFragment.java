package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.FragmentRecordBinding;
import com.prox.voicechanger.utils.PermissionUtils;

public class RecordFragment extends Fragment {
    private FragmentRecordBinding binding;

    private Handler handler;
    private Runnable runnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "RecordFragment: onCreateView");
        binding = FragmentRecordBinding.inflate(inflater, container, false);

        init();

        binding.btnRecord.setOnClickListener(view -> {
            if (PermissionUtils.checkPermission(requireContext(), requireActivity())){
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_recordFragment_to_stopRecordFragment);
            }
        });

        binding.btnMore.setOnClickListener(view ->
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_recordFragment_to_settingFragment));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "RecordFragment: onDestroyView");
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        runnable = null;
        handler = null;
        binding = null;
    }

    private void init() {
        Log.d(TAG, "RecordFragment: init");
        Animation rotate1Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate1);
        Animation rotate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate2);
        Animation rotate3Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate3);
        Animation rotate4Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate4);
        binding.aniRecord.icAniRecord1.startAnimation(rotate2Animation);
        binding.aniRecord.icAniRecord2.startAnimation(rotate4Animation);
        binding.aniRecord.icAniRecord3.startAnimation(rotate1Animation);
        binding.aniRecord.icAniRecord4.startAnimation(rotate2Animation);
        binding.aniRecord.icAniRecord5.startAnimation(rotate4Animation);
        binding.aniRecord.icAniRecord6.startAnimation(rotate3Animation);

        AnimationDrawable rocketAnimation = (AnimationDrawable) binding.aniRecord.icAniRecord7.getBackground();
        rocketAnimation.start();

        Animation translate1Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate1);
        binding.aniRecord.icAniRecord.startAnimation(translate1Animation);

        new Handler().postDelayed(() -> {
            binding.btnMore.setVisibility(View.VISIBLE);
            binding.txtContent.setVisibility(View.VISIBLE);
            binding.txtContent2.setVisibility(View.VISIBLE);
            binding.btnRecord.setVisibility(View.VISIBLE);
            binding.txtMess.txtMess.setText(R.string.mess_start_record);
            binding.txtMess.getRoot().setVisibility(View.VISIBLE);

            Animation translate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2);
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    binding.txtMess.getRoot().startAnimation(translate2Animation);
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);
        }, 1500);
    }
}