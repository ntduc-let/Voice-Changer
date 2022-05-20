package com.prox.voicechanger;

import android.app.Application;

import com.proxglobal.proxads.adsv2.ads.ProxAds;
import com.proxglobal.purchase.ProxPurchase;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class VoiceChangerApp extends Application {
    public static final String TAG = "ntduc";
    public static final String FOLDER_APP = "Voice Changer";

    public static final ProxAds instance = ProxAds.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
//        List<String> listINAPId = Collections.emptyList();
//        List<String> listSubsId = Collections.singletonList(BuildConfig.id_subs);
        ProxPurchase.getInstance().initBilling(this);
    }
}
