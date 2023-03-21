package com.devccv.popuprss.util;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public final class Notification {
    public static boolean BrowserOpenLink(String link) throws IOException {
        if (Desktop.isDesktopSupported()) {
            URI uri = URI.create(link);
            Desktop dp = Desktop.getDesktop();
            if (dp.isSupported(Desktop.Action.BROWSE)) {
                dp.browse(uri);
                return true;
            }
        }
        return false;
    }
}
