package com.prox.voicechanger.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.prox.voicechanger.model.FileVoice;

import java.util.List;

@Dao
public interface FileVoiceDAO {
    @Insert
    void insert(FileVoice fileVoice);

    @Update
    void update(FileVoice fileVoice);

    @Delete
    void delete(FileVoice fileVoice);

    @Query("SELECT * FROM filevoice WHERE path LIKE '%mp3'")
    List<FileVoice> getAllVoice();

    @Query("SELECT * FROM filevoice WHERE path LIKE '%mp4'")
    List<FileVoice> getAllVideo();

    @Query("SELECT * FROM filevoice WHERE path= :path")
    FileVoice check(String path);
}
