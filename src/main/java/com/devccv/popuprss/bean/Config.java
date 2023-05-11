package com.devccv.popuprss.bean;

public final class Config {
    private String version = "1.0";
    private String language;
    private String rssLink;
    private int checkDelay;
    private String proxy;
    private String proxyURL;
    private boolean checkOnStart;
    private boolean autoPopup;
    private boolean autoMinimize;
    private boolean pushSystemNotify;

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

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getProxyURL() {
        return proxyURL;
    }

    public void setProxyURL(String proxyURL) {
        this.proxyURL = proxyURL;
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

    public boolean isAutoMinimize() {
        return autoMinimize;
    }

    public void setAutoMinimize(boolean autoMinimize) {
        this.autoMinimize = autoMinimize;
    }

    public boolean isPushSystemNotify() {
        return pushSystemNotify;
    }

    public void setPushSystemNotify(boolean pushSystemNotify) {
        this.pushSystemNotify = pushSystemNotify;
    }
}
