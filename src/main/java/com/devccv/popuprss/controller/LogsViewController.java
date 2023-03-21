package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.thread.FlushLogThread;
import com.devccv.popuprss.thread.RSSMonitorThread;
import com.devccv.popuprss.util.ResourceBundleUtil;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class LogsViewController implements Initializable {
    public static AtomicBoolean stopUpdateLogUI = new AtomicBoolean(true);
    public static AtomicBoolean pauseButtonSelected = new AtomicBoolean(false);
    private static FlushLogThread FLUSH_LOG_THREAD;
    private static RSSMonitorThread RSS_MONITOR_THREAD;
    private static final Lock flushLogLock = new ReentrantLock();
    private static final Condition canFlushCondition = flushLogLock.newCondition();
    @FXML
    private TextArea logsTextArea;
    public static final BlockingQueue<String> logHolder = new LinkedBlockingQueue<>();
    @FXML
    private MFXRectangleToggleNode startButton;
    @FXML
    private MFXRectangleToggleNode pauseButton;
    @FXML
    private MFXRectangleToggleNode clearButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //设置字体
        logsTextArea.setFont(ResourceBundleUtil.logFont);
        startButton.setFont(ResourceBundleUtil.logFont);
        pauseButton.setFont(ResourceBundleUtil.logFont);
        clearButton.setFont(ResourceBundleUtil.logFont);

        //设置按钮图标
        ImageView icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/pause.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        pauseButton.setLabelLeadingIcon(icon);
        icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/clear.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        clearButton.setLabelLeadingIcon(icon);
        icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/play.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        startButton.setLabelLeadingIcon(icon);

        //开启日志刷新线程
        stopUpdateLogUI.set(false);
        FLUSH_LOG_THREAD = new FlushLogThread(logsTextArea, flushLogLock, canFlushCondition);
        FLUSH_LOG_THREAD.start();
    }

    public static void newLog(String log) {
        //可供外部调用的，添加新日志方法
        String newLog = "\n" + "[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + log;
        try {
            logHolder.put(newLog);
        } catch (InterruptedException ignored) {
            return;
        }
        if (!stopUpdateLogUI.get() && !pauseButtonSelected.get()) {
            if (flushLogLock.tryLock()) {
                try {
                    canFlushCondition.signalAll();
                } finally {
                    flushLogLock.unlock();
                }
            }
        }
    }

    @FXML
    public void onMouseClickedStartBtn() {
        //启停RSS监视线程
        if (startButton.isSelected()) {
            RSS_MONITOR_THREAD = new RSSMonitorThread();
            RSS_MONITOR_THREAD.start();
        } else {
            RSS_MONITOR_THREAD.interrupt();
        }
    }

    @FXML
    private void onMouseClickedClearBtn() {
        //清空日志
        logsTextArea.setText("[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + ResourceBundleUtil.getStringValue("log_clear"));
        clearButton.setSelected(false);
        System.gc();
    }

    @FXML
    private void onMouseClickedPauseBtn() {
        //暂停日志，清理内存
        pauseButtonSelected.set(pauseButton.isSelected());
        System.gc();
    }

    public static void tryShutdownFlushLogThread() {
        if (FLUSH_LOG_THREAD != null) FLUSH_LOG_THREAD.interrupt();
    }
}
