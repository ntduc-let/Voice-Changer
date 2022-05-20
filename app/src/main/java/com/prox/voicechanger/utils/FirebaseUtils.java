package com.prox.voicechanger.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseUtils {

    public static void sendEventSubmitRate(Context context, String comment, int rate){
        Bundle bundle = new Bundle();
        bundle.putString("event_type", "rated");
        bundle.putString("comment", comment);
        bundle.putString("star", rate + " star");
        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
    }

    public static void sendEventLaterRate(Context context){
        Bundle bundle = new Bundle();
        bundle.putString("event_type", "cancel");
        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
    }

    public static void sendEventChangeRate(Context context, int rate){
        Bundle bundle = new Bundle();
        bundle.putString("event_type", "rated");
        bundle.putString("star", rate + " star");
        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
    }
}
