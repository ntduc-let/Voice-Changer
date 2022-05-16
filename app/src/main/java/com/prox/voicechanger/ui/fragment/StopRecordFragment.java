package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.ui.activity.ChangeVoiceActivity.PATH_FILE;
import static com.prox.voicechanger.ui.dialog.NameDialog.RECORD_TO_CHANGE_VOICE;

import android.content.Intent;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogMoreOptionBinding;
import com.prox.voicechanger.databinding.FragmentStopRecordBinding;
import com.prox.voicechanger.media.Recorder;
import com.prox.voicechanger.ui.activity.ChangeVoiceActivity;
import com.prox.voicechanger.ui.dialog.MoreOptionDialog;
import com.prox.voicechanger.utils.FileUtils;
import com.prox.voicechanger.utils.NumberUtils;

public class StopRecordFragment extends Fragment {
    private FragmentStopRecordBinding binding;
    private Recorder recorder;
    private boolean isStop;

    private final Handler handler = new Handler();
    private Runnable runnableAnimation;
    private final Runnable runnableTime = new Runnable() {
        @Override
        public void run() {
            binding.timelineTextView.setText(NumberUtils.formatAsTime(recorder.getCurrentTime()));
            binding.visualizer.addAmp(recorder.getMaxAmplitude(), recorder.getTickDuration());
            handler.post(this);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "StopRecordFragment: onCreateView");
        binding = FragmentStopRecordBinding.inflate(inflater, container, false);

        init();

        recording();

        binding.btnBack.setOnClickListener(view -> popBackStack());

        binding.btnStop.setOnClickListener(view -> {
            stopRecord();

            Intent intent = new Intent(requireActivity(), ChangeVoiceActivity.class);
            intent.setAction(RECORD_TO_CHANGE_VOICE);
            intent.putExtra(PATH_FILE, FileUtils.getTempRecordingFilePath(requireContext()));
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
        });

        binding.btnMoreOption.setOnClickListener(view -> {
            MoreOptionDialog dialog = new MoreOptionDialog(
                    requireContext(),
                    requireActivity(),
                    DialogMoreOptionBinding.inflate(getLayoutInflater())
            );
            dialog.show();
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "StopRecordFragment: onStart");
        super.onStart();
        if (isStop){
            popBackStack();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "StopRecordFragment: onStop");
        super.onStop();
        isStop = true;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "StopRecordFragment: onDestroyView");
        super.onDestroyView();
        stopRecord();
        binding = null;
        isStop = false;
    }

    private void init() {
        Log.d(TAG, "StopRecordFragment: init");

        binding.txtMess.getRoot().setVisibility(View.VISIBLE);
        binding.txtMess.txtMess.setText(R.string.mess_stop_record);

        Animation translate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2);

        runnableAnimation = new Runnable() {
            @Override
            public void run() {
                binding.txtMess.getRoot().startAnimation(translate2Animation);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnableAnimation);
    }

    private void recording() {
        Log.d(TAG, "StopRecordFragment: recording");
        recorder = new Recorder(requireContext());
        recorder.start();
        handler.post(runnableTime);
    }

    private void stopRecord(){
        Log.d(TAG, "StopRecordFragment: stopRecord");
        handler.removeCallbacks(runnableAnimation);
        handler.removeCallbacks(runnableTime);

        recorder.stop();
        recorder.release();

        binding.txtMess.getRoot().setVisibility(View.INVISIBLE);
    }

    private void popBackStack() {
        stopRecord();

        NavController navController= Navigation.findNavController(requireActivity(), R.id.nav_host_record_activity);
        navController.popBackStack();
        Log.d(TAG, "StopRecordFragment: To RecordFragment");
    }
}