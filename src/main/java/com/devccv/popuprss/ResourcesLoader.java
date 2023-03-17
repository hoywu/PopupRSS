package com.devccv.popuprss;

import java.io.InputStream;
import java.net.URL;

public class ResourcesLoader {
    private ResourcesLoader() {
    }

    public static URL loadURL(String path) {
        return ResourcesLoader.class.getResource(path);
    }

    public static String load(String path) {
        return loadURL(path).toString();
    }

    public static InputStream loadStream(String name) {
        return ResourcesLoader.class.getResourceAsStream(name);
    }
}
