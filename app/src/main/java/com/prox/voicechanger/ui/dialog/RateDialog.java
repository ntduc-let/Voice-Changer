package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogRateBinding;

public class RateDialog extends CustomDialog{

    public RateDialog(@NonNull Context context, DialogRateBinding binding) {
        super(context, binding.getRoot());
        Log.d(TAG, "RateDialog: create");
        setCancelable(true);

        binding.btnRate1.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
        });

        binding.btnRate2.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
        });

        binding.btnRate3.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
        });

        binding.btnRate4.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
        });

        binding.btnRate5.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
        });
    }
}
