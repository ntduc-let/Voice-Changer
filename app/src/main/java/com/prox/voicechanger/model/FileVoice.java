package com.prox.voicechanger.model;

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
    private boolean isExist;

    public FileVoice() {
    }

    public FileVoice(int id, int src, String name, String path, long duration, long size, long date, boolean isExist) {
        this.id = id;
        this.src = src;
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.date = date;
        this.isExist = isExist;
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

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }
}
