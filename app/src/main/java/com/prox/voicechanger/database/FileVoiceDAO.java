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

    @Query("UPDATE filevoice SET isExist = 0")
    void updateIsExist();

    @Delete
    void delete(FileVoice fileVoice);

    @Query("DELETE FROM filevoice WHERE isExist = 0")
    void deleteNotExist();

    @Query("SELECT * FROM filevoice")
    List<FileVoice> getAll();

    @Query("SELECT * FROM filevoice WHERE path= :path")
    List<FileVoice> check(String path);
}
