//package com.prox.voicechanger.utils;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//
//import com.google.firebase.analytics.FirebaseAnalytics;
//
//public class FirebaseUtils {
//    public static final int MENU_XLSX = 1;
//    public static final int MENU_PPTX = 2;
//    public static final int MENU_PDF = 3;
//
//    public static void sendEventOpenFile(Context context, Uri data, String path){
//        Bundle bundle = new Bundle();
//        bundle.putString("event_type", "open_file");
//        bundle.putString("source_app", data.toString());
//        bundle.putString("file_name", path==null?"null":FileUtils.getName(path));
//        bundle.putString("extension", path==null?"null":FileUtils.getType(path));
//        FirebaseAnalytics.getInstance(context).logEvent("prox_office_reader", bundle);
//    }
//
//    public static void sendEventRequestPermission(Context context){
//        Bundle bundle = new Bundle();
//        if (PermissionUtils.permission(context)){
//            bundle.putString("event_type", "success");
//        }else {
//            bundle.putString("event_type", "error");
//        }
//        FirebaseAnalytics.getInstance(context).logEvent("prox_permission", bundle);
//    }
//
//    public static void sendEventSubmitRate(Context context, String comment, int rate){
//        Bundle bundle = new Bundle();
//        bundle.putString("event_type", "rated");
//        bundle.putString("comment", comment);
//        bundle.putString("star", rate + " star");
//        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
//    }
//
//    public static void sendEventLaterRate(Context context){
//        Bundle bundle = new Bundle();
//        bundle.putString("event_type", "cancel");
//        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
//    }
//
//    public static void sendEventChangeRate(Context context, int rate){
//        Bundle bundle = new Bundle();
//        bundle.putString("event_type", "rated");
//        bundle.putString("star", rate + " star");
//        FirebaseAnalytics.getInstance(context).logEvent("prox_rating_layout", bundle);
//    }
//
//    public static void sendEventMenu(Context context, int menu){
//        Bundle bundle = new Bundle();
//        if (menu == MENU_XLSX){
//            bundle.putString("event_type", "click_xlsx_manager");
//        }else if (menu == MENU_PPTX){
//            bundle.putString("event_type", "click_pptx_manager");
//        }else if (menu == MENU_PDF){
//            bundle.putString("event_type", "click_pdf_manager");
//        }
//        FirebaseAnalytics.getInstance(context).logEvent("prox_menu_layout", bundle);
//    }
//}
