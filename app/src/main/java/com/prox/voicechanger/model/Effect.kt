package com.prox.voicechanger.model;

public class Effect {
    private int id;
    private int src;
    private String title;
    private String changeVoice;

    public Effect() {
    }

    public Effect(int id, int src, String title, String changeVoice) {
        this.id = id;
        this.src = src;
        this.title = title;
        this.changeVoice = changeVoice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChangeVoice() {
        return changeVoice;
    }

    public void setChangeVoice(String changeVoice) {
        this.changeVoice = changeVoice;
    }
}
