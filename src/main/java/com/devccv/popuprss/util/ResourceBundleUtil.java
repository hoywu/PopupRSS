package com.devccv.popuprss.util;

import com.devccv.popuprss.ResourcesLoader;
import javafx.scene.text.Font;

import java.util.Locale;
import java.util.ResourceBundle;

public final class ResourceBundleUtil {
    public static ResourceBundle resource;
    public static Font titleFont;
    public static Font subTitleFont;
    public static Font logFont;

    static {
        resource = ResourceBundle.getBundle("com.devccv.popuprss.string", Locale.getDefault());
        titleFont = Font.loadFont(ResourcesLoader.loadStream("font/Lobster.ttf"), 20);
        subTitleFont = Font.loadFont(ResourcesLoader.loadStream("font/Sarasa.ttf"), 15);
        logFont = Font.font(subTitleFont.getFamily(), 12);
    }

    public static String getStringValue(String key) {
        return resource.getString(key);
    }

}
