package com.devccv.popuprss.thread;

import com.devccv.popuprss.App;
import com.devccv.popuprss.controller.LogsViewController;
import com.devccv.popuprss.util.ResourceBundleUtil;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public final class FlushLogThread extends Thread {
    private final TextArea logsTextArea;
    private final Lock lock;
    private final Condition condition;
    private final StringBuilder stringBuilder = new StringBuilder(32);

    public FlushLogThread(TextArea logsTextArea, Lock lock, Condition condition) {
        super("FlushLogThread");
        this.setDaemon(true);
        this.logsTextArea = logsTextArea;
        this.lock = lock;
        this.condition = condition;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            flush();
            while (true) {
                condition.await();
                if (LogsViewController.logHolder.isEmpty()) continue;
                flush();
            }
        } catch (InterruptedException e) {
            return;
        } finally {
            lock.unlock();
        }
    }

    private void flush() throws InterruptedException {
        stringBuilder.setLength(0);
        while (!LogsViewController.logHolder.isEmpty()) {
            stringBuilder.append(LogsViewController.logHolder.take());
        }
        if (logsTextArea.getLength() > 200000) {
            Platform.runLater(logsTextArea::clear);
            System.gc();
            Platform.runLater(() -> logsTextArea.setText("[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] "
                    + ResourceBundleUtil.getStringValue("log_rotate")));
        }
        Platform.runLater(() -> logsTextArea.appendText(stringBuilder.toString()));
    }
}
