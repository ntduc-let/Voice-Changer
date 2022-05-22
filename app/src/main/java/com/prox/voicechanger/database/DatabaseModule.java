package com.prox.voicechanger.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides
    @Singleton
    public FileVoiceDatabase provideDatabase(
            @ApplicationContext Context context){
        return Room.databaseBuilder(context,
                FileVoiceDatabase.class,
                FileVoiceDatabase.DATABASE_NAME)
                .addMigrations(FileVoiceDatabase.migration_1_to_2)
                .allowMainThreadQueries()
                .build();
    }

    @Provides
    @Singleton
    public FileVoiceDAO provideDAO(@NonNull FileVoiceDatabase database){
        return database.dao();
    }
}
