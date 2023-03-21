package com.devccv.popuprss.thread;

import java.net.Proxy;
import java.net.URL;

public final class RSSMonitorThread extends Thread {
    private final Proxy proxy;
    private final URL url;

    public RSSMonitorThread(Proxy proxy, URL url) {
        this.proxy = proxy;
        this.url = url;
    }

    @Override
    public void run() {

    }
}
