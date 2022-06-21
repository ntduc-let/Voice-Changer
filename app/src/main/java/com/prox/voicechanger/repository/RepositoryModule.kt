package com.prox.voicechanger.repository

import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import com.prox.voicechanger.database.FileVoiceDAO
import dagger.Module

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Inject
    @Singleton
    fun provideRepository(dao: FileVoiceDAO): FileVoiceRepository {
        return FileVoiceRepository(dao)
    }
}