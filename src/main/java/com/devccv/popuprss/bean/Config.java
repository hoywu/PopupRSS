package com.devccv.popuprss.bean;

public class Config {
    private String version = "1.0";
    private String language;
    private String rssLink;
    private int checkDelay;
    private boolean checkOnStart;
    private boolean autoPopup;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRssLink() {
        return rssLink;
    }

    public void setRssLink(String rssLink) {
        this.rssLink = rssLink;
    }

    public int getCheckDelay() {
        return checkDelay;
    }

    public void setCheckDelay(int checkDelay) {
        this.checkDelay = checkDelay;
    }

    public boolean isCheckOnStart() {
        return checkOnStart;
    }

    public void setCheckOnStart(boolean checkOnStart) {
        this.checkOnStart = checkOnStart;
    }

    public boolean isAutoPopup() {
        return autoPopup;
    }

    public void setAutoPopup(boolean autoPopup) {
        this.autoPopup = autoPopup;
    }
}
