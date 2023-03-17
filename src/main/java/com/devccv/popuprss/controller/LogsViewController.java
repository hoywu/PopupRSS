package com.devccv.popuprss.controller;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.widget.MyToggleNode;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class LogsViewController implements Initializable {
    public static Runnable flushLogHolder; // 刷新日志方法
    private static final ExecutorService flushLogThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());
    @FXML
    private TextArea logsTextArea;
    private static final BlockingQueue<String> logHolder = new LinkedBlockingQueue<>();
    public static boolean stopUpdateLogUI = true;
    private static boolean pauseButtonSelected = false;
    private static final ReentrantLock flushLock = new ReentrantLock();
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

        //构造刷新日志暂存区方法
        flushLogHolder = () -> {
            if (logHolder.isEmpty()) return;
            CompletableFuture.supplyAsync(() -> {
                StringBuilder stringBuilder = new StringBuilder(32);
                while (!logHolder.isEmpty()) {
                    try {
                        stringBuilder.append(logHolder.take());
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return stringBuilder.toString();
            }, flushLogThread).whenComplete((result, ex) -> {
                if (result != null) Platform.runLater(() -> logsTextArea.appendText(result));
            });
        };

        //将暂存区的日志输出到UI
        stopUpdateLogUI = false;
        flushLogHolder.run();
    }

    public static void newLog(String log) {
        String newLog = "\n" + "[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + log;
        try {
            logHolder.put(newLog);
        } catch (InterruptedException ignored) {
        }
        if (!stopUpdateLogUI && !pauseButtonSelected) flushLogHolder.run();
    }

    @FXML
    protected void onMouseClickedClearBtn() {
        logsTextArea.setText("[" + App.DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "] " + ResourceBundleUtil.getStringValue("clear_log"));
        clearButton.setSelected(false);

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        newLog("123");
                        newLog("456");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    @FXML
    protected void onMouseClickedPauseBtn() {
        pauseButtonSelected = pauseButton.isSelected();
    }
}
