package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.graphics.Color;
import android.graphics.Shader;
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
import com.prox.voicechanger.utils.ColorUtils;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "SettingFragment: onCreateView");
        binding = FragmentSettingBinding.inflate(inflater, container, false);

        init();

        binding.btnClose.setOnClickListener(view -> {
            NavController navController= Navigation.findNavController(requireActivity(), R.id.nav_host_record_activity);
            navController.popBackStack();
        });

        return binding.getRoot();
    }

    private void init() {
        Log.d(TAG, "SettingFragment: init");
        Shader shader = ColorUtils.textShader(Color.parseColor("#4B5DFC"), Color.parseColor("#F7277E"));
        binding.layoutPremium.txtPremium1.getPaint().setShader(shader);
        binding.layoutPremium.txtPremium2.getPaint().setShader(shader);
    }
}