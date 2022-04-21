package com.prox.voicechanger.viewmodel;

import static com.prox.voicechanger.VoiceChangerApp.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.prox.voicechanger.model.FileVoice;
import com.prox.voicechanger.repository.FileVoiceRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FileVoiceViewModel extends ViewModel {
    private final FileVoiceRepository repository;

    private final MutableLiveData<List<FileVoice>> fileVoices;

    @Inject
    public FileVoiceViewModel(@NonNull FileVoiceRepository repository) {
        this.repository = repository;

        fileVoices = repository.getFileVoices();
    }

    public LiveData<List<FileVoice>> getFileVoices() {
        return fileVoices;
    }

    public List<FileVoice> check(String path){
        return repository.check(path);
    }

    public void insert(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: insert "+fileVoice.getPath());
        repository.insert(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
    }

    public void update(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: update "+fileVoice.getPath());
        repository.update(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
    }

    public void delete(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: delete "+fileVoice.getPath());
        repository.delete(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
    }
}
