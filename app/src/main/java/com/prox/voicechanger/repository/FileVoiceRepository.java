package com.prox.voicechanger.repository;

import android.os.AsyncTask;

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

    public MutableLiveData<Integer> getNumberFileVoices(){
        MutableLiveData<Integer> numberFileVoices = new MutableLiveData<>();
        numberFileVoices.setValue(dao.getAll().size());
        return numberFileVoices;
    }

    public List<FileVoice> check(String path){
        return dao.check(path);
    }

    public void insert(FileVoice fileVoice){
        new InsertAsyncTask(dao).execute(fileVoice);
    }

    public void update(FileVoice fileVoice){
        new UpdateAsyncTask(dao).execute(fileVoice);
    }

    public void updateIsExist(){
        new UpdateIsExistAsyncTask(dao).execute();
    }

    public void delete(FileVoice fileVoice){
        new DeleteAsyncTask(dao).execute(fileVoice);
    }

    public void deleteNotExist(){
        new DeleteNotExistAsyncTask(dao).execute();
    }

    private static class InsertAsyncTask extends AsyncTask<FileVoice, Void, Void> {
        private final FileVoiceDAO dao;

        private InsertAsyncTask(FileVoiceDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FileVoice... fileVoices) {
            dao.insert(fileVoices[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<FileVoice, Void, Void> {
        private final FileVoiceDAO dao;

        private UpdateAsyncTask(FileVoiceDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FileVoice... fileVoices) {
            dao.update(fileVoices[0]);
            return null;
        }
    }

    private static class UpdateIsExistAsyncTask extends AsyncTask<Void, Void, Void> {
        private final FileVoiceDAO dao;

        private UpdateIsExistAsyncTask(FileVoiceDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.updateIsExist();
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<FileVoice, Void, Void> {
        private final FileVoiceDAO dao;

        private DeleteAsyncTask(FileVoiceDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FileVoice... fileVoices) {
            dao.delete(fileVoices[0]);
            return null;
        }
    }

    private static class DeleteNotExistAsyncTask extends AsyncTask<Void, Void, Void> {
        private final FileVoiceDAO dao;

        private DeleteNotExistAsyncTask(FileVoiceDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteNotExist();
            return null;
        }
    }

}
