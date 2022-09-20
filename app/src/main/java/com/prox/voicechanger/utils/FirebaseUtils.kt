package com.prox.voicechanger.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseUtils {
    @JvmStatic
    fun sendEventSubmitRate(context: Context?, comment: String?, rate: Int) {
        val bundle = Bundle()
        bundle.putString("event_type", "rated")
        bundle.putString("comment", comment)
        bundle.putString("star", "$rate star")
        FirebaseAnalytics.getInstance(context!!).logEvent("prox_rating_layout", bundle)
    }

    @JvmStatic
    fun sendEventLaterRate(context: Context?) {
        val bundle = Bundle()
        bundle.putString("event_type", "cancel")
        FirebaseAnalytics.getInstance(context!!).logEvent("prox_rating_layout", bundle)
    }

    @JvmStatic
    fun sendEventChangeRate(context: Context?, rate: Int) {
        val bundle = Bundle()
        bundle.putString("event_type", "rated")
        bundle.putString("star", "$rate star")
        FirebaseAnalytics.getInstance(context!!).logEvent("prox_rating_layout", bundle)
    }

    fun sendEvent(context: Context?, nameEvent: String?, typeEvent: String?) {
        val bundle = Bundle()
        bundle.putString("event_type", typeEvent)
        FirebaseAnalytics.getInstance(context!!).logEvent(nameEvent!!, bundle)
    }
}