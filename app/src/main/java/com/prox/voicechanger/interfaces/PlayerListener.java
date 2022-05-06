package com.prox.voicechanger.interfaces;

public interface PlayerListener {
    void setNewPath(String path);
    void start();
    void pause();
    void resume();
    void stop();
    void release();
}
