package com.prox.voicechanger.ui.activity;

import static com.prox.voicechanger.VoiceChangerApp.TAG;
import static com.prox.voicechanger.utils.PermissionUtils.REQUEST_PERMISSION;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.prox.voicechanger.R;
import com.prox.voicechanger.databinding.ActivityRecordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RecordActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "RecordActivity: onCreate");
        super.onCreate(savedInstanceState);
        ActivityRecordBinding binding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
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

    private void init(){
        Log.d(TAG, "RecordActivity: init");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                navController.navigate(R.id.action_recordFragment_to_stopRecordFragment);
                Log.d(TAG, "RecordActivity: To StopRecordFragment");
            } else {
                Toast.makeText(this, R.string.request_write_setting, Toast.LENGTH_SHORT).show();
            }
        }
    }
}