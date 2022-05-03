package com.prox.voicechanger.interfaces;

public interface PlayerListener {
    void start(String path);
    void pause();
    void resume();
    void stop();
    void release();
}
