package com.prox.voicechanger.model;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "filevoice")
public class FileVoice {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int src;
    private String name;
    private String path;
    private long duration;
    private long size;
    private long date;
    private Bitmap bitmap;

    public FileVoice() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
