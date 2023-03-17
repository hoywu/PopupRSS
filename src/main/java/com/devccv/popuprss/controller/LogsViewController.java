package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.thread.FlushLogThread;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.widget.MyToggleNode;
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

public class LogsViewController implements Initializable {
    public static AtomicBoolean stopUpdateLogUI = new AtomicBoolean(true);
    public static AtomicBoolean pauseButtonSelected = new AtomicBoolean(false);
    private static FlushLogThread FLUSH_LOG_THREAD;
    private static final Lock flushLogLock = new ReentrantLock();
    private static final Condition canFlushCondition = flushLogLock.newCondition();
    @FXML
    private TextArea logsTextArea;
    public static final BlockingQueue<String> logHolder = new LinkedBlockingQueue<>();
    @FXML
    private MFXRectangleToggleNode pauseButton;
    @FXML
    private MyToggleNode clearButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //设置字体
        logsTextArea.setFont(ResourceBundleUtil.logFont);
        pauseButton.setFont(ResourceBundleUtil.logFont);
        clearButton.setFont(ResourceBundleUtil.logFont);

        //设置按钮图标
        ImageView icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/pause.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        pauseButton.setLabelLeadingIcon(icon);
        pauseButton.setLabelTrailingIcon(null);
        icon = new ImageView(new Image(ResourcesLoader.loadStream("icon/clear.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        clearButton.setLabelLeadingIcon(icon);
        clearButton.setLabelTrailingIcon(null);

        //开启日志刷新线程
        stopUpdateLogUI.set(false);
        FLUSH_LOG_THREAD = new FlushLogThread(logsTextArea, flushLogLock, canFlushCondition);
        FLUSH_LOG_THREAD.start();
    }

    public static void newLog(String log) {
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
    protected void onMouseClickedClearBtn() {
        logsTextArea.setText("[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + ResourceBundleUtil.getStringValue("clear_log"));
        clearButton.setSelected(false);
        System.gc();
    }

    @FXML
    protected void onMouseClickedPauseBtn() {
        pauseButtonSelected.set(pauseButton.isSelected());
        System.gc();
    }

    public static void tryShutdownFlushLogThread() {
        if (FLUSH_LOG_THREAD != null) FLUSH_LOG_THREAD.interrupt();
    }
}
