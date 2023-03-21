package com.devccv.popuprss;

import com.devccv.popuprss.controller.MainController;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.ResourceBundleUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class App extends Application {
    public static final ExecutorService FIXED_THREAD_POOL = Executors.newFixedThreadPool(5);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.setProperty("prism.lcdtext", "false");

        //读取配置文件，设置语言
        if ("Chinese".equals(ConfigManager.CONFIG.getLanguage())) {
            Locale.setDefault(Locale.CHINA);
        } else {
            Locale.setDefault(Locale.US);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(ResourcesLoader.loadURL("fxml/main-view.fxml"), ResourceBundleUtil.resource);
        fxmlLoader.setControllerFactory(c -> new MainController(primaryStage));
        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setTitle("PopupRSS - Gengo Task Manager");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.show();

        //居中窗口
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }
}
