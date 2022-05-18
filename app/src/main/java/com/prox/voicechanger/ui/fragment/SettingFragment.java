package com.prox.voicechanger.ui.fragment;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Shader;
import android.net.Uri;
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
import com.prox.voicechanger.databinding.DialogRateBinding;
import com.prox.voicechanger.databinding.FragmentSettingBinding;
import com.prox.voicechanger.ui.dialog.RateDialog;
import com.prox.voicechanger.utils.ColorUtils;

public class SettingFragment extends Fragment {
    private static final String URI_POLICY = "https://hellowordapp.github.io/policy/privacy.html";

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
            Log.d(TAG, "SettingFragment: To RecordFragment");
        });

        binding.btnRate.setOnClickListener(view -> {
            RateDialog dialog = new RateDialog(
                    requireContext(),
                    DialogRateBinding.inflate(requireActivity().getLayoutInflater()));
            dialog.show();
            Log.d(TAG, "SettingFragment: Show RateDialog");
        });

        binding.btnShareApp.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id="+requireActivity().getApplicationContext().getPackageName());
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
        });

        binding.btnPolicy.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_POLICY)));
            requireActivity().overridePendingTransition(R.anim.anim_right_left_1, R.anim.anim_right_left_2);
            Log.d(TAG, "SettingFragment: To "+URI_POLICY);
        });

        return binding.getRoot();
    }

    private void init() {
        Log.d(TAG, "SettingFragment: init");
        Shader shader1 = ColorUtils.textShader(
                Color.parseColor("#4B5DFC"),
                Color.parseColor("#F7277E"),
                binding.layoutPremium.txtPremium1.getTextSize());
        Shader shader2 = ColorUtils.textShader(
                Color.parseColor("#4B5DFC"),
                Color.parseColor("#F7277E"),
                binding.layoutPremium.txtPremium2.getTextSize());
        binding.layoutPremium.txtPremium1.getPaint().setShader(shader1);
        binding.layoutPremium.txtPremium2.getPaint().setShader(shader2);
    }
}