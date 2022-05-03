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
    private static final String ID_FACEBOOK = "fb://page/105955561135473";
    private static final String URI_FACEBOOK = "https://www.facebook.com/Proxglobalstudio";
    private static final String EMAIL_FEEDBACK = "elaineeyui@gmail.com";
    private static final String URI_POLICY = "https://hellowordapp.github.io/policy/privacy.html";
    private static final String URI_TERMS = "https://hellowordapp.github.io/policy/privacy.html";

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

        binding.btnFacebook.setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ID_FACEBOOK)));
            } catch (Exception e) {
                Log.d(TAG, "SettingFragment: Application not installed");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_FACEBOOK)));
            }
            Log.d(TAG, "SettingFragment: To "+URI_FACEBOOK);
        });

        binding.btnContact.setOnClickListener(view -> {
            Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
            selectorIntent.setData(Uri.parse("mailto:"));

            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_FEEDBACK});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            emailIntent.setSelector(selectorIntent);

            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.notification_send_mail)));
            Log.d(TAG, "SettingFragment: To Mail");
        });

        binding.btnPolicy.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_POLICY)));
            Log.d(TAG, "SettingFragment: To "+URI_POLICY);
        });

        binding.btnTerms.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_TERMS)));
            Log.d(TAG, "SettingFragment: To "+URI_TERMS);
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