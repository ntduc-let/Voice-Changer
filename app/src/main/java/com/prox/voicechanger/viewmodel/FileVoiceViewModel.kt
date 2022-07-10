package com.prox.voicechanger.viewmodel

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.prox.voicechanger.repository.FileVoiceRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.prox.voicechanger.VoiceChangerApp
import com.prox.voicechanger.model.FileVoice

@HiltViewModel
class FileVoiceViewModel @Inject constructor(private val repository: FileVoiceRepository) :
    ViewModel() {
    private val fileVoices: MutableLiveData<List<FileVoice?>?> = repository.fileVoices
    private val fileVideos: MutableLiveData<List<FileVoice?>?> = repository.fileVideos
    private val pathPlayer = MutableLiveData<String>()
    private val isExecuteConvertRecording = MutableLiveData<Boolean>()
    private val isExecuteText = MutableLiveData<Boolean>()
    private val isExecuteSave = MutableLiveData<Boolean>()
    private val isExecuteCustom = MutableLiveData<Boolean>()
    private val isExecuteAddImage = MutableLiveData<Boolean>()
    private val loading = MutableLiveData<Float>()
    fun getFileVoices(): MutableLiveData<List<FileVoice?>?> {
        return fileVoices
    }

    fun getFileVideos(): MutableLiveData<List<FileVoice?>?> {
        return fileVideos
    }

    fun check(path: String?): FileVoice? {
        return repository.check(path)
    }

    fun getPathPlayer(): LiveData<String> {
        return pathPlayer
    }

    fun setPathPlayer(path: String?) {
        pathPlayer.postValue(path)
    }

    fun setExecuteConvertRecording(b: Boolean) {
        isExecuteConvertRecording.postValue(b)
    }

    fun isExecuteConvertRecording(): LiveData<Boolean> {
        return isExecuteConvertRecording
    }

    fun setExecuteText(b: Boolean) {
        isExecuteText.postValue(b)
    }

    fun isExecuteText(): LiveData<Boolean> {
        return isExecuteText
    }

    fun setExecuteSave(b: Boolean) {
        isExecuteSave.postValue(b)
    }

    fun isExecuteSave(): LiveData<Boolean> {
        return isExecuteSave
    }

    fun setExecuteCustom(b: Boolean) {
        isExecuteCustom.postValue(b)
    }

    fun isExecuteCustom(): LiveData<Boolean> {
        return isExecuteCustom
    }

    fun setExecuteAddImage(b: Boolean) {
        isExecuteAddImage.postValue(b)
    }

    fun isExecuteAddImage(): LiveData<Boolean> {
        return isExecuteAddImage
    }

    fun setLoading(f: Float) {
        loading.postValue(f)
    }

    fun getLoading(): LiveData<Float> {
        return loading
    }

    fun insert(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: insert " + fileVoice.path)
        repository.insert(fileVoice)
        fileVoices.value = repository.fileVoices.value
        fileVideos.value = repository.fileVideos.value
    }

    fun insertBG(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: insertBG " + fileVoice.path)
        repository.insert(fileVoice)
        fileVoices.postValue(repository.fileVoicesBG.value)
        fileVideos.postValue(repository.fileVideosBG.value)
    }

    fun insertVideoBG(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: insertVideoBG " + fileVoice.path)
        repository.insert(fileVoice)
        fileVideos.postValue(repository.fileVideosBG.value)
    }

    fun update(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: update " + fileVoice.path)
        repository.update(fileVoice)
        fileVoices.value = repository.fileVoices.value
        fileVideos.value = repository.fileVideos.value
    }

    fun updateBG(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: updateBG " + fileVoice.path)
        repository.update(fileVoice)
        fileVoices.postValue(repository.fileVoicesBG.value)
        fileVideos.postValue(repository.fileVideosBG.value)
    }

    fun delete(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: delete " + fileVoice.path)
        repository.delete(fileVoice)
        fileVoices.value = repository.fileVoices.value
        fileVideos.value = repository.fileVideos.value
    }

    fun deleteBG(fileVoice: FileVoice) {
        Log.d(VoiceChangerApp.TAG, "FileVoiceViewModel: deleteBG " + fileVoice.path)
        repository.delete(fileVoice)
        fileVoices.postValue(repository.fileVoicesBG.value)
        fileVideos.postValue(repository.fileVideosBG.value)
    }

}