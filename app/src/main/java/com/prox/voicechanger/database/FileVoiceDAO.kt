package com.prox.voicechanger.database

import androidx.annotation.Nullable
import androidx.room.*
import com.prox.voicechanger.model.FileVoice

@Dao
interface FileVoiceDAO {
    @Insert
    fun insert(fileVoice: FileVoice?)

    @Update
    fun update(fileVoice: FileVoice?)

    @Delete
    fun delete(fileVoice: FileVoice?)

    @get:Query("SELECT * FROM filevoice WHERE path LIKE '%mp3'")
    val allVoice: List<FileVoice?>?

    @get:Query("SELECT * FROM filevoice WHERE path LIKE '%mp4'")
    val allVideo: List<FileVoice?>?

    @Query("SELECT * FROM filevoice WHERE path= :path")
    fun check(path: String?): FileVoice?
}