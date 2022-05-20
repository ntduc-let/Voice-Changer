package com.prox.voicechanger.ui.dialog;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.DialogRateBinding;
import com.prox.voicechanger.interfaces.RateListener;

public class RateDialog extends CustomDialog{

    private int starRate;
    private static SharedPreferences sp;
    private final Context context;


    public RateDialog(@NonNull Context context, DialogRateBinding binding, RateListener rateListener) {
        super(context, binding.getRoot());
        Log.d(TAG, "RateDialog: create");
        setCancelable(false);
        this.context = context;

        sp = context.getSharedPreferences("prox", Context.MODE_PRIVATE);

        binding.btnRate1.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
            binding.edtComment.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnLater.setVisibility(View.GONE);
            starRate = 1;
        });

        binding.btnRate2.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
            binding.edtComment.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnLater.setVisibility(View.GONE);
            starRate = 2;
        });

        binding.btnRate3.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_not_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
            binding.edtComment.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnLater.setVisibility(View.GONE);
            starRate = 3;
        });

        binding.btnRate4.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_not_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
            sp.edit().putBoolean("isRated", true).apply();
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            } catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
            }
            Toast.makeText(context, R.string.thank_rate, Toast.LENGTH_SHORT).show();
            rateListener.rate();
            cancel();
        });

        binding.btnRate5.setOnClickListener(view -> {
            binding.btnRate1.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate2.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate3.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate4.setImageResource(R.drawable.ic_star_rate);
            binding.btnRate5.setImageResource(R.drawable.ic_star_rate);
            binding.txtRate.setText(R.string.txt_rate_done);
            sp.edit().putBoolean("isRated", true).apply();
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            } catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
            }
            Toast.makeText(context, R.string.thank_rate, Toast.LENGTH_SHORT).show();
            rateListener.rate();
            cancel();
        });

        binding.btnLater.setOnClickListener(view -> {
            rateListener.rate();
            cancel();
        });

        binding.btnSubmit.setOnClickListener(view -> {
            sp.edit().putBoolean("isRated", true).apply();
            Toast.makeText(context, R.string.thank_rate, Toast.LENGTH_SHORT).show();
            rateListener.rate();
            cancel();
        });
    }

    @Override
    public void show() {
        if (!isRated(context)){
            super.show();
        }
    }

    public static boolean isRated(Context context){
        if(sp == null) sp = context.getSharedPreferences("prox", Context.MODE_PRIVATE);
        return sp.getBoolean("isRated", false);
    }
}
