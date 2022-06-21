package com.prox.voicechanger.database

import android.content.Context
import androidx.annotation.Nullable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.room.Room
import dagger.Module
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context?
    ): FileVoiceDatabase {
        return Room.databaseBuilder(
            context!!,
            FileVoiceDatabase::class.java,
            FileVoiceDatabase.DATABASE_NAME
        )
            .addMigrations(FileVoiceDatabase.migration_1_to_2)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideDAO(database: FileVoiceDatabase): FileVoiceDAO {
        return database.dao()!!
    }
}