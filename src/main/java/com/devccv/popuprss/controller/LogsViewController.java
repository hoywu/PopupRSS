package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.thread.FlushLogThread;
import com.devccv.popuprss.thread.RSSMonitorThread;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.util.Utils;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.Proxy;
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

        //修复模糊
        Platform.runLater(() -> {
            logsTextArea.setCache(false);
            ScrollPane sp = (ScrollPane) logsTextArea.getChildrenUnmodifiable().get(0);
            sp.setCache(false);
            for (Node n : sp.getChildrenUnmodifiable()) {
                n.setCache(false);
            }
        });

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
        new FlushLogThread(logsTextArea, flushLogLock, canFlushCondition).start();

        //自动开始
        if (ConfigManager.CONFIG.isCheckOnStart()) {
            Platform.runLater(() -> {
                startButton.setSelected(true);
                onMouseClickedStartBtn();
            });
        }
    }

    public static void newLog(String log) {
        //可供外部调用的，添加新日志方法
        if (log.isBlank()) return;
        String newLog = "\n" + "[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + log;
        if (App.GUI) {
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
        } else {
            System.out.print(newLog);
        }
    }

    @FXML
    public void onMouseClickedStartBtn() {
        //启停RSS监视线程
        if (startButton.isSelected()) {
            Proxy proxy;
            try {
                proxy = Utils.getProxyInstance();
            } catch (Exception e) {
                Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_proxy_error")));
                startButton.setSelected(false);
                return;
            }

            URL url;
            try {
                url = Utils.getURLInstance();
            } catch (Exception e) {
                Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_rss_link_error")));
                startButton.setSelected(false);
                return;
            }

            Platform.runLater(() -> MainController.switchToEnableStatus.accept(ResourceBundleUtil.getStringValue("status_running")));
            RSS_MONITOR_THREAD = new RSSMonitorThread(proxy, url);
            RSS_MONITOR_THREAD.start();
        } else {
            Platform.runLater(() -> MainController.switchToDisableStatus.accept(ResourceBundleUtil.getStringValue("status_stop")));
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
}
