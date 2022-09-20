package com.prox.voicechanger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.proxglobal.purchase.ProxPurchase
import com.proxglobal.proxads.adsv2.ads.ProxAds

@HiltAndroidApp
class VoiceChangerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //        List<String> listINAPId = Collections.emptyList();
//        List<String> listSubsId = Collections.singletonList(BuildConfig.id_subs);
        ProxPurchase.getInstance().initBilling(this)
    }

    companion object {
        const val TAG = "ntduc"
        const val FOLDER_APP = "Voice Changer"
    }
}