package com.prox.voicechanger.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object NumberUtils {
    @SuppressLint("DefaultLocale")
    fun formatAsTime(time: Long): String {
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(time) % 60L).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(time) % 60L).toInt()
        val hours = TimeUnit.MILLISECONDS.toHours(time).toInt()
        return if (hours == 0) String.format(
            "%02d:%02d",
            minutes,
            seconds
        ) else String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @SuppressLint("SimpleDateFormat")
    fun formatAsDate(date: Long): String {
        return SimpleDateFormat("HH:mm | dd.MM.yy").format(date)
    }

    fun formatAsSize(size: Long): String {
        return (size / 1024).toString() + "kB"
    }
}