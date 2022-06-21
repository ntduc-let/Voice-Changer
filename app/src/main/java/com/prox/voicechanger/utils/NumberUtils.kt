package com.prox.voicechanger.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class NumberUtils {
    @SuppressLint("DefaultLocale")
    public static String formatAsTime(long time) {
        int seconds = (int)(TimeUnit.MILLISECONDS.toSeconds(time) % (long)60);
        int minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(time) % (long)60);
        int hours = (int)TimeUnit.MILLISECONDS.toHours(time);

        return hours==0 ? String.format("%02d:%02d", minutes, seconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatAsDate(long date){
        return new SimpleDateFormat("HH:mm | dd.MM.yy").format(date);
    }

    public static String formatAsSize(long size){
        return (size/1024) + "kB";
    }
}
