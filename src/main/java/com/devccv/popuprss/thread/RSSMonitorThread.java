package com.devccv.popuprss.thread;

import com.devccv.popuprss.App;
import com.devccv.popuprss.bean.Record;
import com.devccv.popuprss.controller.ArchivedViewController;
import com.devccv.popuprss.controller.LogsViewController;
import com.devccv.popuprss.extend.LimitedQueue;
import com.devccv.popuprss.network.RequestResult;
import com.devccv.popuprss.network.SimpleHttps;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Notification;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.util.XML;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

public final class RSSMonitorThread extends Thread {
    private final Runnable checkRSS;
    private final LimitedQueue<String> alreadyPop = new LimitedQueue<>(10);
    private final LimitedQueue<String> alreadyNotify = new LimitedQueue<>(10);

    public RSSMonitorThread(Proxy proxy, URL url) {
        this.setDaemon(true);
        checkRSS = () -> {
            RequestResult result = SimpleHttps.GET(url, proxy);
            if (result.isSucceed()) {
                //网络请求成功，解析RSS XML
                List<Record> records = XML.parseRecord(result.getResponse());

                for (Record record : records) {
                    //遍历取得的任务记录，判断是否需要提醒
                    if (ConfigManager.CONFIG.isAutoPopup() && !alreadyPop.contains(record.getLink())) {
                        try {
                            if (Notification.BrowserOpenLink(record.getLink())) {
                                alreadyPop.add(record.getLink());
                            } else {
                                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_dont_support_popup"));
                            }
                        } catch (IOException e) {
                            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_popup_error"));
                        }
                    }

                    LogsViewController.newLog(record.getFullTitle());
                    ArchivedViewController.newRecord.accept(record);
                }

                LogsViewController.newLog(records.size() + " " + ResourceBundleUtil.getStringValue("log_task_num"));
            } else {
                //网络请求失败
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_check_rss_error") + result.getHeaderFields().get(null).get(0));
            }
        };
    }

    @Override
    public void run() {
        while (true) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_check_rss_once"));
            App.FIXED_THREAD_POOL.submit(checkRSS);

            try {
                Thread.sleep(ConfigManager.CONFIG.getCheckDelay() * 1000L);
            } catch (InterruptedException e) {
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_rss_monitor_interrupted"));
                return;
            }
        }
    }
}
