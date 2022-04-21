package com.prox.voicechanger.repository;

import androidx.lifecycle.MutableLiveData;

import com.prox.voicechanger.database.FileVoiceDAO;
import com.prox.voicechanger.model.FileVoice;

import java.util.List;

import javax.inject.Inject;

public class FileVoiceRepository {
    private final FileVoiceDAO dao;

    @Inject
    public FileVoiceRepository(FileVoiceDAO dao){
        this.dao = dao;
    }

    public MutableLiveData<List<FileVoice>> getFileVoices(){
        MutableLiveData<List<FileVoice>> data = new MutableLiveData<>();
        data.setValue(dao.getAll());
        return data;
    }

    public List<FileVoice> check(String path){
        return dao.check(path);
    }

    public void insert(FileVoice fileVoice){
        dao.insert(fileVoice);
    }

    public void update(FileVoice fileVoice){
        dao.update(fileVoice);
    }

    public void delete(FileVoice fileVoice){
        dao.delete(fileVoice);
    }
}
