package com.prox.voicechanger.database

import com.prox.voicechanger.model.FileVoice
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FileVoice::class], version = 2)
abstract class FileVoiceDatabase : RoomDatabase() {
    abstract fun dao(): FileVoiceDAO?

    companion object {
        const val DATABASE_NAME = "filevoice.db"
        var migration_1_to_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE filevoice ADD COLUMN image TEXT")
            }
        }
    }
}