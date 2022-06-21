package com.prox.voicechanger.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.prox.voicechanger.model.FileVoice;

@Database(entities = {FileVoice.class}, version = 2)
public abstract class FileVoiceDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "filevoice.db";

    public abstract FileVoiceDAO dao();

    public static Migration migration_1_to_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE filevoice ADD COLUMN image TEXT");
        }
    };
}
