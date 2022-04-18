package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.FragmentSettingBinding;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "SettingFragment: onCreateView");
        FragmentSettingBinding binding = FragmentSettingBinding.inflate(inflater, container, false);

        binding.btnClose.setOnClickListener(view -> {
            NavController navController= Navigation.findNavController(requireActivity(), R.id.nav_host_record_activity);
            navController.popBackStack();
        });

        return binding.getRoot();
    }
}