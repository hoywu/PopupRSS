package com.devccv.popuprss.util;

import com.devccv.popuprss.App;
import com.devccv.popuprss.ResourcesLoader;
import com.devccv.popuprss.bean.Config;
import com.devccv.popuprss.controller.LogsViewController;
import com.devccv.popuprss.controller.MainController;
import javafx.application.Platform;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class ConfigManager {
    public static final Config CONFIG;
    private static Properties SETTINGS = new Properties();

    static {
        //从资源读取默认配置
        Properties RESET_TO_DEFAULT = new Properties();
        try (Reader in = new InputStreamReader(ResourcesLoader.loadStream("default-settings.properties"), StandardCharsets.UTF_8)) {
            SETTINGS.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Reader in = new InputStreamReader(ResourcesLoader.loadStream("default-settings.properties"), StandardCharsets.UTF_8)) {
            RESET_TO_DEFAULT.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //读取用户配置
        if (Files.exists(Path.of("settings.properties"))) {
            try (FileReader in = new FileReader("settings.properties", StandardCharsets.UTF_8)) {
                SETTINGS.load(in);
            } catch (IOException exception) {
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_read_config_error"));
            }
            if (SETTINGS.getProperty("config.rssLink").isBlank()) {
                Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_no_rss_link")));
            } else {
                Platform.runLater(() -> MainController.switchToDisableStatus.accept(ResourceBundleUtil.getStringValue("status_ready")));
            }
        } else {
            //没有用户配置则生成默认配置
            if (!"zh".equals(Locale.getDefault().getLanguage())) {
                SETTINGS.setProperty("config.language", "English");
            } else {
                SETTINGS.setProperty("config.language", "Chinese");
            }
            saveSettingsProperties(SETTINGS);
            Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_first_start")));
        }

        //合法性检查
        try {
            if (Integer.parseUnsignedInt(SETTINGS.getProperty("config.checkDelay")) < 60) {
                SETTINGS.setProperty("config.checkDelay", "60");
            }
            //订阅检查
            if (!App.status.isValid()) {
                if (Integer.parseUnsignedInt(SETTINGS.getProperty("config.checkDelay")) < 120) {
                    SETTINGS.setProperty("config.checkDelay", "120");
                }
            }
        } catch (NumberFormatException e) {
            SETTINGS = RESET_TO_DEFAULT;
            Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_config_error")));
        }

        //从配置生成Config对象
        CONFIG = toConfigObj(SETTINGS, "config");
    }

    private static Config toConfigObj(Properties properties, String prefix) {
        //将Properties以指定prefix转为Config
        if (prefix == null) prefix = "";
        else if (!prefix.isEmpty() && !prefix.endsWith(".")) prefix += ".";

        Config config = new Config();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Config.class, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String property = properties.getProperty(prefix + descriptor.getName());
                if (property == null) continue;
                String typeName = descriptor.getPropertyType().getTypeName();
                switch (typeName) {
                    case "java.lang.String" -> descriptor.getWriteMethod().invoke(config, property);
                    case "boolean" -> descriptor.getWriteMethod().invoke(config, Boolean.parseBoolean(property));
                    case "int" -> descriptor.getWriteMethod().invoke(config, Integer.parseUnsignedInt(property));
                    case "long" -> descriptor.getWriteMethod().invoke(config, Long.parseLong(property));
                    case "char" -> descriptor.getWriteMethod().invoke(config, property.charAt(0));
                    case "float" -> descriptor.getWriteMethod().invoke(config, Float.parseFloat(property));
                    case "double" -> descriptor.getWriteMethod().invoke(config, Double.parseDouble(property));
                    case "short" -> descriptor.getWriteMethod().invoke(config, Short.parseShort(property));
                    case "byte" -> descriptor.getWriteMethod().invoke(config, Byte.parseByte(property));
                }
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            //下面的语句是不必要的，格式错误也不会导致此处异常
            Platform.runLater(() -> MainController.switchToErrorStatus.accept(ResourceBundleUtil.getStringValue("status_config_error")));
        }
        return config;
    }

    private static Properties toPropertiesObj(Config config, String prefix) {
        //将Config对象转换为Properties
        Properties properties = new Properties();
        if (prefix == null) prefix = "";
        else if (!prefix.isEmpty() && !prefix.endsWith(".")) prefix += ".";

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Config.class, Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String property = descriptor.getReadMethod().invoke(config).toString();
                properties.setProperty(prefix + descriptor.getName(), property);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    private static void saveSettingsProperties(Properties properties) {
        //将设置Properties存到文件
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("settings.properties"), StandardCharsets.UTF_8)) {
            properties.store(out, "PopupRSS - User Settings");
        } catch (IOException e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_write_config_error"));
        }
    }

    public static void saveConfig() {
        saveSettingsProperties(toPropertiesObj(CONFIG, "config"));
    }
}
