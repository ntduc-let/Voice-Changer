package com.prox.voicechanger.repository

import javax.inject.Inject
import com.prox.voicechanger.database.FileVoiceDAO
import androidx.lifecycle.MutableLiveData
import com.prox.voicechanger.model.FileVoice

class FileVoiceRepository @Inject constructor(private val dao: FileVoiceDAO) {
    val fileVoices: MutableLiveData<List<FileVoice?>?>
        get() {
            val data = MutableLiveData<List<FileVoice?>?>()
            data.value = dao.allVoice
            return data
        }
    val fileVoicesBG: MutableLiveData<List<FileVoice?>?>
        get() {
            val data = MutableLiveData<List<FileVoice?>?>()
            data.postValue(dao.allVoice)
            return data
        }
    val fileVideos: MutableLiveData<List<FileVoice?>?>
        get() {
            val data = MutableLiveData<List<FileVoice?>?>()
            data.value = dao.allVideo
            return data
        }
    val fileVideosBG: MutableLiveData<List<FileVoice?>?>
        get() {
            val data = MutableLiveData<List<FileVoice?>?>()
            data.postValue(dao.allVideo)
            return data
        }

    fun check(path: String?): FileVoice? {
        return dao.check(path)
    }

    fun insert(fileVoice: FileVoice?) {
        dao.insert(fileVoice)
    }

    fun update(fileVoice: FileVoice?) {
        dao.update(fileVoice)
    }

    fun delete(fileVoice: FileVoice?) {
        dao.delete(fileVoice)
    }
}