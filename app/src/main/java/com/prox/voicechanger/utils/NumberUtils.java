package com.prox.voicechanger.utils;

import java.util.concurrent.TimeUnit;

public class NumberUtils {
    public static final String formatAsTime(long time) {
        int seconds = (int)(TimeUnit.MILLISECONDS.toSeconds(time) % (long)60);
        int minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(time) % (long)60);
        int hours = (int)TimeUnit.MILLISECONDS.toHours(time);

        return hours==0 ? String.format("%02d:%02d", minutes, seconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
