package com.prox.voicechanger.viewmodel;

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
    private final MutableLiveData<Integer> numberFileVoices;

    @Inject
    public FileVoiceViewModel(@NonNull FileVoiceRepository repository) {
        this.repository = repository;

        fileVoices = repository.getFileVoices();
        numberFileVoices = repository.getNumberFileVoices();
    }

    public LiveData<List<FileVoice>> getFileVoices() {
        return fileVoices;
    }

    public LiveData<Integer> getNumberFileVoices(){
        return numberFileVoices;
    }

    public void insert(FileVoice fileVoice){
        repository.insert(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        numberFileVoices.setValue(repository.getNumberFileVoices().getValue());
    }

    public void update(FileVoice fileVoice){
        repository.update(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        numberFileVoices.setValue(repository.getNumberFileVoices().getValue());
    }

    public void updateIsExist(){
        repository.updateIsExist();
        fileVoices.setValue(repository.getFileVoices().getValue());
        numberFileVoices.setValue(repository.getNumberFileVoices().getValue());
    }

    public void delete(FileVoice fileVoice){
        repository.delete(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        numberFileVoices.setValue(repository.getNumberFileVoices().getValue());
    }

    public void deleteNotExist(){
        repository.deleteNotExist();
        fileVoices.setValue(repository.getFileVoices().getValue());
        numberFileVoices.setValue(repository.getNumberFileVoices().getValue());
    }
}
