package com.prox.voicechanger.repository;

import com.prox.voicechanger.database.FileVoiceDAO;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Inject
    @Singleton
    public FileVoiceRepository provideRepository(FileVoiceDAO dao){
        return new FileVoiceRepository(dao);
    }
}
