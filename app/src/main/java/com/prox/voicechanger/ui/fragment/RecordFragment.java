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

import com.prox.voicechanger.BuildConfig;
import com.prox.voicechanger.R;
import com.prox.voicechanger.VoiceChangerApp;
import com.prox.voicechanger.databinding.DialogMoreOptionBinding;
import com.prox.voicechanger.databinding.FragmentRecordBinding;
import com.prox.voicechanger.ui.dialog.MoreOptionDialog;
import com.prox.voicechanger.utils.FirebaseUtils;
import com.prox.voicechanger.utils.NetworkUtils;
import com.prox.voicechanger.utils.PermissionUtils;
import com.proxglobal.proxads.adsv2.callback.AdsCallback;
import com.proxglobal.purchase.ProxPurchase;

public class RecordFragment extends Fragment {
    private FragmentRecordBinding binding;

    private final Handler handler = new Handler();
    private Runnable runnableAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "RecordFragment: onCreateView");
        binding = FragmentRecordBinding.inflate(inflater, container, false);

        init();

        binding.btnRecord.setOnClickListener(view -> {
            FirebaseUtils.sendEvent(requireContext(), "Layout_Home", "Click recoding");
            if (PermissionUtils.checkPermission(requireContext(), requireActivity())) {
                VoiceChangerApp.instance.showInterstitial(requireActivity(), "interstitial", new AdsCallback() {
                    @Override
                    public void onClosed() {
                        super.onClosed();
                        Log.d(TAG, "RecordFragment Ads onClosed");
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_recordFragment_to_stopRecordFragment);
                        Log.d(TAG, "RecordFragment: To StopRecordFragment");
                    }

                    @Override
                    public void onError() {
                        super.onError();
                        Log.d(TAG, "RecordFragment Ads onError");
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_recordFragment_to_stopRecordFragment);
                        Log.d(TAG, "RecordFragment: To StopRecordFragment");
                    }
                });
            }
        });

        binding.btnMore.setOnClickListener(view -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_recordFragment_to_settingFragment);
            Log.d(TAG, "RecordFragment: To SettingFragment");
        });

        binding.btnMoreOption.setOnClickListener(view -> {
            if (PermissionUtils.checkPermission(requireContext(), requireActivity())) {
                MoreOptionDialog dialog = new MoreOptionDialog(
                        requireContext(),
                        requireActivity(),
                        DialogMoreOptionBinding.inflate(getLayoutInflater())
                );
                dialog.show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "RecordFragment: onDestroyView");
        super.onDestroyView();
        handler.removeCallbacks(runnableAnimation);
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

        handler.postDelayed(() -> {
            binding.btnMore.setVisibility(View.VISIBLE);
            binding.btnMoreOption.setVisibility(View.VISIBLE);
            binding.txtContent.setVisibility(View.VISIBLE);
            binding.txtContent2.setVisibility(View.VISIBLE);
            binding.btnRecord.setVisibility(View.VISIBLE);
            binding.txtMess.txtMess.setText(R.string.mess_start_record);
            binding.txtMess.getRoot().setVisibility(View.VISIBLE);

            Animation translate2Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_translate2);
            runnableAnimation = new Runnable() {
                @Override
                public void run() {
                    binding.txtMess.getRoot().startAnimation(translate2Animation);
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnableAnimation);
        }, 1500);

        VoiceChangerApp.instance.showMediumNative(requireActivity(), BuildConfig.native_home, binding.adContainer, new AdsCallback() {
            @Override
            public void onClosed() {
                super.onClosed();
                Log.d(TAG, "RecordFragment: Ads onClosed");
            }

            @Override
            public void onError() {
                super.onError();
                Log.d(TAG, "RecordFragment: Ads onError");
            }
        });

        if (ProxPurchase.getInstance().checkPurchased()
                || !NetworkUtils.isNetworkAvailable(requireContext())) {
            binding.adContainer.setVisibility(View.GONE);
        }
    }
}