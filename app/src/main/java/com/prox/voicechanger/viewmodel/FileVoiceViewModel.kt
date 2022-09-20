package com.prox.voicechanger.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.prox.voicechanger.repository.FileVoiceRepository
import com.prox.voicechanger.model.FileVoice
import com.prox.voicechanger.VoiceChangerApp

@HiltViewModel
class FileVoiceViewModel @Inject constructor(private val repository: FileVoiceRepository) :
    ViewModel() {
    val fileVoices: MutableLiveData<List<FileVoice>>
    val fileVideos: MutableLiveData<List<FileVoice>>

    init {
        fileVoices = repository.fileVoices
        fileVideos = repository.fileVideos
    }

    fun getFileVoices(): LiveData<List<FileVoice>> {
        return fileVoices
    }

    fun getFileVideos(): LiveData<List<FileVoice>> {
        return fileVideos
    }

    fun check(path: String?): FileVoice {
        return repository.check(path)
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