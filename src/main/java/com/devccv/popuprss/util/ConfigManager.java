package com.devccv.popuprss.util;

import com.devccv.popuprss.controller.LogsViewController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    public static final Properties settings = new Properties();

    static {
        if (Files.exists(Path.of("settings.properties"))) {
            try (InputStream in = new FileInputStream("settings.properties")) {
                settings.load(in);
            } catch (IOException exception) {
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("read_config_error"));
            }
        } else {

        }
    }
}
