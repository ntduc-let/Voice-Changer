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
    private final MutableLiveData<List<FileVoice>> fileVideos;
    private final MutableLiveData<String> pathPlayer = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isExecuteConvertRecording = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isExecuteText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isExecuteSave = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isExecuteAddImage = new MutableLiveData<>();
    private final MutableLiveData<Float> loading = new MutableLiveData<>();

    @Inject
    public FileVoiceViewModel(@NonNull FileVoiceRepository repository) {
        this.repository = repository;

        fileVoices = repository.getFileVoices();
        fileVideos = repository.getFileVideos();
    }

    public LiveData<List<FileVoice>> getFileVoices() {
        return fileVoices;
    }

    public LiveData<List<FileVoice>> getFileVideos() {
        return fileVideos;
    }

    public FileVoice check(String path){
        return repository.check(path);
    }

    public LiveData<String> getPathPlayer() {
        return pathPlayer;
    }

    public void setPathPlayer(String path){
        pathPlayer.postValue(path);
    }

    public void setExecuteConvertRecording(Boolean b){
        isExecuteConvertRecording.postValue(b);
    }

    public LiveData<Boolean> isExecuteConvertRecording() {
        return isExecuteConvertRecording;
    }

    public void setExecuteText(Boolean b){
        isExecuteText.postValue(b);
    }

    public LiveData<Boolean> isExecuteText() {
        return isExecuteText;
    }

    public void setExecuteSave(Boolean b){
        isExecuteSave.postValue(b);
    }

    public LiveData<Boolean> isExecuteSave() {
        return isExecuteSave;
    }

    public void setExecuteAddImage(Boolean b){
        isExecuteAddImage.postValue(b);
    }

    public LiveData<Boolean> isExecuteAddImage() {
        return isExecuteAddImage;
    }

    public void setLoading(Float f){
        loading.postValue(f);
    }

    public LiveData<Float> getLoading() {
        return loading;
    }

    public void insert(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: insert "+fileVoice.getPath());
        repository.insert(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        fileVideos.setValue(repository.getFileVideos().getValue());
    }

    public void insertBG(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: insertBG "+fileVoice.getPath());
        repository.insert(fileVoice);
        fileVoices.postValue(repository.getFileVoicesBG().getValue());
        fileVideos.postValue(repository.getFileVideosBG().getValue());
    }

    public void update(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: update "+fileVoice.getPath());
        repository.update(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        fileVideos.setValue(repository.getFileVideos().getValue());
    }

    public void updateBG(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: updateBG "+fileVoice.getPath());
        repository.update(fileVoice);
        fileVoices.postValue(repository.getFileVoicesBG().getValue());
        fileVideos.postValue(repository.getFileVideosBG().getValue());
    }

    public void delete(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: delete "+fileVoice.getPath());
        repository.delete(fileVoice);
        fileVoices.setValue(repository.getFileVoices().getValue());
        fileVideos.setValue(repository.getFileVideos().getValue());
    }

    public void deleteBG(FileVoice fileVoice){
        Log.d(TAG, "FileVoiceViewModel: deleteBG "+fileVoice.getPath());
        repository.delete(fileVoice);
        fileVoices.postValue(repository.getFileVoicesBG().getValue());
        fileVideos.postValue(repository.getFileVideosBG().getValue());
    }
}
