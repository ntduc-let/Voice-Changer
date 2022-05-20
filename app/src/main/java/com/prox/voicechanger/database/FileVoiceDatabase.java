package com.prox.voicechanger.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prox.voicechanger.model.FileVoice;

@Database(entities = {FileVoice.class}, version = 1)
public abstract class FileVoiceDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "filevoice.db";

    public abstract FileVoiceDAO dao();
}
