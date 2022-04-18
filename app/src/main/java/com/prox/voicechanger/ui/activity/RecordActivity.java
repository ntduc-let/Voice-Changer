package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.ActivityRecordBinding;

public class RecordActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "RecordActivity: onCreate");
        super.onCreate(savedInstanceState);
        ActivityRecordBinding binding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_record_activity);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }else{
            Log.d(TAG, "RecordActivity: navHostFragment null");
            return;
        }

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "RecordActivity: onDestroy");
        appBarConfiguration = null;
        navController = null;
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}